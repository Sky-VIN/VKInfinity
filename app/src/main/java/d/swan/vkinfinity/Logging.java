package d.swan.vkinfinity;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

class Logging {
    void Write(String message) {
        String dateTime = new SimpleDateFormat("dd/MM/yyyy, HH:mm:ss").format(System.currentTimeMillis());
        File logFile = new File(Environment.getExternalStorageDirectory().getPath() + "/VKI.log");
        boolean isLogFileExists = logFile.exists();
        try {
            FileWriter fWriter = new FileWriter(logFile, true);
            if (!isLogFileExists)
                fWriter.append("Created at ").append(dateTime).append("\n\n");

            fWriter.append("[").append(dateTime).append("]: ").append(message).append("\n");
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
