package d.swan.vkinfinity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Кастомный Action Bar
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
        actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        TextView tvLog = (TextView) findViewById(R.id.tvLog);
        tvLog.setText("");

        File logFile = new File("/sdcard/VKI.log");
        if (logFile.exists()) {
            try {
                FileReader reader = new FileReader(logFile);
                char[] buffer = new char[(int) logFile.length()];
                reader.read(buffer);
                tvLog.setText(new String(buffer));
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else tvLog.append("(Empty)");
    }
}
