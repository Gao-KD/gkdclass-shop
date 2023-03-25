package org.example.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    /***
     * SpringBoot 提供的一个简单邮箱发送对象，直接注入
     */
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    @Override
    public void sendMail(String to, String subject, String content) {
        //创建一个邮箱消息对象
        SimpleMailMessage message = new SimpleMailMessage();

        //配置邮件发送人
        message.setFrom(from);

        //配置邮件收件人
        message.setTo(to);

        //配置邮件主题
        message.setSubject(subject);

        //配置邮件内容
        message.setText(content);

        mailSender.send(message);
        log.info("邮件发送成功:"+message.toString());

    }
}
