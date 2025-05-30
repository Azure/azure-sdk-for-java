// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

public class MetricsCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getClass("com.azure.monitor.query.implementation.metrics", "AzureMonitorMetricsDataAPIBuilder")
            .customizeAst(ast -> ast.getClassByName("AzureMonitorMetricsDataAPIBuilder").ifPresent(clazz -> {
                // Update createHttpPipeline to better handle Managed Identity.
                clazz.getMethodsByName("createHttpPipeline").forEach(method -> method.getBody().ifPresent(body -> {
                    String bodyStr = body.toString().replace(
                        "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", host)));",
                        "String localHost = (host != null) ? host : \"https://management.azure.com\";"
                            + "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", localHost)));");
                    method.setBody(StaticJavaParser.parseBlock(bodyStr));
                }));

                // Update validateClient to not validate subscriptionId was set as there isn't a way to do so using the
                // public builder.
                clazz.getMethodsByName("validateClient").forEach(method -> method.getBody().ifPresent(body -> {
                    String bodyStr = body.toString().replace(
                        "Objects.requireNonNull(subscriptionId, \"'subscriptionId' cannot be null.\");", "");
                    method.setBody(StaticJavaParser.parseBlock(bodyStr));
                }));
            }));
    }

}
