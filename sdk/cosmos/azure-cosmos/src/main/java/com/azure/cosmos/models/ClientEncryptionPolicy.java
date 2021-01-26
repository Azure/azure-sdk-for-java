// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Client encryption policy.
 */
public final class ClientEncryptionPolicy {

    private JsonSerializable jsonSerializable;

    /**
     * Paths of the item that need encryption along with path-specific settings.
     */
    @JsonProperty("includedPaths")
    private List<ClientEncryptionIncludedPath> includedPaths;

    @JsonProperty("policyFormatVersion")
    private int policyFormatVersion;

    public ClientEncryptionPolicy(List<ClientEncryptionIncludedPath> paths) {
        this.validateIncludedPaths(paths);
        this.includedPaths = paths;
        this.policyFormatVersion = 1;
    }

    /**
     * Constructor.
     */
    public ClientEncryptionPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the client encryption policy.
     */
    ClientEncryptionPolicy(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Constructor.
     *
     * @param objectNode the object node that represents the client encryption policy.
     */
    ClientEncryptionPolicy(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    public List<ClientEncryptionIncludedPath> getIncludedPaths() {
        return this.includedPaths;
    }

    int getPolicyFormatVersion() {
        return policyFormatVersion;
    }

    void setPolicyFormatVersion(int policyFormatVersion) {
        this.policyFormatVersion = policyFormatVersion;
    }

    private void validateIncludedPaths(List<ClientEncryptionIncludedPath> clientEncryptionIncludedPath) {
        List<String> includedPathsList = new ArrayList<>();
        for (ClientEncryptionIncludedPath path : clientEncryptionIncludedPath) {
            this.validateClientEncryptionIncludedPath(path);
            if (includedPathsList.contains(path.path)) {
                throw new IllegalArgumentException("Duplicate Path found in clientEncryptionIncludedPath.");
            }

            includedPathsList.add(path.path);
        }
    }

    private void validateClientEncryptionIncludedPath(ClientEncryptionIncludedPath clientEncryptionIncludedPath) {
        if (clientEncryptionIncludedPath == null) {
            throw new IllegalArgumentException("clientEncryptionIncludedPath is null");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.path)) {
            throw new IllegalArgumentException("path in clientEncryptionIncludedPath is empty");
        }

        if (clientEncryptionIncludedPath.path.charAt(0) != '/'
            || clientEncryptionIncludedPath.path.lastIndexOf('/') != 0
            || clientEncryptionIncludedPath.path.substring(1).equals("id")) {
            throw new IllegalArgumentException("Invalid path " + clientEncryptionIncludedPath.path);
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.clientEncryptionKeyId)) {
            throw new IllegalArgumentException("clientEncryptionKeyId in clientEncryptionIncludedPath is empty");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.encryptionType)) {
            throw new IllegalArgumentException("encryptionType in clientEncryptionIncludedPath is empty");
        }

        if (!clientEncryptionIncludedPath.encryptionType.equals("Deterministic") &&
            !clientEncryptionIncludedPath.encryptionType.equals("Randomized")) {
            throw new IllegalArgumentException("EncryptionType should be either 'Deterministic' or 'Randomized'.");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.encryptionAlgorithm)) {
            throw new IllegalArgumentException("encryptionAlgorithm in clientEncryptionIncludedPath is empty");
        }

        if (!clientEncryptionIncludedPath.encryptionAlgorithm.equals("AEAD_AES_256_CBC_HMAC_SHA256")) {
            throw new IllegalArgumentException("EncryptionAlgorithm should be 'AEAD_AES_256_CBC_HMAC_SHA256'.");
        }
    }
}
