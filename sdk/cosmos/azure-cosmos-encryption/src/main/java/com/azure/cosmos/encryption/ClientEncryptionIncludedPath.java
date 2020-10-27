// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * // TODO once the public api is finalized add setter and getter
 * Path that needs encryption and associated settings within {@link ClientEncryptionPolicy}
 */
public class ClientEncryptionIncludedPath {

    /// <summary>
    /// Path to be encrypted. Must be a top level path, eg. /salary
    /// </summary>
    @JsonProperty("path")
    public String path;

    /// <summary>
    /// Identifier of the Data Encryption Key to be used to encrypt the path.
    /// </summary>
    @JsonProperty("dataEncryptionKeyId")
    public String dataEncryptionKeyId;

    /// <summary>
    /// Type of encryption to be performed. Egs.: Deterministic Randomized
    /// </summary>
    @JsonProperty("EncryptionType")
    public String encryptionType;


    // todo
    // [JsonProperty(PropertyName = "customSerializerIdentifier")]
    // public string CustomSerializerIdentifier { get; set; }
}
