package cn.rfidcn.scheduledjob.mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

public class SimpleMailSender  {  
	
public boolean sendTextMail(MailSenderInfo mailInfo) {    
    // 判断是否需要身份认证 
    MyAuthenticator authenticator = null;    
    Properties pro = mailInfo.getProperties();   
    if (mailInfo.isValidate()) {    
    // 如果需要身份认证，则创建一个密码验证器    
      authenticator = new MyAuthenticator(mailInfo.getUserName(), mailInfo.getPassword());    
    }   
    // 根据邮件会话属性和密码验证器构造一个发送邮件的session    
    Session sendMailSession = Session.getDefaultInstance(pro,authenticator);    
    try {    
    // 根据session创建一个邮件消息    
    Message mailMessage = new MimeMessage(sendMailSession);    
    // 创建邮件发送者地址    
    Address from = new InternetAddress(mailInfo.getFromAddress());    
    // 设置邮件消息的发送者    
    mailMessage.setFrom(from);    
    // 创建邮件的接收者地址，并设置到邮件消息中    
//    Address to = new InternetAddress(mailInfo.getToAddress());    
//    mailMessage.setRecipient(Message.RecipientType.TO,to);
    String toStr[] = mailInfo.getToAddresses();
    Address to[] = new Address[toStr.length];
    for(int i=0;i<to.length;i++){
    	to[i] = new InternetAddress(toStr[i]);    
    }
    mailMessage.setRecipients(Message.RecipientType.TO,to);
    
    // 设置邮件消息的主题    
    mailMessage.setSubject(mailInfo.getSubject());    
    // 设置邮件消息发送的时间    
    mailMessage.setSentDate(new Date());    
    // 设置邮件消息的主要内容    
//    String mailContent = mailInfo.getContent();    
//    mailMessage.setText(mailContent);   
    
    Multipart multipart = new MimeMultipart();  
    MimeBodyPart contentPart = new MimeBodyPart();  
    contentPart.setText(mailInfo.getContent());  
    multipart.addBodyPart(contentPart);
    
    String[] attachFileNames = mailInfo.getAttachFileNames();
    if(attachFileNames!=null && attachFileNames.length>0){  
        for(String fn : attachFileNames){  
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(new File(fn));  
            attachmentPart.setDataHandler(new DataHandler(source));  
            attachmentPart.setFileName(MimeUtility.encodeWord(fn, "utf8", null));  
            multipart.addBodyPart(attachmentPart);  
        }   
    }

    if (mailInfo.getAttachedFiles() != null && mailInfo.getAttachedFiles().length > 0) {
        for (File file : mailInfo.getAttachedFiles()) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            FileDataSource source = new FileDataSource(file);
            attachmentPart.setDataHandler(new DataHandler(source));
            attachmentPart.setFileName(file.getName());
            multipart.addBodyPart(attachmentPart);
        }
    }

    mailMessage.setContent(multipart);  
    
    
    // 发送邮件   
    Transport.send(mailMessage);   
    return true;    
    } catch (MessagingException | UnsupportedEncodingException ex) {    
        ex.printStackTrace();    
    }    
    return false;    
  }    
}
