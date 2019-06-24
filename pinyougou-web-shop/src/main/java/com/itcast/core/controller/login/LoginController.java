package com.itcast.core.controller.login;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/showName.do")
    public Map<String,String> showName(){
        Map<String,String> map=new HashMap<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username",username);
        return map;
    }

}
