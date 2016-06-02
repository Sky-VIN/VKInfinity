package d.swan.vkinfinity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Properties properties = new Properties();
        properties.LoadData(context);
        if(properties.isEnabled) {
            new File("/sdcard/VKI/log.txt").delete();
            context.startService(new Intent("d.swan.vkinfinity.Service"));
        }
        else
            context.stopService(new Intent("d.swan.vkinfinity.Service"));
    }
}
