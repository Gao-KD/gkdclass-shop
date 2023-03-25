package org.example.biz;

import lombok.extern.slf4j.Slf4j;
import org.example.UserApplication;
import org.example.service.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class MailTest {


    @Autowired
    private MailService mailService;
    @Test
    public void mailTest(){
        mailService.sendMail("435452414@qq.com","杭州电子科技大学——郭灵拯 面试通知（3月26日14点30分）","您好：\n" +
                "   \n" +
                "      欢迎您应聘本公司 JAVA开发 岗位，您的学识、经历给我们留下了良好的印象。为了彼此进一步的了解，请您3月26日14点30分前来本公司参加正式面试。\n" +
                "      公司地址：杭州电子科技大学信息工程学院 \n" +
                "        交通路线：\n" +
                "        1、地铁16号线到青山湖科技城地铁口，B出口公交站，坐专线车（8点25分、8点35分、8点45分、8点55分、9点05分到点发车）或者864公交车，即可直接到杭州电子科技大学信息工程学院楼下\n" +
                "\n" +
                "        面试当天六号楼715寝室。\n" +
                "\n" +
                "      友情提醒:收到面试通知，请您准时参加，如有任何变化请提前通知，方便后续安排，谢谢!\n");
    }
}
