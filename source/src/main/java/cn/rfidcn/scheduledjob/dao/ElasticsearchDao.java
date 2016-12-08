package cn.rfidcn.scheduledjob.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import com.alibaba.fastjson.JSON;

import cn.rfidcn.scheduledjob.ip.ReverseIpClient;
import cn.rfidcn.scheduledjob.util.ApplicationConfig;

public class ElasticsearchDao {

    Client client;

    private final static ElasticsearchDao singleton = new ElasticsearchDao();

    private ElasticsearchDao() {
        client = new TransportClient(ImmutableSettings.builder().put("cluster.name", "es").build())
                .addTransportAddress(new InetSocketTransportAddress(ApplicationConfig.ElasticSearchServer, 9300));
    }

    public static ElasticsearchDao getInstance() {
        return singleton;
    }

    public synchronized Map<String, Integer> getDistinctErrorsAndCounts()
            throws InterruptedException, ExecutionException, TimeoutException, SQLException {
        Calendar calendar = Calendar.getInstance();
        Date endtime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -60);
        Date startime = calendar.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00.000");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startime)).to(format.format(endtime)))
                .must(QueryBuilders.matchQuery("l", "ERROR"));
        SearchResponse response = client.prepareSearch("appeventsindex").setSize(50000).setQuery(queryBuilder).execute().get(50,
                TimeUnit.MINUTES);

        SearchHit[] hits = response.getHits().getHits();
        System.out.println(hits.length);
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<Integer> ignoreList = getAeList();
        System.out.println(ignoreList.size());
        try {
            for (int i = 0; i < hits.length; i++) {
                String d = hits[i].getSource().get("d") == null ? "" : hits[i].getSource().get("d").toString();
                String ae = hits[i].getSource().get("ae") == null ? "" : hits[i].getSource().get("ae").toString();
                String h = hits[i].getSource().get("h") == null ? "" : hits[i].getSource().get("h").toString();

                boolean skip = false;
                if (!ae.equals("")) {
                    int aeInt = Integer.parseInt(ae);
                    if (ignoreList != null && ignoreList.contains(aeInt)) {
                        skip = true;
                    }
                } else {
                    skip = true;
                }
                if (skip) {
                    continue;
                }

                String msg = hits[i].getSource().get("msg").toString();
                if (msg == null || msg.trim().length() == 0) {
                    continue;
                }
                String key = new StringBuilder().append(ae).append(" , ").append(d).append(" , ").append(h).append(" , ")
                        .append(msg).toString();
                int count = map.containsKey(key) ? map.get(key) : 0;
                map.put(key, ++count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;

    }

    public synchronized String archive9503() throws InterruptedException, ExecutionException {
        Calendar calendar = Calendar.getInstance();
        Date endtime = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        Date startime = calendar.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00.000");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startime)).to(format.format(endtime)))
                .must(QueryBuilders.matchQuery("l", "ERROR")).must(QueryBuilders.matchQuery("ae", 9503));

        SearchResponse response = client.prepareSearch("appeventsindex").setSize(50000).setQuery(queryBuilder).execute().get();
        SearchHit[] hits = response.getHits().getHits();
        BulkRequestBuilder bb = client.prepareBulk();

        for (int i = 0; i < hits.length; i++) {
            bb.add(client.prepareIndex("error_9503", "error_9503").setSource(hits[i].getSourceAsString()));
        }

        BulkResponse resp = bb.execute().actionGet();
        if (resp.hasFailures()) {
            System.out.println(resp.buildFailureMessage());
            return null;
        } else {
            System.out.println("done archiving error 9503");
        }
        return "9503 successfully archived! " + hits.length;
    }

    public synchronized String[] getMissingBlobErrors() throws InterruptedException, ExecutionException, SQLException {
        Calendar calendar = Calendar.getInstance();
        Date endtime = calendar.getTime();
        calendar.add(Calendar.DATE, -1);
        Date startime = calendar.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd 00:00:00.000");
        DateFormat formatWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startime)).to(format.format(endtime)))
                .must(QueryBuilders.matchQuery("l", "ERROR")).must(QueryBuilders.matchQuery("ae", 7809));

        SearchResponse response = client.prepareSearch("appeventsindex").addSort("ts", SortOrder.DESC).setSize(50000)
                .setQuery(queryBuilder).execute().get();

        SearchHit[] hits = response.getHits().getHits();
        System.out.println(hits.length);
        if (hits.length == 0) {
            return null;
        }

        StringBuilder allBuilder = new StringBuilder();
        StringBuilder yibaoBuilder = new StringBuilder();

        allBuilder.append("timestamp,sequenceNumber,honestId,batchId,companyId,ipAddress\n");
        yibaoBuilder.append("timestamp,sequenceNumber,honestId,batchId,companyId,ipAddress\n");

        Pattern patternWithCompany = Pattern
                .compile("seqNum:\\[(.*)\\], honestId:\\[(.*)\\], tagBatchId:\\[(.*)\\], companyId:\\[(.*)\\]");
        Pattern p = Pattern.compile("seqNum:\\[(.*)\\], honestId:\\[(.*)\\], tagBatchId:\\[(.*)\\]");

        BulkRequestBuilder bb = client.prepareBulk();

        long startTime = System.currentTimeMillis();

        Map<String, String> map = getAllStateAndCitys();

        String stateAndCityPY = null;
        String state = null;
        String city = null;
        for (int i = 0; i < hits.length; i++) {
            String seqNo = null;
            String hid = null;
            String bid = null;
            String c = null;

            String msg = hits[i].getSource().get("msg").toString();
            Matcher matchWithCompany = patternWithCompany.matcher(msg);
            Matcher m = p.matcher(msg);
            if (matchWithCompany.find()) {
                seqNo = matchWithCompany.group(1);
                hid = matchWithCompany.group(2);
                bid = matchWithCompany.group(3);
                c = matchWithCompany.group(4);
            } else if (m.find()) {
                seqNo = m.group(1);
                hid = m.group(2);
                bid = m.group(3);
            }

            Object timestampAsNum = hits[i].getSource().get("ts");
            String timestamp = null;
            if (timestampAsNum instanceof Long) {
                timestamp = formatWithTime.format(new Date(((Long) timestampAsNum).longValue()));
            } else {
                // 2016-07-06 23:51:00.688
                timestamp = timestampAsNum.toString();
            }

            String oip = hits[i].getSource().get("oip").toString();
            String[] temp = ReverseIpClient.getGeoInfo(oip);
            state = temp[1];
            city = temp[2];
            stateAndCityPY = state + city;
            if (map.size() > 0) {
                stateAndCityPY = stateAndCityPY.toLowerCase();
                if (map.get(stateAndCityPY) != null) {
                    String[] str = map.get(stateAndCityPY).split(",");
                    state = str[0];
                    city = str[1];
                }
            }

            allBuilder.append(timestamp).append(",").append(seqNo).append(",").append(hid).append(",").append(bid).append(",")
                    .append(c).append(",").append(oip).append(",").append(state).append(",").append(city).append("\n");

            if (c != null && c.equals("10027")) { // || (hid != null && hid.toLowerCase().startsWith("c/"))) {
                yibaoBuilder.append(timestamp).append(",").append(seqNo).append(",").append(hid).append(",").append(bid)
                        .append(",").append(c).append(",").append(oip).append(",").append(state).append(",").append(city)
                        .append("\n");
            }

            bb.add(client.prepareIndex("error_7809", "error_7809", hits[i].getSource().get("r").toString())
                    .setSource(hits[i].getSourceAsString()));
        }

        System.out.println("total time is :" + (System.currentTimeMillis() - startTime) + " ms");
        System.out.println("do archive...");
        BulkResponse resp = bb.execute().actionGet();
        if (resp.hasFailures()) {
            System.out.println(resp.buildFailureMessage());
        } else {
            System.out.println("done archiving error 7809");
        }

        return new String[] { allBuilder.toString(), yibaoBuilder.toString() };
    }

    public synchronized String getSpecialError() throws InterruptedException, ExecutionException, TimeoutException {
        Calendar calendar = Calendar.getInstance();
        Date endtime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -5);
        Date startime = calendar.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00.000");

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startime)).to(format.format(endtime)))
                .must(QueryBuilders.matchQuery("l", "ERROR"));

        SearchResponse scrollResp = client.prepareSearch("appeventsindex").setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000)).setQuery(queryBuilder).setSize(10000).execute().actionGet();

        StringBuilder sb = new StringBuilder();
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                String d = hit.getSource().get("d") == null ? "" : hit.getSource().get("d").toString();
                String ae = hit.getSource().get("ae") == null ? "" : hit.getSource().get("ae").toString();
                String h = hit.getSource().get("h") == null ? "" : hit.getSource().get("h").toString();
                String msg = hit.getSource().get("msg").toString();

                if (msg == null || msg.trim().length() == 0 || !msg
                        .contains("java.sql.SQLException: An attempt by a client to checkout a Connection has timed out")) {
                    continue;
                }
                sb.append(ae).append(" , ").append(d).append(" , ").append(h).append(" , ").append(msg).append("\n");
                break;
            }
            if (sb.toString().length() > 100) {
                break;
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
                    .actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        System.out.println("done scroll!!");
        return sb.toString();
    }

    public synchronized String getOOMError() throws InterruptedException, ExecutionException, TimeoutException {
        Calendar calendar = Calendar.getInstance();
        Date endtime = calendar.getTime();
        calendar.add(Calendar.MINUTE, -5);
        Date startime = calendar.getTime();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:00.000");

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startime)).to(format.format(endtime)))
                .must(QueryBuilders.matchQuery("l", "ERROR"));

        SearchResponse scrollResp = client.prepareSearch("appeventsindex").setSearchType(SearchType.SCAN)
                .setScroll(new TimeValue(60000)).setQuery(queryBuilder).setSize(10000).execute().actionGet();

        StringBuilder sb = new StringBuilder();
        while (true) {
            for (SearchHit hit : scrollResp.getHits().getHits()) {
                String d = hit.getSource().get("d") == null ? "" : hit.getSource().get("d").toString();
                String ae = hit.getSource().get("ae") == null ? "" : hit.getSource().get("ae").toString();
                String h = hit.getSource().get("h") == null ? "" : hit.getSource().get("h").toString();
                String msg = hit.getSource().get("msg").toString();

                if (msg == null || msg.trim().length() == 0 || !msg.contains("java.lang.OutOfMemoryError: PermGen space")) {
                    continue;
                }
                sb.append(ae).append(" , ").append(d).append(" , ").append(h).append(" , ").append(msg).append("\n");
                break;
            }
            if (sb.toString().length() > 100) {
                break;
            }

            scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute()
                    .actionGet();
            //Break condition: No hits are returned
            if (scrollResp.getHits().getHits().length == 0) {
                break;
            }
        }
        System.out.println("done scroll!!");
        return sb.toString();
    }

    public synchronized void removeOld() throws InterruptedException, ExecutionException {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -7);
        Date startime = calendar.getTime();
        client.prepareDeleteByQuery("appeventsindex").setQuery(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").lt(startime)).must(QueryBuilders.termsQuery("l", "INFO"))).execute().get();
    }

    private List<Integer> getAeList() throws SQLException {

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        String json = "";
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            String sql = "select v from configs where k='scheduledjobIgoreAe'";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                json = rs.getString(1);
            }
            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        if (!"".equals(json)) {
            return JSON.parseArray(json, Integer.class);
        }
        return null;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager
                .getConnection("jdbc:mysql://10.20.18.61:3306/pipeline?user=datawriter&password=IEtZkgzoimkAhEYKcD49vmG3");
        return connection;
    }

    private Map<String, String> getAllStateAndCitys() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        Map<String, String> map = null;

        try {
            conn = getDBConnection();
            stmt = conn.createStatement();
            String sql = "select lower(concat(a.py_state,a.py_city)) as keystr,concat_ws(',',a.cn_state,a.cn_city) as valuestr from city_name_mapping a";
            rs = stmt.executeQuery(sql);
            rs.last();//move to last row
            map = new HashMap<String, String>(nextPowerOf2(rs.getRow()));
            rs.beforeFirst();//move to first row
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        if (map != null && map.size() > 0) {
            return map;
        }

        return new HashMap<String, String>();
    }

    private Connection getDBConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connection = DriverManager
                .getConnection("jdbc:mysql://10.20.18.61:3306/shop?user=datawriter&password=IEtZkgzoimkAhEYKcD49vmG3");
        return connection;
    }

    /**
     * 
     * @Title: 		 nextPowerOf2   
     * @Description: calculate the smallest number which bigger than param of a  
     * @param a
     * @return   
     * @throws
     */
    private int nextPowerOf2(final int a) {
        int b = 1;
        while (b < a) {
            b = b << 1;
        }
        return b;
    }
}
