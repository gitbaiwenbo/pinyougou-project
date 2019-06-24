package com.itcast.core.controller.user;

import com.itcast.core.entity.Result;
import com.itcast.core.pojo.user.User;
import com.itcast.core.service.user.UserService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.itcast.core.utils.checkphone.PhoneFormatCheckUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName UserController
 * @Description 用户个人中心系统
 * @Author 传智播客
 * @Date 13:04 2019/3/30
 * @Version 2.1
 **/
@RestController
@RequestMapping("/user")
public class UserController {

    @Reference
    private UserService userService;

    /**
     * @author 栗子
     * @Description 短信发送
     * @Date 13:06 2019/3/30
     * @param phone
     * @return cn.itcast.core.entity.Result
     **/
    @RequestMapping("/sendCode.do")
    public Result sendCode(String phone){
        try {
            //校验手机号是否合法
            boolean chinaPhoneLegal = PhoneFormatCheckUtils.isChinaPhoneLegal(phone);
            if (!chinaPhoneLegal){
                return new Result(false,"手机号不合法");
            }
            userService.sendCode(phone);
            return new Result(true, "短信发送成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "短信发送失败");
        }
    }
    @RequestMapping("/add.do")
    public Result add(String smscode,@RequestBody User user){
        try {
            userService.add(smscode,user);
            return new Result(false,"注册成功");
        } catch (RuntimeException e){
            return new Result(false,e.getMessage());
        } catch(Exception e) {
            e.printStackTrace();
            return new Result(false,"注册失败");
        }
    }
}
