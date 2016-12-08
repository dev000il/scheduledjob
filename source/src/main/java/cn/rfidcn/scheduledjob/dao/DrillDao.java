package cn.rfidcn.scheduledjob.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.elasticsearch.common.base.Joiner;

public class DrillDao {

	  private static String DrillJDBCClassName = "org.apache.drill.jdbc.Driver";
	  private static String DrillConnectionString = "jdbc:drill:zk=prod2:2181,prod1:2181,prod3:2181/drill/drillprod;schema=hbase";
	  private static String comma=",";
	    
	    
	  private static String q = "SELECT CONVERT_FROM(T.log.ts,'UTF8') AS ts, CONVERT_FROM(T.log.hid,'UTF8') AS hid, "
	  		+ " CONVERT_FROM(T.log.bid,'UTF8') AS bid,  CONVERT_FROM(T.log.num,'UTF8') AS num, "
		    + " CAST(T.log.`pri` AS INT) AS pri, CONVERT_FROM(T.log.oip,'UTF8') AS oip, CONVERT_FROM(T.log.pn,'UTF8') AS pn, "
	  		+ " CONVERT_FROM(T.log.rwds,'UTF8') AS rwds,  CONVERT_FROM(T.log.pts,'UTF8') AS pts "
	        + " FROM hbase.activities AS T "
	    	+ " WHERE T.row_key > '%s' AND T.row_key < '%s' AND CONVERT_FROM(T.log.d,'UTF8') <> 'ScanHandler' AND CAST(T.log.`at` AS INT) = 2 AND CAST(T.log.`pri` AS INT) = 137" ;
	   
	  
	  private static String hdRwdsQ = "SELECT CONVERT_FROM(T.log.ts,'UTF8') AS ts,  CONVERT_FROM(T.log.rn,'UTF8') AS rn, "
	  		+ " CONVERT_FROM(T.log.rid,'UTF8') AS rid, CONVERT_FROM(T.log.srn,'UTF8') AS srn, CONVERT_FROM(T.log.spn,'UTF8') AS spn, "
	  		+ " CONVERT_FROM(T.log.sp,'UTF8') AS sp, CONVERT_FROM(T.log.sc,'UTF8') AS sc, CONVERT_FROM(T.log.sa,'UTF8') AS sa,  "
	  		+ " CONVERT_FROM(T.log.c,'UTF8') as c,  CONVERT_FROM(T.log.pri,'UTF8') as pri "
	  		+ " FROM hbase.activities AS T "
	  		+ " WHERE T.row_key >= '%s' AND T.row_key < '%s' AND CONVERT_FROM(T.log.d,'UTF8') <> 'ScanHandler' AND CAST(T.log.`at` AS INT) = 3 ";
	  
	  
	  private Connection connection = null;
	  
	  private static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    
	  private final static DrillDao singleton = new DrillDao(); 
	  
	  private DrillDao(){
	  }
	  
	  public static DrillDao getInstance(){
			return singleton;
      }
	    
	  static {
	      try {
	        Class.forName(DrillJDBCClassName);  
	      } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	      }
	    }
	   
	  public synchronized String getHDReward(){
		  Calendar calendar = Calendar.getInstance();
	      Date endtime = calendar.getTime();
	      endtime.setHours(11);
	      endtime.setMinutes(0);
	      endtime.setSeconds(0);
	      calendar.add(Calendar.DAY_OF_MONTH, -1);
	      Date startime = calendar.getTime();
	      startime.setHours(23);
	      startime.setMinutes(0);
	      startime.setSeconds(0);
	      String query = String.format(hdRwdsQ, padRowKey(startime.getTime()),padRowKey(endtime.getTime()));
	      StringBuilder sb = new StringBuilder();
	      try{
	    	  connection = DriverManager.getConnection(DrillConnectionString);
			  Statement stmt = connection.createStatement();
			  ResultSet result = stmt.executeQuery(query);
			  while(result.next()){
	        		String ts = result.getString("ts");
	        		try{
	        			Date t = new Date(Long.parseLong(ts));
	        			ts = sdf.format(t);
	        		}catch(Exception e){
	        			continue;
	        		}
	        		String c = result.getString("c");
	        		String pri = result.getString("pri");
	        		System.out.println(c+" "+pri);
	        		if("2057".equals(c) && ("179".equals(pri) || "180".equals(pri))){
	        			String rn = result.getString("rn")== null ? "" :result.getString("rn") ;
	        			String rid = result.getString("rid") ==null? "": result.getString("rid");
	        			String srn = result.getString("srn") == null? "": result.getString("srn");
	        			String spn = result.getString("spn") == null? "" : result.getString("spn");
	        			String sp = result.getString("sp")==null?"" :result.getString("sp");
	        			String sc = result.getString("sc")==null?"" :result.getString("sc");
	        			String sa = result.getString("sa")==null? "" : result.getString("sa");
	        			sb.append(Joiner.on(comma).join(new String[]{ts,rn,rid,srn,spn,sp,sc,sa})).append("\n");
	        		}
	        	}
	        	connection.close(); 
	      } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		  }finally{
			  try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		return sb.toString();
	  }
	  
	 public synchronized String getPri137(){
		 Calendar calendar = Calendar.getInstance();
		 Date endtime = calendar.getTime();
		 calendar.add(Calendar.MINUTE, -60);
		 Date startime = calendar.getTime();
		 String query = String.format(q, padRowKey(startime.getTime()),padRowKey(endtime.getTime()));
		 
		 StringBuilder sb = new StringBuilder();
		 try {
			connection = DriverManager.getConnection(DrillConnectionString);
			Statement stmt = connection.createStatement();
			 
			ResultSet result = stmt.executeQuery(query);
			int count =0;
        	while(result.next()){
        		String ts = result.getString("ts");
        		try{
        			Date t = new Date(Long.parseLong(ts));
        			ts = sdf.format(t);
        		}catch(Exception e){
        			continue;
        		}
        		
        		String hid = result.getString("hid");
        		String bid = result.getString("bid");
        		String num = result.getString("num");
        		//String pid = result.getString("pid");
        		int pri = result.getInt("pri");
        		String oip = result.getString("oip");
        		String pn = result.getString("pn");
        		String rwds = result.getString("rwds");
        		String pts = result.getString("pts");
        		sb.append(Joiner.on(comma).join(new String[]{ts,hid,bid,num,String.valueOf(pri),oip,pn,rwds,pts})).append("\n");
        		count++;
        	}
        	 System.out.println("pri137 count: "+count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		 return sb.toString();
	 }
	 
	 private  String padRowKey(Number n) {
	      String s = null;
	      int length = 8;
	      if (n instanceof Long) {
	          s = Long.toHexString(n.longValue());
	          length = 16;
	       } else if (n instanceof Integer) {
	          s = Integer.toHexString(n.intValue());
	       }
	       StringBuilder sb = new StringBuilder();
	       for (int i = s.length(); i < length; i++) {
	         sb.append(0);
	       }
	        return sb.append(s).toString();
	}
	 
}
