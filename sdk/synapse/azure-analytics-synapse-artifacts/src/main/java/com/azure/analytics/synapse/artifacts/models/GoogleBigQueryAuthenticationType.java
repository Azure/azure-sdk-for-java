// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.synapse.artifacts.models;

import com.azure.core.annotation.Generated;
import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/**
 * The OAuth 2.0 authentication mechanism used for authentication. ServiceAuthentication can only be used on self-hosted
 * IR.
 */
public final class GoogleBigQueryAuthenticationType extends ExpandableStringEnum<GoogleBigQueryAuthenticationType> {
    /**
     * Static value ServiceAuthentication for GoogleBigQueryAuthenticationType.
     */
    @Generated
    public static final GoogleBigQueryAuthenticationType SERVICE_AUTHENTICATION = fromString("ServiceAuthentication");

    /**
     * Static value UserAuthentication for GoogleBigQueryAuthenticationType.
     */
    @Generated
    public static final GoogleBigQueryAuthenticationType USER_AUTHENTICATION = fromString("UserAuthentication");

    /**
     * Creates a new instance of GoogleBigQueryAuthenticationType value.
     * 
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Generated
    @Deprecated
    public GoogleBigQueryAuthenticationType() {
    }

    /**
     * Creates or finds a GoogleBigQueryAuthenticationType from its string representation.
     * 
     * @param name a name to look for.
     * @return the corresponding GoogleBigQueryAuthenticationType.
     */
    @Generated
    public static GoogleBigQueryAuthenticationType fromString(String name) {
        return fromString(name, GoogleBigQueryAuthenticationType.class);
    }

    /**
     * Gets known GoogleBigQueryAuthenticationType values.
     * 
     * @return known GoogleBigQueryAuthenticationType values.
     */
    @Generated
    public static Collection<GoogleBigQueryAuthenticationType> values() {
        return values(GoogleBigQueryAuthenticationType.class);
    }
}
