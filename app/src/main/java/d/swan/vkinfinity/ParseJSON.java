package d.swan.vkinfinity;

import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseJSON {
    public String getInfo(VKResponse response, String field) {
        String result = null;
        try {
            JSONObject jsonResponse = response.json.getJSONArray("response").getJSONObject(0);
            result = jsonResponse.getString(field);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
