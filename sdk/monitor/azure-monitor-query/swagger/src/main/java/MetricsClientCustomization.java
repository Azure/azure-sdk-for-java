// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.slf4j.Logger;

import java.util.function.Consumer;

/**
 * This customizes the generated Azure Metrics Batch client. The following changes are made by this customization:
 * <li>Update the scope of bearer token policy to use the default audience instead of the endpoint.</li>
 */
public class MetricsClientCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeAzureMonitorMetricBatchBuilder(libraryCustomization.getClass("com.azure.monitor.query.implementation.metricsbatch", "AzureMonitorMetricBatchBuilder"));
    }

    private void customizeAzureMonitorMetricBatchBuilder(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.monitor.query.models.MetricsAudience");

        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("MetricsAudience", "audience")
                .addAnnotation("Generated")
                .setJavadocComment("The audience indicating the authorization scope of metrics clients.")
                .createSetter()
                .setName("audience")
                .setType("AzureMonitorMetricBatchBuilder")
                .setBody(new BlockStmt()
                    .addStatement("this.audience = audience;")
                    .addStatement("return this;"))
                .addAnnotation("Generated")
                .setJavadocComment("Sets The audience.\n" +
                    "     *\n" +
                    "     * @param audience the audience indicating the authorization scope of metrics clients.\n" +
                    "     * @return the AzureMonitorMetricBatchBuilder.");
        });

        classCustomization.getMethod("createHttpPipeline")
            .replaceBody("Configuration buildConfiguration\n" +
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
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)\n" +
            "            .forEach(p -> policies.add(p));\n" +
            "        HttpPolicyProviders.addBeforeRetryPolicies(policies);\n" +
            "        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));\n" +
            "        policies.add(new AddDatePolicy());\n" +
            "        if (tokenCredential != null) {\n" +
            "            policies.add(\n" +
            "                new BearerTokenAuthenticationPolicy(tokenCredential,  audience == null ? MetricsAudience.AZURE_PUBLIC_CLOUD.toString() + \"/.default\" : audience.toString()  + \"/.default\"));\n" +
            "        }\n" +
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)\n" +
            "            .forEach(p -> policies.add(p));\n" +
            "        HttpPolicyProviders.addAfterRetryPolicies(policies);\n" +
            "        policies.add(new HttpLoggingPolicy(localHttpLogOptions));\n" +
            "        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))\n" +
            "            .httpClient(httpClient).clientOptions(localClientOptions).build();\n" +
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
