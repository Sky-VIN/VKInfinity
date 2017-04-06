package d.swan.vkinfinity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogActivity extends Activity {

    TextView tvLog;
    private File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/VKI.log");

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setCustomActionBar(); // Кастомный Action Bar

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        tvLog = (TextView) findViewById(R.id.tvLog);

        fillHistory();
    }

    private void fillHistory() {
        tvLog.setText("");
        if (logFile.exists()) {
            try {
                FileReader reader = new FileReader(logFile);
                char[] buffer = new char[(int) logFile.length()];
                reader.read(buffer);
                tvLog.setText(new String(buffer));
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            tvLog.setTypeface(Typeface.DEFAULT);
            tvLog.setTextColor(getResources().getColor(R.color.mainText));
            tvLog.setGravity(Gravity.NO_GRAVITY);
        } else {
            tvLog.append("\n(Empty)\n");
            tvLog.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            tvLog.setTextColor(getResources().getColor(R.color.shadow));
            tvLog.setGravity(Gravity.CENTER);
        }
    }


    private void setCustomActionBar() {
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(false); //не показываем иконку приложения
        actionBar.setDisplayShowTitleEnabled(false); // и заголовок тоже прячем
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setCustomView(R.layout.action_bar);

        TextView title = (TextView) findViewById(R.id.tvTitle);
        title.setText("Infinity :: " + getResources().getString(R.string.logText));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refreshLogButton:
                fillHistory();
                Toast.makeText(this, R.string.refreshText, Toast.LENGTH_SHORT).show();
                break;
            case R.id.clearLogButton:
                new AlertDialog.Builder(LogActivity.this)
                        .setTitle(R.string.app_name)
                        .setMessage(R.string.clearHistory)
                        .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (logFile.exists()) {
                                    logFile.delete();
                                    fillHistory();
                                }
                            }
                        })
                        .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .show();
                break;
        }
        return true;
    }
}
