// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms.properties;

import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.jms.support.QosSettings;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link ConfigurationProperties} for configuring Azure Service Bus JMS.
 */
@ConfigurationProperties(prefix = AzureServiceBusJmsProperties.PREFIX)
public class AzureServiceBusJmsProperties implements InitializingBean, PasswordlessProperties {

    private static final String SERVICE_BUS_SCOPE_AZURE = "https://servicebus.azure.net/.default";
    private static final String SERVICE_BUS_SCOPE_AZURE_CHINA = SERVICE_BUS_SCOPE_AZURE;
    private static final String SERVICE_BUS_SCOPE_AZURE_GERMANY = SERVICE_BUS_SCOPE_AZURE;
    private static final String SERVICE_BUS_SCOPE_AZURE_US_GOVERNMENT = SERVICE_BUS_SCOPE_AZURE;

    // Whether to enable supporting azure identity token credentials
    private boolean passwordlessEnabled = false;

    private static final Map<CloudType, String> SERVICEBUS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(CloudType.AZURE, SERVICE_BUS_SCOPE_AZURE);
            put(CloudType.AZURE_CHINA, SERVICE_BUS_SCOPE_AZURE_CHINA);
            put(CloudType.AZURE_GERMANY, SERVICE_BUS_SCOPE_AZURE_GERMANY);
            put(CloudType.AZURE_US_GOVERNMENT, SERVICE_BUS_SCOPE_AZURE_US_GOVERNMENT);
        }
    };
    /**
     * Service Bus JMS properties prefix.
     */
    public static final String PREFIX = "spring.jms.servicebus";

    private AzureProfileProperties profile = new AzureProfileProperties();

    private String scopes;

    private TokenCredentialProperties credential = new TokenCredentialProperties();

    /**
     * Whether to enable Servive Bus JMS autoconfiguration.
     */
    private boolean enabled = true;

    /**
     * The Service Bus namespace.
     */
    private String nameSpace;

    /**
     * Connection string to connect to a Service Bus namespace.
     */
    private String connectionString;
    /**
     * Service Bus topic client ID. Only works for the bean of topicJmsListenerContainerFactory.
     */
    private String topicClientId;
    /**
     * Connection idle timeout duration that how long the client expects Service Bus to keep a connection alive when no messages delivered.
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-transport-v1.0-os.html#doc-doc-idle-time-out">AMQP specification</a>
     * @see <a href="https://learn.microsoft.com/azure/service-bus-messaging/service-bus-amqp-troubleshoot#link-is-not-created">Service Bus AMQP Errors</a>
     */
    private Duration idleTimeout = Duration.ofMinutes(2);
    /**
     * Pricing tier for a Service Bus namespace.
     */
    private String pricingTier;

    private final Listener listener = new Listener();

    private final PrefetchPolicy prefetchPolicy = new PrefetchPolicy();

    @NestedConfigurationProperty
    private final JmsPoolConnectionFactoryProperties pool = new JmsPoolConnectionFactoryProperties();

    /**
     * Whether to enable Service Bus JMS autoconfiguration.
     * @return Whether to enable Service Bus autoconfiguration
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set whether to enable Service Bus JMS autoconfiguation.
     * @param enabled whether to enable Service Bus autoconfiguration.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * The properties for a pooled connection factory.
     * @return the properties for a pooled connection factory.
     */
    public JmsPoolConnectionFactoryProperties getPool() {
        return pool;
    }

    /**
     * Get the connection string to connect to a Service Bus namespace.
     * @return the connection string to connect to a Service Bus namespace.
     */
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Set the connection string to connect to a Service Bus namespace.
     * @param connectionString the connection string to connect to a Service Bus namespace.
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     * Get the Service Bus topic client ID.
     * @return the Service Bus topic client ID.
     */
    public String getTopicClientId() {
        return topicClientId;
    }

    /**
     * Set the Service Bus topic client ID.
     * @param topicClientId the Service Bus topic client ID.
     */
    public void setTopicClientId(String topicClientId) {
        this.topicClientId = topicClientId;
    }

    /**
     * Get the pricing tier for a Service Bus namespace.
     * @return the pricing tier for a Service Bus namespace.
     */
    public String getPricingTier() {
        return this.pricingTier;
    }

    /**
     * Set the pricing tier for a Service Bus namespace.
     * @param pricingTier the pricing tier for a Service Bus namespace.
     */
    public void setPricingTier(String pricingTier) {
        this.pricingTier = pricingTier;
    }

    /**
     * Get the connection idle timeout duration.
     * @return the connection idle timeout duration.
     */
    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Set the connection idle timeout duration.
     * @param idleTimeout the connection idle timeout duration.
     */
    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Get the listener related properties.
     * @return the listener related properties.
     */
    public Listener getListener() {
        return listener;
    }

    /**
     * Get the prefetch policy related properties.
     * @return the prefetch policy related properties.
     */
    public PrefetchPolicy getPrefetchPolicy() {
        return prefetchPolicy;
    }

    /**
     * Get the Service Bus namespace.
     * @return the Service Bus namespace.
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Set the Service Bus namespace.
     * @param nameSpace the Service Bus namespace.
     */
    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    /**
     * Get the scopes required for the access token.
     *
     * @return scopes required for the access token
     */
    @Override
    public String getScopes() {
        return this.scopes == null ? getDefaultScopes() : this.scopes;
    }

    /**
     * Set the scopes required for the access token.
     *
     * @param scopes the scopes required for the access token
     */
    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    /**
     * Whether to enable connections authenticating with Azure AD, default is false.
     *
     * @return enable connections authenticating with Azure AD if true, otherwise false.
     */
    @Override
    public boolean isPasswordlessEnabled() {
        return passwordlessEnabled;
    }

    /**
     * Set the value to enable/disable connections authenticating with Azure AD.
     * If not set, by default the value is false.
     *
     * @param passwordlessEnabled the passwordlessEnabled
     */
    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    /**
     * Get the profile
     * @return the profile
     */
    @Override
    public AzureProfileProperties getProfile() {
        return profile;
    }

    /**
     * Set the profile
     * @param profile the profile properties related to an Azure subscription
     */
    public void setProfile(AzureProfileProperties profile) {
        this.profile = profile;
    }

    /**
     * Get the credential properties.
     *
     * @return the credential properties.
     */
    @Override
    public TokenCredentialOptions getCredential() {
        return credential;
    }

    /**
     * Set the credential properties.
     *
     * @param credential the credential properties
     */
    public void setCredential(TokenCredentialOptions credential) {
        this.credential = credential;
    }

    /**
     * Validate spring.jms.servicebus related properties.
     *
     * @throws IllegalArgumentException If connectionString is empty.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (isPasswordlessEnabled()) {
            if (!StringUtils.hasText(nameSpace)) {
                throw new IllegalArgumentException("Passwordless connections enabled, 'spring.jms.servicebus.namespace' should be provided.");
            }
        } else {
            if (!StringUtils.hasText(connectionString)) {
                throw new IllegalArgumentException("'spring.jms.servicebus.connection-string' should be provided.");
            }
        }

        if (null == pricingTier || !pricingTier.matches("(?i)premium|standard|basic")) {
            throw new IllegalArgumentException("'spring.jms.servicebus.pricing-tier' is not valid");
        }
    }

    /**
     * Properties to configure {@link org.apache.qpid.jms.policy.JmsDefaultPrefetchPolicy} for {@link
     * org.apache.qpid.jms.JmsConnectionFactory} .
     */
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

        /**
         * Gets the all prefetch value.
         *
         * @return The all prefect value.
         */
        public int getAll() {
            return Math.max(all, 0);
        }

        /**
         * Sets the all prefetch value.
         *
         * @param all The all prefetch value.
         */
        public void setAll(int all) {
            this.all = all;
        }

        /**
         * Gets the durable topic prefetch value.
         *
         * @return The durable topic prefetch value.
         */
        public int getDurableTopicPrefetch() {
            return durableTopicPrefetch > 0 ? durableTopicPrefetch : getAll();
        }

        /**
         * Sets the durable topic prefetch value.
         *
         * @param durableTopicPrefetch The durable topic prefetch value.
         */
        public void setDurableTopicPrefetch(int durableTopicPrefetch) {
            this.durableTopicPrefetch = durableTopicPrefetch;
        }

        /**
         * Gets the queue browser prefetch value.
         *
         * @return The queue browser prefetch value.
         */
        public int getQueueBrowserPrefetch() {
            return queueBrowserPrefetch > 0 ? queueBrowserPrefetch : getAll();
        }

        /**
         * Sets the queue browser prefetch value.
         *
         * @param queueBrowserPrefetch The queue browser prefetch value.
         */
        public void setQueueBrowserPrefetch(int queueBrowserPrefetch) {
            this.queueBrowserPrefetch = queueBrowserPrefetch;
        }

        /**
         * Gets the queue prefetch value.
         *
         * @return The queue prefetch value.
         */
        public int getQueuePrefetch() {
            return queuePrefetch > 0 ? queuePrefetch : getAll();
        }

        /**
         * Sets the queue prefetch value.
         *
         * @param queuePrefetch The queue prefetch value.
         */
        public void setQueuePrefetch(int queuePrefetch) {
            this.queuePrefetch = queuePrefetch;
        }

        /**
         * Gets the topic prefetch value.
         *
         * @return The topic prefetch value.
         */
        public int getTopicPrefetch() {
            return topicPrefetch > 0 ? topicPrefetch : getAll();
        }

        /**
         * Sets the topic prefetch value.
         *
         * @param topicPrefetch The topic prefetch value.
         */
        public void setTopicPrefetch(int topicPrefetch) {
            this.topicPrefetch = topicPrefetch;
        }
    }

    /**
     * Properties to configure {@link org.springframework.jms.annotation.JmsListener} for {@link
     * org.springframework.jms.config.AbstractJmsListenerContainerFactory}.
     */
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

        /**
         * Whether reply destination type is topic.
         *
         * @return Whether reply destination type is topic.
         */
        public Boolean isReplyPubSubDomain() {
            return replyPubSubDomain;
        }

        /**
         * Sets whether reply destination is topic.
         *
         * @param replyPubSubDomain Whether reply destination is topic.
         */
        public void setReplyPubSubDomain(Boolean replyPubSubDomain) {
            this.replyPubSubDomain = replyPubSubDomain;
        }

        /**
         * Gets the reply QoS settings.
         *
         * @return The reply QoS settings.
         */
        public QosSettings getReplyQosSettings() {
            return replyQosSettings;
        }

        /**
         * Sets the reply QoS settings.
         *
         * @param replyQosSettings The reply QoS settings.
         */
        public void setReplyQosSettings(QosSettings replyQosSettings) {
            this.replyQosSettings = replyQosSettings;
        }

        /**
         * Whether the subscription is durable.
         *
         * @return Whether the subscription is durable.
         */
        public Boolean isSubscriptionDurable() {
            return subscriptionDurable;
        }

        /**
         * Sets whether the subscription is durable.
         *
         * @param subscriptionDurable Whether the subscription is durable.
         */
        public void setSubscriptionDurable(Boolean subscriptionDurable) {
            this.subscriptionDurable = subscriptionDurable;
        }

        /**
         * Whether the subscription is shared.
         *
         * @return Whether the subscription is shared.
         */
        public Boolean isSubscriptionShared() {
            return subscriptionShared;
        }

        /**
         * Sets whether the subscription is shared.
         *
         * @param subscriptionShared Whether the subscription is shared.
         */
        public void setSubscriptionShared(Boolean subscriptionShared) {
            this.subscriptionShared = subscriptionShared;
        }

        /**
         * Gets the phase.
         *
         * @return The phase.
         */
        public Integer getPhase() {
            return phase;
        }

        /**
         * Sets the phase.
         *
         * @param phase The phase.
         */
        public void setPhase(Integer phase) {
            this.phase = phase;
        }
    }

    private String getDefaultScopes() {
        return SERVICEBUS_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), SERVICE_BUS_SCOPE_AZURE);
    }
}
