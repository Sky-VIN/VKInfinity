package d.swan.vkinfinity;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Logging {
    public void Write(Context context, String message) {
        boolean isNew = true;
        File logfolder = new File("/sdcard/VKI");
        if(!logfolder.exists())
            logfolder.mkdir();

        if (new File(logfolder + "/log.txt").exists())
            isNew = false;

        try {
            FileWriter fWriter = new FileWriter(logfolder + "/log.txt", true);
            if (isNew)
                fWriter.append("Created at " + new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(System.currentTimeMillis()) + "\n\n");

            fWriter.append("[" + new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(System.currentTimeMillis()) + "]: " + message + "\n");
            fWriter.close();
        } catch (IOException e)
        {
            Toast.makeText(context, e.getMessage().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
