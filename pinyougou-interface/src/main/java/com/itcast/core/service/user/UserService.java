package com.itcast.core.service.user;

import com.itcast.core.pojo.user.User;

public interface UserService {
    /**
     * 发送短信验证码
     * @param phone
     */
    public void sendCode(String phone);

    /**
     * 用户注册
     * @param smscode
     * @param user
     */
    public void add(String smscode, User user);
}
