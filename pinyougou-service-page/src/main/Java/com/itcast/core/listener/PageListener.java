package com.itcast.core.listener;

import com.itcast.core.service.staticPage.StaticPageService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.Message;
import javax.jms.MessageListener;

public class PageListener implements MessageListener {


    @Resource
    private StaticPageService staticPageService;

    /**
     * @author 栗子
     * @Description 获取消息-消费消息
     * @Date 12:47 2019/3/28
     * @param message
     * @return void
     **/
    @Override
    public void onMessage(Message message) {



        try {
            // 获取消息
            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            System.out.println("消费者service-page获取id：" + id);
            // 消费消息
            staticPageService.getHtml(Long.parseLong(id));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
