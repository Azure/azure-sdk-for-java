// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.import com.azure.autorest.customization.ClassCustomization;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the TypeSpec generated code for Azure AI Image Analysis.
 */
public class LogsQueryCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.monitor.query.logs.implementation", "MonitorQueryLogsClientBuilder")
            .customizeAst(ast -> ast.getClassByName("MonitorQueryLogsClientBuilder").ifPresent(clazz ->
                clazz.getMethodsByName("createHttpPipeline").forEach(method -> method.getBody().ifPresent(body -> {
                    String target = "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES));";
                    String replacement = "String localEndpoint = (endpoint != null) ? endpoint : \"https://api.loganalytics.io\";\n" +
                        "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, String.format(\"%s/.default\", localEndpoint)));";
                    method.setBody(StaticJavaParser.parseBlock(body.toString().replace(target, replacement)));
                }))));

        String original = customization.getRawEditor().getFileContent("src/main/java/module-info.java");
        String replace = original.replace("exports com.azure.monitor.query.logs.implementation;", "exports com.azure.monitor.query.logs.models;");

        customization.getRawEditor().replaceFile("src/main/java/module-info.java", replace);

    }
}
