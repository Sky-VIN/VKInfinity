package d.swan.vkinfinity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

public class SettingsActivity extends Activity implements OnItemSelectedListener, OnCheckedChangeListener, OnClickListener {

    private boolean isResumed = false;
    private boolean isLogined = false;

    ToggleButton tbOnOf;
    TextView tvOnOff, tvUser, tvOnline;
    ImageView iwAvatar;
    Button loginBtn;
    Spinner spnrLang;
    LinearLayout infoLayout;

    private final static String[] lang = new String[]{"English", "Русский", "Українська"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        VKSdk.wakeUpSession(this, new VKCallback<VKSdk.LoginState>() {
            @Override
            public void onResult(VKSdk.LoginState res) {
                if (isResumed) {
                    switch (res) {
                        case LoggedOut:
                            showLogouted();
                            break;
                        case LoggedIn:
                            showLogined();
                            break;
                        case Pending:
                            break;
                        case Unknown:
                            break;
                    }
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });



        tvOnOff = (TextView) findViewById(R.id.tvOnOff);
        tvUser = (TextView) findViewById(R.id.tvUser);
        tvOnline = (TextView) findViewById(R.id.tvOnline);
        iwAvatar = (ImageView) findViewById(R.id.iwAvatar);
        infoLayout = (LinearLayout) findViewById(R.id.infoLayout);

        loginBtn = (Button) findViewById(R.id.loginBtn);
        loginBtn.setOnClickListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lang);
        spnrLang = (Spinner) findViewById(R.id.spnrLang);
        spnrLang.setAdapter(adapter);
        spnrLang.setPrompt("Language");
        spnrLang.setSelection(0);
        spnrLang.setOnItemSelectedListener(this);


        tbOnOf = (ToggleButton) findViewById(R.id.tbOnOff);
        tbOnOf.setOnCheckedChangeListener(this);
    }

    private void showLogined() {
        isLogined = true;
        loginBtn.setText("Log Out");
        VKRequest setOnline = new VKRequest("account.setOffline");
        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.FIELDS, "first_name, last_name, online, photo_100"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                ParseJSON parseJSON  = new ParseJSON();

                tvUser.setText(parseJSON.getInfo(response, "first_name") + " " + parseJSON.getInfo(response, "last_name"));

                if(parseJSON.getInfo(response, "online").equals("0"))
                    tvOnline.setText("Offline");
                else
                    tvOnline.setText("Online");

                new ImageFromURL(iwAvatar).execute(parseJSON.getInfo(response, "photo_100"));

            }
        });
    }

    private void showLogouted() {
        isLogined = false;
        iwAvatar.setImageResource(R.drawable.user);
        tvUser.setText("");
        tvOnline.setText("");
        loginBtn.setText("Log In");
    }

    // Выбор языка
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                Toast.makeText(this, lang[position], Toast.LENGTH_SHORT).show();
                break;
            case 1:
                Toast.makeText(this, lang[position], Toast.LENGTH_SHORT).show();
                break;
            case 2:
                Toast.makeText(this, lang[position], Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //nothing
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked)
            infoLayout.setVisibility(View.VISIBLE);
        else
            infoLayout.setVisibility(View.INVISIBLE);
    }


    // Надатие на кнопку логина
    @Override
    public void onClick(View v) {
        if(isLogined) {
            VKSdk.logout();
            showLogouted();
        }
        else
            VKSdk.login(this, null);
    }

    // Обработка входа
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VKCallback<VKAccessToken> callback = new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showLogined();
            }

            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                Toast.makeText(getApplicationContext(), "User didn't pass Authorization", Toast.LENGTH_SHORT).show();
            }
        };

        if (!VKSdk.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // -------------------------------------------------- || --------------------------------------------------
    // -------------------------------------------------- || --------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
        if (VKSdk.isLoggedIn()) {
            showLogined();
        } else {
            showLogouted();
        }
    }

    @Override
    protected void onPause() {
        isResumed = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
