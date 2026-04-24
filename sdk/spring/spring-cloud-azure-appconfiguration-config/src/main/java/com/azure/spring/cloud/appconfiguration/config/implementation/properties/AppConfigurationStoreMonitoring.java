// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

import jakarta.annotation.PostConstruct;

/**
 * Configuration properties for monitoring changes in an Azure App Configuration store.
 */
public final class AppConfigurationStoreMonitoring {

    /**
     * Enables monitoring for configuration changes. When enabled, at least one
     * trigger must be configured.
     */
    private boolean enabled = false;

    /**
     * Interval between configuration refresh checks. Must be at least 1 second.
     * Defaults to 30 seconds.
     */
    private Duration refreshInterval = Duration.ofSeconds(30);

    /**
     * Interval between feature flag refresh checks. Must be at least 1 second.
     * Defaults to 30 seconds.
     */
    private Duration featureFlagRefreshInterval = Duration.ofSeconds(30);

    /**
     * Sentinel keys that trigger a configuration refresh when their values change.
     */
    private List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();

    /**
     * Configuration for validating push notification refresh requests.
     */
    private PushNotification pushNotification = new PushNotification();

    /**
     * Returns whether configuration monitoring is enabled.
     *
     * @return {@code true} if monitoring is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether configuration monitoring is enabled.
     *
     * @param enabled {@code true} to enable monitoring, {@code false} to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the interval between configuration refresh checks.
     *
     * @return the refresh interval
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Sets the interval between configuration refresh checks. Must be at least 1 second.
     *
     * @param refreshInterval the refresh interval duration
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * Returns the interval between feature flag refresh checks.
     *
     * @return the feature flag refresh interval
     */
    public Duration getFeatureFlagRefreshInterval() {
        return featureFlagRefreshInterval;
    }

    /**
     * Sets the interval between feature flag refresh checks. Must be at least 1 second.
     *
     * @param featureFlagRefreshInterval the feature flag refresh interval duration
     */
    public void setFeatureFlagRefreshInterval(Duration featureFlagRefreshInterval) {
        this.featureFlagRefreshInterval = featureFlagRefreshInterval;
    }

    /**
     * Returns the list of triggers that initiate a configuration refresh.
     *
     * @return the list of {@link AppConfigurationStoreTrigger} instances
     */
    public List<AppConfigurationStoreTrigger> getTriggers() {
        return triggers;
    }

    /**
     * Sets the list of triggers that initiate a configuration refresh.
     *
     * @param triggers the list of {@link AppConfigurationStoreTrigger} instances
     */
    public void setTriggers(List<AppConfigurationStoreTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * Returns the push notification configuration.
     *
     * @return the {@link PushNotification} settings
     */
    public PushNotification getPushNotification() {
        return pushNotification;
    }

    /**
     * Sets the push notification configuration.
     *
     * @param pushNotification the {@link PushNotification} settings
     */
    public void setPushNotification(PushNotification pushNotification) {
        this.pushNotification = pushNotification;
    }

    /**
     * Validates that refresh intervals are at least 1 second and, when monitoring
     * is enabled, that all configured triggers are valid.
     */
    @PostConstruct
    void validateAndInit() {
        if (enabled) {
            // Triggers are not required defaults to use collection monitoring if not set
            for (AppConfigurationStoreTrigger trigger : triggers) {
                trigger.validateAndInit();
            }
        }
        Assert.isTrue(refreshInterval.getSeconds() >= 1, "Minimum refresh interval time is 1 Second.");
        Assert.isTrue(featureFlagRefreshInterval.getSeconds() >= 1, "Minimum Feature Flag refresh interval time is 1 Second.");
    }

    /**
     * Access tokens used to validate push notification refresh requests.
     */
    public static class PushNotification {

        /**
         * Primary token for validating push notification requests.
         */
        private AccessToken primaryToken = new AccessToken();

        /**
         * Secondary (fallback) token for validating push notification requests.
         */
        private AccessToken secondaryToken = new AccessToken();

        /**
         * Returns the primary access token.
         *
         * @return the primary {@link AccessToken}
         */
        public AccessToken getPrimaryToken() {
            return primaryToken;
        }

        /**
         * Sets the primary access token.
         *
         * @param primaryToken the primary {@link AccessToken}
         */
        public void setPrimaryToken(AccessToken primaryToken) {
            this.primaryToken = primaryToken;
        }

        /**
         * Returns the secondary access token.
         *
         * @return the secondary {@link AccessToken}
         */
        public AccessToken getSecondaryToken() {
            return secondaryToken;
        }

        /**
         * Sets the secondary access token.
         *
         * @param secondaryToken the secondary {@link AccessToken}
         */
        public void setSecondaryToken(AccessToken secondaryToken) {
            this.secondaryToken = secondaryToken;
        }
    }

    /**
     * A name/secret pair used to verify push notification refresh requests.
     */
    public static class AccessToken {

        /**
         * Identifier for this access token.
         */
        private String name;

        /**
         * Secret value used for validation.
         */
        private String secret;

        /**
         * Returns the token name.
         *
         * @return the token name, or {@code null} if not set
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the token name.
         *
         * @param name the token name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Returns the token secret.
         *
         * @return the token secret, or {@code null} if not set
         */
        public String getSecret() {
            return secret;
        }

        /**
         * Sets the token secret.
         *
         * @param secret the token secret
         */
        public void setSecret(String secret) {
            this.secret = secret;
        }

        /**
         * Returns whether this token has both a name and secret configured.
         *
         * @return {@code true} if both name and secret are non-null
         */
        public boolean isValid() {
            return this.name != null && this.secret != null;
        }

    }

}
