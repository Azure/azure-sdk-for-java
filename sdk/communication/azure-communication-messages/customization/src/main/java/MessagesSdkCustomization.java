// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.*;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.List;

public class MessagesSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = libraryCustomization.getPackage("com.azure.communication.messages");

        ClassCustomization notificationMessagesClientBuilderCustomization = packageCustomization.getClass("NotificationMessagesClientBuilder");
        addAuthTrait(notificationMessagesClientBuilderCustomization);
        addConnectionStringClientMethod(notificationMessagesClientBuilderCustomization, "NotificationMessagesClientBuilder");
        addHttpPipelineAuthPolicyMethod(notificationMessagesClientBuilderCustomization);
        updateHttpPipelineMethod(notificationMessagesClientBuilderCustomization);

        ClassCustomization messageTemplateClientBuilderCustomization = packageCustomization.getClass("MessageTemplateClientBuilder");
        addAuthTrait(messageTemplateClientBuilderCustomization);
        addConnectionStringClientMethod(messageTemplateClientBuilderCustomization, "MessageTemplateClientBuilder");
        addHttpPipelineAuthPolicyMethod(messageTemplateClientBuilderCustomization);
        updateHttpPipelineMethod(messageTemplateClientBuilderCustomization);

        updateTemplateLocationConstructorWithGeoPositionParameter(libraryCustomization);
        addPositionGetterInTemplateLocation(libraryCustomization);
        updateWhatsAppMessageTemplateItemWithBinaryDataContent(libraryCustomization);

        customizeNotificationContentModel(libraryCustomization);
        customizeMessageTemplateValueModel(libraryCustomization);
        customizeMessageTemplateItemModel(libraryCustomization);
    }

    private void customizeNotificationContentModel(LibraryCustomization customization) {
        PackageCustomization modelPackageCustomization = customization.getPackage("com.azure.communication.messages.models");
        ClassCustomization notificationContentModelCustomization = modelPackageCustomization.getClass("NotificationContent");
        notificationContentModelCustomization
            .setModifier(Modifier.PUBLIC | Modifier.ABSTRACT)
            .getConstructor("NotificationContent")
            .setModifier(Modifier.PROTECTED);
    }

    private void customizeMessageTemplateValueModel(LibraryCustomization customization) {
        PackageCustomization modelPackageCustomization = customization.getPackage("com.azure.communication.messages.models");
        ClassCustomization notificationContentModelCustomization = modelPackageCustomization.getClass("MessageTemplateValue");
        notificationContentModelCustomization
            .setModifier(Modifier.PUBLIC | Modifier.ABSTRACT)
            .getConstructor("MessageTemplateValue")
            .setModifier(Modifier.PROTECTED);
    }

    private void customizeMessageTemplateItemModel(LibraryCustomization customization) {
        PackageCustomization modelPackageCustomization = customization.getPackage("com.azure.communication.messages.models");
        ClassCustomization notificationContentModelCustomization = modelPackageCustomization.getClass("MessageTemplateItem");
        notificationContentModelCustomization
            .setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);
    }

    private void addAuthTrait(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.core.client.traits.TokenCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.KeyCredentialTrait");
        classCustomization.addImports("com.azure.core.client.traits.ConnectionStringTrait");
        classCustomization.customizeAst(compilationUnit -> {
            compilationUnit.getClassByName(classCustomization.getClassName()).ifPresent(builderClass -> {
                ClassOrInterfaceDeclaration clientBuilderClass = builderClass.asClassOrInterfaceDeclaration();
                NodeList<ClassOrInterfaceType> implementedTypes = clientBuilderClass.getImplementedTypes();
                boolean hasTokenCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("TokenCredentialTrait"));
                if (!hasTokenCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("TokenCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasKeyCredentialTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("KeyCredentialTrait"));
                if (!hasKeyCredentialTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("KeyCredentialTrait<%s>", classCustomization.getClassName()));
                }

                boolean hasConnectionStringTrait = implementedTypes.stream()
                    .anyMatch(implementedType -> implementedType.getNameAsString().equals("ConnectionStringTrait"));
                if (!hasConnectionStringTrait) {
                    clientBuilderClass
                        .addImplementedType(String.format("ConnectionStringTrait<%s>", classCustomization.getClassName()));
                }
            });
        });
    }

    private void addConnectionStringClientMethod(ClassCustomization classCustomization, String methodReturnType) {
        classCustomization.addMethod(
            "public "+methodReturnType+" connectionString(String connectionString) {" +
                "CommunicationConnectionString connection = new CommunicationConnectionString(connectionString);"+
                "this.credential(new KeyCredential(connection.getAccessKey()));"+
                "this.endpoint(connection.getEndpoint());"+
                "return this;"+
                "}",
            List.of(
                "com.azure.communication.common.implementation.CommunicationConnectionString"
            ));

        classCustomization
            .getMethod("connectionString")
            .getJavadoc()
            .setDescription("Set a connection string for authorization.\n" +
                "@param connectionString valid connectionString as a string.\n" +
                "@return the updated NotificationMessagesClientBuilder object.");
    }

    private void addHttpPipelineAuthPolicyMethod(ClassCustomization classCustomization) {
        classCustomization.addImports("com.azure.core.credential.AzureKeyCredential");
        classCustomization.addMethod(
            "private HttpPipelinePolicy createHttpPipelineAuthPolicy() {" +
                "        if (tokenCredential != null) {" +
                "            return new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES);" +
                "        } else if (keyCredential != null) {" +
                "            return new HmacAuthenticationPolicy(new AzureKeyCredential(keyCredential.getKey()));" +
                "        } else {" +
                "            throw LOGGER.logExceptionAsError(" +
                "                new IllegalStateException(\"Missing credential information while building a client.\"));" +
                "        }" +
                "    }",
            List.of(
                "com.azure.communication.common.implementation.HmacAuthenticationPolicy"
            ));
    }

    private void updateHttpPipelineMethod(ClassCustomization classCustomization) {
        MethodCustomization methodCustomization = classCustomization.getMethod("createHttpPipeline");
        methodCustomization.replaceBody("Configuration buildConfiguration" +
            "            = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;" +
            "        HttpLogOptions localHttpLogOptions = this.httpLogOptions == null ? new HttpLogOptions() : this.httpLogOptions;" +
            "        ClientOptions localClientOptions = this.clientOptions == null ? new ClientOptions() : this.clientOptions;" +
            "        List<HttpPipelinePolicy> policies = new ArrayList<>();" +
            "        String clientName = PROPERTIES.getOrDefault(SDK_NAME, \"UnknownName\");" +
            "        String clientVersion = PROPERTIES.getOrDefault(SDK_VERSION, \"UnknownVersion\");" +
            "        String applicationId = CoreUtils.getApplicationId(localClientOptions, localHttpLogOptions);" +
            "        policies.add(new UserAgentPolicy(applicationId, clientName, clientVersion, buildConfiguration));" +
            "        policies.add(new RequestIdPolicy());" +
            "        policies.add(new AddHeadersFromContextPolicy());" +
            "        HttpHeaders headers = new HttpHeaders();" +
            "        localClientOptions.getHeaders()" +
            "            .forEach(header -> headers.set(HttpHeaderName.fromString(header.getName()), header.getValue()));" +
            "        if (headers.getSize() > 0) {" +
            "            policies.add(new AddHeadersPolicy(headers));" +
            "        }" +
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_CALL)" +
            "            .forEach(p -> policies.add(p));" +
            "        HttpPolicyProviders.addBeforeRetryPolicies(policies);" +
            "        policies.add(ClientBuilderUtil.validateAndGetRetryPolicy(retryPolicy, retryOptions, new RetryPolicy()));" +
            "        policies.add(new AddDatePolicy());" +
            "        policies.add(createHttpPipelineAuthPolicy());" +
            "        this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)" +
            "            .forEach(p -> policies.add(p));" +
            "        HttpPolicyProviders.addAfterRetryPolicies(policies);" +
            "        policies.add(new HttpLoggingPolicy(localHttpLogOptions));" +
            "        HttpPipeline httpPipeline = new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))" +
            "            .httpClient(httpClient).clientOptions(localClientOptions).build();" +
            "        return httpPipeline;");
    }

    private void updateTemplateLocationConstructorWithGeoPositionParameter(LibraryCustomization libraryCustomization) {
        ClassCustomization messageTemplateLocationCustomization = libraryCustomization
            .getPackage("com.azure.communication.messages.models")
            .getClass("MessageTemplateLocation");
        messageTemplateLocationCustomization.getConstructor("MessageTemplateLocation")
            .replaceParameters(
                "@JsonProperty(value = \"name\") String refValue, GeoPosition geoPosition",
                List.of("com.azure.core.models.GeoPosition")
            )
            .replaceBody( "super(refValue);" +
                "this.latitude = geoPosition.getLatitude();" +
                "this.longitude = geoPosition.getLongitude();"
            );
        messageTemplateLocationCustomization
            .getConstructor("MessageTemplateLocation")
            .getJavadoc()
            .removeParam("longitude")
            .removeParam("latitude")
            .setParam("geoPosition", "the geoPosition value to set.");
    }

    private void addPositionGetterInTemplateLocation(LibraryCustomization libraryCustomization) {
        ClassCustomization messageTemplateLocationCustomization = libraryCustomization
            .getPackage("com.azure.communication.messages.models")
            .getClass("MessageTemplateLocation");

        messageTemplateLocationCustomization.removeMethod("getLatitude");
        messageTemplateLocationCustomization.removeMethod("getLongitude");

        messageTemplateLocationCustomization.addMethod(
            "public GeoPosition getPosition() {" +
            "    return new GeoPosition(this.longitude, this.latitude);" +
            "}");

        messageTemplateLocationCustomization
            .getMethod("getPosition")
            .getJavadoc()
            .setDescription("Get the geo position: The longitude and latitude of the location.\n" +
                "@return the GeoPosition object.");
    }

    private void updateWhatsAppMessageTemplateItemWithBinaryDataContent(LibraryCustomization libraryCustomization) {
        ClassCustomization whatsAppMessageTemplateItemCustomization = libraryCustomization
            .getPackage("com.azure.communication.messages.models.channels")
            .getClass("WhatsAppMessageTemplateItem");

        whatsAppMessageTemplateItemCustomization
            .addImports("com.azure.core.util.BinaryData")
            .getMethod("getContent")
            .setReturnType("BinaryData", "return BinaryData.fromObject(returnValue);", true);
    }
}
