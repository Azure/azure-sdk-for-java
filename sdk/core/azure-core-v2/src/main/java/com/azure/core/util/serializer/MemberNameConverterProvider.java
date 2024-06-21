// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Interface to be implemented by an azure-core plugin that wishes to provide a {@link MemberNameConverter}
 * implementation.
 */
public interface MemberNameConverterProvider {

    /**
     * Creates a new instance of the {@link MemberNameConverter} that this MemberNameConverterProvider is configured to
     * create.
     *
     * @return A new {@link MemberNameConverter} instance.
     */
    MemberNameConverter createInstance();
}
