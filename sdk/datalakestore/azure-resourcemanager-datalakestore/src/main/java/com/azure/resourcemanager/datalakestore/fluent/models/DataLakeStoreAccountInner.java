// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakestore.fluent.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.Resource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.datalakestore.models.DataLakeStoreAccountState;
import com.azure.resourcemanager.datalakestore.models.DataLakeStoreAccountStatus;
import com.azure.resourcemanager.datalakestore.models.EncryptionConfig;
import com.azure.resourcemanager.datalakestore.models.EncryptionIdentity;
import com.azure.resourcemanager.datalakestore.models.EncryptionProvisioningState;
import com.azure.resourcemanager.datalakestore.models.EncryptionState;
import com.azure.resourcemanager.datalakestore.models.FirewallAllowAzureIpsState;
import com.azure.resourcemanager.datalakestore.models.FirewallState;
import com.azure.resourcemanager.datalakestore.models.TierType;
import com.azure.resourcemanager.datalakestore.models.TrustedIdProviderState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Data Lake Store account information. */
@JsonFlatten
@Immutable
public class DataLakeStoreAccountInner extends Resource {
    @JsonIgnore private final ClientLogger logger = new ClientLogger(DataLakeStoreAccountInner.class);

    /*
     * The Key Vault encryption identity, if any.
     */
    @JsonProperty(value = "identity", access = JsonProperty.Access.WRITE_ONLY)
    private EncryptionIdentity identity;

    /*
     * The unique identifier associated with this Data Lake Store account.
     */
    @JsonProperty(value = "properties.accountId", access = JsonProperty.Access.WRITE_ONLY)
    private UUID accountId;

    /*
     * The provisioning status of the Data Lake Store account.
     */
    @JsonProperty(value = "properties.provisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private DataLakeStoreAccountStatus provisioningState;

    /*
     * The state of the Data Lake Store account.
     */
    @JsonProperty(value = "properties.state", access = JsonProperty.Access.WRITE_ONLY)
    private DataLakeStoreAccountState state;

    /*
     * The account creation time.
     */
    @JsonProperty(value = "properties.creationTime", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime creationTime;

    /*
     * The account last modified time.
     */
    @JsonProperty(value = "properties.lastModifiedTime", access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime lastModifiedTime;

    /*
     * The full CName endpoint for this account.
     */
    @JsonProperty(value = "properties.endpoint", access = JsonProperty.Access.WRITE_ONLY)
    private String endpoint;

    /*
     * The default owner group for all new folders and files created in the
     * Data Lake Store account.
     */
    @JsonProperty(value = "properties.defaultGroup", access = JsonProperty.Access.WRITE_ONLY)
    private String defaultGroup;

    /*
     * The Key Vault encryption configuration.
     */
    @JsonProperty(value = "properties.encryptionConfig", access = JsonProperty.Access.WRITE_ONLY)
    private EncryptionConfig encryptionConfig;

    /*
     * The current state of encryption for this Data Lake Store account.
     */
    @JsonProperty(value = "properties.encryptionState", access = JsonProperty.Access.WRITE_ONLY)
    private EncryptionState encryptionState;

    /*
     * The current state of encryption provisioning for this Data Lake Store
     * account.
     */
    @JsonProperty(value = "properties.encryptionProvisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private EncryptionProvisioningState encryptionProvisioningState;

    /*
     * The list of firewall rules associated with this Data Lake Store account.
     */
    @JsonProperty(value = "properties.firewallRules", access = JsonProperty.Access.WRITE_ONLY)
    private List<FirewallRuleInner> firewallRules;

    /*
     * The list of virtual network rules associated with this Data Lake Store
     * account.
     */
    @JsonProperty(value = "properties.virtualNetworkRules", access = JsonProperty.Access.WRITE_ONLY)
    private List<VirtualNetworkRuleInner> virtualNetworkRules;

    /*
     * The current state of the IP address firewall for this Data Lake Store
     * account.
     */
    @JsonProperty(value = "properties.firewallState", access = JsonProperty.Access.WRITE_ONLY)
    private FirewallState firewallState;

    /*
     * The current state of allowing or disallowing IPs originating within
     * Azure through the firewall. If the firewall is disabled, this is not
     * enforced.
     */
    @JsonProperty(value = "properties.firewallAllowAzureIps", access = JsonProperty.Access.WRITE_ONLY)
    private FirewallAllowAzureIpsState firewallAllowAzureIps;

    /*
     * The list of trusted identity providers associated with this Data Lake
     * Store account.
     */
    @JsonProperty(value = "properties.trustedIdProviders", access = JsonProperty.Access.WRITE_ONLY)
    private List<TrustedIdProviderInner> trustedIdProviders;

    /*
     * The current state of the trusted identity provider feature for this Data
     * Lake Store account.
     */
    @JsonProperty(value = "properties.trustedIdProviderState", access = JsonProperty.Access.WRITE_ONLY)
    private TrustedIdProviderState trustedIdProviderState;

    /*
     * The commitment tier to use for next month.
     */
    @JsonProperty(value = "properties.newTier", access = JsonProperty.Access.WRITE_ONLY)
    private TierType newTier;

    /*
     * The commitment tier in use for the current month.
     */
    @JsonProperty(value = "properties.currentTier", access = JsonProperty.Access.WRITE_ONLY)
    private TierType currentTier;

    /**
     * Get the identity property: The Key Vault encryption identity, if any.
     *
     * @return the identity value.
     */
    public EncryptionIdentity identity() {
        return this.identity;
    }

    /**
     * Get the accountId property: The unique identifier associated with this Data Lake Store account.
     *
     * @return the accountId value.
     */
    public UUID accountId() {
        return this.accountId;
    }

    /**
     * Get the provisioningState property: The provisioning status of the Data Lake Store account.
     *
     * @return the provisioningState value.
     */
    public DataLakeStoreAccountStatus provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the state property: The state of the Data Lake Store account.
     *
     * @return the state value.
     */
    public DataLakeStoreAccountState state() {
        return this.state;
    }

    /**
     * Get the creationTime property: The account creation time.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Get the lastModifiedTime property: The account last modified time.
     *
     * @return the lastModifiedTime value.
     */
    public OffsetDateTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Get the endpoint property: The full CName endpoint for this account.
     *
     * @return the endpoint value.
     */
    public String endpoint() {
        return this.endpoint;
    }

    /**
     * Get the defaultGroup property: The default owner group for all new folders and files created in the Data Lake
     * Store account.
     *
     * @return the defaultGroup value.
     */
    public String defaultGroup() {
        return this.defaultGroup;
    }

    /**
     * Get the encryptionConfig property: The Key Vault encryption configuration.
     *
     * @return the encryptionConfig value.
     */
    public EncryptionConfig encryptionConfig() {
        return this.encryptionConfig;
    }

    /**
     * Get the encryptionState property: The current state of encryption for this Data Lake Store account.
     *
     * @return the encryptionState value.
     */
    public EncryptionState encryptionState() {
        return this.encryptionState;
    }

    /**
     * Get the encryptionProvisioningState property: The current state of encryption provisioning for this Data Lake
     * Store account.
     *
     * @return the encryptionProvisioningState value.
     */
    public EncryptionProvisioningState encryptionProvisioningState() {
        return this.encryptionProvisioningState;
    }

    /**
     * Get the firewallRules property: The list of firewall rules associated with this Data Lake Store account.
     *
     * @return the firewallRules value.
     */
    public List<FirewallRuleInner> firewallRules() {
        return this.firewallRules;
    }

    /**
     * Get the virtualNetworkRules property: The list of virtual network rules associated with this Data Lake Store
     * account.
     *
     * @return the virtualNetworkRules value.
     */
    public List<VirtualNetworkRuleInner> virtualNetworkRules() {
        return this.virtualNetworkRules;
    }

    /**
     * Get the firewallState property: The current state of the IP address firewall for this Data Lake Store account.
     *
     * @return the firewallState value.
     */
    public FirewallState firewallState() {
        return this.firewallState;
    }

    /**
     * Get the firewallAllowAzureIps property: The current state of allowing or disallowing IPs originating within Azure
     * through the firewall. If the firewall is disabled, this is not enforced.
     *
     * @return the firewallAllowAzureIps value.
     */
    public FirewallAllowAzureIpsState firewallAllowAzureIps() {
        return this.firewallAllowAzureIps;
    }

    /**
     * Get the trustedIdProviders property: The list of trusted identity providers associated with this Data Lake Store
     * account.
     *
     * @return the trustedIdProviders value.
     */
    public List<TrustedIdProviderInner> trustedIdProviders() {
        return this.trustedIdProviders;
    }

    /**
     * Get the trustedIdProviderState property: The current state of the trusted identity provider feature for this Data
     * Lake Store account.
     *
     * @return the trustedIdProviderState value.
     */
    public TrustedIdProviderState trustedIdProviderState() {
        return this.trustedIdProviderState;
    }

    /**
     * Get the newTier property: The commitment tier to use for next month.
     *
     * @return the newTier value.
     */
    public TierType newTier() {
        return this.newTier;
    }

    /**
     * Get the currentTier property: The commitment tier in use for the current month.
     *
     * @return the currentTier value.
     */
    public TierType currentTier() {
        return this.currentTier;
    }

    /** {@inheritDoc} */
    @Override
    public DataLakeStoreAccountInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DataLakeStoreAccountInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (identity() != null) {
            identity().validate();
        }
        if (encryptionConfig() != null) {
            encryptionConfig().validate();
        }
        if (firewallRules() != null) {
            firewallRules().forEach(e -> e.validate());
        }
        if (virtualNetworkRules() != null) {
            virtualNetworkRules().forEach(e -> e.validate());
        }
        if (trustedIdProviders() != null) {
            trustedIdProviders().forEach(e -> e.validate());
        }
    }
}
