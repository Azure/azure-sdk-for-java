// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents an authorization rule to access Service Bus entities.
 *
 * @see SharedAccessAuthorizationRule
 * @see CreateQueueOptions#getAuthorizationRules()
 * @see CreateTopicOptions#getAuthorizationRules()
 */
public interface AuthorizationRule {
    /**
     * Gets the access rights for the rule.
     *
     * @return The access rights for the rule.
     */
    List<AccessRights> getAccessRights();

    /**
     * Gets the claim type.
     *
     * @return The claim type.
     */
    String getClaimType();

    /**
     * Gets the claim value.
     *
     * @return The claim value.
     */
    String getClaimValue();

    /**
     * Gets the date time this rule was created.
     *
     * @return The date time this rule was created.
     */
    OffsetDateTime getCreatedAt();

    /**
     * Gets the name of the authorization rule.
     *
     * @return name of the authoriation rule.
     */
    String getKeyName();

    /**
     * Gets the date time this rule was last modified.
     *
     * @return The date time this rule was last modified.
     */
    OffsetDateTime getModifiedAt();

    /**
     * Gets the primary key.
     *
     * @return The primary key.
     */
    String getPrimaryKey();

    /**
     * Gets the secondary key.
     *
     * @return The secondary key.
     */
    String getSecondaryKey();
}
