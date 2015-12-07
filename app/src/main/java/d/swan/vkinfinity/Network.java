package d.swan.vkinfinity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

    public boolean check(Context context) {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        if(cManager.getActiveNetworkInfo() == null) {
            return false;
        } else
            return true;
    }
}
