// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import org.slf4j.Logger;

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;

public class MetricsQueryCustomization extends Customization {

    /**
     * Customizes the MonitorQueryMetricsClientBuilder by adding audience support for authorization scopes.
     * This customization adds a MetricsQueryAudience field and setter to the builder, and modifies
     * the HTTP pipeline creation to use the audience when configuring bearer token authentication.
     * This customization also updates the module-info.java file to export the metrics models package.
     *
     * @param libraryCustomization The library customization object for modifying generated code.
     * @param logger The logger for recording customization activities.
     */
    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        String original = libraryCustomization.getRawEditor().getFileContent("src/main/java/module-info.java");
        String replace = original.replace("exports com.azure.monitor.query.metrics.implementation;", "");

        libraryCustomization.getRawEditor().replaceFile("src/main/java/module-info.java", replace);

        libraryCustomization.getClass("com.azure.monitor.query.metrics.implementation", "MonitorQueryMetricsClientBuilder")
            .customizeAst(ast -> {
                ast.addImport("com.azure.monitor.query.metrics.models.MetricsAudience");

                ast.getClassByName("MonitorQueryMetricsClientBuilder").ifPresent(clazz -> {
                    clazz.addPrivateField("MetricsAudience", "audience")
                        .addMarkerAnnotation("Generated")
                        .setJavadocComment("The audience indicating the authorization scope of metrics clients.")
                        .createSetter()
                        .setName("audience")
                        .setType("MonitorQueryMetricsClientBuilder")
                        .setBody(new BlockStmt().addStatement("this.audience = audience;").addStatement("return this;"))
                        .addMarkerAnnotation("Generated")
                        .setJavadocComment(
                            new Javadoc(JavadocDescription.parseText("Sets the audience.")).addBlockTag("param",
                                    "audience", "the audience indicating the authorization scope of metrics clients.")
                                .addBlockTag("return", "the MonitorQueryMetricsClientBuilder."));

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
