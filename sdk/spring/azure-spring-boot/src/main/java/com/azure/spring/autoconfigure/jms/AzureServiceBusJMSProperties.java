// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jms.support.QosSettings;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * {@link ConfigurationProperties} for configuring Azure Service Bus JMS.
 */
@Validated
@ConfigurationProperties("spring.jms.servicebus")
public class AzureServiceBusJMSProperties {

    private String connectionString;

    /**
     * JMS clientID. Only works for the bean of topicJmsListenerContainerFactory.
     */
    private String topicClientId;

    private int idleTimeout = 1800000;

    private String pricingTier;

    private final Listener listener = new Listener();

    private final PrefetchPolicy prefetchPolicy = new PrefetchPolicy();

    /**
     * Gets the connection string.
     *
     * @return the connection string
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Sets the connection string.
     *
     * @param connectionString the connection string
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Gets the topic client ID.
     *
     * @return the topic client ID
     */
    public String getTopicClientId() {
        return topicClientId;
    }

    /**
     * Sets the topic client ID.
     *
     * @param topicClientId the topic client ID
     */
    public void setTopicClientId(String topicClientId) {
        this.topicClientId = topicClientId;
    }

    /**
     * Gets the pricing tier.
     *
     * @return the pricing tier
     */
    public String getPricingTier() {
        return pricingTier;
    }

    /**
     * Sets the pricing tier.
     *
     * @param pricingTier the pricing tier
     */
    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    /**
     * Gets the idle timeout.
     *
     * @return the idle timeout
     */
    public int getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the idle timeout.
     *
     * @param idleTimeout the idle timeout
     */
    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Gets the listener.
     *
     * @return the listener
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Gets the prefetch policy.
     *
     * @return the prefetch policy
     */
    public PrefetchPolicy getPrefetchPolicy() {
        return prefetchPolicy;
    }

    /**
     * Validate spring.jms.servicebus related properties.
     *
     * @throws IllegalArgumentException If connectionString is empty.
     */
    @SuppressFBWarnings
    @PostConstruct
    public void validate() {
        if (!StringUtils.hasText(connectionString)) {
            throw new IllegalArgumentException("'spring.jms.servicebus.connection-string' should be provided");
        }

        if (!pricingTier.matches("(?i)premium|standard|basic")) {
            throw new IllegalArgumentException("'spring.jms.servicebus.pricing-tier' is not valid");
        }
    }

    /**
     * Properties to configure {@link org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy} for
     * {@link org.apache.qpid.jms.JmsConnectionFactory} .
     */
    public static class PrefetchPolicy {

        private int all = 0;

        private int durableTopicPrefetch = 0;

        private int queueBrowserPrefetch = 0;

        private int queuePrefetch = 0;

        private int topicPrefetch = 0;

        /**
         * Gets all.
         *
         * @return all
         */
        public int getAll() {
            return Math.max(all, 0);
        }

        /**
         * Sets all.
         * @param all all
         */
        public void setAll(int all) {
            this.all = all;
        }

        /**
         * @return Returns the durableTopicPrefetch.
         */
        public int getDurableTopicPrefetch() {
            return durableTopicPrefetch > 0 ? durableTopicPrefetch : getAll();
        }

        /**
         * @param durableTopicPrefetch Sets the durable topic prefetch value
         */
        public void setDurableTopicPrefetch(int durableTopicPrefetch) {
            this.durableTopicPrefetch = durableTopicPrefetch;
        }

        /**
         *
         * @return Returns the queueBrowserPrefetch.
         */
        public int getQueueBrowserPrefetch() {
            return queueBrowserPrefetch > 0 ? queueBrowserPrefetch : getAll();
        }

        /**
         * @param queueBrowserPrefetch The queueBrowserPrefetch to set.
         */
        public void setQueueBrowserPrefetch(int queueBrowserPrefetch) {
            this.queueBrowserPrefetch = queueBrowserPrefetch;
        }

        /**
         * @return Returns the queuePrefetch.
         */
        public int getQueuePrefetch() {
            return queuePrefetch > 0 ? queuePrefetch : getAll();
        }

        /**
         * @param queuePrefetch The queuePrefetch to set.
         */
        public void setQueuePrefetch(int queuePrefetch) {
            this.queuePrefetch = queuePrefetch;
        }

        /**
         * @return Returns the topicPrefetch.
         */
        public int getTopicPrefetch() {
            return topicPrefetch > 0 ? topicPrefetch : getAll();
        }

        /**
         * @param topicPrefetch The topicPrefetch to set.
         */
        public void setTopicPrefetch(int topicPrefetch) {
            this.topicPrefetch = topicPrefetch;
        }

    }


    /**
     * Properties to configure {@link org.springframework.jms.annotation.JmsListener} for
     * {@link org.springframework.jms.config.AbstractJmsListenerContainerFactory}.
     */
    public static class Listener {

        /**
         * Whether the reply destination type is topic. Only works for the bean of topicJmsListenerContainerFactory.
         */
        private Boolean replyPubSubDomain;

        /**
         * Configure the {@link QosSettings} to use when sending a reply.
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
         * Specify the phase in which this container should be started and
         * stopped.
         */
        private Integer phase;

        /**
         * Whether the reply destination is topic.
         *
         * @return whether the reply destination is topic
         */
        public Boolean isReplyPubSubDomain() {
            return replyPubSubDomain;
        }

        /**
         * Sets whether the reply destination is topic.
         *
         * @param replyPubSubDomain whether the reply destination is topic
         */
        public void setReplyPubSubDomain(Boolean replyPubSubDomain) {
            this.replyPubSubDomain = replyPubSubDomain;
        }

        /**
         * Gets the reply QoS settings.
         *
         * @return the reply QoS settings
         */
        public QosSettings getReplyQosSettings() {
            return replyQosSettings;
        }

        /**
         * Sets the reply QoS settings.
         *
         * @param replyQosSettings the reply QoS settings
         */
        public void setReplyQosSettings(QosSettings replyQosSettings) {
            this.replyQosSettings = replyQosSettings;
        }

        /**
         * Whether to make the subscription durable.
         *
         * @return whether to make the subscription durable
         */
        public Boolean isSubscriptionDurable() {
            return subscriptionDurable;
        }

        /**
         * Sets whether to make the subscription durable.
         *
         * @param subscriptionDurable whether to make the subscription durable.
         */
        public void setSubscriptionDurable(Boolean subscriptionDurable) {
            this.subscriptionDurable = subscriptionDurable;
        }

        /**
         * Whether to make the subscription shared.
         *
         * @return whether to make the subscription shared.
         */
        public Boolean isSubscriptionShared() {
            return subscriptionShared;
        }

        /**
         * Sets whether to make the subscription shared.
         *
         * @param subscriptionShared whether to make the subscription shared
         */
        public void setSubscriptionShared(Boolean subscriptionShared) {
            this.subscriptionShared = subscriptionShared;
        }

        /**
         * Gets the phase in which this container should be started and stopped.
         *
         * @return the phase in which this container should be started and stopped
         */
        public Integer getPhase() {
            return phase;
        }

        /**
         * Sets the phase in which this container should be started and stopped.
         *
         * @param phase the phase in which this container should be started and stopped
         */
        public void setPhase(Integer phase) {
            this.phase = phase;
        }
    }
}
