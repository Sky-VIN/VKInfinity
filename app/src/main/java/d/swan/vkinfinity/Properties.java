package d.swan.vkinfinity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

public class Properties {
    private final String PREF_NAME = "Properties";
    protected boolean isEnabled = true;
    protected String locale = "en";

    public void LoadData(Context context) {
        SharedPreferences sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isEnabled = sPref.getBoolean("Enabled", isEnabled);
        locale = sPref.getString("Locale", locale);
    }

    public void SaveData(Context context, boolean isEnabled, String locale) {
        SharedPreferences sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("Enabled", isEnabled);
        editor.putString("Locale", locale);
        editor.apply();
    }
}
