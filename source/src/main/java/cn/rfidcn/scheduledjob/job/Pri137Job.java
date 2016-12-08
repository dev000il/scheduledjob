package cn.rfidcn.scheduledjob.job;

import java.util.Date;

import cn.rfidcn.scheduledjob.dao.DrillDao;
import cn.rfidcn.scheduledjob.util.MailSender;

public class Pri137Job implements Runnable{

	@Override
	public void run() {
		System.out.println("start pri137 @" + new Date());
		String msg = DrillDao.getInstance().getPri137();
		if(msg==null || msg.trim().length()==0){
			System.out.println("not pri127 found, end....");	
			return;
		}
		System.out.println("send pri137 msg out...");	
		MailSender.getInstance().send( new String[]{"da.zhang@sao.so","wenbin.lu@sao.so","steve.liu@sao.so", "pitar.leung@sao.so","jay.jia@sao.so", "philip.luo@sao.so"}, 
				"pri137 hourly stats", "ts,hid,bid,num,pri,oip,pn,rwds,pts\n"+msg);
		System.out.println("end pri137 job");
	}

}
