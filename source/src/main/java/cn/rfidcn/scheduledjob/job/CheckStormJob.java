/*
 * package cn.rfidcn.scheduledjob.job;
 * 
 * import java.util.Date;
 * 
 * import org.apache.curator.framework.CuratorFramework; import
 * org.apache.curator.framework.CuratorFrameworkFactory; import
 * org.apache.curator.retry.RetryNTimes; import org.apache.zookeeper.data.Stat;
 * 
 * import cn.rfidcn.scheduledjob.model.StormStats; import
 * cn.rfidcn.scheduledjob.util.MailSender;
 * 
 * public class CheckStormJob implements Runnable{
 * 
 * @Override public void run() { CuratorFramework client =
 * CuratorFrameworkFactory
 * .builder().connectString("prod1:2181,prod2:2181,prod3:2181").retryPolicy(new
 * RetryNTimes(3, 1000)).build(); client.start(); StormStats stats_act =
 * checkJob(client, "/transactional/actspoutv3/coordinator"); StormStats
 * stats_app = checkJob(client, "/transactional/appspoutv2/coordinator");
 * client.close(); long now = System.currentTimeMillis();
 * 
 * stats_act.setJob("activitylog"); stats_act.setLastOkTransactionTimestamp(new
 * Date(stats_act.getLastOkTransactionTime()));
 * 
 * stats_app.setJob("appevent"); stats_app.setLastOkTransactionTimestamp(new
 * Date(stats_app.getLastOkTransactionTime()));
 * 
 * System.out.println(stats_act+"\n"+stats_app);
 * 
 * if( now - stats_act.getLastOkTransactionTime() > 10*60*1000 ||
 * stats_act.getReAttemptCounts() > 10 ||
 * now-stats_app.getLastOkTransactionTime() > 10*60*1000 ||
 * stats_app.getReAttemptCounts() > 10 ){
 * System.out.println("storm job have problem!!!!");
 * MailSender.getInstance().send( new String[]{"da.zhang@sao.so" ,
 * "wenbin.lu@sao.so","steve.liu@sao.so", "pitar.leung@sao.so"},
 * "storm job error", stats_act+"\n"+stats_app);
 * System.out.println("end checking storm job"); } }
 * 
 * private StormStats checkJob(CuratorFramework client, String path){ StormStats
 * stormstats = new StormStats(); try { Stat stat =
 * client.checkExists().forPath(path+"/currtx");
 * stormstats.setLastOkTransactionTime(stat.getMtime());
 * 
 * String s = new
 * String(client.getData().forPath(path+"/currattempts"),"UTF-8"); int i =
 * s.indexOf(":"); int j = s.indexOf("}"); int attempts
 * =Integer.parseInt(s.substring(i+1, j));
 * stormstats.setReAttemptCounts(attempts); } catch (Exception e) { // TODO
 * Auto-generated catch block e.printStackTrace(); }
 * 
 * return stormstats; } }
 */