/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for admin user access key names.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_3_0)
public class AccessKeyName extends ExpandableStringEnum<AccessKeyName> {
    /** Static value 'password'. */
    public static final AccessKeyName PRIMARY_KEY = fromString(PasswordName.PASSWORD.toString());

    /** Static value 'password2'. */
    public static final AccessKeyName SECONDARY_KEY = fromString(PasswordName.PASSWORD2.toString());

    /**
     * Finds or creates an access key name based on the provided name.
     * @param name a name
     * @return a AccessKeyName instance
     */
    public static AccessKeyName fromString(String name) {
        return fromString(name, AccessKeyName.class);
    }

    /**
     * @return known admin user access key names
     */
    public static Collection<AccessKeyName> values() {
        return values(AccessKeyName.class);
    }
}
