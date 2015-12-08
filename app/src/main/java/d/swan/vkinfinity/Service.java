package d.swan.vkinfinity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Service extends android.app.Service  {
    private Timer timer = new Timer();

    private void Log(String message) {
        File logfile = new File("/sdcard/VKI");
        if(!logfile.exists())
            logfile.mkdir();
        try {
            FileWriter fWriter = new FileWriter(logfile + "/log.txt", true);
            fWriter. append("[" + new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(System.currentTimeMillis()) + "]: " + message + "\n");
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        Log("Service onCreate");
        // Загрузка настроек
        Properties properties = new Properties();
        properties.LoadData(this);

        // Установка языка
        Configuration config = new Configuration();
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        getResources().updateConfiguration(config, null);

        // Отправка запроса на выявления пользователя
        VKRequest request = VKApi.users().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                // Заполнение данных пользователя
                String info = new ParseJSON().getInfo(response, "first_name");
                info += " " + new ParseJSON().getInfo(response, "last_name");
                info += " :: ID: " + new ParseJSON().getInfo(response, "id");

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Connected) + info, Toast.LENGTH_SHORT).show();
                Log(info);
            }
        });

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log("Service onStartCommand");
        setOnline();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log("Service onDestroy");
        timer.cancel();
        super.onDestroy();
        System.exit(0);
    }


    @Override
    public IBinder onBind(Intent intent) {
        Log("Service onBind");
        return null;
    }

    private void setOnline() {
        Task task = new Task();
        timer.schedule(task, 0, 300000);
    }

    @Override
    public void onLowMemory() {
        Log("Service onLowMemory!");
        super.onLowMemory();
    }

    public class Task extends TimerTask {

        @Override
        public void run() {
            if(new Network().check(getApplicationContext())) {
                VKRequest request = new VKRequest("account.setOnline");
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Log(response.json.toString());
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Log(error.toString());
                    }
                });
            } else
                Log("No Internet connection!");
        }
    }
}
