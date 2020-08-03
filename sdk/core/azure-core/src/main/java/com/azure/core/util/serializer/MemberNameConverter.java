// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.lang.reflect.Member;

/**
 * Generic interface that attempts to retrieve the JSON serialized property name from {@link Member}.
 */
public interface MemberNameConverter {
    /**
     * Get property member name from the class field.
     *
     * @param member Gets property name from the class member.
     * @return The serializer member name.
     */
    String convertMemberName(Member member);
}
