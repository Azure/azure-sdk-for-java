// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

/**
 * Customization class for Queue Storage.
 */
public class QueueStorageCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.storage.queue.models");

        // Changes to JacksonXmlRootElement for classes that have been renamed.
        models.getClass("QueueAnalyticsLogging").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"Logging\")");

        models.getClass("QueueMetrics").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"Metrics\")");

        models.getClass("QueueRetentionPolicy").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"RetentionPolicy\")");

        models.getClass("QueueServiceStatistics").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"StorageServiceStats\")");

        models.getClass("QueueSignedIdentifier").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"SignedIdentifier\")");

        models.getClass("QueueAccessPolicy").removeAnnotation("@JacksonXmlRootElement")
            .addAnnotation("@JacksonXmlRootElement(localName = \"AccessPolicy\")");
    }
}
