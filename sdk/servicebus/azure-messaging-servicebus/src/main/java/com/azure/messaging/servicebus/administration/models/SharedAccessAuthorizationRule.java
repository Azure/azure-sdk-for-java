// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.models.AuthorizationRuleImpl;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * A shared access key for accessing Service Bus entities.
 *
 * @see CreateQueueOptions#getAuthorizationRules()
 * @see CreateTopicOptions#getAuthorizationRules()
 */
@Fluent
public final class SharedAccessAuthorizationRule implements AuthorizationRule {
    /**
     * There one type of authorization rule.
     */
    private static final String FIXED_CLAIM_TYPE = "SharedAccessKey";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final List<AccessRights> accessRights;
    private final OffsetDateTime createdAt;
    private final String keyName;
    private final ClientLogger logger = new ClientLogger(SharedAccessAuthorizationRule.class);
    private final OffsetDateTime modifiedAt;

    private String primaryKey;
    private String secondaryKey;

    /**
     * Creates an instance with the given key name and access rights. The {@link #getPrimaryKey() primary key} and
     * {@link #getSecondaryKey() secondary key} is randomly generated.
     *
     * @param keyName The name of the rule.
     * @param accessRights The access rights.
     *
     * @throws NullPointerException if {@code keyName} or {@code accessRights} are null.
     * @throws IllegalArgumentException if {@code keyName} are empty strings.
     */
    public SharedAccessAuthorizationRule(String keyName, List<AccessRights> accessRights) {
        this(keyName, generateRandomKey(), generateRandomKey(), accessRights);
    }

    /**
     * Creates an instance with the given key name, primary key, secondary key, and access rights. The {@link
     * #getSecondaryKey() secondary key} is randomly generated.
     *
     * @param keyName The name of the rule.
     * @param primaryKey The primary key.
     * @param accessRights The access rights.
     *
     * @throws NullPointerException if {@code keyName}, {@code primaryKey}, or {@code accessRights} are null.
     * @throws IllegalArgumentException if {@code keyName}, {@code primaryKey} are empty strings.
     */
    public SharedAccessAuthorizationRule(String keyName, String primaryKey, List<AccessRights> accessRights) {
        this(keyName, primaryKey, generateRandomKey(), accessRights);
    }

    /**
     * Creates an instance with the given key name, primary key, secondary key, and access rights.
     *
     * @param keyName The name of the rule.
     * @param primaryKey The primary key.
     * @param secondaryKey The secondary key.
     * @param accessRights The access rights.
     *
     * @throws NullPointerException if {@code keyName}, {@code primaryKey}, {@code secondaryKey}, or {@code
     *     accessRights} are null.
     * @throws IllegalArgumentException if {@code keyName}, {@code primaryKey}, {@code secondaryKey} are empty
     *     strings.
     */
    public SharedAccessAuthorizationRule(String keyName, String primaryKey, String secondaryKey,
        List<AccessRights> accessRights) {
        this.keyName = Objects.requireNonNull(keyName, "'keyName' cannot be null.");
        this.primaryKey = Objects.requireNonNull(primaryKey, "'primaryKey' cannot be null.");
        this.secondaryKey = Objects.requireNonNull(secondaryKey, "'secondaryKey' cannot be null.");
        this.accessRights = new ArrayList<>(Objects.requireNonNull(accessRights,
            "'accessRights' cannot be null."));
        this.createdAt = null;
        this.modifiedAt = null;

        if (keyName.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'keyName' cannot be an empty string."));
        } else if (primaryKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'primaryKey' cannot be an empty string."));
        } else if (secondaryKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'secondaryKey' cannot be an empty string."));
        }
    }

    /**
     * Creates an instance using the implementation model.
     *
     * @param implementation Implementation model.
     */
    SharedAccessAuthorizationRule(AuthorizationRuleImpl implementation) {
        this.keyName = implementation.getKeyName();
        this.primaryKey = implementation.getPrimaryKey();
        this.secondaryKey = implementation.getSecondaryKey();
        this.accessRights = new ArrayList<>(implementation.getRights());
        this.createdAt = implementation.getCreatedTime();
        this.modifiedAt = implementation.getModifiedTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccessRights> getAccessRights() {
        return accessRights;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClaimType() {
        return FIXED_CLAIM_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClaimValue() {
        return "None";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getKeyName() {
        return keyName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OffsetDateTime getModifiedAt() {
        return modifiedAt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Sets the primary key.
     *
     * @param primaryKey The primary key to set.
     *
     * @return The updated {@link SharedAccessAuthorizationRule} object.
     * @throws NullPointerException if {@code primaryKey} is null.
     * @throws IllegalArgumentException if {@code primaryKey} is an empty string.
     */
    public SharedAccessAuthorizationRule setPrimaryKey(String primaryKey) {
        if (Objects.isNull(primaryKey)) {
            throw logger.logExceptionAsError(new NullPointerException("'primaryKey' cannot be null."));
        } else if (primaryKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'primaryKey' cannot be an empty string."));
        }
        this.primaryKey = primaryKey;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSecondaryKey() {
        return secondaryKey;
    }

    /**
     * Sets the secondary key.
     *
     * @param secondaryKey The secondary key to set.
     *
     * @return The updated {@link SharedAccessAuthorizationRule} object.
     * @throws NullPointerException if {@code secondary} is null.
     * @throws IllegalArgumentException if {@code secondary} is an empty string.
     */
    public SharedAccessAuthorizationRule setSecondaryKey(String secondaryKey) {
        if (Objects.isNull(secondaryKey)) {
            throw logger.logExceptionAsError(new NullPointerException("'primaryKey' cannot be null."));
        } else if (secondaryKey.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'primaryKey' cannot be an empty string."));
        }
        this.secondaryKey = secondaryKey;
        return this;
    }

    /**
     * Generates a random Base64 encoded key.
     *
     * @return A base 64 encoded key.
     */
    private static String generateRandomKey() {
        final byte[] key256 = new byte[32];

        SECURE_RANDOM.nextBytes(key256);
        return Base64.getEncoder().encodeToString(key256);
    }
}
