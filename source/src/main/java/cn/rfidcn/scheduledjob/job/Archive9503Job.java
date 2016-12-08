package cn.rfidcn.scheduledjob.job;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import cn.rfidcn.scheduledjob.dao.ElasticsearchDao;
import cn.rfidcn.scheduledjob.util.MailSender;

public class Archive9503Job implements Runnable{

	@Override
	public void run() {
		System.out.println("start Archiving 9503 error Job @" + new Date());
		ElasticsearchDao esDao = ElasticsearchDao.getInstance();
		String msg = null;
		try {
			msg = esDao.archive9503();
			//esDao.removeOld();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msg==null){
			System.out.println("no error found, done!");
			return;
		}
		System.out.println("archive 9503 send out email.." + new Date());
		MailSender.getInstance().send( new String[]{"da.zhang@sao.so"}, "archive 9503", msg);
		System.out.println("end archive 9503..");
	}

}
