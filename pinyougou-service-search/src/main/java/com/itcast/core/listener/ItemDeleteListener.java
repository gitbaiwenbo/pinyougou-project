package com.itcast.core.listener;

import com.itcast.core.service.search.ItemSearchService;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class ItemDeleteListener implements MessageListener{
    @Resource
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        try {
            ActiveMQTextMessage activeMQTextMessage= (ActiveMQTextMessage) message;
            String id = activeMQTextMessage.getText();
            itemSearchService.deleteItemFromSolr(Long.parseLong(id));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
