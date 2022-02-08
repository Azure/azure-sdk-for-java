// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.keyprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdeSupportBridgeHelpers {
    private final static Logger logger = LoggerFactory.getLogger(MdeSupportBridgeHelpers.class);
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
