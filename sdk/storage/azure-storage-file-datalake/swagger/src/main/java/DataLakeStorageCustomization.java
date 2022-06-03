// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File DataLake Storage.
 */
public class DataLakeStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.datalake.implementation.models");

        implementationModels.getClass("BlobHierarchyListSegment")
            .addAnnotation("@JsonDeserialize(using = com.azure.storage.file.datalake.implementation.util.CustomHierarchicalListingDeserializer.class)");

        implementationModels.getClass("FileSystemList")
            .getProperty("filesystems")
            .removeAnnotation("@JsonProperty")
            .addAnnotation("@JsonProperty(value = \"filesystems\")");

        implementationModels.getClass("PathList")
            .getProperty("paths")
            .removeAnnotation("@JsonProperty")
            .addAnnotation("@JsonProperty(value = \"paths\")");

        implementationModels.getClass("SetAccessControlRecursiveResponse")
            .getProperty("failedEntries")
            .removeAnnotation("@JsonProperty")
            .addAnnotation("@JsonProperty(\"failedEntries\")");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.datalake.models");

        models.getClass("PathExpiryOptions")
            .rename("PathExpiryMode");

    }
}
