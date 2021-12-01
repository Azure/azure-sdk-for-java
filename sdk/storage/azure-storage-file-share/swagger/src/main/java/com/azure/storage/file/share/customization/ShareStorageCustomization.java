// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.customization;

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
        implementationModels.getClass("CopyFileSmbInfo")
            .removeAnnotation("@JacksonXmlRootElement(localName = \"CopyFileSmbInfo\")")
            .addAnnotation("@JacksonXmlRootElement(localName = \"copy-file-smb-info\")");

        PackageCustomization models = customization.getPackage("com.azure.storage.file.share.models");
        models.getClass("ShareFileRangeList").addAnnotation("@JsonDeserialize(using = ShareFileRangeListDeserializer.class)");

        // Replace JacksonXmlRootElement annotations that are causing a semantic breaking change.
        ClassCustomization shareFileHttpHeaders = models.getClass("ShareFileHttpHeaders");
        shareFileHttpHeaders.removeAnnotation("@JacksonXmlRootElement(localName = \"ShareFileHttpHeaders\")")
            .addAnnotation("@JacksonXmlRootElement(localName = \"share-file-http-headers\")");

        ClassCustomization sourceModifiedAccessConditions = models.getClass("SourceModifiedAccessConditions");
        sourceModifiedAccessConditions.removeAnnotation("@JacksonXmlRootElement(localName = \"SourceModifiedAccessConditions\")")
            .addAnnotation("@JacksonXmlRootElement(localName = \"source-modified-access-conditions\")");

        // Update incorrect JsonProperty of Metrics
        ClassCustomization shareServiceProperties = models.getClass("ShareServiceProperties");
        PropertyCustomization hourMetrics = shareServiceProperties.getProperty("hourMetrics");
        hourMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")")
            .addAnnotation("@JsonProperty(value = \"HourMetrics\")");
        PropertyCustomization minuteMetrics = shareServiceProperties.getProperty("minuteMetrics");
        minuteMetrics.removeAnnotation("@JsonProperty(value = \"Metrics\")")
            .addAnnotation("@JsonProperty(value = \"MinuteMetrics\")");

    }
}
