package d.swan.vkinfinity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Properties properties = new Properties();
        properties.LoadData(context);
        if(properties.isEnabled)
            context.startService(new Intent("d.swan.vkinfinity.Service"));
        else
            context.stopService(new Intent("d.swan.vkinfinity.Service"));
    }
}
