package cn.rfidcn.scheduledjob.util;

import cn.rfidcn.scheduledjob.mail.MailSenderInfo;
import cn.rfidcn.scheduledjob.mail.SimpleMailSender;

import java.io.File;

public class MailSender {
    MailSenderInfo mailInfo;
    SimpleMailSender sms;

    private static MailSender singleton = new MailSender();

    public static MailSender getInstance() {
        return singleton;
    }

    private MailSender() {
        // mailInfo = new MailSenderInfo();
        // mailInfo.setMailServerHost("smtp.rfidce.cn");
        // mailInfo.setMailServerPort("25");
        // mailInfo.setValidate(true);
        // mailInfo.setUserName("support@rfidce.cn");
        // mailInfo.setPassword("pC@m*2rJcSrS)kksMkQb");
        // mailInfo.setFromAddress("support@rfidce.cn");

        mailInfo = new MailSenderInfo();
        mailInfo.setMailServerHost("smtp.exmail.qq.com");
        mailInfo.setMailServerPort("587");
        mailInfo.setValidate(true);
        mailInfo.setUserName("tyninja@sao.so");
        mailInfo.setPassword("3J84xj~;07X8F24^h%+*b03Go");
        mailInfo.setFromAddress("tyninja@sao.so");

        // mailInfo = new MailSenderInfo();
        // mailInfo.setMailServerHost("smtp.sina.com");
        // mailInfo.setMailServerPort("25");
        // mailInfo.setValidate(true);
        // mailInfo.setUserName("tyninja@sina.com ");
        // mailInfo.setPassword("qkpy4DRw");
        // mailInfo.setFromAddress("tyninja@sina.com");

        sms = new SimpleMailSender();
    }

    public synchronized void send(String[] recipients, String subject, String msg, File[] attachedFiles, String[] attachFileNames) {
        mailInfo.setToAddresses(recipients);
        mailInfo.setSubject(subject);
        mailInfo.setContent(msg);
        mailInfo.setAttachFileNames(attachFileNames);
        mailInfo.setAttachedFiles(attachedFiles);
        sms.sendTextMail(mailInfo);
    }

    public synchronized void send(String[] recipients, String subject, String msg, String[] attachFileNames) {
        send(recipients, subject, msg, null, attachFileNames);
    }

    public synchronized void send(String[] recipients, String subject, String msg, File[] attachedFiles) {
        send(recipients, subject, msg, attachedFiles, null);
    }

    public synchronized void send(String[] recipients, String subject, String msg) {
        send(recipients, subject, msg, null, null);
    }
}
