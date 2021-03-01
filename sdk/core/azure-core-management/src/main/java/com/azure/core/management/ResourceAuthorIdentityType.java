// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.Collection;

/** Defines values for identity type of resource author. */
public final class ResourceAuthorIdentityType extends ExpandableStringEnum<ResourceAuthorIdentityType> {

    /** Static value User for ResourceAuthorIdentityType, represents an AAD user. */
    public static final ResourceAuthorIdentityType USER = fromString("User");

    /** Static value Application for ResourceAuthorIdentityType, represents an AAD application. */
    public static final ResourceAuthorIdentityType APPLICATION = fromString("Application");

    /** Static value ManagedIdentity for ResourceAuthorIdentityType, represents a Managed Identity. */
    public static final ResourceAuthorIdentityType MANAGED_IDENTITY = fromString("ManagedIdentity");

    /** Static value Key for ResourceAuthorIdentityType. */
    public static final ResourceAuthorIdentityType KEY = fromString("Key");

    /**
     * Creates or finds a ResourceAuthorIdentityType from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ResourceAuthorIdentityType.
     */
    @JsonCreator
    public static ResourceAuthorIdentityType fromString(String name) {
        return fromString(name, ResourceAuthorIdentityType.class);
    }

    /** @return known ResourceAuthorIdentityType values. */
    public static Collection<ResourceAuthorIdentityType> values() {
        return values(ResourceAuthorIdentityType.class);
    }
}
