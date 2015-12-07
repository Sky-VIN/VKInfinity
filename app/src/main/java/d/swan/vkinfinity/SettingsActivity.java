package d.swan.vkinfinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private boolean isLoggedIn = false;
    private boolean serviceState = false;
    private Properties properties = new Properties();
    private Configuration config = new Configuration();

    Button loginBtn;
    ToggleButton tbOnOf;
    ImageView iwAvatar, iwLang;
    TextView tvUser, tvStatus, tvOnline;
    LinearLayout langLayout, userLayout;

    private void noNetwork() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        Toast.makeText(this, R.string.Network, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        // Сохранение параметров
        properties.SaveData(this, tbOnOf.isChecked(), isLoggedIn, Locale.getDefault().toString());
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // Сохранение параметров
        properties.SaveData(this, tbOnOf.isChecked(), isLoggedIn, Locale.getDefault().toString());
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
        tbOnOf = (ToggleButton) findViewById(R.id.tbOnOff);
        tbOnOf.setChecked(isEnabled);
        tbOnOf.setOnCheckedChangeListener(this);

        // Определение полей
        userLayout = (LinearLayout) findViewById(R.id.userLayout);

        // Определение иконки и поля языка
        langLayout = (LinearLayout) findViewById(R.id.langLayout);
        langLayout.setOnClickListener(this);
        iwLang = (ImageView) findViewById(R.id.iwLang);
        iwLang.setImageResource(R.drawable.flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("VKI", " ");
        Log.d("VKI", "- - - - - - - - - - - - - - - - - - - - - - - - -");

        // Загрузка параметров
        properties.LoadData(this);
        isEnabled = properties.isEnabled;
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Assignment(); // Вынес определения єлементов єкрана в отдельный метод для красоты

        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                switch (res) {
                    //Если вход не был выполнен
                    case LoggedOut:
                        Log.d("VKI", "If LoggedOut in Wakeup");
                        showLoggedOut();
                        break;
                    // Если вход был выполнен
                    case LoggedIn:
                        Log.d("VKI", "If LoggedIn in Wakeup");
                        showLoggedIn();
                        break;
                    case Unknown:
                        Log.d("VKI", "If Unknown in Wakeup");
                        VKSdk.logout();
                        showLoggedOut();
                        break;
                    case Pending:
                        Log.d("VKI", "If Pending in Wakeup");
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

    // Если вход был выполнен
    private void showLoggedIn() {
        isLoggedIn = true;
        properties.SaveData(this, isEnabled, isLoggedIn, Locale.getDefault().toString());

        // Смена кнопки
        loginBtn.setText(R.string.loginBtnLogout);


        if(isEnabled && !serviceState) {
            Log.d("VKI", "START service in showLoggedIn");
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
        isLoggedIn = false;
        isEnabled = false;
        properties.SaveData(this, isEnabled, isLoggedIn, Locale.getDefault().toString());
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
        loginBtn.setText(R.string.loginBtnLogin);
        tbOnOf.setChecked(false);

        if(serviceState) {
            Log.d("VKI", "STOP service in showLoggedOut");
            stopService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = false;
        }
    }

    // Нажатие на тумблер сервиса
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isEnabled = true;
            if(VKSdk.isLoggedIn())
                showLoggedIn();
            else
                showLoggedOut();
        }
        else if(serviceState) {
            Log.d("VKI", "STOP service in tbOnOff");
            stopService(new Intent("d.swan.vkinfinity.Service"));
            serviceState = false;
        }
    }

    // обработка кнопок
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Нажатие на кнопку логина
            case R.id.loginBtn:
                if(new Network().check(this)) {
                    if(VKSdk.isLoggedIn()) {
                        VKSdk.logout();
                        showLoggedOut();
                    }
                    else
                        VKSdk.login(this, null);
                } else
                    noNetwork();
                break;

            // Нажатие на кнопку языка (на поле langLayout)
            case R.id.langLayout:
                ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, getResources().getTextArray(R.array.languages));
                new AlertDialog.Builder(this)
                        .setTitle(R.string.lang)
                        .setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
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
                                dialog.dismiss();
                                properties.SaveData(SettingsActivity.this, tbOnOf.isChecked(), isLoggedIn, Locale.getDefault().toString());
                                SettingsActivity.super.recreate();
                            }
                        })
                        .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
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
