package cn.rfidcn.scheduledjob.util;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Properties;

public class SettingsHelper {
    private Properties prop = new Properties();

    public boolean load(String path) {
        try {
            InputStream in = SettingsHelper.class.getResourceAsStream(path);
            prop.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean save(String path) {
        try {
            Writer writer = new FileWriter(path);
            prop.store(writer, "writer");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getValue(String key) {
        return prop.getProperty(key);
    }

    public void setValue(String key, String value) {
        prop.put(key, value);
    }

}
