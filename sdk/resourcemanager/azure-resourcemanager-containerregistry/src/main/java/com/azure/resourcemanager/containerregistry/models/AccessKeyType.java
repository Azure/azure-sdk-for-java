// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerregistry.models;

import com.azure.core.util.ExpandableStringEnum;
import java.util.Collection;

/** Defines values for admin user access key names. */
public class AccessKeyType extends ExpandableStringEnum<AccessKeyType> {
    /** Primary key. */
    public static final AccessKeyType PRIMARY = fromString(PasswordName.PASSWORD.toString());

    /** Secondary key. */
    public static final AccessKeyType SECONDARY = fromString(PasswordName.PASSWORD2.toString());

    /**
     * Finds or creates an access key name based on the provided name.
     *
     * @param name a name
     * @return a AccessKeyType instance
     */
    public static AccessKeyType fromString(String name) {
        return fromString(name, AccessKeyType.class);
    }

    /** @return known admin user access key names */
    public static Collection<AccessKeyType> values() {
        return values(AccessKeyType.class);
    }
}
