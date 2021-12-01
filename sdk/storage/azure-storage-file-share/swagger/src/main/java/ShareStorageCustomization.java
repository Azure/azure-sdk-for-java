// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.autorest.customization.PropertyCustomization;
import org.slf4j.Logger;

/**
 * Customization class for File Share Storage.
 */
public class ShareStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        // Add these annotations since the default deserializer does not handle these cases correctly.
        PackageCustomization implementationModels = customization.getPackage("com.azure.storage.file.share.implementation.models");
        implementationModels.getClass("FilesAndDirectoriesListSegment").addAnnotation("@JsonDeserialize(using = com.azure.storage.file.share.implementation.util.FilesAndDirectoriesListSegmentDeserializer.class)");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");
        models.getClass("ShareFileRangeList").addAnnotation("@JsonDeserialize(using = ShareFileRangeListDeserializer.class)");

        // Update incorrect JsonProperty of Metrics
        ClassCustomization shareServiceProperties = models.getClass("ShareServiceProperties");
        shareServiceProperties.getProperty("hourMetrics").removeAnnotation("@JsonProperty")
            .addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        shareServiceProperties.getProperty("minuteMetrics").removeAnnotation("@JsonProperty")
            .addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");
    }
}
