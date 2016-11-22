package d.swan.vkinfinity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.SystemClock;
import android.widget.Toast;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;

class Manager {
    private Context context;
    private AlarmManager aManager;
    private PendingIntent pIntent;
    private Logging logging = new Logging();

    Manager(Context context) {
        this.context = context;
    }

    void start() {
        logging.Write("Service started");

        // Загрузка настроек
        Properties properties = new Properties(context);
        properties.LoadData();

        // Установка языка
        Configuration config = new Configuration();
        config.locale = new Locale(properties.locale);
        Locale.setDefault(config.locale);
        context.getResources().updateConfiguration(config, null);

        // Отправка запроса на выявления пользователя
        VKRequest request = VKApi.users().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                // Заполнение данных пользователя
                JSON json = new JSON();
                String info;
                info = json.getInfo(response, "first_name");
                info += " ";
                info += json.getInfo(response, "last_name");
                Toast.makeText(context, context.getResources().getString(R.string.Connected) + info, Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(context, Receiver.class);
        pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        aManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        aManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 600000, pIntent);
    }

    void stop() {
        try {
            aManager.cancel(pIntent);
            logging.Write("Service stopped");
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
