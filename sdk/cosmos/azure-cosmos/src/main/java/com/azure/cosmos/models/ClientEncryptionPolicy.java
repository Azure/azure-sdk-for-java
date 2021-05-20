// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Client encryption policy.
 */
@Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class ClientEncryptionPolicy {

    private JsonSerializable jsonSerializable;

    /**
     * Paths of the item that need encryption along with path-specific settings.
     */
    @JsonProperty("includedPaths")
    private List<ClientEncryptionIncludedPath> includedPaths;

    @JsonProperty("policyFormatVersion")
    private int policyFormatVersion;

    /**
     * Constructor.
     *
     * @param paths list of path of the item that need encryption along with path-specific settings.
     */
    @Beta(value = Beta.SinceVersion.V4_14_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
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

    /**
     * Gets the list of path of the item that need encryption along with path-specific settings.
     * @return includedPaths
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public List<ClientEncryptionIncludedPath> getIncludedPaths() {
        return this.includedPaths;
    }

    /**
     * Version of the client encryption policy definition.
     * @return policyFormatVersion
     */
    @Beta(value = Beta.SinceVersion.V4_16_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public int getPolicyFormatVersion() {
        return policyFormatVersion;
    }

    void validatePartitionKeyPathsAreNotEncrypted(List<List<String>> partitionKeyPathTokens) {
        checkNotNull(partitionKeyPathTokens, "partitionKeyPathTokens cannot be null");
        List<String> propertiesToEncrypt =
            this.includedPaths.stream().map(clientEncryptionIncludedPath -> clientEncryptionIncludedPath.getPath().substring(1)).collect(Collectors.toList());

        for (List<String> tokensInPath : partitionKeyPathTokens) {
            checkNotNull(tokensInPath);
            if (tokensInPath.size() > 0) {
                String topLevelToken = tokensInPath.get(0);
                if (propertiesToEncrypt.contains(topLevelToken)) {
                    throw new IllegalArgumentException(String.format("Path %s which is part of the partition key " +
                        "cannot be included" +
                        " in the ClientEncryptionPolicy.", topLevelToken));
                }
            }
        }
    }

    private void validateIncludedPaths(List<ClientEncryptionIncludedPath> clientEncryptionIncludedPath) {
        List<String> includedPathsList = new ArrayList<>();
        for (ClientEncryptionIncludedPath path : clientEncryptionIncludedPath) {
            this.validateClientEncryptionIncludedPath(path);
            if (includedPathsList.contains(path.getPath())) {
                throw new IllegalArgumentException("Duplicate Path found in clientEncryptionIncludedPath.");
            }

            includedPathsList.add(path.getPath());
        }
    }

    private void validateClientEncryptionIncludedPath(ClientEncryptionIncludedPath clientEncryptionIncludedPath) {
        if (clientEncryptionIncludedPath == null) {
            throw new IllegalArgumentException("clientEncryptionIncludedPath is null");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.getPath())) {
            throw new IllegalArgumentException("path in clientEncryptionIncludedPath is empty");
        }

        if (clientEncryptionIncludedPath.getPath().charAt(0) != '/'
            || clientEncryptionIncludedPath.getPath().lastIndexOf('/') != 0
            || clientEncryptionIncludedPath.getPath().substring(1).equals("id")) {
            throw new IllegalArgumentException("Invalid path " + clientEncryptionIncludedPath.getPath());
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.getClientEncryptionKeyId())) {
            throw new IllegalArgumentException("clientEncryptionKeyId in clientEncryptionIncludedPath is empty");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.getEncryptionType())) {
            throw new IllegalArgumentException("encryptionType in clientEncryptionIncludedPath is empty");
        }

        if (!clientEncryptionIncludedPath.getEncryptionType().equals(Constants.Properties.DETERMINISTIC) &&
            !clientEncryptionIncludedPath.getEncryptionType().equals(Constants.Properties.RANDOMIZED)) {
            throw new IllegalArgumentException("EncryptionType should be either 'Deterministic' or 'Randomized'.");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.getEncryptionAlgorithm())) {
            throw new IllegalArgumentException("encryptionAlgorithm in clientEncryptionIncludedPath is empty");
        }

        if (!clientEncryptionIncludedPath.getEncryptionAlgorithm().equals("AEAD_AES_256_CBC_HMAC_SHA256")) {
            throw new IllegalArgumentException("EncryptionAlgorithm should be 'AEAD_AES_256_CBC_HMAC_SHA256'.");
        }
    }
}
