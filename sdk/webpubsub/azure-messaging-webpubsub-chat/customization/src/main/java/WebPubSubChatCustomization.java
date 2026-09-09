// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Modifier;
import org.slf4j.Logger;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/** Customizes the generated Azure Web PubSub Chat client library. */
public final class WebPubSubChatCustomization extends Customization {
    private static final String PACKAGE_NAME = "com.azure.messaging.webpubsub.chat";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization chatPackage = libraryCustomization.getPackage(PACKAGE_NAME);

        chatPackage.getClass("WebPubSubChatServiceClientBuilder").customizeAst(ast -> {
            ast.addImport("com.azure.core.client.traits.AzureKeyCredentialTrait");
            ast.addImport("com.azure.core.client.traits.ConnectionStringTrait");
            ast.addImport("com.azure.core.credential.AzureKeyCredential");
            ast.addImport("com.azure.core.util.UrlBuilder");
            ast.addImport("java.net.MalformedURLException");
            ast.addImport("java.net.URL");
            ast.addImport("java.util.HashMap");
            ast.addImport("java.util.Locale");

            ast.getClassByName("WebPubSubChatServiceClientBuilder").ifPresent(builder -> {
                if (builder.getImplementedTypes()
                    .stream()
                    .noneMatch(type -> type.getNameAsString().equals("AzureKeyCredentialTrait"))) {
                    builder.addImplementedType("AzureKeyCredentialTrait<WebPubSubChatServiceClientBuilder>");
                }
                if (builder.getImplementedTypes()
                    .stream()
                    .noneMatch(type -> type.getNameAsString().equals("ConnectionStringTrait"))) {
                    builder.addImplementedType("ConnectionStringTrait<WebPubSubChatServiceClientBuilder>");
                }

                builder.addField("AzureKeyCredential", "keyCredential", Modifier.Keyword.PRIVATE);
                builder.addField("String", "reverseProxyEndpoint", Modifier.Keyword.PRIVATE);

                builder.addMethod("credential", Modifier.Keyword.PUBLIC)
                    .addMarkerAnnotation("Override")
                    .addParameter("AzureKeyCredential", "credential")
                    .setType("WebPubSubChatServiceClientBuilder")
                    .setBody(parseBlock("{"
                        + "this.keyCredential = Objects.requireNonNull(credential, \"'credential' cannot be null.\");"
                        + "return this;"
                        + "}"))
                    .setJavadocComment(new com.github.javaparser.javadoc.Javadoc(parseText(
                        "Sets the Azure key credential used to authenticate requests."))
                            .addBlockTag("param", "credential", "The Azure key credential.")
                            .addBlockTag("return", "The updated builder."));

                builder.addMethod("connectionString", Modifier.Keyword.PUBLIC)
                    .addMarkerAnnotation("Override")
                    .addParameter("String", "connectionString")
                    .setType("WebPubSubChatServiceClientBuilder")
                    .setBody(parseBlock("{"
                        + "Objects.requireNonNull(connectionString, \"'connectionString' cannot be null.\");"
                        + "Map<String, String> connectionStringParams = parseConnectionString(connectionString);"
                        + "if (!connectionStringParams.containsKey(\"endpoint\") "
                        + "|| !connectionStringParams.containsKey(\"accesskey\")) {"
                        + "throw LOGGER.logExceptionAsError(new IllegalArgumentException("
                        + "\"Connection string does not contain required 'endpoint' and 'accesskey' values\"));"
                        + "}"
                        + "this.keyCredential = new AzureKeyCredential(connectionStringParams.get(\"accesskey\"));"
                        + "String connectionStringEndpoint = connectionStringParams.get(\"endpoint\");"
                        + "URL url;"
                        + "try {"
                        + "url = new URL(connectionStringEndpoint);"
                        + "this.endpoint = connectionStringEndpoint;"
                        + "} catch (MalformedURLException exception) {"
                        + "throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("
                        + "\"Connection string contains invalid endpoint\", exception));"
                        + "}"
                        + "String port = connectionStringParams.get(\"port\");"
                        + "if (!CoreUtils.isNullOrEmpty(port)) {"
                        + "this.endpoint = UrlBuilder.parse(url).setPort(port).toString();"
                        + "}"
                        + "return this;"
                        + "}"))
                    .setJavadocComment(new com.github.javaparser.javadoc.Javadoc(parseText(
                        "Sets the Web PubSub connection string used to configure the endpoint and access key."))
                            .addBlockTag("param", "connectionString", "The Web PubSub connection string.")
                            .addBlockTag("return", "The updated builder."));

                builder.addMethod("reverseProxyEndpoint", Modifier.Keyword.PUBLIC)
                    .addParameter("String", "reverseProxyEndpoint")
                    .setType("WebPubSubChatServiceClientBuilder")
                    .setBody(parseBlock("{"
                        + "this.reverseProxyEndpoint = reverseProxyEndpoint;"
                        + "return this;"
                        + "}"))
                    .setJavadocComment(new com.github.javaparser.javadoc.Javadoc(parseText(
                        "Sets the reverse proxy endpoint."))
                            .addBlockTag("param", "reverseProxyEndpoint", "The reverse proxy endpoint.")
                            .addBlockTag("return", "The updated builder."));

                builder.addMethod("parseConnectionString", Modifier.Keyword.PRIVATE)
                    .addParameter("String", "connectionString")
                    .setType("Map<String, String>")
                    .setBody(parseBlock("{"
                        + "String[] parameters = connectionString.split(\";\");"
                        + "Map<String, String> connectionStringParams = new HashMap<>();"
                        + "for (String parameter : parameters) {"
                        + "String[] parameterParts = parameter.split(\"=\", 2);"
                        + "if (parameterParts.length != 2) { continue; }"
                        + "String key = parameterParts[0].trim().toLowerCase(Locale.ROOT);"
                        + "if (connectionStringParams.containsKey(key)) {"
                        + "throw LOGGER.logExceptionAsError(new IllegalArgumentException("
                        + "\"Duplicate connection string key parameter provided for key '\" + key + \"'\"));"
                        + "}"
                        + "connectionStringParams.put(key, parameterParts[1].trim());"
                        + "}"
                        + "return connectionStringParams;"
                        + "}"));

                builder.getMethodsByName("validateClient").forEach(method -> method.setBody(parseBlock("{"
                    + "Objects.requireNonNull(endpoint, \"'endpoint' cannot be null.\");"
                    + "if (hub == null || hub.isEmpty()) {"
                    + "throw LOGGER.logExceptionAsError(new IllegalStateException("
                    + "\"hub is not valid - it must be non-null and non-empty.\"));"
                    + "}"
                    + "}")));

                builder.getMethodsByName("createHttpPipeline").forEach(method -> method.setBody(parseBlock("{"
                    + "Configuration buildConfiguration = (configuration == null) "
                    + "? Configuration.getGlobalConfiguration() : configuration;"
                    + "HttpLogOptions localHttpLogOptions = this.httpLogOptions == null "
                    + "? new HttpLogOptions() : this.httpLogOptions;"
                    + "ClientOptions localClientOptions = this.clientOptions == null "
                    + "? new ClientOptions() : this.clientOptions;"
                    + "List<HttpPipelinePolicy> policies = new ArrayList<>();"
                    + "String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");"
                    + "String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");"
                    + "String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);"
                    + "policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));"
                    + "policies.add(new RequestIdPolicy());"
                    + "policies.add(new AddHeadersFromContextPolicy());"
                    + "HttpHeaders headers = CoreUtils.createHttpHeadersFromClientOptions(localClientOptions);"
                    + "if (headers != null) { policies.add(new AddHeadersPolicy(headers)); }"
                    + "this.pipelinePolicies.stream()"
                    + ".filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)"
                    + ".forEach(policies::add);"
                    + "HttpPolicyProviders.addBeforeRetryPolicies(policies);"
                    + "policies.add(ClientBuilderUtil.validateAndGetRetryPolicy("
                    + "retryPolicy, retryOptions, new RetryPolicy()));"
                    + "policies.add(new AddDatePolicy());"
                    + "if (keyCredential != null) {"
                    + "policies.add(new WebPubSubAuthenticationPolicy(keyCredential));"
                    + "} else if (tokenCredential != null) {"
                    + "policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES));"
                    + "} else {"
                    + "throw LOGGER.logExceptionAsError(new IllegalStateException("
                    + "\"No credential available to create the client.\"));"
                    + "}"
                    + "if (!CoreUtils.isNullOrEmpty(reverseProxyEndpoint)) {"
                    + "policies.add(new ReverseProxyPolicy(reverseProxyEndpoint));"
                    + "}"
                    + "this.pipelinePolicies.stream()"
                    + ".filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)"
                    + ".forEach(policies::add);"
                    + "HttpPolicyProviders.addAfterRetryPolicies(policies);"
                    + "policies.add(new HttpLoggingPolicy(localHttpLogOptions));"
                    + "return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))"
                    + ".httpClient(httpClient).clientOptions(localClientOptions).build();"
                    + "}")));

                builder.getMethodsByName("buildAsyncClient").forEach(method -> method.setBody(parseBlock("{"
                    + "return new WebPubSubChatServiceAsyncClient(buildInnerClient(), keyCredential);"
                    + "}")));
                builder.getMethodsByName("buildClient").forEach(method -> method.setBody(parseBlock("{"
                    + "return new WebPubSubChatServiceClient(buildInnerClient(), keyCredential);"
                    + "}")));
            });
        });

        customizeTokenClient(chatPackage, "WebPubSubChatServiceClient", false);
        customizeTokenClient(chatPackage, "WebPubSubChatServiceAsyncClient", true);

        addNimbusModuleRequirement(libraryCustomization);
        fixGeneratedPagingCalls(libraryCustomization);
        removeTrailingJavadocWhitespace(libraryCustomization);
    }

    private static void customizeTokenClient(PackageCustomization chatPackage, String className, boolean async) {
        chatPackage.getClass(className).customizeAst(ast -> {
            ast.getImports()
                .removeIf(importDeclaration -> importDeclaration.getNameAsString()
                    .equals("com.azure.messaging.webpubsub.chat.implementation.models.GenerateClientTokenResponse"));
            ast.addImport("com.azure.core.credential.AzureKeyCredential");
            ast.addImport("com.azure.messaging.webpubsub.chat.models.GetClientAccessTokenOptions");
            ast.addImport("com.azure.messaging.webpubsub.chat.models.WebPubSubClientAccessToken");
            if (async) {
                ast.addImport("reactor.core.publisher.Mono");
            }

            ast.getClassByName(className).ifPresent(client -> {
                client.getMethodsByName("generateClientToken").forEach(method -> method.remove());
                client.getMethodsByName("generateClientTokenWithResponse").forEach(method -> method.remove());
                client.addField("AzureKeyCredential", "keyCredential", Modifier.Keyword.PRIVATE,
                    Modifier.Keyword.FINAL);
                client.getConstructors().forEach(constructor -> {
                    constructor.addParameter("AzureKeyCredential", "keyCredential");
                    constructor.getBody().addStatement("this.keyCredential = keyCredential;");
                });

                client.addMethod("getClientAccessToken", Modifier.Keyword.PUBLIC)
                    .addParameter("GetClientAccessTokenOptions", "options")
                    .setType(async ? "Mono<WebPubSubClientAccessToken>" : "WebPubSubClientAccessToken")
                    .setBody(parseBlock(async
                        ? "{ return WebPubSubClientAccessTokenFactory.createAsync(serviceClient, keyCredential, options); }"
                        : "{ return WebPubSubClientAccessTokenFactory.create(serviceClient, keyCredential, options); }"))
                    .setJavadocComment(new com.github.javaparser.javadoc.Javadoc(parseText(
                        "Creates a client access token for connecting to Azure Web PubSub Chat."))
                            .addBlockTag("param", "options", "Options for creating the client access token.")
                            .addBlockTag("return", async
                                ? "A publisher containing the client access token."
                                : "The client access token."));
            });
        });
    }

    private static void addNimbusModuleRequirement(LibraryCustomization libraryCustomization) {
        String path = "src/main/java/module-info.java";
        String moduleInfo = libraryCustomization.getRawEditor().getFileContent(path);
        if (!moduleInfo.contains("requires com.nimbusds.jose.jwt;")) {
            libraryCustomization.getRawEditor()
                .replaceFile(path, moduleInfo.replace("    requires transitive com.azure.core;",
                    "    requires transitive com.azure.core;\n    requires com.nimbusds.jose.jwt;"));
        }
    }

    private static void fixGeneratedPagingCalls(LibraryCustomization libraryCustomization) {
        replaceInGeneratedFiles(libraryCustomization, "listMessages(\"c.room1.abcd1234\", null, null, 10)",
            "listMessages(\"c.room1.abcd1234\")", "ListMessages.java", "ListMessagesTests.java");
        replaceInGeneratedFiles(libraryCustomization, "listRoles(10, null)", "listRoles()", "ListRoles.java",
            "ListRolesTests.java");
        replaceInGeneratedFiles(libraryCustomization, "listRoomMembers(\"room1\", 10, null)",
            "listRoomMembers(\"room1\")", "ListRoomMembers.java", "ListRoomMembersTests.java");
    }

    private static void replaceInGeneratedFiles(LibraryCustomization libraryCustomization, String oldValue,
        String newValue, String sampleFile, String testFile) {
        String samplePath = "src/samples/java/com/azure/messaging/webpubsub/chat/generated/" + sampleFile;
        String testPath = "src/test/java/com/azure/messaging/webpubsub/chat/generated/" + testFile;
        String sample = libraryCustomization.getRawEditor().getFileContent(samplePath);
        String test = libraryCustomization.getRawEditor().getFileContent(testPath);
        libraryCustomization.getRawEditor().replaceFile(samplePath, sample.replace(oldValue, newValue));
        libraryCustomization.getRawEditor().replaceFile(testPath, test.replace(oldValue, newValue));
    }

    private static void removeTrailingJavadocWhitespace(LibraryCustomization libraryCustomization) {
        String path
            = "src/main/java/com/azure/messaging/webpubsub/chat/implementation/WebPubSubChatServiceClientImpl.java";
        String implementation = libraryCustomization.getRawEditor().getFileContent(path);
        libraryCustomization.getRawEditor()
            .replaceFile(path, implementation.replace("* \r\n", "*\r\n").replace("* \n", "*\n"));
    }
}