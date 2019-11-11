/*
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityActionOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;
import com.microsoft.windowsazure.services.media.entityoperations.EntityUpdateOperation;
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;
import com.sun.jersey.api.client.GenericType;

public final class AssetFile {
    private static final String ENTITY_SET = "Files";

    // Prevent instantiation
    private AssetFile() {
    }

    public static Creator create(String parentAssetId, String name) {
        return new Creator(parentAssetId, name);
    }

    public static final class Creator extends
            EntityOperationSingleResultBase<AssetFileInfo> implements
            EntityCreateOperation<AssetFileInfo> {
        private final String parentAssetId;
        private final String name;
        private String contentChecksum;
        private Long contentFileSize;
        private String encryptionKeyId;
        private String encryptionScheme;
        private String encryptionVersion;
        private String initializationVector;
        private Boolean isEncrypted;
        private Boolean isPrimary;
        private String mimeType;

        private Creator(String parentAssetId, String name) {
            super(ENTITY_SET, AssetFileInfo.class);
            this.parentAssetId = parentAssetId;
            this.name = name;
        }

        @Override
        public Object getRequestContents() throws ServiceException {
            AssetFileType content = new AssetFileType().setName(name)
                    .setParentAssetId(parentAssetId)
                    .setContentChecksum(contentChecksum)
                    .setContentFileSize(contentFileSize)
                    .setEncryptionKeyId(encryptionKeyId)
                    .setEncryptionScheme(encryptionScheme)
                    .setEncryptionVersion(encryptionVersion)
                    .setInitializationVector(initializationVector)
                    .setIsEncrypted(isEncrypted).setIsPrimary(isPrimary)
                    .setMimeType(mimeType);

            return content;
        }

        /**
         * @param contentChecksum
         *            the contentChecksum to set
         */
        public Creator setContentChecksum(String contentChecksum) {
            this.contentChecksum = contentChecksum;
            return this;
        }

        /**
         * @param contentFileSize
         *            the contentFileSize to set
         */
        public Creator setContentFileSize(Long contentFileSize) {
            this.contentFileSize = contentFileSize;
            return this;
        }

        /**
         * @param encryptionKeyId
         *            the encryptionKeyId to set
         */
        public Creator setEncryptionKeyId(String encryptionKeyId) {
            this.encryptionKeyId = encryptionKeyId;
            return this;
        }

        /**
         * @param encryptionScheme
         *            the encryptionScheme to set
         */
        public Creator setEncryptionScheme(String encryptionScheme) {
            this.encryptionScheme = encryptionScheme;
            return this;
        }

        /**
         * @param encryptionVersion
         *            the encryptionVersion to set
         */
        public Creator setEncryptionVersion(String encryptionVersion) {
            this.encryptionVersion = encryptionVersion;
            return this;
        }

        /**
         * @param initializationVector
         *            the initializationVector to set
         */
        public Creator setInitializationVector(String initializationVector) {
            this.initializationVector = initializationVector;
            return this;
        }

        /**
         * @param isEncrypted
         *            the isEncrypted to set
         */
        public Creator setIsEncrypted(Boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
            return this;
        }

        /**
         * @param isPrimary
         *            the isPrimary to set
         */
        public Creator setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        /**
         * @param mimeType
         *            the mimeType to set
         */
        public Creator setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

    }

    /**
     * Call the CreateFileInfos action on the server for the given asset
     * 
     * @param assetId
     *            asset to create file infos for
     * @return The action operation object to pass to rest proxy.
     * @throws UnsupportedEncodingException
     */
    public static EntityActionOperation createFileInfos(String assetId) {
        String encodedId;
        try {
            encodedId = URLEncoder.encode(assetId, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // This can never happen unless JVM is broken
            throw new RuntimeException(ex);
        }
        return new DefaultActionOperation("CreateFileInfos").addQueryParameter(
                "assetid", "'" + encodedId + "'");
    }

    /**
     * Call the service to get a single asset file entity
     * 
     * @param assetFileId
     *            id of file to get
     * @return the get operation to pass to rest proxy
     */
    public static EntityGetOperation<AssetFileInfo> get(String assetFileId) {
        return new DefaultGetOperation<AssetFileInfo>(ENTITY_SET, assetFileId,
                AssetFileInfo.class);
    }

    /**
     * Calls the service to list all files
     * 
     * @return The list operation to pass to rest proxy.
     */
    public static DefaultListOperation<AssetFileInfo> list() {
        return new DefaultListOperation<AssetFileInfo>(ENTITY_SET,
                new GenericType<ListResult<AssetFileInfo>>() {
                });
    }

    /**
     * Create an operation that will list all the AssetFiles at the given link.
     * 
     * @param link
     *            Link to request AssetFiles from.
     * @return The list operation.
     */
    public static DefaultListOperation<AssetFileInfo> list(
            LinkInfo<AssetFileInfo> link) {
        return new DefaultListOperation<AssetFileInfo>(link.getHref(),
                new GenericType<ListResult<AssetFileInfo>>() {
                });
    }

    public static Updater update(String assetFileId) {
        return new Updater(assetFileId);
    }

    public static final class Updater extends EntityOperationBase implements
            EntityUpdateOperation {
        private String contentChecksum;
        private Long contentFileSize;
        private String encryptionKeyId;
        private String encryptionScheme;
        private String encryptionVersion;
        private String initializationVector;
        private Boolean isEncrypted;
        private Boolean isPrimary;
        private String mimeType;

        private Updater(String assetFileId) {
            super(new EntityOperationBase.EntityIdUriBuilder(ENTITY_SET,
                    assetFileId));
        }

        @Override
        public Object getRequestContents() {
            return new AssetFileType().setContentChecksum(contentChecksum)
                    .setContentFileSize(contentFileSize)
                    .setEncryptionKeyId(encryptionKeyId)
                    .setEncryptionScheme(encryptionScheme)
                    .setEncryptionVersion(encryptionVersion)
                    .setInitializationVector(initializationVector)
                    .setIsEncrypted(isEncrypted).setIsPrimary(isPrimary)
                    .setMimeType(mimeType);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.microsoft.windowsazure.services.media.entityoperations.
         * EntityOperation
         * #setProxyData(com.microsoft.windowsazure.services.media
         * .entityoperations.EntityProxyData)
         */
        @Override
        public void setProxyData(EntityProxyData proxyData) {
            // Deliberately empty
        }

        /**
         * @param contentChecksum
         *            the contentChecksum to set
         */
        public Updater setContentChecksum(String contentChecksum) {
            this.contentChecksum = contentChecksum;
            return this;
        }

        /**
         * @param contentFileSize
         *            the contentFileSize to set
         */
        public Updater setContentFileSize(Long contentFileSize) {
            this.contentFileSize = contentFileSize;
            return this;
        }

        /**
         * @param encryptionKeyId
         *            the encryptionKeyId to set
         */
        public Updater setEncryptionKeyId(String encryptionKeyId) {
            this.encryptionKeyId = encryptionKeyId;
            return this;
        }

        /**
         * @param encryptionScheme
         *            the encryptionScheme to set
         */
        public Updater setEncryptionScheme(String encryptionScheme) {
            this.encryptionScheme = encryptionScheme;
            return this;
        }

        /**
         * @param encryptionVersion
         *            the encryptionVersion to set
         */
        public Updater setEncryptionVersion(String encryptionVersion) {
            this.encryptionVersion = encryptionVersion;
            return this;
        }

        /**
         * @param initializationVector
         *            the initializationVector to set
         */
        public Updater setInitializationVector(String initializationVector) {
            this.initializationVector = initializationVector;
            return this;
        }

        /**
         * @param isEncrypted
         *            the isEncrypted to set
         */
        public Updater setIsEncrypted(Boolean isEncrypted) {
            this.isEncrypted = isEncrypted;
            return this;
        }

        /**
         * @param isPrimary
         *            the isPrimary to set
         */
        public Updater setIsPrimary(Boolean isPrimary) {
            this.isPrimary = isPrimary;
            return this;
        }

        /**
         * @param mimeType
         *            the mimeType to set
         */
        public Updater setMimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

    }

    /**
     * Calls the service to delete an asset file entity
     * 
     * @param assetFileId
     *            file to delete
     * @return the delete operation object
     */
    public static EntityDeleteOperation delete(String assetFileId) {
        return new DefaultDeleteOperation(ENTITY_SET, assetFileId);
    }
}
