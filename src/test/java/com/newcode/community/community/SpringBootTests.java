package com.newcode.community.community;

import com.newcode.community.community.entity.DiscussPost;
import com.newcode.community.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    private DiscussPost post;

    @Autowired
    DiscussPostService discussPostService;

    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    @Before
    public void before(){
        System.out.println("before");
        //生成测试数据，那么所有测试方法都会得到一份独立的数据
        post = new DiscussPost();
        post.setUserId(111);
        post.setTitle("Test Title");
        post.setContent("Test Content");
        post.setCreateTime(new Date());
        discussPostService.insertDiscussPost(post);
    }

    @After
    public void after(){
        System.out.println("after");
        //删除测试数据
        discussPostService.updateStatus(post.getId(),2);
    }

    @Test
    public void testFindById(){
        DiscussPost discussPost = discussPostService.findDiscussPostById(post.getId());
        Assert.assertNotNull(discussPost);
        Assert.assertEquals(discussPost.getTitle(),post.getTitle());
    }

}
