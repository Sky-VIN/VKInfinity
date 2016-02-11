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

    private boolean isEnabled = false;
    private boolean serviceState = false;
    private Properties properties = new Properties();
    private Configuration config = new Configuration();

    ImageView iwAvatar, iwOnOff, iwLang;
    TextView tvUser, tvOnline, loginText;
    LinearLayout userLayout, loginLayout, serviceLayout, langLayout, aboutLayout;

    private void noNetwork() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        Toast.makeText(this, R.string.Network, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        // Сохранение параметров
        properties.SaveData(this, isEnabled, Locale.getDefault().toString());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Сохранение параметров
        properties.SaveData(this, isEnabled, Locale.getDefault().toString());
        super.onBackPressed();
        System.exit(0);
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
        loginText = (TextView)findViewById(R.id.loginText);

        // Определения тумблера сервиса
        serviceLayout = (LinearLayout) findViewById(R.id.serviceLayout);
        serviceLayout.setOnClickListener(this);
        iwOnOff = (ImageView) findViewById(R.id.iwOnOff);
        if(isEnabled)
            iwOnOff.setImageResource(R.drawable.switch_on);
        else iwOnOff.setImageResource(R.drawable.switch_off);

        // определение кнопки языка
        langLayout = (LinearLayout) findViewById(R.id.langLayout);
        langLayout.setOnClickListener(this);
        iwLang = (ImageView) findViewById(R.id.iwLang);
        iwLang.setImageResource(R.drawable.flag);

        // определение кнопки About
        aboutLayout = (LinearLayout) findViewById(R.id.aboutLayout);
        aboutLayout.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Кастомный Action Bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
        actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);

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
                        // nothing
                        break;
                }
            }

            @Override
            public void onError(VKError error) {
                // nothing
            }
        });

    }

    // Если вход был выполнен
    private void showLoggedIn() {
        // Смена кнопки
        loginText.setText(R.string.loginBtnLogout);

        // проверка и запуск сервиса
        if(isEnabled && !serviceState) {
            startService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = true;
            iwOnOff.setImageResource(R.drawable.switch_on);
        }

        // проверка наличия Интернета
        if(new Network().check(getApplicationContext())) {
            // подготовка запроса
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, photo_100"));
            // отправка и обработка результата запроса
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
        loginText.setText(R.string.loginBtnLogin);

        isEnabled = false;
        iwOnOff.setImageResource(R.drawable.switch_off);

        if(serviceState) {
            stopService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = false;
        }
    }

    // обработка кнопок
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            // кнопка входа
            case R.id.loginLayout:
                if (new Network().check(this)) {
                    if (VKSdk.isLoggedIn()) {
                        VKSdk.logout();
                        showLoggedOut();
                    } else
                        VKSdk.login(this, null);
                } else
                    noNetwork();
                break;

            // кнопка сервиса
            case R.id.serviceLayout:
                if (!isEnabled) {
                    if(VKSdk.isLoggedIn()) {
                        isEnabled = true;
                        iwOnOff.setImageResource(R.drawable.switch_on);
                        showLoggedIn();
                    }
                    else
                        showLoggedOut();
                }
                else {
                    isEnabled = false;
                    iwOnOff.setImageResource(R.drawable.switch_off);
                    if(serviceState) {
                        stopService(new Intent("d.swan.vkinfinity.Service"));
                        serviceState = false;
                    }
                }
                break;

            // Выбор языка
            case R.id.langLayout:
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.languages));
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
                        properties.SaveData(getApplicationContext(), isEnabled, Locale.getDefault().toString());
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
                break;

            // вызов окна about
            case R.id.aboutLayout:
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
                break;
        }
    }

    // Обработка результата попытки входа
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                isEnabled = true;
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
