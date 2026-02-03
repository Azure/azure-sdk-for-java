// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class MetricsNamespacesCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        String replace = libraryCustomization.getRawEditor().getFileContent("src/main/java/com/azure/monitor/query/implementation" +
                        "/metricsnamespaces/MonitorManagementClientBuilder.java")
                .replace("policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/" +
                                ".default\", host)));",
                        "String localHost = (host != null) ? host : \"https://management.azure.com\";\n" +
                                "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", localHost)));");
        libraryCustomization.getRawEditor().replaceFile("src/main/java/com/azure/monitor/query/implementation" +
                "/metricsnamespaces/MonitorManagementClientBuilder.java", replace);
    }
}
