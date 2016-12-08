package cn.rfidcn.scheduledjob;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cn.rfidcn.scheduledjob.job.AppeventAllErrorsSummaryJob;
import cn.rfidcn.scheduledjob.job.AppeventMissingBolbErrorsJob;

public class JobDispatcher {

    public static void main(String args[]) {
//        int now = new Date().getHours();
//        int next = 0;
//        if (now < 9) {
//            next = 9 - now;
//        } else {
//            next = 24 - now + 9;
//        }
//        System.out.println("run after hours: " + next);

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new AppeventAllErrorsSummaryJob(), 0, 60, TimeUnit.MINUTES);

        Calendar calendar = Calendar.getInstance();
        Date current = calendar.getTime();

        // Tomorrow around 1AM
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date target = calendar.getTime();

        long diff = target.getTime() - current.getTime();
        long diffHours = diff / (60 * 60 * 1000) % 24;
        System.out.println("Run after hours: " + diffHours);

        service.scheduleAtFixedRate(new AppeventMissingBolbErrorsJob(), diffHours, 24, TimeUnit.HOURS);

        // service.scheduleAtFixedRate(new Archive9503Job(), next , 24,
        // TimeUnit.HOURS);
        // service.scheduleAtFixedRate(new AppeventSpecialErrorsJob(), 0, 5,
        // TimeUnit.MINUTES);
        // service.scheduleAtFixedRate(new CheckStormJob(), 0, 30,
        // TimeUnit.MINUTES);
        // service.scheduleAtFixedRate(new AppeventOOMJob(), 0, 5,
        // TimeUnit.MINUTES);
        // service.scheduleAtFixedRate(new Pri137Job(), 0, 60,
        // TimeUnit.MINUTES);
    }
}
