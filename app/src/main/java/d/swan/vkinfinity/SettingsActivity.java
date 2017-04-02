package d.swan.vkinfinity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.Locale;

public class SettingsActivity extends Activity implements OnClickListener {

    private int schedule;
    private Manager manager;
    private Network network;
    private Properties properties;
    private boolean isServiceEnabled;
    private Configuration config = new Configuration();

    ScrollView mainScrollView;
    ImageView iwAvatar, iwServiceOnOff, iwLang;
    TextView tvUser, tvOnline, scheduleUnit;
    LinearLayout userLayout, logoutLayout, serviceLayout, langLayout, scheduleLayout, logLayout, aboutLayout;

    private void noNetwork() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        Toast.makeText(this, R.string.Network, Toast.LENGTH_SHORT).show();
    }

    private void setService(boolean OnOff) {
        if (OnOff) {
            manager.start();
            iwServiceOnOff.setImageResource(R.drawable.switch_on);
        } else {
            manager.stop();
            iwServiceOnOff.setImageResource(R.drawable.switch_off);
        }
    }

    @Override
    protected void onPause() {
        properties.SaveData(isServiceEnabled, Locale.getDefault().toString(), schedule);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        properties.SaveData(isServiceEnabled, Locale.getDefault().toString(), schedule);
        super.onBackPressed();
        finish();
    }

    private void Assignment() {
        mainScrollView = (ScrollView) findViewById(R.id.mainScrollView);

        // Определение данных пользователя
        userLayout = (LinearLayout) findViewById(R.id.userLayout);
        tvUser = (TextView) findViewById(R.id.tvUser);
        iwAvatar = (ImageView) findViewById(R.id.iwAvatar);
        tvOnline = (TextView) findViewById(R.id.tvOnline);

        // Определение кнопки входа
        logoutLayout = (LinearLayout) findViewById(R.id.logoutLayout);
        logoutLayout.setOnClickListener(this);

        // Определения тумблера сервиса
        serviceLayout = (LinearLayout) findViewById(R.id.serviceLayout);
        serviceLayout.setOnClickListener(this);
        iwServiceOnOff = (ImageView) findViewById(R.id.iwServiceOnOff);
        if (isServiceEnabled) iwServiceOnOff.setImageResource(R.drawable.switch_on);
        else iwServiceOnOff.setImageResource(R.drawable.switch_off);

        // определение кнопки языка
        langLayout = (LinearLayout) findViewById(R.id.langLayout);
        langLayout.setOnClickListener(this);
        iwLang = (ImageView) findViewById(R.id.iwLang);
        iwLang.setImageResource(R.drawable.flag);

        // определение кнопки периода обновления
        scheduleLayout = (LinearLayout) findViewById(R.id.scheduleLayout);
        scheduleLayout.setOnClickListener(this);
        scheduleUnit = (TextView) findViewById(R.id.scheduleUnit);
        scheduleUnit.setText(String.valueOf(schedule) + " " + getResources().getString(R.string.scheduleUnit));

        // определение кнопки Log
        logLayout = (LinearLayout) findViewById(R.id.logLayout);
        logLayout.setOnClickListener(this);

        // определение кнопки About
        aboutLayout = (LinearLayout) findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setCustomActionBar(); // Кастомный Action Bar
        loadData(); // Загрузка параметров

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        manager = new Manager(this);
        network = new Network(this);

        Assignment(); // Вынес определения єлементов єкрана в отдельный метод для красоты

        if (VKSdk.isLoggedIn()) {
            showLoggedIn();
        } else {
            mainScrollView.setVisibility(View.INVISIBLE);
            VKSdk.login(this, null);
        }
    }

    // Кастомный Action Bar
    private void setCustomActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
        actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);

        TextView title = (TextView) findViewById(R.id.tvTitle);
        title.setText("Infinity");

        Button refreshButton = (Button) findViewById(R.id.actionButton);
        refreshButton.setBackground(getResources().getDrawable(R.drawable.ic_refresh));
        refreshButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                loadUserData();
                Toast.makeText(getApplicationContext(), R.string.refreshText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadData() {
        properties = new Properties(this);
        properties.LoadData();
        schedule = properties.schedule;
        isServiceEnabled = properties.isEnabled;
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);
    }

    private void showLoggedIn() {
        if (network.check()) {
            userLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
            );
            loadUserData();
        } else
            noNetwork();
    }

    private void loadUserData() {
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, photo_100"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {

                JSON json = new JSON();
                tvUser.setText("");
                tvUser.append(json.getInfo(response, "first_name"));
                tvUser.append(" ");
                tvUser.append(json.getInfo(response, "last_name"));

                if (json.getInfo(response, "online").equals("0"))
                    tvOnline.setText(R.string.offline);
                else
                    tvOnline.setText(R.string.online);

                Uri uri = Uri.parse(json.getInfo(response, "photo_100"));
                Picasso.with(getApplicationContext())
                        .load(uri)
                        .into(iwAvatar);
            }
        });
    }

    // обработка кнопок
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // кнопка входа
            case R.id.logoutLayout:
                if (network.check())
                    logoutDialog();
                else noNetwork();
                break;

            // кнопка сервиса
            case R.id.serviceLayout:
                if (!isServiceEnabled) {
                    isServiceEnabled = true;
                    setService(isServiceEnabled);
                    showLoggedIn();
                } else {
                    isServiceEnabled = false;
                    setService(isServiceEnabled);
                }
                break;

            // Выбор языка
            case R.id.langLayout:
                languageDialog();
                break;

            // Выбор времени обновления
            case R.id.scheduleLayout:
                scheduleDialog();
                break;

            // кнопка Log
            case R.id.logLayout:
                startActivity(new Intent(this, LogActivity.class));
                break;

            // вызов окна about
            case R.id.aboutLayout:
                aboutDialog();
                break;
        }
    }

    private void logoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.logoutMessage)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        isServiceEnabled = false;
                        setService(isServiceEnabled);
                        VKSdk.logout();
                        SettingsActivity.super.recreate();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void languageDialog() {
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        getResources().getStringArray(R.array.languages)
                );

        new AlertDialog.Builder(this)
                .setTitle(R.string.langText)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                config.locale = new Locale("en");
                                break;
                            case 1:
                                config.locale = new Locale("ru");
                                break;
                            case 2:
                                config.locale = new Locale("uk");
                                break;
                        }
                        Locale.setDefault(config.locale);
                        properties.SaveData(isServiceEnabled, Locale.getDefault().toString(), schedule);
                        SettingsActivity.super.recreate();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void scheduleDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        new AlertDialog.Builder(this)
                .setTitle(R.string.scheduleText)
                .setMessage(R.string.scheduleWarning)
                .setView(editText)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        String unit = editText.getText().toString();
                        schedule = Integer.parseInt(unit);
                        scheduleUnit.setText(unit + " " + getResources().getString(R.string.scheduleUnit));

                        properties.SaveData(isServiceEnabled, Locale.getDefault().toString(), schedule);
                        SettingsActivity.super.recreate();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void aboutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("VK Infinity")
                .setMessage(R.string.aboutMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    // Обработка результата попытки входа
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                mainScrollView.setVisibility(View.VISIBLE);
                showLoggedIn();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), R.string.AuthErr, Toast.LENGTH_SHORT).show();
                SettingsActivity.super.recreate();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
