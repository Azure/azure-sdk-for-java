// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.botservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The parameters to provide for the Kik channel. */
@Fluent
public final class KikChannelProperties {
    /*
     * The Kik user name
     */
    @JsonProperty(value = "userName", required = true)
    private String username;

    /*
     * Kik API key. Value only returned through POST to the action Channel List
     * API, otherwise empty.
     */
    @JsonProperty(value = "apiKey")
    private String apiKey;

    /*
     * Whether this channel is validated for the bot
     */
    @JsonProperty(value = "isValidated")
    private Boolean isValidated;

    /*
     * Whether this channel is enabled for the bot
     */
    @JsonProperty(value = "isEnabled", required = true)
    private boolean isEnabled;

    /**
     * Get the username property: The Kik user name.
     *
     * @return the username value.
     */
    public String username() {
        return this.username;
    }

    /**
     * Set the username property: The Kik user name.
     *
     * @param username the username value to set.
     * @return the KikChannelProperties object itself.
     */
    public KikChannelProperties withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get the apiKey property: Kik API key. Value only returned through POST to the action Channel List API, otherwise
     * empty.
     *
     * @return the apiKey value.
     */
    public String apiKey() {
        return this.apiKey;
    }

    /**
     * Set the apiKey property: Kik API key. Value only returned through POST to the action Channel List API, otherwise
     * empty.
     *
     * @param apiKey the apiKey value to set.
     * @return the KikChannelProperties object itself.
     */
    public KikChannelProperties withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    /**
     * Get the isValidated property: Whether this channel is validated for the bot.
     *
     * @return the isValidated value.
     */
    public Boolean isValidated() {
        return this.isValidated;
    }

    /**
     * Set the isValidated property: Whether this channel is validated for the bot.
     *
     * @param isValidated the isValidated value to set.
     * @return the KikChannelProperties object itself.
     */
    public KikChannelProperties withIsValidated(Boolean isValidated) {
        this.isValidated = isValidated;
        return this;
    }

    /**
     * Get the isEnabled property: Whether this channel is enabled for the bot.
     *
     * @return the isEnabled value.
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }

    /**
     * Set the isEnabled property: Whether this channel is enabled for the bot.
     *
     * @param isEnabled the isEnabled value to set.
     * @return the KikChannelProperties object itself.
     */
    public KikChannelProperties withIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (username() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException("Missing required property username in model KikChannelProperties"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(KikChannelProperties.class);
}
