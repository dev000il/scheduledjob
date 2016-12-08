package cn.rfidcn.scheduledjob.util;


public class ApplicationConfig {
    public static SettingsHelper setting = null;
    
    static {
        String path = "/application_config.properties";
        // 初始化配置文件
        if (!LoadSetting(path)) {
            System.out.println("ERROR loading setting files!!");
        }
    }

    /**
     * initialize global setting
     * 
     * @param path
     * @return
     */
    private static boolean LoadSetting(String path) {
        if (setting == null) {
            setting = new SettingsHelper();
            if (!setting.load(path)) {
                return false;
            }
        }
        return true;
    }
    
    public static final String ElasticSearchServer = setting.getValue("elasticsearch.server");
    public static final String elasticsearchUsername = setting.getValue("elasticsearch.user");
    public static final String elasticsearchPassword = setting.getValue("elasticsearch.password");
  
}
