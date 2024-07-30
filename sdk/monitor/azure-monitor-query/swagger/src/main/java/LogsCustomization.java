// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import org.slf4j.Logger;

public class LogsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        ClassCustomization azureLogAnalytics = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "AzureLogAnalytics");
        azureLogAnalytics.rename("AzureLogAnalyticsImpl");

        ClassCustomization metadatas = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "Metadatas");
        metadatas.rename("MetadatasImpl");

        ClassCustomization queries = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "Queries");
        queries.rename("QueriesImpl");

        ClassCustomization azureLogAnalyticsBuilder = libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "AzureLogAnalyticsBuilder");
        azureLogAnalyticsBuilder.rename("AzureLogAnalyticsImplBuilder");
        String replace = libraryCustomization.getRawEditor().getFileContent("src/main/java/com/azure/monitor/query/implementation" +
                        "/logs/AzureLogAnalyticsImplBuilder.java")
                .replace("policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/" +
                                ".default\", host)));",
                    "String localHost;\n" +
                        "        if (host != null) {\n" +
                        "            try {\n" +
                        "                localHost = new java.net.URL(host).getHost();\n" +
                        "            } catch (java.net.MalformedURLException e) {\n" +
                        "                throw new RuntimeException(e);\n" +
                        "            }\n" +
                        "        } else {\n" +
                        "            localHost = \"api.loganalytics.io\";\n" +
                        "        }\n" +
                        "        policies.add(new BearerTokenAuthenticationPolicy(\n" +
                        "            tokenCredential,\n" +
                        "            String.format(\"https://%s/.default\", localHost)));");
        libraryCustomization.getRawEditor().replaceFile("src/main/java/com/azure/monitor/query/implementation" +
                "/logs/AzureLogAnalyticsImplBuilder.java", replace);
    }
}
