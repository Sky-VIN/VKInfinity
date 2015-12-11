package d.swan.vkinfinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

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

public class SettingsActivity extends Activity implements OnCheckedChangeListener, OnClickListener {

    private boolean isEnabled = false;
    private boolean serviceState = false;
    private Properties properties = new Properties();
    private Configuration config = new Configuration();

    Button loginBtn;
    Switch swOnOff;
    ImageView iwAvatar;
    TextView tvUser, tvStatus, tvOnline;
    LinearLayout userLayout;

    private void noNetwork() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        Toast.makeText(this, R.string.Network, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        // Сохранение параметров
        properties.SaveData(this, swOnOff.isChecked(), Locale.getDefault().toString());
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        // Сохранение параметров
        properties.SaveData(this, swOnOff.isChecked(), Locale.getDefault().toString());
        super.onBackPressed();
        System.exit(0);
    }

    private void Assignment() {

        // Определение данных пользователя
        tvUser = (TextView) findViewById(R.id.tvUser);
        iwAvatar = (ImageView) findViewById(R.id.iwAvatar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvOnline = (TextView) findViewById(R.id.tvOnline);

        // Определение кнопки входа
        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        // Определения тумблера сервиса
        swOnOff = (Switch) findViewById(R.id.swOnOff);
        swOnOff.setChecked(isEnabled);
        swOnOff.setOnCheckedChangeListener(this);

        // Определение полей
        userLayout = (LinearLayout) findViewById(R.id.userLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Загрузка параметров
        properties.LoadData(this);
        isEnabled = properties.isEnabled;
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);

        // Проверка установлено ли офф приложение ВК.
        final String app = "com.vkontakte.android";
        PackageManager pManager = getPackageManager();
        try {
            PackageInfo pInfo = pManager.getPackageInfo(app, 0);
        } catch (PackageManager.NameNotFoundException e) {
            new AlertDialog.Builder(this)
                    .setTitle("VK Infinity")
                    .setMessage(R.string.AppWarning)
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Assignment(); // Вынес определения єлементов єкрана в отдельный метод для красоты

        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                switch (res) {
                    //Если вход не был выполнен
                    case LoggedOut:
                        showLoggedOut();
                        break;
                    // Если вход был выполнен
                    case LoggedIn:
                        showLoggedIn();
                        break;
                    case Unknown:
                        VKSdk.logout();
                        showLoggedOut();
                        break;
                    case Pending:
                        showLoggedIn();
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
                // nothing
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.en:
                setLanguage(new Locale("en"));
                return true;
            case R.id.ru:
                setLanguage(new Locale("ru"));
                return true;
            case R.id.ua:
                setLanguage(new Locale("uk"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setLanguage(Locale locale) {
        Locale.setDefault(locale);
        properties.SaveData(SettingsActivity.this, swOnOff.isChecked(), Locale.getDefault().toString());
        this.recreate();
    }

    // Если вход был выполнен
    private void showLoggedIn() {

        // Смена кнопки
        loginBtn.setText(R.string.loginBtnLogout);
        loginBtn.setBackgroundColor(getResources().getColor(R.color.logoutButton));
        loginBtn.setTextColor(getResources().getColor(R.color.logoutText));


        if(isEnabled && !serviceState) {
            startService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = true;
        }

        if(new Network().check(getApplicationContext())) {
            // Отправка запроса
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, status, photo_100"));

            // Обработка результата запроса
            request.executeWithListener(new VKRequest.VKRequestListener() {
                // Если запрос успешно выполнен
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    // Присваивание значений полям пользователя:
                    // Метод распарсинга результата запроса
                    ParseJSON parseJSON = new ParseJSON();
                    tvUser.setText(parseJSON.getInfo(response, "first_name")); // Имя
                    tvUser.append(" " + parseJSON.getInfo(response, "last_name")); // Фамилия

                    tvStatus.setText(parseJSON.getInfo(response, "status")); // Статус

                    if (parseJSON.getInfo(response, "online").equals("0"))
                        tvOnline.setText("Offline"); // Если статус offline
                    else
                        tvOnline.setText("Online"); // Если статус online

                    // Скачивание и установка аватара
                    new ImageFromURL(iwAvatar).execute(parseJSON.getInfo(response, "photo_100"));

                    // Установка параметров Layout с данными пользователя
                    userLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            });
        } else
            noNetwork();
    }

    // Если вход не был выполнен
    private void showLoggedOut() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        swOnOff.setChecked(false);

        loginBtn.setText(R.string.loginBtnLogin);
        loginBtn.setBackgroundColor(getResources().getColor(R.color.loginButton));
        loginBtn.setTextColor(getResources().getColor(R.color.loginText));

        if(serviceState) {
            stopService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = false;
        }
    }

    // Нажатие на тумблер сервиса
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if(VKSdk.isLoggedIn())
                showLoggedIn();
            else {
                swOnOff.setChecked(false);
                showLoggedOut();
            }

            properties.SaveData(this, true, Locale.getDefault().toString());
        }
        else if(serviceState) {
            stopService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = false;
        }
    }

    // обработка кнопок
    @Override
    public void onClick(View v) {
        if(new Network().check(this)) {
            if(VKSdk.isLoggedIn()) {
                VKSdk.logout();
                showLoggedOut();
            }
            else
                VKSdk.login(this, null);
        } else
            noNetwork();
    }

    // Обработка результата попытки входа
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showLoggedIn();
            }

            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                Toast.makeText(getApplicationContext(), R.string.AuthErr, Toast.LENGTH_SHORT).show();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
