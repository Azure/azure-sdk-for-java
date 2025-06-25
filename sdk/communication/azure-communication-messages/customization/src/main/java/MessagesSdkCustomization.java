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
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocSnippet;
import org.slf4j.Logger;

import java.util.List;

import static com.github.javaparser.StaticJavaParser.parseBlock;
import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

public class MessagesSdkCustomization extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        logger.info("Customizing the JobRouterAdministrationClientBuilder class");
        PackageCustomization packageCustomization = libraryCustomization.getPackage("com.azure.communication.messages");

        updateBuilderClass(packageCustomization, "NotificationMessagesClientBuilder");
        updateBuilderClass(packageCustomization, "MessageTemplateClientBuilder");

        PackageCustomization modelsPackage = libraryCustomization.getPackage("com.azure.communication.messages.models");
        updateModelClassModifierToAbstract(modelsPackage, "NotificationContent");
        updateModelClassModifierToAbstract(modelsPackage, "MessageTemplateValue");
        updateModelClassModifierToAbstract(modelsPackage, "MessageTemplateItem");
        customizeMessageTemplateLocation(modelsPackage);
        customizeMessageTemplateItemModel(modelsPackage);

        //Handle Interactive message content models
        updateModelClassModifierToAbstract(modelsPackage, "MessageContent");
        updateModelClassModifierToAbstract(modelsPackage, "ActionBindings");
        updateJavaDocForMethodFromJson(modelsPackage, "ActionBindings");
        updateJavaDocForMethodFromJson(modelsPackage, "MessageContent");
        customizeInteractiveMessage(modelsPackage);

        PackageCustomization channelsModelsPackage = libraryCustomization.getPackage(
            "com.azure.communication.messages.models.channels");
        updateWhatsAppMessageTemplateItemWithBinaryDataContent(channelsModelsPackage);

        addDeprecateAnnotationToMediaNotificationContent(modelsPackage);

        addDeprecateAnnotationForImageV0CommunicationKind(modelsPackage);

        customizeActionGroup(modelsPackage);
        customizeActionGroupContent(modelsPackage);
        customizeButtonSetContent(modelsPackage);
    }

    private void updateModelClassModifierToAbstract(PackageCustomization modelsPackage, String className) {
        modelsPackage.getClass(className)
            .customizeAst(ast ->  ast.getClassByName(className)
                .ifPresent(clazz -> clazz.setModifiers(Modifier.Keyword.PUBLIC, Modifier.Keyword.ABSTRACT)
                    .getConstructors().get(0).setModifiers(Modifier.Keyword.PROTECTED)));
        removeJsonKnownDiscriminatorMethod(modelsPackage, className);
    }

    private void customizeMessageTemplateItemModel(PackageCustomization modelsPackage) {
        modelsPackage.getClass("MessageTemplateItem").customizeAst(ast -> {
            ast.addImport("com.azure.communication.messages.implementation.accesshelpers.MessageTemplateItemAccessHelper");
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
                    .addMarkerAnnotation("Override")
                    .addParameter("String", "connectionString")
                    .setType(className)
                    .setBody(parseBlock("{"
                        + "CommunicationConnectionString connection = new CommunicationConnectionString(connectionString);"
                        + "this.credential(new KeyCredential(connection.getAccessKey()));"
                        + "this.endpoint(connection.getEndpoint()); return this; }"))
                    .setJavadocComment(new Javadoc(parseText("Set a connection string for authorization."))
                        .addBlockTag("param", "connectionString", "valid connectionString as a string.")
                        .addBlockTag("return", "the updated " + className + " object."));

                clazz.addMethod("createHttpPipelineAuthPolicy", Modifier.Keyword.PRIVATE)
                    .setType("HttpPipelinePolicy")
                    .setBody(parseBlock("{ if (tokenCredential != null) {"
                        + "return new BearerTokenAuthenticationPolicy(tokenCredential, DEFAULT_SCOPES);"
                        + "} else if (keyCredential != null) {"
                        + "return new HmacAuthenticationPolicy(new AzureKeyCredential(keyCredential.getKey()));"
                        + "} else {"
                        + "throw LOGGER.logExceptionAsError(new IllegalStateException(\"Missing credential information while building a client.\"));"
                        + "} }"));

                clazz.getMethodsByName("createHttpPipeline").get(0)
                    .setBody(parseBlock("{"
                        + "Configuration buildConfiguration = (configuration == null) ? Configuration.getGlobalConfiguration() : configuration;"
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
                        + "policies.add(createHttpPipelineAuthPolicy());"
                        + "this.pipelinePolicies.stream().filter(p -> p.getPipelinePosition() == HttpPipelinePosition.PER_RETRY)"
                        + "    .forEach(p -> policies.add(p));"
                        + "HttpPolicyProviders.addAfterRetryPolicies(policies);"
                        + "policies.add(new HttpLoggingPolicy(localHttpLogOptions));"
                        + "return new HttpPipelineBuilder().policies(policies.toArray(new HttpPipelinePolicy[0]))"
                        + "    .httpClient(httpClient).clientOptions(localClientOptions).build(); }"));
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
                    .setJavadocComment(new Javadoc(parseText("Creates an instance of MessageTemplateLocation class."))
                        .addBlockTag("param", "refValue", "the refValue value to set.")
                        .addBlockTag("param", "geoPosition", "the geoPosition value to set."));

                clazz.getMethodsByName("getLatitude").forEach(Node::remove);
                clazz.getMethodsByName("getLongitude").forEach(Node::remove);

                clazz.addMethod("getPosition", Modifier.Keyword.PUBLIC)
                    .setType("GeoPosition")
                    .setBody(parseBlock("{return new GeoPosition(this.longitude, this.latitude);}"))
                    .setJavadocComment(new Javadoc(parseText("Get the geo position: The longitude and latitude of the location."))
                        .addBlockTag("return", "the GeoPosition object."));
            });
        });
    }

    private void customizeInteractiveMessage(PackageCustomization modelsPackage) {
        modelsPackage.getClass("InteractiveMessage").customizeAst(ast -> ast.getClassByName("InteractiveMessage")
            .ifPresent(clazz -> {
                clazz.getMethodsByName("getHeaderProperty").forEach(m -> {
                    m.setName("getHeader");
                    m.getJavadoc().ifPresent(javadoc -> m.setJavadocComment(new Javadoc(
                        parseText(javadoc.getDescription().toText().replace("headerProperty", "header")))
                        .addBlockTag("retrun", javadoc.getBlockTags().get(0).toText()
                            .replace("headerProperty", "header"))));
                });

                clazz.getMethodsByName("setHeaderProperty").forEach(m -> {
                    m.setName("setHeader");
                    m.getParameter(0).setName("header");
                    m.setBody(parseBlock("{ this.headerProperty = header; return this; }"));
                    m.getJavadoc().ifPresent(javadoc -> {
                        Javadoc newJavadoc = new Javadoc(parseText(javadoc.getDescription().toText()
                            .replace("headerProperty", "header")));
                        for (int i = 0; i < javadoc.getBlockTags().size(); i++) {
                            JavadocBlockTag tag = javadoc.getBlockTags().get(i);
                            if (tag.getType() != JavadocBlockTag.Type.PARAM
                                || !tag.getName().map("headerProperty"::equals).orElse(false)) {
                                newJavadoc.getBlockTags().add(tag);
                            } else {
                                newJavadoc.addBlockTag("param", "header",
                                    tag.getContent().toString().replace("headerProperty", "header"));
                            }
                        }
                        m.setJavadocComment(newJavadoc);
                    });
                });
            }));
    }

    private void updateWhatsAppMessageTemplateItemWithBinaryDataContent(PackageCustomization channelsModelsPackage) {
        channelsModelsPackage.getClass("WhatsAppMessageTemplateItem").customizeAst(ast -> {
            // ast.addImport("com.azure.core.util.BinaryData");
            ast.addImport("com.azure.communication.messages.implementation.accesshelpers.MessageTemplateItemAccessHelper");
            ast.getClassByName("WhatsAppMessageTemplateItem").ifPresent(clazz -> {
                // clazz.getMethodsByName("getContent")
                //     .get(0)
                //     .setType("BinaryData")
                //     .setBody(StaticJavaParser.parseBlock("{return BinaryData.fromObject(this.content);}"));

                clazz.getMethodsByName("fromJson").forEach(method -> method.getBody().ifPresent(body -> {
                    String bodyStr = body.toString().replace("deserializedWhatsAppMessageTemplateItem.setName(name);",
                        "MessageTemplateItemAccessHelper.setName(deserializedWhatsAppMessageTemplateItem, name);");
                    method.setBody(parseBlock(bodyStr));
                }));
            });
        });
    }

    private void removeJsonKnownDiscriminatorMethod(PackageCustomization modelPackage, String className) {
        modelPackage.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent( clazz -> {
            clazz.getMethodsByName("fromJson").forEach(method -> method.getBody().ifPresent(body -> {
                String bodyStr = body.toString().replace("return fromJsonKnownDiscriminator(readerToUse.reset());",
                    "throw new IllegalStateException(\"Invalid Kind value - \" +discriminatorValue);");
                method.setBody(parseBlock(bodyStr));
            }));
            clazz.getMethodsByName("fromJsonKnownDiscriminator").forEach(Node::remove);
        }));
    }

    private void addDeprecateAnnotationToMediaNotificationContent(PackageCustomization modelsPackage) {
        modelsPackage.getClass("MediaNotificationContent").customizeAst(ast -> ast.getClassByName("MediaNotificationContent")
            .ifPresent(clazz -> clazz.addMarkerAnnotation(Deprecated.class).getJavadoc()
                .ifPresent(doc -> {
                    // Remove the @deprecated comment as it cause special character and fails in style check
                    String description = doc.getDescription().getElements().get(0).toText().replace("&#064;deprecated", "@deprecated");
                    doc.getDescription().getElements().set(0, new JavadocSnippet(description));
                    clazz.setJavadocComment(doc.toComment());
                })));
    }

    private  void addDeprecateAnnotationForImageV0CommunicationKind(PackageCustomization modelsPackage) {
        modelsPackage.getClass("CommunicationMessageKind").customizeAst(ast -> ast.getClassByName("CommunicationMessageKind")
            .flatMap(clazz -> clazz.getFieldByName("IMAGE_V0"))
            .ifPresent(f -> {
                f.addMarkerAnnotation(Deprecated.class);
                // Reducing size comment by replacing with @deprecated since it doesn't fit single line and fails in style check
                f.getJavadocComment().ifPresent(comment ->
                    f.setJavadocComment(comment.getContent().replace("Image message type.", "@deprecated")));
            }));
    }

    private void updateJavaDocForMethodFromJson(PackageCustomization modelPackage, String className) {
        String originalDocText = String.format("@throws IOException If an error occurs while reading the %s.", className);
        modelPackage.getClass(className).customizeAst(ast -> ast.getClassByName(className).ifPresent( clazz ->
            clazz.getMethodsByName("fromJson").forEach(method -> method.getJavadoc().ifPresent(javadoc -> {
                String javadocStr = javadoc.toText().replace(originalDocText, "@throws IllegalStateException If the "
                    + "deserialized JSON object was missing any required properties.\n" + originalDocText);
                method.setJavadocComment(javadocStr);
            }))));
    }

    private void customizeActionGroup(PackageCustomization modelsPackage) {
        modelsPackage.getClass("ActionGroup").customizeAst(ast -> ast.getClassByName("ActionGroup").ifPresent(clazz -> {
            clazz.getConstructorByParameterTypes(String.class, List.class).ifPresent(ctor -> ctor.setBody(
                parseBlock(ctor.getBody().toString().replace("this.items = items;", "this.items = new ArrayList<>(items);"))));

            clazz.getMethodsByName("getItems").forEach(method -> method.getBody().ifPresent(body -> method.setBody(
                parseBlock(body.toString().replace("return this.items;", "return new ArrayList<>(this.items);")))));
        }));
    }

    private void customizeActionGroupContent(PackageCustomization modelsPackage) {
        modelsPackage.getClass("ActionGroupContent").customizeAst(ast -> ast.getClassByName("ActionGroupContent").ifPresent(clazz -> {
            clazz.getConstructorByParameterTypes(String.class, List.class).ifPresent(ctor -> ctor.setBody(
                parseBlock(ctor.getBody().toString().replace("this.groups = groups;", "this.groups = new ArrayList<>(groups);"))));

            clazz.getMethodsByName("getGroups").forEach(method -> method.getBody().ifPresent(body -> method.setBody(
                parseBlock(body.toString().replace("return this.groups;", "return new ArrayList<>(this.groups);")))));
        }));
    }

    private void customizeButtonSetContent(PackageCustomization modelsPackage) {
        modelsPackage.getClass("ButtonSetContent").customizeAst(ast -> ast.getClassByName("ButtonSetContent").ifPresent(clazz -> {
            clazz.getConstructorByParameterTypes(List.class).ifPresent(ctor -> ctor.setBody(parseBlock(
                ctor.getBody().toString().replace("this.buttons = buttons;", "this.buttons = new ArrayList<>(buttons);"))));

            clazz.getMethodsByName("getButtons").forEach(method -> method.getBody().ifPresent(body -> method.setBody(
                parseBlock(body.toString().replace("return this.buttons;", "return new ArrayList<>(this.buttons);")))));
        }));
    }
}
