// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

/**
 * This customizes the generated Azure Metrics Batch client. The following changes are made by this customization:
 * <li>Update the scope of bearer token policy to use the default audience instead of the endpoint.</li>
 */
public class MetricsClientCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {

        String updatedFileContents = libraryCustomization
            .getRawEditor()
            .getFileContent("src/main/java/com/azure/monitor/query/implementation/metricsbatch/AzureMonitorMetricBatchBuilder.java")
            .replace("policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/" +
                    ".default\", endpoint)));",
                    "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, \"https://metrics.monitor.azure.com/.default\"));");

        libraryCustomization
            .getRawEditor()
            .replaceFile("src/main/java/com/azure/monitor/query/implementation/metricsbatch/AzureMonitorMetricBatchBuilder.java", updatedFileContents);
    }
}
