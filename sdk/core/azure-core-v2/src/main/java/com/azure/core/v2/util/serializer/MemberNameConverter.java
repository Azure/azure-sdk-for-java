// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.util.serializer;

import java.lang.reflect.Member;

/**
 * Generic interface that attempts to retrieve the JSON serialized property name from {@link Member}.
 */
public interface MemberNameConverter {
    /**
     * Attempts to get the JSON serialized property name from the passed {@link Member}.
     * <p>
     * If a {@link java.lang.reflect.Constructor} or {@link java.lang.reflect.Executable} is passed {@code null} will be
     * returned.
     *
     * @param member The {@link Member} that will have its JSON serialized property name retrieved.
     * @return The JSON property name for the {@link Member}.
     */
    String convertMemberName(Member member);
}
