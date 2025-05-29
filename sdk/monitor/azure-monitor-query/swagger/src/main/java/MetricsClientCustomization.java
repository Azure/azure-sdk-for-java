// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

/**
 * This customizes the generated Azure Metrics Batch client. The following changes are made by this customization:
 * <li>Update the scope of bearer token policy to use the default audience instead of the endpoint.</li>
 */
public class MetricsClientCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        libraryCustomization.getClass("com.azure.monitor.query.implementation.metricsbatch", "AzureMonitorMetricBatchBuilder")
            .customizeAst(ast -> {
                ast.addImport("com.azure.monitor.query.models.MetricsAudience");

                ast.getClassByName("AzureMonitorMetricBatchBuilder").ifPresent(clazz -> {
                    clazz.addPrivateField("MetricsAudience", "audience")
                        .addMarkerAnnotation("Generated")
                        .setJavadocComment("The audience indicating the authorization scope of metrics clients.")
                        .createSetter()
                        .setName("audience")
                        .setType("AzureMonitorMetricBatchBuilder")
                        .setBody(new BlockStmt().addStatement("this.audience = audience;").addStatement("return this;"))
                        .addMarkerAnnotation("Generated")
                        .setJavadocComment(
                            new Javadoc(JavadocDescription.parseText("Sets the audience.")).addBlockTag("param",
                                    "audience", "the audience indicating the authorization scope of metrics clients.")
                                .addBlockTag("return", "the AzureMonitorMetricBatchBuilder."));

                    clazz.getMethodsByName("createHttpPipeline")
                        .forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                            "{ Configuration buildConfiguration  = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;"
                                + "HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;"
                                + "ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;"
                                + "List<HttpPipelinePolicy> policies = new ArrayList<>();"
                                + "String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");"
                                + "String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");"
                                + "String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);"
                                + "policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));"
                                + "policies.add(new RequestIdPolicy());"
                                + "policies.add(new AddHeadersFromContextPolicy());"
                                + "HttpHeaders headers = new HttpHeaders();"
                                + "localClientOptions.getHeaders()"
                                + "    .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));"
                                + "if (headers.getSize() > 0) { policies.add(new AddHeadersPolicy(headers)); }"
                                + "this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)"
                                + "    .forEach(p -> policies.add(p));"
                                + "HttpPolicyProviders.addBeforeRetryPolicies(policies);"
                                + "policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));"
                                + "policies.add(new AddDatePolicy());"
                                + "if (tokenCredential != null) {"
                                + "    policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, audience == null ? MetricsAudience.AZURE_PUBLIC_CLOUD.toString() + \"/.default\" : audience.toString() + \"/.default\"));"
                                + "}"
                                + "this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)"
                                + "    .forEach(p -> policies.add(p));"
                                + "HttpPolicyProviders.addAfterRetryPolicies(policies);"
                                + "policies.add(new HttpLoggingPolicy(localHttpLogOptions));"
                                + "return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))"
                                + "    .httpClient(httpClient).clientOptions(localClientOptions).build(); }")));
                });
            });
    }
}
