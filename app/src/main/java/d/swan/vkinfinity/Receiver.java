package d.swan.vkinfinity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public class Receiver extends BroadcastReceiver {

    private Logging logging = new Logging();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (new Network(context).check()) {
            VKRequest request = new VKRequest("account.setOnline");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    if (response.json.toString().equals("{\"response\":1}"))
                        logging.Write("Online");
                    else
                        logging.Write(response.json.toString());
                }

                @Override
                public void onError(VKError error) {
                    logging.Write(error.errorMessage);
                }
            });
        } else
            logging.Write("No Internet connection!");
    }
}
