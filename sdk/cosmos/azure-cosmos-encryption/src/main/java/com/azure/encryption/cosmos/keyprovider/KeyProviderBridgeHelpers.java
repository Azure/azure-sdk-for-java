// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.encryption.cosmos.keyprovider;

import com.azure.cosmos.implementation.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.implementation.Warning.INTERNAL_USE_ONLY_WARNING;

/**
 * DO NOT USE.
 * This is meant to be used only internally as a bridge access to classes in
 * com.azure.encryption.cosmos.keyprovider
 **/
@Warning(value = INTERNAL_USE_ONLY_WARNING)
public class KeyProviderBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(KeyProviderBridgeHelpers.class);
    public static final class EncryptionKeyWrapProviderHelper {
        static {
            ensureClassLoaded(EncryptionKeyWrapProvider.class);
        }
        private static EncryptionKeyWrapProviderAccessor accessor;

        private EncryptionKeyWrapProviderHelper() {}

        public static void setPEncryptionKeyWrapProviderAccessor(final EncryptionKeyWrapProviderAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException("EncryptionKeyWrapProvider accessor already initialized!");
            }

            accessor = newAccessor;
        }

        public static EncryptionKeyWrapProviderAccessor getEncryptionKeyWrapProviderAccessor() {
            if (accessor == null) {
                throw new IllegalStateException("EncryptionKeyWrapProvider accessor is not initialized!");
            }

            return accessor;
        }

        public interface EncryptionKeyWrapProviderAccessor {
            EncryptionKeyStoreProviderImpl getEncryptionKeyStoreProviderImpl(EncryptionKeyWrapProvider encryptionKeyWrapProvider);
        }
    }

    private static <T> void ensureClassLoaded(Class<T> classType) {
        try {
            // ensures the class is loaded
            Class.forName(classType.getName());
        } catch (ClassNotFoundException e) {
            logger.error("cannot load class {}", classType.getName());
            throw new RuntimeException(e);
        }
    }
}
