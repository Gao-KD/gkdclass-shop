package org.example.service;

public interface MailService {
    //发送邮箱验证码
    void sendMail(String to,String subject,String content);
}
