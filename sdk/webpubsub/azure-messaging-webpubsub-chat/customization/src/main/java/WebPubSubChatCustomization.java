// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.slf4j.Logger;

/** Customizes the generated Azure Web PubSub Chat client library. */
public final class WebPubSubChatCustomization extends Customization {
    private static final String PACKAGE_NAME = "com.azure.messaging.webpubsub.chat";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization chatPackage = libraryCustomization.getPackage(PACKAGE_NAME);

        chatPackage.getClass("WebPubSubChatServiceClientBuilder").customizeAst(ast -> {
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
            });
        });

        customizeTokenClient(chatPackage, "WebPubSubChatServiceClient");
        customizeTokenClient(chatPackage, "WebPubSubChatServiceAsyncClient");

        customizeLifecycleDocumentation(libraryCustomization);
        removeTrailingJavadocWhitespace(libraryCustomization);
    }

    static void customizeLifecycleDocumentation(LibraryCustomization libraryCustomization) {
        String root = "src/main/java/com/azure/messaging/webpubsub/chat/";
        for (String file : new String[] {
            "WebPubSubChatServiceClient.java",
            "WebPubSubChatServiceAsyncClient.java",
            "implementation/WebPubSubChatServiceClientImpl.java",
            "models/ChatTopic.java" }) {
            String path = root + file;
            String source = libraryCustomization.getRawEditor().getFileContent(path);
            libraryCustomization.getRawEditor().replaceFile(path, correctLifecycleDocumentation(source));
        }
    }

    static String correctLifecycleDocumentation(String source) {
        for (MethodDeclaration method : StaticJavaParser.parse(source).findAll(MethodDeclaration.class)) {
            if (!method.getJavadocComment().isPresent()) {
                continue;
            }
            String original = method.getJavadocComment().get().getContent();
            String corrected = original;
            String responseHeading = "<p><strong>Response Body Schema</strong></p>";
            int responseStart = corrected.indexOf(responseHeading);
            if (responseStart >= 0) {
                corrected = corrected.substring(0, responseStart)
                    + corrected.substring(responseStart).replace("(Optional, Required on create)", "(Required)");
            }
            int requestStart = corrected.indexOf("<p><strong>Request Body Schema</strong></p>");
            if (requestStart >= 0) {
                if (responseStart < requestStart) {
                    throw new IllegalStateException("Missing response schema for " + method.getNameAsString());
                }
                String request = corrected.substring(requestStart, responseStart);
                String readOnlyFields;
                String name = method.getNameAsString();
                if (name.startsWith("updateMessage")) {
                    readOnlyFields = "id|createdAt|etag";
                } else if (name.startsWith("createOrReplaceRole")) {
                    readOnlyFields = "name|etag";
                } else if (name.startsWith("createOrReplaceRoomMember")) {
                    readOnlyFields = "userId|etag";
                } else if (name.startsWith("createOrReplaceRoom")) {
                    readOnlyFields = "id|defaultConversation|etag";
                } else if (name.startsWith("createOrReplaceUser")) {
                    readOnlyFields = "id|etag";
                } else if (name.startsWith("createOrReplaceTopic") || name.startsWith("updateTopic")) {
                    readOnlyFields = "id|createdAt|createdBy|conversationId|etag";
                } else {
                    throw new IllegalStateException("Unrecognized request schema for " + name);
                }
                request = request.replaceAll("(?m)^\\h*\\*\\h+(?:" + readOnlyFields + "):[^\\r\\n]*\\R", "");
                request = request.replace("(Optional, Required on create)",
                    name.startsWith("update") ? "(Optional)" : "(Required)");
                if (name.startsWith("createOrReplaceTopic")) {
                    request = request.replaceAll("(state: [^\\r\\n]+)\\(Required\\)", "$1(Optional)");
                }
                corrected = corrected.substring(0, requestStart) + request + corrected.substring(responseStart);
            }
            if (method.getNameAsString().equals("setState")) {
                corrected = corrected.replace("Required when create the resource.",
                    "Optional when creating a topic. Set this property to change the topic state when updating it.");
            }
            if (!original.equals(corrected)) {
                source = source.replace(original, corrected);
            }
        }
        return source;
    }

    private static void customizeTokenClient(PackageCustomization chatPackage, String className) {
        chatPackage.getClass(className).customizeAst(ast -> {
            ast.getImports()
                .removeIf(importDeclaration -> importDeclaration.getNameAsString()
                    .equals("com.azure.messaging.webpubsub.chat.implementation.models.GenerateClientTokenResponse"));

            ast.getClassByName(className).ifPresent(client -> {
                client.getMethodsByName("generateClientToken").forEach(method -> method.remove());
                client.getMethodsByName("generateClientTokenWithResponse").forEach(method -> method.remove());
            });
        });
    }

    private static void removeTrailingJavadocWhitespace(LibraryCustomization libraryCustomization) {
        String path
            = "src/main/java/com/azure/messaging/webpubsub/chat/implementation/WebPubSubChatServiceClientImpl.java";
        String implementation = libraryCustomization.getRawEditor().getFileContent(path);
        libraryCustomization.getRawEditor()
            .replaceFile(path, implementation.replace("* \r\n", "*\r\n").replace("* \n", "*\n"));
    }
}
