package cn.rfidcn.scheduledjob.job;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import cn.rfidcn.scheduledjob.dao.ElasticsearchDao;
import cn.rfidcn.scheduledjob.util.MailSender;


public class AppeventOOMJob implements Runnable{

	@Override
	public void run() {
		System.out.println("start AppeventOOMJob @" + new Date());
		ElasticsearchDao esDao = ElasticsearchDao.getInstance();
		String msg="";
		try {
			msg = esDao.getOOMError();
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msg==null || msg.length()==0){
			System.out.println("no error found, done!");
			return;
		}
		System.out.println("send out email.." + new Date());
		MailSender.getInstance().send( new String[]{"feng.zhou@sao.so","pitar.leung@sao.so","steve.liu@sao.so","da.zhang@sao.so"}, "[high] OOM errors", msg);
		System.out.println("end AppeventOOMJob");
		
	}

}
