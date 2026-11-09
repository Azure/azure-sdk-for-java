// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

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

    /**
     * Constructor.
     *
     * @param paths list of path of the item that need encryption along with path-specific settings.
     *              the PolicyFormatVersion will be set to 1 which is the default value.
     *              Note: If you need to include partition key or id field paths as part of the ClientEncryptionPolicy, please set PolicyFormatVersion to 2.
     */
    public ClientEncryptionPolicy(List<ClientEncryptionIncludedPath> paths) {
        this.policyFormatVersion = 1;
        validateIncludedPaths(paths, policyFormatVersion);
        this.includedPaths = paths;
    }

    /**
     * Constructor.
     *
     * @param paths list of path of the item that need encryption along with path-specific settings.
     * @param policyFormatVersion version of the client encryption policy definition. Current supported versions are 1 and 2. Default version is 1.
     *                            Note: If you need to include partition key or id field paths as part of the ClientEncryptionPolicy, please set PolicyFormatVersion to 2.
     */
    public ClientEncryptionPolicy(List<ClientEncryptionIncludedPath> paths, int policyFormatVersion) {
        if (policyFormatVersion > 2 || policyFormatVersion < 1) {
            throw new IllegalArgumentException("Supported versions of client encryption policy are 1 and 2.");
        }
        this.policyFormatVersion = policyFormatVersion;
        validateIncludedPaths(paths, policyFormatVersion);
        this.includedPaths = paths;
    }

    /**
     * Constructor.
     */
    public ClientEncryptionPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Gets the list of paths of the item that need encryption along with path-specific settings.
     * @return includedPaths
     */
    public List<ClientEncryptionIncludedPath> getIncludedPaths() {
        return this.includedPaths;
    }

    /**
     * Version of the client encryption policy definition.
     * @return policyFormatVersion
     */
    public int getPolicyFormatVersion() {
        return policyFormatVersion;
    }

    /**
     * Ensures that partition key paths specified in the client encryption policy for encryption are encrypted using Deterministic encryption algorithm.
     * @param partitionKeyPathTokens Tokens corresponding to validated partition key.
     */
    void validatePartitionKeyPathsIfEncrypted(List<List<String>> partitionKeyPathTokens) {
        checkNotNull(partitionKeyPathTokens, "partitionKeyPathTokens cannot be null");


        for (List<String> tokensInPath : partitionKeyPathTokens) {
            checkNotNull(tokensInPath);
            if (tokensInPath.size() > 0) {
                String topLevelToken = tokensInPath.get(0);

                // paths in included paths start with "/". Get the ClientEncryptionIncludedPath and validate.
                List<ClientEncryptionIncludedPath> encrypterPartitionKeyPath =
                    this.includedPaths.stream().filter(clientEncryptionIncludedPath -> clientEncryptionIncludedPath.getPath().substring(1).equals(topLevelToken)).collect(Collectors.toList());

                if (encrypterPartitionKeyPath.size() >0) {
                    if (this.policyFormatVersion < 2) {
                        throw new IllegalArgumentException(String.format("Path %s which is part of the partition key " +
                            "cannot be encrypted" +
                            " with PolicyFormatVersion %s. Please use PolicyFormatVersion 2.", topLevelToken, policyFormatVersion));
                    }

                    // for the ClientEncryptionIncludedPath found check the encryption type.
                    if (!encrypterPartitionKeyPath.stream().map(encrypter -> encrypter.getEncryptionType()).findFirst().orElse(null).equals(Constants.Properties.DETERMINISTIC)) {
                        throw new IllegalArgumentException(String.format("Path %s which is part of the partition key " +
                            "has to be encrypted" +
                            " with Deterministic type Encryption.", topLevelToken));
                    }

                }
            }
        }
    }

    private static void validateIncludedPaths(List<ClientEncryptionIncludedPath> clientEncryptionIncludedPath, int policyFormatVersion) {
        List<String> includedPathsList = new ArrayList<>();
        for (ClientEncryptionIncludedPath path : clientEncryptionIncludedPath) {
            validateClientEncryptionIncludedPath(path, policyFormatVersion);
            if (includedPathsList.contains(path.getPath())) {
                throw new IllegalArgumentException("Duplicate Path found in clientEncryptionIncludedPath.");
            }

            includedPathsList.add(path.getPath());
        }
    }

    private static void validateClientEncryptionIncludedPath(ClientEncryptionIncludedPath clientEncryptionIncludedPath, int policyFormatVersion) {
        if (clientEncryptionIncludedPath == null) {
            throw new IllegalArgumentException("clientEncryptionIncludedPath is null");
        }

        if (StringUtils.isEmpty(clientEncryptionIncludedPath.getPath())) {
            throw new IllegalArgumentException("path in clientEncryptionIncludedPath is empty");
        }

        if (clientEncryptionIncludedPath.getPath().charAt(0) != '/'
            || clientEncryptionIncludedPath.getPath().lastIndexOf('/') != 0) {
            throw new IllegalArgumentException("Invalid path " + clientEncryptionIncludedPath.getPath());
        }

        if (clientEncryptionIncludedPath.getPath().substring(1).equals(Constants.Properties.ID)) {
            if (policyFormatVersion < 2) {
                throw new IllegalArgumentException(String.format("Path %s cannot be encrypted with policyFormatVersion %s.", clientEncryptionIncludedPath.getPath(), policyFormatVersion));
            }

            if (!clientEncryptionIncludedPath.getEncryptionType().equals(Constants.Properties.DETERMINISTIC)) {
                throw new IllegalArgumentException(String.format("Only deterministic encryption type is supported for path %s.", clientEncryptionIncludedPath.getPath()));
            }
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
