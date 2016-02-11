package d.swan.vkinfinity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.Locale;

public class Service extends android.app.Service  {
    private AlarmManager aManager;
    private Intent intent;
    private PendingIntent pIntent;

    @Override
    public void onCreate() {
        new Logging().Write("Service created");
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

                Toast.makeText(getApplicationContext(), getResources().getString(R.string.Connected) + info, Toast.LENGTH_SHORT).show();
            }
        });

        intent = new Intent(this, Receiver.class);
        pIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        aManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        aManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 300000, pIntent);

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Logging().Write("Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        new Logging().Write("Service destroyed");
        aManager.cancel(pIntent);
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        new Logging().Write("Service onBind");
        return null;
    }
}
