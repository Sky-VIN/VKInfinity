package d.swan.vkinfinity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.File;

public class StartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Properties properties = new Properties(context);
        properties.LoadData();

        if (properties.isEnabled) {
            new File(Environment.getExternalStorageDirectory().getPath() + "/VKI.log").delete();
            new Manager(context).start();
        }
    }
}
