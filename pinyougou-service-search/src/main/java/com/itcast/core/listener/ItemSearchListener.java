package com.itcast.core.listener;

import com.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

public class ItemSearchListener implements MessageListener {
    @Resource
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        //获取消息体
        ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) message;
        try {
            String id = activeMQTextMessage.getText();
            System.out.println("消费者service-search获取到的id："+id);
            itemSearchService.addItemToSolr(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
