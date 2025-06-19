// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * Customization class for Monitor. These customizations will be applied on top of the generated code.
 */
public class MonitorIngestionCustomizations extends Customization {

    /**
     * Customizes the generated code.
     *
     * <br/>
     *
     * The following customizations are applied:
     *
     * <ol>
     *     <li>The package customization for the package `com.azure.monitor.ingestion.implementation`.</li>
     * </ol>
     *
     * @param libraryCustomization The library customization.
     * @param logger The logger.
     */
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        monitorIngestionImplementation(libraryCustomization.getPackage("com.azure.monitor.ingestion.implementation"), logger);
    }

    /**
     * Customizes the generated code for the package com.azure.monitor.ingestion.implementation.
     *
     * <br/>
     *
     * The following classes are customized:
     * <ol>
     *     <li>IngestionUsingDataCollectionRulesClientBuilder</li>
     * </ol>
     *
     * @param packageCustomization The package customization.
     * @param logger The logger.
     */
    private void monitorIngestionImplementation(PackageCustomization packageCustomization, Logger logger) {
        IngestionUsingDataCollectionRulesClientBuilderCustomization(packageCustomization.getClass("IngestionUsingDataCollectionRulesClientBuilder"), logger);
    }

    /**
     * Customizes the generated code for `IngestionUsingDataCollectionRulesClientBuilder`.
     *
     * <br/>
     *
     * The following customizations are applied:
     *
     * <ol>
     *     <li>Adds an import statement for the class `LogsIngestionAudience`.</li>
     *     <li>Adds a field `audience` of type `LogsIngestionAudience` to the class.</li>
     *     <li>Adds a Javadoc for the field `audience`.</li>
     *     <li>Adds the generated annotation to the field `audience`.</li>
     *     <li>Adds a setter for the field `audience`.</li>
     *     <li>Adds a Javadoc for the setter.</li>
     *     <li>Adds the generated annotation to the setter.</li>
     *     <li>Replaces the body of the method `createHttpPipeline()` with a custom implementation that sets the
     *     audience in the `BearerTokenAuthenticationPolicy`.</li>
     * </ol>
     *
     * @param classCustomization The class customization.
     * @param logger The logger.
     */
    private void IngestionUsingDataCollectionRulesClientBuilderCustomization(ClassCustomization classCustomization, Logger logger) {
        classCustomization.addImports("com.azure.monitor.ingestion.models.LogsIngestionAudience");



        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("LogsIngestionAudience", "audience")
                .addAnnotation("Generated")
                .setJavadocComment("The audience indicating the authorization scope of log ingestion clients.")
                .createSetter()
                .setName("audience")
                .setType("IngestionUsingDataCollectionRulesClientBuilder")
                .setBody(new BlockStmt()
                    .addStatement("this.audience = audience;")
                    .addStatement("return this;"))
                .addAnnotation("Generated")
                .setJavadocComment("Sets The audience.\n" +
                    "     *\n" +
                    "     * @param audience the audience indicating the authorization scope of log ingestion clients.\n" +
                    "     * @return the IngestionUsingDataCollectionRulesClientBuilder.");
        });


        classCustomization.getMethod("createHttpPipeline").replaceBody("Configuration buildConfiguration\n" +
            "            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;\n" +
            "        HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;\n" +
            "        ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;\n" +
            "        List<HttpPipelinePolicy> policies = new ArrayList<>();\n" +
            "        String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");\n" +
            "        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");\n" +
            "        String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);\n" +
            "        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));\n" +
            "        policies.add(new RequestIdPolicy());\n" +
            "        policies.add(new AddHeadersFromContextPolicy());\n" +
            "        HttpHeaders headers = new HttpHeaders();\n" +
            "        localClientOptions.getHeaders()\n" +
            "            .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));\n" +
            "        if (headers.getSize() > 0) {\n" +
            "            policies.add(new AddHeadersPolicy(headers));\n" +
            "        }\n" +
            "        this.pipelinePolicies.stream()\n" +
            "            .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)\n" +
            "            .forEach(p -> policies.add(p));\n" +
            "        HttpPolicyProviders.addBeforeRetryPolicies(policies);\n" +
            "        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));\n" +
            "        policies.add(new AddDatePolicy());\n" +
            "        if (tokenCredential != null) {\n" +
            "            policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, audience == null ? DEFAULT_SCOPES : new String[] { audience.toString() }));\n" +
            "        }\n" +
            "        this.pipelinePolicies.stream()\n" +
            "            .filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)\n" +
            "            .forEach(p -> policies.add(p));\n" +
            "        HttpPolicyProviders.addAfterRetryPolicies(policies);\n" +
            "        policies.add(new HttpLoggingPolicy(localHttpLogOptions));\n" +
            "        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))\n" +
            "            .httpClient(httpClient)\n" +
            "            .clientOptions(localClientOptions)\n" +
            "            .build();\n" +
            "        return httpPipeline;");

    }


    /**
     * Customizes the abstract syntax tree of a class.
     * @param classCustomization The class customization.
     * @param consumer The consumer.
     */
    private static void customizeAst(ClassCustomization classCustomization, Consumer<ClassOrInterfaceDeclaration> consumer) {
        classCustomization.customizeAst(ast -> consumer.accept(ast.getClassByName(classCustomization.getClassName())
            .orElseThrow(() -> new RuntimeException("Class not found. " + classCustomization.getClassName()))));
    }
}
