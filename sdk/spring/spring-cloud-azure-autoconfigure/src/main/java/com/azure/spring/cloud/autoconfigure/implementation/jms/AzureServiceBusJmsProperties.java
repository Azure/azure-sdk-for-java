// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.jms.support.QosSettings;
import org.springframework.util.StringUtils;

import java.time.Duration;

@ConfigurationProperties(prefix = AzureServiceBusJmsProperties.PREFIX)
public class AzureServiceBusJmsProperties implements InitializingBean {

    /**
     * Service Bus JMS properties prefix.
     */
    public static final String PREFIX = "spring.jms.servicebus";

    /**
     * Whether to enable Service Bus JMS autoconfiguration.
     */
    private boolean enabled = true;

    /**
     * Connection string to connect to a Service Bus namespace.
     */
    private String connectionString;
    /**
     * Service Bus topic client ID. Only works for the bean of topicJmsListenerContainerFactory.
     */
    private String topicClientId;
    /**
     * Connection idle timeout duration.
     */
    private Duration idleTimeout = Duration.ofMinutes(30);
    /**
     * Pricing tier for a Service Bus namespace.
     */
    private String pricingTier;

    private final Listener listener = new Listener();

    private final PrefetchPolicy prefetchPolicy = new PrefetchPolicy();

    @NestedConfigurationProperty
    private final JmsPoolConnectionFactoryProperties pool = new JmsPoolConnectionFactoryProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public JmsPoolConnectionFactoryProperties getPool() {
        return pool;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getTopicClientId() {
        return topicClientId;
    }

    public void setTopicClientId(String topicClientId) {
        this.topicClientId = topicClientId;
    }

    public String getPricingTier() {
        return this.pricingTier;
    }

    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Listener getListener() {
        return listener;
    }

    public PrefetchPolicy getPrefetchPolicy() {
        return prefetchPolicy;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("'spring.jms.servicebus.connection-string' should be provided");
        }

        if (null == pricingTier || !pricingTier.matches("(?i)premium|standard|basic")) {
            throw new IllegalArgumentException("'spring.jms.servicebus.pricing-tier' is not valid");
        }
    }

    public static class PrefetchPolicy {

        /**
         * Fallback value for prefetch option in this Service Bus namespace.
         */
        private int all = 0;
        /**
         * The number of prefetch for durable topic.
         */
        private int durableTopicPrefetch = 0;
        /**
         * The number of prefetch for queue browser.
         */
        private int queueBrowserPrefetch = 0;
        /**
         * The number of prefetch for queue.
         */
        private int queuePrefetch = 0;
        /**
         * The number of prefetch for topic.
         */
        private int topicPrefetch = 0;

        public int getAll() {
            return Math.max(all, 0);
        }

        public void setAll(int all) {
            this.all = all;
        }

        public int getDurableTopicPrefetch() {
            return durableTopicPrefetch > 0 ? durableTopicPrefetch : getAll();
        }

        public void setDurableTopicPrefetch(int durableTopicPrefetch) {
            this.durableTopicPrefetch = durableTopicPrefetch;
        }

        public int getQueueBrowserPrefetch() {
            return queueBrowserPrefetch > 0 ? queueBrowserPrefetch : getAll();
        }

        public void setQueueBrowserPrefetch(int queueBrowserPrefetch) {
            this.queueBrowserPrefetch = queueBrowserPrefetch;
        }

        public int getQueuePrefetch() {
            return queuePrefetch > 0 ? queuePrefetch : getAll();
        }

        public void setQueuePrefetch(int queuePrefetch) {
            this.queuePrefetch = queuePrefetch;
        }

        public int getTopicPrefetch() {
            return topicPrefetch > 0 ? topicPrefetch : getAll();
        }

        public void setTopicPrefetch(int topicPrefetch) {
            this.topicPrefetch = topicPrefetch;
        }
    }

    public static class Listener {

        /**
         * Whether the reply destination type is topic. Only works for the bean of topicJmsListenerContainerFactory.
         */
        private Boolean replyPubSubDomain;

        /**
         * The QosSettings to use when sending a reply.
         */
        private QosSettings replyQosSettings;

        /**
         * Whether to make the subscription durable. Only works for the bean of topicJmsListenerContainerFactory.
         */
        private Boolean subscriptionDurable = Boolean.TRUE;

        /**
         * Whether to make the subscription shared. Only works for the bean of topicJmsListenerContainerFactory.
         */
        private Boolean subscriptionShared;

        /**
         * The phase in which this container should be started and stopped.
         */
        private Integer phase;

        public Boolean isReplyPubSubDomain() {
            return replyPubSubDomain;
        }

        public void setReplyPubSubDomain(Boolean replyPubSubDomain) {
            this.replyPubSubDomain = replyPubSubDomain;
        }

        public QosSettings getReplyQosSettings() {
            return replyQosSettings;
        }

        public void setReplyQosSettings(QosSettings replyQosSettings) {
            this.replyQosSettings = replyQosSettings;
        }

        public Boolean isSubscriptionDurable() {
            return subscriptionDurable;
        }

        public void setSubscriptionDurable(Boolean subscriptionDurable) {
            this.subscriptionDurable = subscriptionDurable;
        }

        public Boolean isSubscriptionShared() {
            return subscriptionShared;
        }

        public void setSubscriptionShared(Boolean subscriptionShared) {
            this.subscriptionShared = subscriptionShared;
        }

        public Integer getPhase() {
            return phase;
        }

        public void setPhase(Integer phase) {
            this.phase = phase;
        }
    }
}
