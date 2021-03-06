package com.newcode.community.community.service;

import com.newcode.community.community.dao.LoginTicketMapper;
import com.newcode.community.community.dao.UserMapper;
import com.newcode.community.community.entity.LoginTicket;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.CommunityUtil;
import com.newcode.community.community.util.MailClient;
import com.newcode.community.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

/*    @Autowired
    LoginTicketMapper loginTicketMapper;*/

    @Autowired
    RedisTemplate redisTemplate;

    public User findUserById(int id){
        User user = getCache(id);
        if(user == null){
            user = initCache(id);
        }
        return user;
    }

    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","参数不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","该账户已存在！");
            return map;
        }

        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setType(0);            //普通用户
        user.setStatus(0);          //还没激活
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setCreateTime(new Date());
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        userMapper.insertUser(user);

        //激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活链接形式:http://localhost:8080/community/activation/101/code
        String url = domain + contextPath +"/activation/" + user.getId() +"/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    public int activation(int userId,String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }

        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活！");
        }

        if(!CommunityUtil.md5(password + user.getSalt()).equalsIgnoreCase(user.getPassword())){
            map.put("passwordMsg","密码不正确！");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * expiredSeconds));
//        loginTicketMapper.insertLoginTicket(loginTicket);
        //将登录凭证写入redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String rediKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(rediKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(rediKey,loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId,String headerUrl){
//        return userMapper.updateHeader(userId,headerUrl);
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }

    public int updatePassword(int userId,String password){
//        return userMapper.updatePassword(userId,password);
        int rows = userMapper.updatePassword(userId,password);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取值
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到就初始化缓存数据
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600,TimeUnit.SECONDS);
        return user;
    }
    //3.数据变更时要更新缓存数据
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    //获取spring security的权限
    public List<? extends GrantedAuthority> getAuthorities(int userId){
        User user = userMapper.selectById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
