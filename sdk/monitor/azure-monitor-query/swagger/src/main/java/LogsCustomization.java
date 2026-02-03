// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

public class LogsCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getClass("com.azure.monitor.query.implementation.logs", "AzureLogAnalyticsBuilder")
            .customizeAst(ast -> ast.getClassByName("AzureLogAnalyticsBuilder").ifPresent(clazz ->
                clazz.getMethodsByName("createHttpPipeline").forEach(method -> method.getBody().ifPresent(body -> {
                    String target = "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", host)));";
                    String replacement = "String localHost;if (host != null) {try {localHost = new java.net.URL(host).getHost();} "
                        + "catch (java.net.MalformedURLException e) {throw new RuntimeException(e);}} else {"
                        + "localHost = \"api.loganalytics.io\";}policies.add(new BearerTokenAuthenticationPolicy("
                        + "tokenCredential,String.format(\"https://%s/.default\", localHost)));";
                    method.setBody(StaticJavaParser.parseBlock(body.toString().replace(target, replacement)));
                }))));
    }
}
