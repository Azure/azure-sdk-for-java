// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class MetricsNamespacesCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization metricsNamespaces = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MetricNamespaces");
        metricsNamespaces.rename("MetricNamespacesImpl");

        ClassCustomization metricsNamespacesClient = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MonitorManagementClient");
        metricsNamespacesClient.rename("MetricsNamespacesClientImpl");

        ClassCustomization metricsNamespacesClientBuilder = libraryCustomization
                .getClass("com.azure.monitor.query.implementation.metricsnamespaces", "MonitorManagementClientBuilder");
        metricsNamespacesClientBuilder.rename("MetricsNamespacesClientImplBuilder");

        String replace = libraryCustomization.getRawEditor().getFileContent("src/main/java/com/azure/monitor/query/implementation" +
                        "/metricsnamespaces/MetricsNamespacesClientImplBuilder.java")
                .replace("policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/" +
                                ".default\", host)));",
                        "String localHost = (host != null) ? host : \"https://management.azure.com\";\n" +
                                "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", localHost)));");
        libraryCustomization.getRawEditor().replaceFile("src/main/java/com/azure/monitor/query/implementation" +
                "/metricsnamespaces/MetricsNamespacesClientImplBuilder.java", replace);
    }
}
