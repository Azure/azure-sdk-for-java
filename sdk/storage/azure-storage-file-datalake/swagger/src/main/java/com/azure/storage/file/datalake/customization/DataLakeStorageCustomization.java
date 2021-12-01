// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.customization;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File DataLake Storage.
 */
public class DataLakeStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getPackage("com.azure.storage.file.datalake.implementation.models")
            .getClass("BlobHierarchyListSegment")
            .addAnnotation("@JsonDeserialize(using = com.azure.storage.file.datalake.implementation.util.CustomHierarchicalListingDeserializer.class)");
    }
}
