// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.EncryptionKeyWrapMetadata;

public class ImplementationBridgeHelpers {
    public abstract static class EncryptionKeyWrapMetadataHelper {
        private static EncryptionKeyWrapMetadataAccessor accessor;

        protected EncryptionKeyWrapMetadataHelper() {
        }

        public static void setEncryptionKeyWrapMetadataAccessor(final EncryptionKeyWrapMetadataAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException();
            }

            accessor = newAccessor;
        }

        public static EncryptionKeyWrapMetadataAccessor getEncryptionKeyWrapMetadataAccessor() {
            if (accessor == null) {
                throw new IllegalStateException();
            }

            return accessor;
        }

        public static String getType(EncryptionKeyWrapMetadata metadata) {
            return accessor.getType(metadata);
        }

        public static String getAlgorithm(EncryptionKeyWrapMetadata metadata) {
            return accessor.getAlgorithm(metadata);
        }

        public static EncryptionKeyWrapMetadata create(String type, String value, String algorithm) {
            return accessor.create(type, value, algorithm);
        }

        public static EncryptionKeyWrapMetadata create(String type, String value) {
            return accessor.create(type, value);
        }

        public interface EncryptionKeyWrapMetadataAccessor {

            EncryptionKeyWrapMetadata create(String type, String value, String algorithm);

            EncryptionKeyWrapMetadata create(String type, String value);

            String getType(EncryptionKeyWrapMetadata metadata);

            String getAlgorithm(EncryptionKeyWrapMetadata metadata);
        }
    }

    public abstract static class AzureKeyVaultKeyWrapMetadataHelper {
        private static AzureKeyVaultKeyWrapMetadataAccessor accessor;

        protected AzureKeyVaultKeyWrapMetadataHelper() {
        }

        public static void setAzureKeyVaultKeyWrapMetadataAccessor(final AzureKeyVaultKeyWrapMetadataAccessor newAccessor) {
            if (accessor != null) {
                throw new IllegalStateException();
            }

            accessor = newAccessor;
        }

        public static AzureKeyVaultKeyWrapMetadataAccessor getAzureKeyVaultKeyWrapMetadataAccessor() {
            if (accessor == null) {
                throw new IllegalStateException();
            }

            return accessor;
        }

        public static String getTypeConstant() {
            return accessor.getTypeConstant();
        }

        public interface AzureKeyVaultKeyWrapMetadataAccessor {

            String getTypeConstant();

        }
    }
}
