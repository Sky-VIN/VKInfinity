package d.swan.vkinfinity;

import android.content.Context;
import android.content.SharedPreferences;

public class Properties {
    private static final String PREF_NAME = "Properties";
    protected boolean isEnabled = true;
    protected boolean isLoggedIn = false;
    protected String locale = "en";

    public void LoadData(Context context) {
        SharedPreferences sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isEnabled = sPref.getBoolean("Enabled", isEnabled);
        isLoggedIn = sPref.getBoolean("LoggedIn", isLoggedIn);
        locale = sPref.getString("Locale", locale);
    }

    public void SaveData(Context context, boolean isEnabled, boolean isLoggedIn, String locale) {
        SharedPreferences sPref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean("Enabled", isEnabled);
        editor.putBoolean("LoggedIn", isLoggedIn);
        editor.putString("Locale", locale);
        editor.apply();
    }

}
