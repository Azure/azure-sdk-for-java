// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

import java.lang.reflect.Member;

/**
 * Generic interface to get the property name through serialization.
 */
public interface PropertyNameSerializer {
    /**
     * Get property member name from the class field.
     *
     * @param member Gets property name from the class member.
     * @return The serializer member name.
     */
    String getSerializerMemberName(Member member);
}
