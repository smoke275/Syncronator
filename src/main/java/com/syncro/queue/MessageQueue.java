package com.syncro.queue;

import java.util.concurrent.SynchronousQueue;

public class MessageQueue extends SynchronousQueue<Message>{
    private static MessageQueue messageQueueUI = new MessageQueue();
    private static MessageQueue messageQueueWS = new MessageQueue();
    private static MessageQueue messageQueueSV = new MessageQueue();

    public MessageQueue getUIQueue(){
        return messageQueueUI;
    }
    public MessageQueue getWSQueue(){
        return messageQueueWS;
    }
    public MessageQueue getSVQueue(){
        return messageQueueSV;
    }
}
