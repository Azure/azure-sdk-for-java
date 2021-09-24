// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.jms.support.QosSettings;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PostConstruct;

/**
 * {@link ConfigurationProperties} for configuring Azure Service Bus JMS.
 */
@Validated
@ConfigurationProperties(AzureServiceBusJmsProperties.PREFIX)
// TODO(xiada): does this need to implement AzureProperties?
public class AzureServiceBusJmsProperties {

    public static final String PREFIX = "spring.jms.servicebus";

    private String connectionString;

    /**
     * JMS clientID. Only works for the bean of topicJmsListenerContainerFactory.
     */
    private String topicClientId;

    private int idleTimeout = 1800000;

    private String pricingTier;

    private final Listener listener = new Listener();

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
        return pricingTier;
    }

    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public Listener getListener() {
        return listener;
    }

    /**
     * Validate spring.jms.servicebus related properties.
     *
     * @throws IllegalArgumentException If connectionString is empty.
     */
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
