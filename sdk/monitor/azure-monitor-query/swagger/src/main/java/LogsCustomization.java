// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

public class LogsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization packageCustomization = libraryCustomization.getPackage("com.azure.monitor.query.implementation.logs");
        packageCustomization.getClass("AzureLogAnalytics").rename("AzureLogAnalyticsImpl");
        packageCustomization.getClass("Metadatas").rename("MetadatasImpl");
        packageCustomization.getClass("Queries").rename("QueriesImpl");

        packageCustomization.getClass("AzureLogAnalyticsBuilder").rename("AzureLogAnalyticsImplBuilder")
            .customizeAst(ast -> ast.getClassByName("AzureLogAnalyticsImplBuilder").ifPresent(clazz -> {
                MethodDeclaration createHttpPipeline = clazz.getMethodsByName("createHttpPipeline").get(0);
                String target = "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", host)));";
                String replacement = "String localHost;if (host != null) {try {localHost = new java.net.URL(host).getHost();} "
                    + "catch (java.net.MalformedURLException e) {throw new RuntimeException(e);}} else {"
                    + "localHost = \"api.loganalytics.io\";}policies.add(new BearerTokenAuthenticationPolicy("
                    + "tokenCredential,String.format(\"https://%s/.default\", localHost)));";
                createHttpPipeline.setBody(StaticJavaParser.parseBlock(createHttpPipeline.getBody().get().toString()
                    .replace(target, replacement)));
            }));
    }
}
