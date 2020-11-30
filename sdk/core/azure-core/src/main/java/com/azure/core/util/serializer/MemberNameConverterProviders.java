// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class is a proxy for using a {@link MemberNameConverterProvider} loaded from the classpath.
 */
public final class MemberNameConverterProviders {
    private static final String CANNOT_FIND_MEMBER_NAME_CONVERTER_PROVIDER =
        "Cannot find any member name converter provider on the classpath.";

    private static MemberNameConverterProvider defaultProvider;
    private static boolean attemptedLoad;

    /**
     * Creates an instance of {@link MemberNameConverter} using the first {@link MemberNameConverterProvider} found in
     * the classpath.
     *
     * @return A new instance of {@link MemberNameConverter}.
     */
    public static MemberNameConverter createInstance() {
        if (defaultProvider == null) {
            loadFromClasspath();
        }

        return defaultProvider.createInstance();
    }

    private static synchronized void loadFromClasspath() {
        if (attemptedLoad && defaultProvider != null) {
            return;
        } else if (attemptedLoad) {
            throw new IllegalStateException(CANNOT_FIND_MEMBER_NAME_CONVERTER_PROVIDER);
        }

        attemptedLoad = true;
        Iterator<MemberNameConverterProvider> iterator =
            ServiceLoader.load(MemberNameConverterProvider.class).iterator();
        if (iterator.hasNext()) {
            defaultProvider = iterator.next();
        } else {
            throw new IllegalStateException(CANNOT_FIND_MEMBER_NAME_CONVERTER_PROVIDER);
        }
    }

    private MemberNameConverterProviders() {
        // no-op
    }
}
