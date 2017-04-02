package d.swan.vkinfinity;

import android.content.Context;
import android.net.ConnectivityManager;

class Network {
    private Context context;

    Network(Context context) {
        this.context = context;
    }

    boolean check() {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }
}
