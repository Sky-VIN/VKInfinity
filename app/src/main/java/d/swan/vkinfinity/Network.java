package d.swan.vkinfinity;

import android.content.Context;
import android.net.ConnectivityManager;

public class Network {
    private Context context;

    Network(Context context) {
        this.context = context;
    }

    public boolean check() {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cManager.getActiveNetworkInfo() != null;
    }
}
