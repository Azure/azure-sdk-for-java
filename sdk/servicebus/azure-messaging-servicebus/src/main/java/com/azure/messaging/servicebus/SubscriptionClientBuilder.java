package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.StringUtil;
import reactor.core.scheduler.Scheduler;

public final class SubscriptionClientBuilder {
    public SubscriptionClientBuilder(){

    }

    public SubscriptionClientBuilder connectionString(String connectionString) {
        return this;
    }
    public SubscriptionClientBuilder proxyOptions(ProxyOptions proxyOptions) {
        return this;
    }

    public SubscriptionClientBuilder connectionString(String connectionString, String topicName) {
        return connectionString(connectionString);
    }

    public SubscriptionClientBuilder topicName(String topicName) {
        return this;
    }
    public SubscriptionClientBuilder retryPolicy(AmqpRetryPolicy retryPolicy) {
        return this;
    }
    public SubscriptionClientBuilder transportType(TransportType transportType) {
        return this;
    }
    /** package- private method*/
    SubscriptionClientBuilder scheduler(Scheduler scheduler) {
        return this;

    }

    public SubscriptionClientBuilder addRule(Rule rule) {
        return this;
    }
    SubscriptionAsyncClient buildAsyncSubscriptionClient() {
        return null;
    }
}
