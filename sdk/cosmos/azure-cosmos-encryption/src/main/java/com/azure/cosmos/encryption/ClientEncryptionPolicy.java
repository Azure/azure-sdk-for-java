// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Client encryption policy.
 * TODO once the public api is finalized add setter and getter
 */
public class ClientEncryptionPolicy {
    
    /**
     * Initializes a new instance of ClientEncryptionPolicy.
     */
    public ClientEncryptionPolicy() {
        this.policyFormatVersion = 1;
    }

    /// <summary>
    /// Paths of the item that need encryption along with path-specific settings.
    /// </summary>

    @JsonProperty("includedPaths")
    public List<ClientEncryptionIncludedPath> includedPaths = new ArrayList<ClientEncryptionIncludedPath>();

    /// <summary>
    /// Information about storage of the Data Encryption Keys in wrapped (encrypted) form.
    /// </summary>
    @JsonProperty("dataEncryptionKeyMetadata")
    public CosmosDataEncryptionKeyProviderMetadata dataEncryptionKeyMetadata;

    @JsonProperty("policyFormatVersion")
    public int policyFormatVersion;
}
