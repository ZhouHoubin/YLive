package z.hobin.ylive.util;

import android.content.res.Resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    public static String readRaw(Resources resources, int id) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream is = resources.openRawResource(id);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String str = "";
            while ((str = br.readLine()) != null) {
                builder.append(str + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
