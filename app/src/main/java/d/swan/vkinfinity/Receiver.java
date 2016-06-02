package d.swan.vkinfinity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if(new Network().check(context)) {
            VKRequest request = new VKRequest("account.setOnline");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                    if (response.json.toString().equals("{\"response\":1}"))
                        new Logging().Write(context, "Online");
                    else
                        new Logging().Write(context, response.json.toString());
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    new Logging().Write(context, error.toString());
                }
            });
        } else
            new Logging().Write(context, "No Internet connection!");
    }
}
