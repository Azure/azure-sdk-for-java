// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.properties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.util.Assert;

/**
 * Properties for Monitoring an Azure App Configuratin Store.
 */
public class AppConfigurationStoreMonitoring {

    private boolean enabled = false;

    private Duration refreshInterval = Duration.ofSeconds(30);

    private Duration featureFlagRefreshInterval = Duration.ofSeconds(30);

    private List<AppConfigurationStoreTrigger> triggers = new ArrayList<>();

    private PushNotification pushNotification = new PushNotification();

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the refreshInterval
     */
    public Duration getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * The minimum time between checks. The minimum valid time is 1s. The default refresh interval is 30s.
     *
     * @param refreshInterval minimum time between refresh checks
     */
    public void setRefreshInterval(Duration refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * @return the featureFlagRefreshInterval
     */
    public Duration getFeatureFlagRefreshInterval() {
        return featureFlagRefreshInterval;
    }

    /**
     * The minimum time between checks of feature flags. The minimum valid time is 1s. The default refresh interval is 30s.
     * @param featureFlagRefreshInterval minimum time between refresh checks for feature flags
     */
    public void setFeatureFlagRefreshInterval(Duration featureFlagRefreshInterval) {
        this.featureFlagRefreshInterval = featureFlagRefreshInterval;
    }

    /**
     * @return the triggers
     */
    public List<AppConfigurationStoreTrigger> getTriggers() {
        return triggers;
    }

    /**
     * @param triggers the triggers to set
     */
    public void setTriggers(List<AppConfigurationStoreTrigger> triggers) {
        this.triggers = triggers;
    }

    /**
     * @return the pushNotification
     */
    public PushNotification getPushNotification() {
        return pushNotification;
    }

    /**
     * @param pushNotification the pushNotification to set
     */
    public void setPushNotification(PushNotification pushNotification) {
        this.pushNotification = pushNotification;
    }

    @PostConstruct
    public void validateAndInit() {
        if (enabled) {
            Assert.notEmpty(triggers, "Triggers need to be set if refresh is enabled.");
            for (AppConfigurationStoreTrigger trigger : triggers) {
                trigger.validateAndInit();
            }
        }
        Assert.isTrue(refreshInterval.getSeconds() >= 1, "Minimum refresh interval time is 1 Second.");
        Assert.isTrue(featureFlagRefreshInterval.getSeconds() >= 1, "Minimum Feature Flag refresh interval time is 1 Second.");
    }

    /**
     * Push Notification tokens for setting watch interval to 0.
     */
    public static class PushNotification {

        private AccessToken primaryToken = new AccessToken();

        private AccessToken secondaryToken = new AccessToken();

        /**
         * @return the primaryToken
         */
        public AccessToken getPrimaryToken() {
            return primaryToken;
        }

        /**
         * @param primaryToken the primaryToken to set
         */
        public void setPrimaryToken(AccessToken primaryToken) {
            this.primaryToken = primaryToken;
        }

        /**
         * @return the secondaryToken
         */
        public AccessToken getSecondaryToken() {
            return secondaryToken;
        }

        /**
         * @param secondaryToken the secondaryToken to set
         */
        public void setSecondaryToken(AccessToken secondaryToken) {
            this.secondaryToken = secondaryToken;
        }
    }

    /**
     * Token used to verifying Push Refresh Requests
     */
    public static class AccessToken {

        private String name;

        private String secret;

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the secret
         */
        public String getSecret() {
            return secret;
        }

        /**
         * @param secret the secret to set
         */
        public void setSecret(String secret) {
            this.secret = secret;
        }

        public boolean isValid() {
            return this.name != null && this.secret != null;
        }

    }

}
