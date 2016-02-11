package d.swan.vkinfinity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Logging {
    public void Write(String message) {
        File logfile = new File("/sdcard/VKI");
        if(!logfile.exists())
            logfile.mkdir();

        try {
            FileWriter fWriter = new FileWriter(logfile + "/log.txt", true);
            fWriter.append("[" + new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(System.currentTimeMillis()) + "]: " + message + "\n");
            fWriter.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
