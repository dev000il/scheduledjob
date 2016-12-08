package cn.rfidcn.scheduledjob.job;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import cn.rfidcn.scheduledjob.dao.ElasticsearchDao;
import cn.rfidcn.scheduledjob.util.MailSender;

public class AppeventMissingBolbErrorsJob implements Runnable {

    @Override
    public void run() {

        System.out.println("start AppeventMissingBolbErrorsJob @" + new Date());
        ElasticsearchDao esDao = ElasticsearchDao.getInstance();

        String[] emailContent = null;
        try {
            emailContent = esDao.getMissingBlobErrors();
            // esDao.removeOld();
        } catch (InterruptedException | ExecutionException | SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("missing blob send out email.." + new Date());

        //加上UTF-8文件的标识字符 ,给将要输出的内容加上BOM标识
        String fileStr = new String(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
        String allCompanyContent = fileStr + emailContent[0];
        String yibaoContent = fileStr + emailContent[1];

        try {
            Path tempFileNamePath = Paths.get(System.getProperty("user.dir") + "/inactive-scans.csv");
            Files.write(tempFileNamePath, allCompanyContent.getBytes("UTF-8"));
            MailSender.getInstance()
                    .send(new String[] { "data@sao365.cn", "deyu.yu@sao365.cn", "pitar.leung@sao365.cn", "philip.luo@sao365.cn" },
                            //new String[] { "steve.liu@sao365.cn" },
                            "[inactive-scans] ALL - 过去1天blob存储错误明细", "[attachment]", new File[] { tempFileNamePath.toFile() });

            Path yibaoPath = Paths.get(System.getProperty("user.dir") + "/inactive-scans-yibao.csv");
            Files.write(yibaoPath, yibaoContent.getBytes("UTF-8"));
            MailSender.getInstance()
                    .send(new String[] { "data@sao365.cn", "deyu.yu@sao365.cn", "pitar.leung@sao365.cn", "philip.luo@sao365.cn",
                            "zhangshuqin15@crbeverage.com" },
                            //new String[] { "steve.liu@sao365.cn" },
                            "[inactive-scans] 怡宝 - 过去1天blob存储错误明细", "[attachment]", new File[] { yibaoPath.toFile() });
        } catch (Exception e) {
            System.out.println("Exception occurred on job: inactive-scans\n" + e.toString());
        }

        System.out.println("end AppeventMissingBolbErrorsJob");
    }

}
