// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import java.util.List;

/**
 * This class contains values which correlate to the access polices set on a specific file system.
 */
public class FileSystemAccessPolicies {
    private final PublicAccessType dataLakeAccessType;
    private final List<DataLakeSignedIdentifier> identifiers;

    /**
     * Constructs a {@link FileSystemAccessPolicies}.
     *
     * @param dataLakeAccessType Level of public access the file system allows.
     * @param identifiers {@link DataLakeSignedIdentifier DataLakeSignedIdentifiers} associated with the file system.
     */
    public FileSystemAccessPolicies(PublicAccessType dataLakeAccessType, List<DataLakeSignedIdentifier> identifiers) {
        this.dataLakeAccessType = dataLakeAccessType;
        this.identifiers = identifiers;
    }

    /**
     * @return the level of public access the file system allows.
     */
    public PublicAccessType getDataLakeAccessType() {
        return dataLakeAccessType;
    }

    /**
     * @return the {@link DataLakeSignedIdentifier DataLakeSignedIdentifiers} associated with the file system.
     */
    public List<DataLakeSignedIdentifier> getIdentifiers() {
        return this.identifiers;
    }
}
