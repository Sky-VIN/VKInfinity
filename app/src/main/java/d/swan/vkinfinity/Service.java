package d.swan.vkinfinity;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Service extends android.app.Service  {
    private Timer timer = new Timer();
    private boolean isLoggedIn = false;

    @Override
    public void onCreate() {
        Toast.makeText(getApplicationContext(), R.string.ServiceStarted, Toast.LENGTH_SHORT).show();
        Log.d("VKI", "Service STARTED");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("VKI", "Service onStartCommand");
        Properties properties = new Properties();
        properties.LoadData(getApplicationContext());
        isLoggedIn = properties.isLoggedIn;
        setOnline();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), R.string.ServiceStopped, Toast.LENGTH_SHORT).show();
        Log.d("VKI", "Service STOPPED");
        timer.cancel();
        super.onDestroy();
        System.exit(0);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setOnline() {
        Task task = new Task();
        timer.schedule(task, 0, 2500);
    }

    public class Task extends TimerTask {
        Date now;
        @Override
        public void run() {
            now = new Date();
            //VKRequest request = new VKRequest("account.setOnline");

            //Log.d("VKI", "Yesss =))) at " + now);
            if(isLoggedIn && new Network().check(getApplicationContext()))
                Log.d("VKI", now + " Yeess ))))");

            Log.d("VKI", now + " Service: isLoggedIn = " + isLoggedIn + " Network = " + new Network().check(getApplicationContext()));
        }
    }
}
