package d.swan.vkinfinity;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class Service extends android.app.Service {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("VKI", "Service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("VKI", "Service Start Command");
        setOnline();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("VKI", "Service Destroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("VKI", "Service Bind");
        return null;
    }

    private void setOnline() {
        for(int i = 0; i<25; i++) {
            Log.d("VKI", "i = " + i);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
