package com.itcast.core.login;

import jdk.nashorn.internal.ir.annotations.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name.do")
    public Map<String,String> showName(){
        Map<String,String> map=new HashMap<>();
        String loginName= SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName",loginName);
        return map;
    }
}
