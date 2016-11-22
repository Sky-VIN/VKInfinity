package d.swan.vkinfinity;

import android.content.Context;
import android.content.SharedPreferences;

class Properties {
    private SharedPreferences sPref;
    boolean isEnabled = true;
    String locale = "en";

    Properties(Context context) {
        sPref = context.getSharedPreferences("Properties", Context.MODE_PRIVATE);
    }

    void LoadData() {
        isEnabled = sPref.getBoolean("Enabled", isEnabled);
        locale = sPref.getString("Locale", locale);
    }

    void SaveData(boolean isEnabled, String locale) {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("Enabled", isEnabled);
        editor.putString("Locale", locale);
        editor.apply();
    }
}
