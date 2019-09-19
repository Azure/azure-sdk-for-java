package com.microsoft.azure.servicebus.primitives;

import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.IMessageReceiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageLockAutoRenewTask implements Runnable {

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(MessageLockAutoRenewTask.class);

    private final IMessageReceiver receiver;
    private final IMessage message;

    public MessageLockAutoRenewTask(IMessageReceiver receiver, IMessage message) {
        this.receiver = receiver;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            this.receiver.renewMessageLock(message.getLockToken());
        } catch (InterruptedException e) {
            // probably not worth doing anything here
        } catch (ServiceBusException e) {
            if (TRACE_LOGGER.isErrorEnabled()) {
                TRACE_LOGGER.error(String.format("Exception while attempting to renew lock for message : %s",message.getMessageId()), e);
            }
        }
    }
}