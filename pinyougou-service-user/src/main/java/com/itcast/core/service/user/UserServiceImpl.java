package com.itcast.core.service.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.itcast.core.dao.user.UserDao;
import com.itcast.core.pojo.user.User;
import com.itcast.core.utils.MD5.MD5Util;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.jms.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    @Resource
    private JmsTemplate jmsTemplate;
    @Resource
    private Destination smsDestination;
    @Resource
    private RedisTemplate<String ,Object> redisTemplate;
    @Resource
    private UserDao userDao;
    @Override
    public void sendCode(final String phone) {
        final String code= RandomStringUtils.randomNumeric(6);
        System.out.print("验证码："+code);
        redisTemplate.boundValueOps(phone).set(code);
        //设置验证码的失效时间，
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                // 封装消息体
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("phoneNumbers", phone);
                mapMessage.setString("signName", "阮文");
                mapMessage.setString("templateCode", "SMS_140720901");
                mapMessage.setString("templateParam", "{\"code\":\""+code+"\"}");
                return mapMessage;
            }
        });
    }

    @Transactional
    @Override
    public void add(String smscode, User user) {
        String code= (String) redisTemplate.boundValueOps(user.getPhone()).get();
        System.out.print("验证码："+code);
        if (code!=null&&!"".equals(smscode)&&code.equals(smscode)){
            //验证码正确
            //保存用户
            String password= MD5Util.MD5Encode(user.getPassword(),null);
            user.setPassword(password);
            user.setCreated(new Date());
            user.setUpdated(new Date());
            userDao.insertSelective(user);
        }else {
            throw new RuntimeException("验证码不正确！");
        }
    }
}
