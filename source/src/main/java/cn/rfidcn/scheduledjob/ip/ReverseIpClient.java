package cn.rfidcn.scheduledjob.ip;


public class ReverseIpClient {
    static IP2Location ip2Location;
    static {
        ip2Location = new IP2Location();
        ip2Location.IPDatabasePath = "/home/zhangda/IP2LOCATION-LITE-DB11.BIN";
    }

    public static String[] getGeoInfo(String ip) {
        String country = "";
        String state = "";
        String city = "";
        if (ip != null && ip.contains(":")) {
            ip = ip.substring(0, ip.indexOf(":"));
        }
        IPResult ipResult;
        if (ip != null) {
            try {
                ipResult = ip2Location.IPQuery(ip);
                country = ipResult.getCountryLong();
                state = ipResult.getRegion();
                city = ipResult.getCity();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        country = country == null ? "" : country;
        state = state == null ? "" : state;
        city = city == null ? "" : city;
        return new String[] { country, state, city };

    }
}
