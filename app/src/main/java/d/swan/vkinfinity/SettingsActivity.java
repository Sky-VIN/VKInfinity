package d.swan.vkinfinity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
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

    private boolean isResumed = false;
    private boolean isEnabled = true;
    private Properties properties = new Properties();
    private Configuration config = new Configuration();

    Button loginBtn;
    ToggleButton tbOnOf;
    ImageView iwAvatar, iwLang;
    TextView tvUser, tvStatus, tvOnline;
    LinearLayout infoLayout, langLayout, userLayout;


    private void Assignment() {
        tvUser = (TextView) findViewById(R.id.tvUser);
        iwAvatar = (ImageView) findViewById(R.id.iwAvatar);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvOnline = (TextView) findViewById(R.id.tvOnline);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        // On/Off Toggle + User Info Layout
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        tbOnOf = (ToggleButton) findViewById(R.id.tbOnOff);
        tbOnOf.setOnCheckedChangeListener(this);
        tbOnOf.setChecked(isEnabled);

        if(isEnabled)
            infoLayout.setVisibility(View.VISIBLE);
        else
            infoLayout.setVisibility(View.INVISIBLE);

        userLayout = (LinearLayout) findViewById(R.id.userLayout);

        if (VKSdk.isLoggedIn())
            userLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        else
            userLayout.setLayoutParams(new LinearLayout.LayoutParams(0,0));


        // Language
        langLayout = (LinearLayout) findViewById(R.id.langLayout);
        langLayout.setOnClickListener(this);
        iwLang = (ImageView) findViewById(R.id.iwLang);
        iwLang.setImageResource(R.drawable.flag);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if(isEnabled) {
            if(VKSdk.isLoggedIn())
                showLoggedIn();
            else
                showLoggedOut();
        }
    }

    @Override
    protected void onPause() {
        properties.SaveData(this, tbOnOf.isChecked(), Locale.getDefault().toString());
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        properties.LoadData(this);
        isEnabled = properties.isEnabled;

        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Assignment();

        if(isEnabled) {
            VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
                @Override
                public void onResult(VKSdk.LoginState res) {
                    if (isResumed) {
                        switch (res) {
                            case LoggedOut:
                                showLoggedOut();
                                break;
                            case LoggedIn:
                                showLoggedIn();
                                break;
                        }
                    }
                }

                @Override
                public void onError(VKError error) {
                    // nothing
                }
            });
        }
    }

    private void showLoggedIn() {
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, status, photo_200"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                loginBtn.setText(R.string.loginBtnLogout);
                userLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                ParseJSON parseJSON = new ParseJSON();

                tvUser.setText(parseJSON.getInfo(response, "first_name") + " " + parseJSON.getInfo(response, "last_name"));
                tvStatus.setText(parseJSON.getInfo(response, "status"));

                if (parseJSON.getInfo(response, "online").equals("0"))
                    tvOnline.setText("Offline");
                else
                    tvOnline.setText("Online");

                new ImageFromURL(iwAvatar).execute(parseJSON.getInfo(response, "photo_200"));
            }
        });
    }

    private void showLoggedOut() {
        userLayout.setLayoutParams(new LinearLayout.LayoutParams(0,0));
        loginBtn.setText(R.string.loginBtnLogin);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            if(VKSdk.isLoggedIn())
                showLoggedIn();
            infoLayout.setVisibility(View.VISIBLE);
            startService(new Intent(this, Service.class));
        }
        else {
            infoLayout.setVisibility(View.INVISIBLE);
            stopService(new Intent(this, Service.class));
        }
    }

    // обработка кнопок
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Нажатие на кнопку логина
            case R.id.loginBtn:
                if (VKSdk.isLoggedIn()) {
                    VKSdk.logout();
                    showLoggedOut();
                } else
                    VKSdk.login(this, null);
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
                                properties.SaveData(SettingsActivity.this, tbOnOf.isChecked(), Locale.getDefault().toString());
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

    // Обработка входа
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
