package com.cfl.controller;

import com.cfl.utils.DecryptUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@CrossOrigin
public class DemoController {

    @RequestMapping("/testApi/q1")
    public String demoActivityRedirect(@RequestBody String content){
        String aes = "b15e3a042de9406e8b097e9837bf01c5";
        return "推送的加密内容[content]:"+receive(aes,content);
    }

    public String receive(String aes, String content){
        try
        {
            if (StringUtils.isAnyEmpty(aes,content))
            {
                byte[] contentBytes = DecryptUtil.decryptECB(content.getBytes(), aes.getBytes());
                System.out.println("获取JSON成功");
                return new String(contentBytes);
            }
            return "读取内容为空";
        }
        catch (Exception e)
        {
            return "出错啦！\r\n" + e.getMessage();
        }
    }

    @GetMapping("/testApiTwo/q1")
    public String demo02(HttpServletRequest request) throws Exception {
        String jsonString = IOUtils.toString(request.getInputStream());
        JSONObject json = new JSONObject(jsonString);
        if(json != null) {
            System.out.println("json==============================" + json);
            System.out.println("jsonString=======================================" + jsonString);
            return "成功拿到json数据";
        }
        return "拿到json数据失败";
    }


}
