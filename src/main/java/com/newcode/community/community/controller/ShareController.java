package com.newcode.community.community.controller;

import com.newcode.community.community.entity.Event;
import com.newcode.community.community.event.EventProducer;
import com.newcode.community.community.util.CommunityConstant;
import com.newcode.community.community.util.CommunityUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

@Controller
public class ShareController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Value("${community.path.domain}")
    private String domin;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;

    @Autowired
    private EventProducer eventProducer;

    @RequestMapping(value = "/share",method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl){
        //文件名
        String fileName = CommunityUtil.generateUUID();
        System.out.println(htmlUrl);

        Event event = new Event()
                    .setTopic(TOPIC_SHARE)
                    .setData("htmlUrl",htmlUrl)
                    .setData("fileName",fileName)
                    .setData("suffix",".png");
        eventProducer.fireEvent(event);

        HashMap<String,Object> hashMap = new HashMap<>();
//        hashMap.put("shareUrl",domin + contextPath + "/share/image/" + fileName);
        hashMap.put("shareUrl",shareBucketUrl + "/" + fileName);
        return CommunityUtil.getJSONString(0,null,hashMap);
    }

    //获取长图
    //废弃
    @RequestMapping(value = "/share/image/{fileName}",method = RequestMethod.GET)
    @ResponseBody
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response){
        if(StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("文件名不能为空！");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage +"/" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != 0){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败：" + e.getMessage());
        }
    }

}
