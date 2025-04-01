// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.amqp.ClaimsBasedSecurityNode;
import io.clientcore.core.utils.ExpandableEnum;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An enumeration of supported authorization methods with the {@link ClaimsBasedSecurityNode}.
 */
public final class CbsAuthorizationType implements ExpandableEnum<String> {
    private static final Map<String, CbsAuthorizationType> VALUES = new ConcurrentHashMap<>();
    private final String type;

    /**
     * Creates an instance of the {@link CbsAuthorizationType} from a string.
     *
     * @param type The value to create the instance from.
     */
    private CbsAuthorizationType(String type) {
        this.type = type;
    }

    /**
     * Authorize with CBS through a shared access signature.
     */
    public static final CbsAuthorizationType SHARED_ACCESS_SIGNATURE
        = fromString("servicebus.windows.net:sastoken");

    /**
     * Authorize with CBS using a JSON web token.
     * <p>
     * This is used in the case where Azure Active Directory is used for authentication and the authenticated user
     * wants to authorize with Azure Event Hubs.
     */
    public static final CbsAuthorizationType JSON_WEB_TOKEN = fromString("jwt");

    /**
     * Creates or finds an CbsAuthorizationType from its string representation.
     *
     * @param type the type to look for
     * @return the corresponding CbsAuthorizationType
     */
    public static CbsAuthorizationType fromString(String type) {
        if (type == null) {
            return null;
        }
        return VALUES.computeIfAbsent(type, CbsAuthorizationType::new);
    }

    @Override
    public String getValue() {
        return this.type;
    }

    @Override
    public String toString() {
        return this.type;
    }
}
