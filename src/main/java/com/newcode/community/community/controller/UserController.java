package com.newcode.community.community.controller;

import com.newcode.community.community.annotation.LoginRequired;
import com.newcode.community.community.entity.User;
import com.newcode.community.community.service.FollowService;
import com.newcode.community.community.service.LikeService;
import com.newcode.community.community.service.UserService;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.CommunityUtil;
import com.newcode.community.community.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Retention;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;


    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    @Autowired
    FollowService followService;

    @LoginRequired
    @RequestMapping(value = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        //上传文件名称
        String fileName = CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth =  Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);
        return "/site/setting";
    }

    //更新头像路径
    @RequestMapping(value = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
            return CommunityUtil.getJSONString(1,"文件名不能为空！");
        }

        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUsers().getId(),url);
        return CommunityUtil.getJSONString(0);
    }


    //废弃
    @LoginRequired
    @RequestMapping(value = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存储路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生以上");
        }

        //更新当前用户头像的路径(web访问路径)
        User user = hostHolder.getUsers();
        //http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    //废弃
    @RequestMapping(value = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //文件在服务器上存储的路径
        fileName = uploadPath + "/" + fileName;
        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应的图片格式
        response.setContentType("image/" + suffix);

        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName);
             ) {
               byte[] buffer = new byte[1024];
               int b = 0;
               while ((b = fis.read(buffer)) != -1){
                    os.write(buffer,0,b);        //buffer:要写入的数据，off:从buffer哪个元素开始，b:要写入的长度
               }
        } catch (IOException e) {
            logger.error("获取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(value = "/updatePassword",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,Model model){
       if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","密码不能为空！");
            return "/site/setting";
       }
       if(StringUtils.isBlank(newPassword)){
           model.addAttribute("newPasswordMsg","密码不能为空！");
           return "/site/setting";
       }
       User user = hostHolder.getUsers();
       oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
       if(!oldPassword.equals(user.getPassword())){
           model.addAttribute("oldPasswordMsg","密码不正确！");
           return "/site/setting";
       }

       newPassword = CommunityUtil.md5(newPassword + user.getSalt());
       userService.updatePassword(user.getId(),newPassword);
       return "redirect:/index";
    }

    @RequestMapping(value = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new IllegalArgumentException("该用户不存在!");
        }
        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER,userId);
        //当前用户是否关注
        boolean hasFollowed = false;
        if(hostHolder.getUsers() != null){
            hasFollowed = followService.hasFollowed(hostHolder.getUsers().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("followeeCount",followeeCount);
        model.addAttribute("followerCount",followerCount);
        model.addAttribute("hasFollowed",hasFollowed);
        System.out.println(hasFollowed);
        return "/site/profile";
    }

}
