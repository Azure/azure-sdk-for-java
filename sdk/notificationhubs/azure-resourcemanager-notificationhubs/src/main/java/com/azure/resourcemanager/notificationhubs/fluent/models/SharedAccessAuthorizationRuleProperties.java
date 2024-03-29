// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.notificationhubs.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.notificationhubs.models.AccessRights;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * SharedAccessAuthorizationRule properties.
 */
@Fluent
public final class SharedAccessAuthorizationRuleProperties {
    /*
     * Gets or sets the rights associated with the rule.
     */
    @JsonProperty(value = "rights", required = true)
    private List<AccessRights> rights;

    /*
     * Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     */
    @JsonProperty(value = "primaryKey")
    private String primaryKey;

    /*
     * Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     */
    @JsonProperty(value = "secondaryKey")
    private String secondaryKey;

    /*
     * Gets a string that describes the authorization rule.
     */
    @JsonProperty(value = "keyName", access = JsonProperty.Access.WRITE_ONLY)
    private String keyName;

    /*
     * Gets the last modified time for this rule
     */
    @JsonProperty(value = "modifiedTime", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime modifiedTime;

    /*
     * Gets the created time for this rule
     */
    @JsonProperty(value = "createdTime", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdTime;

    /*
     * Gets a string that describes the claim type
     */
    @JsonProperty(value = "claimType", access = JsonProperty.Access.WRITE_ONLY)
    private String claimType;

    /*
     * Gets a string that describes the claim value
     */
    @JsonProperty(value = "claimValue", access = JsonProperty.Access.WRITE_ONLY)
    private String claimValue;

    /*
     * Gets the revision number for the rule
     */
    @JsonProperty(value = "revision", access = JsonProperty.Access.WRITE_ONLY)
    private Integer revision;

    /**
     * Creates an instance of SharedAccessAuthorizationRuleProperties class.
     */
    public SharedAccessAuthorizationRuleProperties() {
    }

    /**
     * Get the rights property: Gets or sets the rights associated with the rule.
     * 
     * @return the rights value.
     */
    public List<AccessRights> rights() {
        return this.rights;
    }

    /**
     * Set the rights property: Gets or sets the rights associated with the rule.
     * 
     * @param rights the rights value to set.
     * @return the SharedAccessAuthorizationRuleProperties object itself.
     */
    public SharedAccessAuthorizationRuleProperties withRights(List<AccessRights> rights) {
        this.rights = rights;
        return this;
    }

    /**
     * Get the primaryKey property: Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     * 
     * @return the primaryKey value.
     */
    public String primaryKey() {
        return this.primaryKey;
    }

    /**
     * Set the primaryKey property: Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     * 
     * @param primaryKey the primaryKey value to set.
     * @return the SharedAccessAuthorizationRuleProperties object itself.
     */
    public SharedAccessAuthorizationRuleProperties withPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * Get the secondaryKey property: Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     * 
     * @return the secondaryKey value.
     */
    public String secondaryKey() {
        return this.secondaryKey;
    }

    /**
     * Set the secondaryKey property: Gets a base64-encoded 256-bit primary key for signing and
     * validating the SAS token.
     * 
     * @param secondaryKey the secondaryKey value to set.
     * @return the SharedAccessAuthorizationRuleProperties object itself.
     */
    public SharedAccessAuthorizationRuleProperties withSecondaryKey(String secondaryKey) {
        this.secondaryKey = secondaryKey;
        return this;
    }

    /**
     * Get the keyName property: Gets a string that describes the authorization rule.
     * 
     * @return the keyName value.
     */
    public String keyName() {
        return this.keyName;
    }

    /**
     * Get the modifiedTime property: Gets the last modified time for this rule.
     * 
     * @return the modifiedTime value.
     */
    public OffsetDateTime modifiedTime() {
        return this.modifiedTime;
    }

    /**
     * Get the createdTime property: Gets the created time for this rule.
     * 
     * @return the createdTime value.
     */
    public OffsetDateTime createdTime() {
        return this.createdTime;
    }

    /**
     * Get the claimType property: Gets a string that describes the claim type.
     * 
     * @return the claimType value.
     */
    public String claimType() {
        return this.claimType;
    }

    /**
     * Get the claimValue property: Gets a string that describes the claim value.
     * 
     * @return the claimValue value.
     */
    public String claimValue() {
        return this.claimValue;
    }

    /**
     * Get the revision property: Gets the revision number for the rule.
     * 
     * @return the revision value.
     */
    public Integer revision() {
        return this.revision;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (rights() == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Missing required property rights in model SharedAccessAuthorizationRuleProperties"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SharedAccessAuthorizationRuleProperties.class);
}
