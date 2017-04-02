package d.swan.vkinfinity;

import android.content.Context;
import android.content.SharedPreferences;

class Properties {
    private SharedPreferences sPref;
    boolean isEnabled = false;
    String locale = "en";
    int schedule = 10;

    Properties(Context context) {
        sPref = context.getSharedPreferences("Properties", Context.MODE_PRIVATE);
    }

    void LoadData() {
        isEnabled = sPref.getBoolean("Enabled", isEnabled);
        locale = sPref.getString("Locale", locale);
        schedule = sPref.getInt("Schedule", schedule);
    }

    void SaveData(boolean isEnabled, String locale, int schedule) {
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("Enabled", isEnabled);
        editor.putString("Locale", locale);
        editor.putInt("Schedule", schedule);
        editor.apply();
    }
}
