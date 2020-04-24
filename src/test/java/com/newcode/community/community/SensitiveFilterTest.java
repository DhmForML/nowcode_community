package com.newcode.community.community;

import com.newcode.community.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveFilterTest {
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text = "我们这里可以嫖娼，qq是123456，还可以赌博，并提供开票服务，" +
                "还提供吸毒场所，还有人兼职卖淫，方便客户选择!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);

        text = "我们这里可以☆嫖☆☆娼☆，qq是123456，还可以☆☆赌☆博☆☆，并提供开票服务，" +
                "还提供☆☆吸☆☆☆毒场所，还有人兼职☆☆☆卖淫☆☆☆，方便客户选择!";
        text = sensitiveFilter.filter(text);
        System.out.println(text);
    }
}
