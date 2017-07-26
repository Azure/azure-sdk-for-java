package com.microsoft.azure.servicebus;

// Should we allow browse/peek on topic?
public interface ITopicClient extends IMessageSender, IMessageBrowser, IMessageEntityClient {
    public String getTopicName();
}
