package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.ProxyOptions;
import reactor.core.scheduler.Scheduler;

public final class TopicClientBuilder {
    public TopicClientBuilder(){

    }

    public TopicClientBuilder connectionString(String connectionString) {
        return this;
    }
    public TopicClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        return this;
    }

    public TopicClientBuilder connectionString(String connectionString, String topicName) {
        return connectionString(connectionString);
    }

    public TopicClientBuilder topicName(String topicName) {
        return this;
    }
    public TopicClientBuilder retryPolicy(AmqpRetryPolicy retryPolicy) {
        return this;
    }
    public TopicClientBuilder transportType(TransportType transportType) {
        return this;
    }
    /** package- private method*/
    TopicClientBuilder scheduler(Scheduler scheduler) {
        return this;

    }


    TopicAsyncClient buildAsyncTopicClient() {
        return null;
    }
}
