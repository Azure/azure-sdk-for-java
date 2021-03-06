// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.apimanagement.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.ProxyResource;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Portal Settings for the Developer Portal. */
@JsonFlatten
@Fluent
public class PortalSettingsContract extends ProxyResource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(PortalSettingsContract.class);

    /*
     * A delegation Url.
     */
    @JsonProperty(value = "properties.url")
    private String url;

    /*
     * A base64-encoded validation key to validate, that a request is coming
     * from Azure API Management.
     */
    @JsonProperty(value = "properties.validationKey")
    private String validationKey;

    /*
     * Subscriptions delegation settings.
     */
    @JsonProperty(value = "properties.subscriptions")
    private SubscriptionsDelegationSettingsProperties subscriptions;

    /*
     * User registration delegation settings.
     */
    @JsonProperty(value = "properties.userRegistration")
    private RegistrationDelegationSettingsProperties userRegistration;

    /*
     * Redirect Anonymous users to the Sign-In page.
     */
    @JsonProperty(value = "properties.enabled")
    private Boolean enabled;

    /*
     * Terms of service contract properties.
     */
    @JsonProperty(value = "properties.termsOfService")
    private TermsOfServiceProperties termsOfService;

    /**
     * Get the url property: A delegation Url.
     *
     * @return the url value.
     */
    public String url() {
        return this.url;
    }

    /**
     * Set the url property: A delegation Url.
     *
     * @param url the url value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the validationKey property: A base64-encoded validation key to validate, that a request is coming from Azure
     * API Management.
     *
     * @return the validationKey value.
     */
    public String validationKey() {
        return this.validationKey;
    }

    /**
     * Set the validationKey property: A base64-encoded validation key to validate, that a request is coming from Azure
     * API Management.
     *
     * @param validationKey the validationKey value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withValidationKey(String validationKey) {
        this.validationKey = validationKey;
        return this;
    }

    /**
     * Get the subscriptions property: Subscriptions delegation settings.
     *
     * @return the subscriptions value.
     */
    public SubscriptionsDelegationSettingsProperties subscriptions() {
        return this.subscriptions;
    }

    /**
     * Set the subscriptions property: Subscriptions delegation settings.
     *
     * @param subscriptions the subscriptions value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withSubscriptions(SubscriptionsDelegationSettingsProperties subscriptions) {
        this.subscriptions = subscriptions;
        return this;
    }

    /**
     * Get the userRegistration property: User registration delegation settings.
     *
     * @return the userRegistration value.
     */
    public RegistrationDelegationSettingsProperties userRegistration() {
        return this.userRegistration;
    }

    /**
     * Set the userRegistration property: User registration delegation settings.
     *
     * @param userRegistration the userRegistration value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withUserRegistration(RegistrationDelegationSettingsProperties userRegistration) {
        this.userRegistration = userRegistration;
        return this;
    }

    /**
     * Get the enabled property: Redirect Anonymous users to the Sign-In page.
     *
     * @return the enabled value.
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled property: Redirect Anonymous users to the Sign-In page.
     *
     * @param enabled the enabled value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the termsOfService property: Terms of service contract properties.
     *
     * @return the termsOfService value.
     */
    public TermsOfServiceProperties termsOfService() {
        return this.termsOfService;
    }

    /**
     * Set the termsOfService property: Terms of service contract properties.
     *
     * @param termsOfService the termsOfService value to set.
     * @return the PortalSettingsContract object itself.
     */
    public PortalSettingsContract withTermsOfService(TermsOfServiceProperties termsOfService) {
        this.termsOfService = termsOfService;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (subscriptions() != null) {
            subscriptions().validate();
        }
        if (userRegistration() != null) {
            userRegistration().validate();
        }
        if (termsOfService() != null) {
            termsOfService().validate();
        }
    }
}
