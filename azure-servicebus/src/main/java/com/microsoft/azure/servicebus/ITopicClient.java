package com.microsoft.azure.servicebus;

// Should we allow browse/peek on topic?
public interface ITopicClient extends IMessageSender, IMessageBrowser, IMessageEntity {

}
