package cn.rfidcn.scheduledjob.job;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.rfidcn.scheduledjob.dao.ElasticsearchDao;
import cn.rfidcn.scheduledjob.model.IdRange;
import cn.rfidcn.scheduledjob.model.MailGroup;
import cn.rfidcn.scheduledjob.util.MailSender;

public class AppeventAllErrorsSummaryJob implements Runnable {

    static String cjh = "junhua.cui@sao365.cn";
    static String steve = "steve.liu@sao365.cn";
    static String peter = "pitar.leung@sao365.cn";
    static String whb = "hanbin.wang@sao365.cn";
    static String zd = "da.zhang@sao365.cn";
    static String zj = "jun.zhou@sao365.cn";
    static String hj = "jing.huang@sao365.cn";
    static String lsh = "senhui.li@sao365.cn";
    static String qwb = "wenbin.qian@sao365.cn";
    static String gm = "ming.gao@sao365.cn";
    static String lyh = "yaohui.li@sao365.cn";
    static String sfd = "fudong.su@sao365.cn";
    static String fxf = "xufeng.fei@sao365.cn";
    static String js = "shuai.jin@sao365.cn";
    static String zf = "feng.zhou@sao365.cn";
    static String xs = "sheng.xu@sao365.cn";
    static String sch = "chaohua.shi@sao365.cn";
    static String xsn = "shuning.xie@sao365.cn";

    static MailGroup[] mailGroups = new MailGroup[] {
            new MailGroup("Portal endpoints related", new String[] { qwb, gm, zj, steve, peter, zf }, new IdRange[] {
                    new IdRange(6000, 6099), new IdRange(6100, 6199), new IdRange(6400, 6499), new IdRange(7100, 7199),
                    new IdRange(7200, 7399), new IdRange(7600, 7799), new IdRange(8400, 8499), new IdRange(8700, 8749),
                    new IdRange(9200, 9299), new IdRange(30200, 30299), new IdRange(30300, 30399) }),
            new MailGroup("Scanning related", new String[] { whb, lsh, lyh, gm, steve, peter, zf }, new IdRange[] {
                    new IdRange(7800, 7899), new IdRange(6900, 7099), new IdRange(8500, 8599), new IdRange(9500, 9549),
                    new IdRange(30400, 30499) }),
            new MailGroup("Tag creation related", new String[] { cjh, whb, qwb, steve, peter, zf }, new IdRange[] {
                    new IdRange(6200, 6299), new IdRange(6700, 6799), new IdRange(8100, 8199) }),
            new MailGroup("Taobao tag creation related", new String[] { cjh, whb, qwb, zj, steve, peter, zf }, new IdRange[] {
                    new IdRange(8900, 8999), new IdRange(9100, 9199) }),
            new MailGroup("Tag encryption/decryptoin related", new String[] { steve, cjh, peter, zf },
                    new IdRange[] { new IdRange(6500, 6599) }),
            new MailGroup("[high] SQLConnection errors", new String[] { cjh, steve, peter, zf }, new IdRange[] { new IdRange(
                    6600, 6699) }),
            new MailGroup("Kafka, avro related", new String[] { zd, cjh, steve, peter, zf }, new IdRange[] { new IdRange(8650,
                    8699) }),
            new MailGroup("Promotion rewards related", new String[] { hj, zj, whb, lsh, steve, peter, zf },
                    new IdRange[] { new IdRange(8200, 8299) }),
            new MailGroup("TAE/scanning related", new String[] { gm, whb, lsh }, new IdRange[] { new IdRange(9000, 9099) }),
            new MailGroup("Pointmall related", new String[] { zj, steve, peter, zf }, new IdRange[] { new IdRange(9400, 9499),
                    new IdRange(9550, 9599), new IdRange(9700, 9799) }),
            new MailGroup("Wechat related", new String[] { zj, lsh, steve, peter, zf }, new IdRange[] { new IdRange(9900, 9999) }),
            new MailGroup("Coupon related", new String[] { whb,xsn,sch,xs }, new IdRange[] {new IdRange(17,19)})
    };

    static MailGroup factoryMailGroup = new MailGroup("Factory related", new String[] { cjh, steve, peter }, new IdRange[] {});
    static MailGroup websiteMailGroup = new MailGroup("Website related", new String[] { fxf, sfd, js, lyh, steve, peter },
            new IdRange[] {});
    static MailGroup zhabeiffw = new MailGroup("ZhabeiFFW related", new String[] { hj, zj, steve, peter, zf, cjh },
            new IdRange[] {});
    static MailGroup jingan = new MailGroup("Jingan related", new String[] { "dev@sao365.cn" }, new IdRange[] {});
    static MailGroup defaultMailGroup = new MailGroup("Unclassified", new String[] { "dev@sao365.cn" }, new IdRange[] {});

    @Override
    public void run() {
        System.out.println("start AppeventAllErrorsSummaryJob @" + new Date());
        ElasticsearchDao esDao = ElasticsearchDao.getInstance();
        Map<MailGroup, String> map = new HashMap<MailGroup, String>();
        try {
            Map<String, Integer> errorStats = esDao.getDistinctErrorsAndCounts();

            System.out.println("dispatch receivers..");
            for (String s : errorStats.keySet()) {
                String[] ss = s.split(",");
                int ae = Integer.parseInt(ss[0].trim());
                String d = ss[1].trim();

                MailGroup mg = findMailGroup(ae, d);
                String errors = map.get(mg);
                if (errors == null) {
                    errors = "";
                }
                StringBuilder sb = new StringBuilder();
                errors += (sb.append("错误出现").append(errorStats.get(s)).append("次,").append(s).append("\n").toString());
                map.put(mg, errors);
            }

            System.out.println("all errors send out email.." + new Date());
            for (MailGroup mg : map.keySet()) {
                MailSender.getInstance().send(mg.getMembers(), mg.getProblemName(), map.get(mg));
            }
            System.out.println("end AppeventAllErrorsSummaryJob");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private MailGroup findMailGroup(int ae, String d) {
        if ("FactoryAppRoot".equalsIgnoreCase(d))
            return factoryMailGroup;
        if ("zhabeiwebsite".equalsIgnoreCase(d))
            return websiteMailGroup;
        if ("zhabeiplatformffw".equalsIgnoreCase(d))
            return zhabeiffw;
        if ("localRole".equalsIgnoreCase(d) || "jinganplatform".equalsIgnoreCase(d))
            return jingan;
        for (MailGroup mg : mailGroups) {
            if (mg.isInGroup(ae)) {
                return mg;
            }
        }
        return defaultMailGroup;
    }
}
