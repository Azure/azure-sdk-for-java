// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import com.azure.spring.cloud.core.properties.PasswordlessProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.jms.support.QosSettings;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = AzureServiceBusJmsProperties.PREFIX)
public class AzureServiceBusJmsProperties implements InitializingBean, PasswordlessProperties {

    /**
     * Service Bus JMS properties prefix.
     */
    public static final String PREFIX = "spring.jms.servicebus";

    private static final String SERVICE_BUS_SCOPE_AZURE = "https://servicebus.azure.net/.default";
    private static final String SERVICE_BUS_SCOPE_AZURE_CHINA = SERVICE_BUS_SCOPE_AZURE;
    private static final String SERVICE_BUS_SCOPE_AZURE_US_GOVERNMENT = SERVICE_BUS_SCOPE_AZURE;

    private static final Map<CloudType, String> SERVICEBUS_SCOPE_MAP = new HashMap<CloudType, String>() {
        {
            put(CloudType.AZURE, SERVICE_BUS_SCOPE_AZURE);
            put(CloudType.AZURE_CHINA, SERVICE_BUS_SCOPE_AZURE_CHINA);
            put(CloudType.AZURE_US_GOVERNMENT, SERVICE_BUS_SCOPE_AZURE_US_GOVERNMENT);
        }
    };

    private AzureProfileConfigurationProperties profile = new AzureProfileConfigurationProperties();

    /**
     * The scopes required for the access token.
     */
    private String scopes;

    private TokenCredentialConfigurationProperties credential = new TokenCredentialConfigurationProperties();

    /**
     * Whether to enable supporting azure identity token credentials.
     *
     * If the value is true, then 'spring.jms.servicebus.namespace' must be set.
     * If the passwordlessEnabled is true, it will try to authenticate connections with Azure AD.
     */
    private boolean passwordlessEnabled = false;

    /**
     * Whether to enable Service Bus JMS autoconfiguration.
     */
    private boolean enabled = true;

    /**
     * The Service Bus namespace.
     */
    private String namespace;

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

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getScopes() {
        return this.scopes == null ? getDefaultScopes() : this.scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    @Override
    public boolean isPasswordlessEnabled() {
        return passwordlessEnabled;
    }

    public void setPasswordlessEnabled(boolean passwordlessEnabled) {
        this.passwordlessEnabled = passwordlessEnabled;
    }

    @Override
    public AzureProfileConfigurationProperties getProfile() {
        return profile;
    }

    public void setProfile(AzureProfileConfigurationProperties profile) {
        this.profile = profile;
    }

    @Override
    public TokenCredentialConfigurationProperties getCredential() {
        return credential;
    }

    public void setCredential(TokenCredentialConfigurationProperties credential) {
        this.credential = credential;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (isPasswordlessEnabled()) {
            if (!StringUtils.hasText(namespace)) {
                throw new IllegalArgumentException("Passwordless connections enabled, 'spring.jms.servicebus.namespace' should be provided.");
            }
        } else {
            if (!StringUtils.hasText(connectionString)) {
                throw new IllegalArgumentException("'spring.jms.servicebus.connection-string' should be provided.");
            }
        }

        if (null == pricingTier || !pricingTier.matches("(?i)premium|standard")) {
            String errMessage = null;
            if ("basic".equalsIgnoreCase(pricingTier)) {
                errMessage = "The basic tier is not supported by Service Bus JMS. Please use standard or premium tier instead.";
            } else {
                errMessage = "'spring.jms.servicebus.pricing-tier' is not valid.";
            }
            throw new IllegalArgumentException(errMessage);
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

    private String getDefaultScopes() {
        return SERVICEBUS_SCOPE_MAP.getOrDefault(getProfile().getCloudType(), SERVICE_BUS_SCOPE_AZURE);
    }
}
