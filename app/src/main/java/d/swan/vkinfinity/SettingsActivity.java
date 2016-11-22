package d.swan.vkinfinity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

    private Manager manager;
    private Network network;
    private Properties properties;
    private boolean isServiceEnabled = false;
    private Configuration config = new Configuration();

    ImageView iwAvatar, iwOnOff, iwLang;
    TextView tvUser, tvOnline, loginText;
    LinearLayout userLayout, loginLayout, serviceLayout, langLayout, logLayout, aboutLayout;

    private void noNetwork() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        Toast.makeText(this, R.string.Network, Toast.LENGTH_SHORT).show();
        setService(false);
    }

    private void setService(boolean OnOff) {
        isServiceEnabled = OnOff;
        if (OnOff) {
            manager.start();
            iwOnOff.setImageResource(R.drawable.switch_on);
        } else {
            manager.stop();
            iwOnOff.setImageResource(R.drawable.switch_off);
            Toast.makeText(this, R.string.Disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        // Сохранение параметров
        properties.SaveData(isServiceEnabled, Locale.getDefault().toString());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Сохранение параметров
        properties.SaveData(isServiceEnabled, Locale.getDefault().toString());
        super.onBackPressed();
    }

    private void Assignment() {
        // Определение данных пользователя
        userLayout = (LinearLayout) findViewById(R.id.userLayout);
        tvUser = (TextView) findViewById(R.id.tvUser);
        iwAvatar = (ImageView) findViewById(R.id.iwAvatar);
        tvOnline = (TextView) findViewById(R.id.tvOnline);

        // Определение кнопки входа
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        loginLayout.setOnClickListener(this);
        loginText = (TextView) findViewById(R.id.loginText);

        // Определения тумблера сервиса
        serviceLayout = (LinearLayout) findViewById(R.id.serviceLayout);
        serviceLayout.setOnClickListener(this);
        iwOnOff = (ImageView) findViewById(R.id.iwOnOff);
        if (isServiceEnabled) iwOnOff.setImageResource(R.drawable.switch_on);
        else iwOnOff.setImageResource(R.drawable.switch_off);

        // определение кнопки языка
        langLayout = (LinearLayout) findViewById(R.id.langLayout);
        langLayout.setOnClickListener(this);
        iwLang = (ImageView) findViewById(R.id.iwLang);
        iwLang.setImageResource(R.drawable.flag);

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
        checkingOfficialApp(); // Проверка установлено ли офф приложение ВК.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        manager = new Manager(this);
        network = new Network(this);
        Assignment(); // Вынес определения єлементов єкрана в отдельный метод для красоты

        if (VKSdk.isLoggedIn())
            showLoggedIn();
        else
            showLoggedOut();
    }

    // Кастомный Action Bar
    private void setCustomActionBar() {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
        actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);
    }

    // Загрузка параметров
    private void loadData() {
        properties = new Properties(this);
        properties.LoadData();
        isServiceEnabled = properties.isEnabled;
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);
    }

    // Проверка установлено ли офф приложение ВК.
    private void checkingOfficialApp() {
        final String app = "com.vkontakte.android";
        PackageManager pManager = getPackageManager();
        try {
            PackageInfo pInfo = pManager.getPackageInfo(app, 0);
        } catch (PackageManager.NameNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.AppWarning)
                    .setCancelable(false)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + app)));
                            }
                        }
                    })
                    .setNegativeButton(R.string.Try, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    // Если вход был выполнен
    private void showLoggedIn() {
        // Смена кнопки
        loginText.setText(R.string.loginBtnLogout);

        // проверка на запуск сервиса
        if (isServiceEnabled) setService(true);

        // проверка наличия Интернета
        if (network.check()) {
            loadUserData();
            userLayout.setLayoutParams(
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT)
            );
        } else
            noNetwork();
    }

    // Если вход не был выполнен
    private void showLoggedOut() {
        loginText.setText(R.string.loginBtnLogin);
        setService(false);
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    private void loadUserData() {
        // подготовка запроса
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, photo_100"));
        // отправка и обработка результата запроса
        request.executeWithListener(new VKRequest.VKRequestListener() {
            // Если запрос успешно выполнен
            @Override
            public void onComplete(VKResponse response) {

                JSON json = new JSON();
                tvUser.setText("");
                tvUser.append(json.getInfo(response, "first_name")); // Имя
                tvUser.append(" ");
                tvUser.append(json.getInfo(response, "last_name")); // Фамилия

                // Статус
                tvOnline.setText("");
                if (json.getInfo(response, "online").equals("0"))
                    tvOnline.append("offline"); // Если статус offline
                else
                    tvOnline.append("online"); // Если статус online

                // Аватар
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
            case R.id.loginLayout:

                // Проверка Интернета
                if (network.check()) {
                    // Авторизирован ли пользователь
                    if (VKSdk.isLoggedIn()) {
                        logoutDialog();
                    } else
                        VKSdk.login(this, null);
                }
                // Если нет Интернета
                else noNetwork();
                break;

            // кнопка сервиса
            case R.id.serviceLayout:

                if (!isServiceEnabled)
                    if (VKSdk.isLoggedIn()) {
                        isServiceEnabled = true;
                        showLoggedIn();
                    } else
                        showLoggedOut();
                else
                    setService(false);

                break;

            // Выбор языка
            case R.id.langLayout:
                languageDialog();
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
                    public void onClick(DialogInterface dialogInterface, int i) {
                        VKSdk.logout();
                        showLoggedOut();
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
                        properties.SaveData(isServiceEnabled, Locale.getDefault().toString());
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
                isServiceEnabled = true;
                showLoggedIn();
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getApplicationContext(), R.string.AuthErr, Toast.LENGTH_SHORT).show();
                showLoggedOut();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
