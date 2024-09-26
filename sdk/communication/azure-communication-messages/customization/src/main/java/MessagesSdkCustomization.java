// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

public class MessagesSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = libraryCustomization.getPackage("com.azure.communication.messages");

        updateBuilderClass(packageCustomization, "NotificationMessagesClientBuilder");
        updateBuilderClass(packageCustomization, "MessageTemplateClientBuilder");

        PackageCustomization modelsPackage = libraryCustomization.getPackage("com.azure.communication.messages.models");
        customizeMessageTemplateLocation(modelsPackage);
        customizeNotificationContentModel(modelsPackage);
        customizeMessageTemplateValueModel(modelsPackage);
        customizeMessageTemplateItemModel(modelsPackage);

        PackageCustomization channelsModelsPackage = libraryCustomization.getPackage(
            "com.azure.communication.messages.models.channels");
        updateWhatsAppMessageTemplateItemWithBinaryDataContent(channelsModelsPackage);
    }

    private void customizeNotificationContentModel(PackageCustomization modelsPackage) {
        modelsPackage.getClass("NotificationContent")
            .customizeAst(ast -> ast.getPrimaryType()
                .ifPresent(clazz -> clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PROTECTED)));
    }

    private void customizeMessageTemplateValueModel(PackageCustomization modelsPackage) {
        modelsPackage.getClass("MessageTemplateValue")
            .customizeAst(ast -> ast.getPrimaryType()
                .ifPresent(clazz -> clazz.getConstructors().get(0).setModifiers(Modifier.Keyword.PROTECTED)));
    }

    private void customizeMessageTemplateItemModel(PackageCustomization modelsPackage) {
        modelsPackage.getClass("MessageTemplateItem")
            .customizeAst(ast -> {
                ast.addImport(
                    "com.azure.communication.messages.implementation.accesshelpers.MessageTemplateItemAccessHelper");
                ast.getClassByName("MessageTemplateItem").ifPresent(clazz -> clazz.addStaticInitializer()
                    .addStatement("MessageTemplateItemAccessHelper.setAccessor(MessageTemplateItem::setName);"));
            });
    }

    private void updateBuilderClass(PackageCustomization packageCustomization, String className) {
        packageCustomization.getClass(className).customizeAst(ast -> {
            ast.addImport("com.azure.core.client.traits.TokenCredentialTrait");
            ast.addImport("com.azure.core.client.traits.KeyCredentialTrait");
            ast.addImport("com.azure.core.client.traits.ConnectionStringTrait");
            ast.addImport("com.azure.communication.common.implementation.CommunicationConnectionString");
            ast.addImport("com.azure.core.credential.AzureKeyCredential");
            ast.addImport("com.azure.communication.common.implementation.HmacAuthenticationPolicy");

            ast.getClassByName(className).ifPresent(clazz -> {
                NodeList<ClassOrInterfaceType> implementedTypes = clazz.getImplementedTypes();
                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"));
                if (!hasTokenCredentialTrait) {
                    clazz.addImplementedType(String.format("TokenCredentialTrait<%s>", className));
                }

                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"));
                if (!hasKeyCredentialTrait) {
                    clazz.addImplementedType(String.format("KeyCredentialTrait<%s>", className));
                }

                boolean hasConnectionStringTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait"));
                if (!hasConnectionStringTrait) {
                    clazz.addImplementedType(String.format("ConnectionStringTrait<%s>", className));
                }

                clazz.addMethod("connectionString", Modifier.Keyword.PUBLIC)
                    .addAnnotation("Override")
                    .addParameter("String", "connectionString")
                    .setType(className)
                    .setBody(StaticJavaParser.parseBlock("{"
                        + "CommunicationConnectionString connection = new CommunicationConnectionString(connectionString);"
                        + "this.credential(new KeyCredential(connection.getAccessKey()));"
                        + "this.endpoint(connection.getEndpoint());" + "return this;" + "}"))
                    .setJavadocComment(new Javadoc(
                        JavadocDescription.parseText("Set a connection string for authorization.")).addBlockTag("param",
                            "connectionString", "valid connectionString as a string.")
                        .addBlockTag("return", "the updated " + className + " object."));

                clazz.addMethod("createHttpPipelineAuthPolicy", Modifier.Keyword.PRIVATE)
                    .setType("HttpPipelinePolicy")
                    .setBody(StaticJavaParser.parseBlock("{" + "if (tokenCredential != null) {"
                        + "return new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES);"
                        + "} else if (keyCredential != null) {"
                        + "return new HmacAuthenticationPolicy(new AzureKeyCredential(keyCredential.getKey()));"
                        + "} else {"
                        + "throw LOGGER.logExceptionAsError(new IllegalStateException(\"Missing credential information while building a client.\"));"
                        + "}" + "}"));

                clazz.getMethodsByName("createHttpPipeline")
                    .get(0)
                    .setBody(StaticJavaParser.parseBlock("{Configuration buildConfiguration"
                        + "            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;"
                        + "        HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;"
                        + "        ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;"
                        + "        List<HttpPipelinePolicy> policies = new ArrayList<>();"
                        + "        String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");"
                        + "        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");"
                        + "        String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);"
                        + "        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));"
                        + "        policies.add(new RequestIdPolicy());"
                        + "        policies.add(new AddHeadersFromContextPolicy());"
                        + "        HttpHeaders headers = new HttpHeaders();" + "        localClientOptions.getHeaders()"
                        + "            .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));"
                        + "        if (headers.getSize() > 0) {"
                        + "            policies.add(new AddHeadersPolicy(headers));" + "        }"
                        + "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)"
                        + "            .forEach(p -> policies.add(p));"
                        + "        HttpPolicyProviders.addBeforeRetryPolicies(policies);"
                        + "        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));"
                        + "        policies.add(new AddDatePolicy());"
                        + "        policies.add(createHttpPipelineAuthPolicy());"
                        + "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)"
                        + "            .forEach(p -> policies.add(p));"
                        + "        HttpPolicyProviders.addAfterRetryPolicies(policies);"
                        + "        policies.add(new HttpLoggingPolicy(localHttpLogOptions));"
                        + "        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))"
                        + "            .httpClient(httpClient).clientOptions(localClientOptions).build();"
                        + "        return httpPipeline;}"));
            });
        });
    }

    private void customizeMessageTemplateLocation(PackageCustomization modelsPackage) {
        modelsPackage.getClass("MessageTemplateLocation").customizeAst(ast -> {
            ast.addImport("com.azure.core.models.GeoPosition");
            ast.getClassByName("MessageTemplateLocation").ifPresent(clazz -> {
                clazz.getConstructors().forEach(ctor -> ctor.setModifiers(Modifier.Keyword.PRIVATE));
                clazz.addConstructor(Modifier.Keyword.PUBLIC)
                    .addParameter("String", "refValue")
                    .addParameter("GeoPosition", "geoPosition")
                    .setBody(new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement("super(refValue);"),
                        StaticJavaParser.parseStatement("this.latitude = geoPosition.getLatitude();"),
                        StaticJavaParser.parseStatement("this.longitude = geoPosition.getLongitude();"))))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Creates an instance of MessageTemplateLocation class.")).addBlockTag("param", "refValue",
                            "the refValue value to set.")
                        .addBlockTag("param", "geoPosition", "the geoPosition value to set."));

                clazz.getMethodsByName("getLatitude").forEach(Node::remove);
                clazz.getMethodsByName("getLongitude").forEach(Node::remove);

                clazz.addMethod("getPosition", Modifier.Keyword.PUBLIC)
                    .setType("GeoPosition")
                    .setBody(StaticJavaParser.parseBlock("{return new GeoPosition(this.longitude, this.latitude);}"))
                    .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                        "Get the geo position: The longitude and latitude of the location.")).addBlockTag("return",
                        "the GeoPosition object."));
            });
        });
    }

    private void updateWhatsAppMessageTemplateItemWithBinaryDataContent(PackageCustomization channelsModelsPackage) {
        channelsModelsPackage.getClass("WhatsAppMessageTemplateItem").customizeAst(ast -> {
            ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport(
                "com.azure.communication.messages.implementation.accesshelpers.MessageTemplateItemAccessHelper");
            ast.getClassByName("WhatsAppMessageTemplateItem").ifPresent(clazz -> {
                clazz.getMethodsByName("getContent")
                    .get(0)
                    .setType("BinaryData")
                    .setBody(StaticJavaParser.parseBlock("{return BinaryData.fromObject(this.content);}"));

                String fromJson = clazz.getMethodsByName("fromJson").get(0).getBody().get().toString()
                    .replace("deserializedWhatsAppMessageTemplateItem.setName(name);",
                        "MessageTemplateItemAccessHelper.setName(deserializedWhatsAppMessageTemplateItem, name);");
                clazz.getMethodsByName("fromJson").get(0).setBody(StaticJavaParser.parseBlock(fromJson));
            });
        });
    }
}
