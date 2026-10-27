// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
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
        libraryCustomization.getPackage(PACKAGE_NAME + ".models")
            .getClass("MessageContent")
            .customizeAst(WebPubSubChatCustomization::customizeMessageContent);

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

    static void customizeMessageContent(CompilationUnit ast) {
        ClassOrInterfaceDeclaration model = ast.getClassByName("MessageContent")
            .orElseThrow(() -> new IllegalStateException("MessageContent model not found."));
        ast.getImports()
            .removeIf(
                importDeclaration -> importDeclaration.getNameAsString().equals("com.azure.core.annotation.Fluent")
                    || importDeclaration.getNameAsString().equals("java.util.HashSet")
                    || importDeclaration.getNameAsString().equals("java.util.Set"));
        ast.addImport("com.azure.core.annotation.Immutable");
        ast.addImport("com.azure.core.util.logging.ClientLogger");
        ast.addImport("java.util.Objects");
        model.getAnnotations().removeIf(annotation -> annotation.getNameAsString().equals("Fluent"));
        if (!model.getAnnotationByName("Immutable").isPresent()) {
            model.addMarkerAnnotation("Immutable");
        }
        model.setJavadocComment("Message content containing exactly one of text or binary data.\n"
            + "Use {@link #fromText(String)} or {@link #fromBinary(byte[])} to create content.\n"
            + "The getter for the unselected representation returns {@code null}.");
        model.getFieldByName("text").get().setFinal(true);
        model.getFieldByName("binary").get().setFinal(true);
        model.getFieldByName("updatedProperties").ifPresent(field -> field.remove());
        if (!model.getFieldByName("LOGGER").isPresent()) {
            model.addFieldWithInitializer("ClientLogger", "LOGGER",
                StaticJavaParser.parseExpression("new ClientLogger(MessageContent.class)"), Modifier.Keyword.PRIVATE,
                Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);
        }
        model.getConstructors().forEach(constructor -> constructor.remove());
        model.addMember(StaticJavaParser
            .parseBodyDeclaration(String.join("\n", "private MessageContent(String text, byte[] binary) {",
                "    this.text = text;", "    this.binary = CoreUtils.clone(binary);", "}")));
        model.getMethodsByName("setText").forEach(method -> method.remove());
        model.getMethodsByName("setBinary").forEach(method -> method.remove());
        model.getMethodsByName("fromText").forEach(method -> method.remove());
        model.getMethodsByName("fromBinary").forEach(method -> method.remove());
        model.addMember(StaticJavaParser.parseBodyDeclaration(String.join("\n", "/**",
            " * Creates text message content.", " *", " * @param text the text content, which may be empty.",
            " * @return content containing the text and no binary data.",
            " * @throws NullPointerException if text is null.", " */",
            "public static MessageContent fromText(String text) {",
            "    return new MessageContent(Objects.requireNonNull(text, \"'text' cannot be null.\"), null);", "}")));
        model.addMember(StaticJavaParser.parseBodyDeclaration(
            String.join("\n", "/**", " * Creates binary message content. The supplied array is defensively copied.",
                " *", " * @param binary the binary content, which may be empty.",
                " * @return content containing the binary data and no text.",
                " * @throws NullPointerException if binary is null.", " */",
                "public static MessageContent fromBinary(byte[] binary) {",
                "    return new MessageContent(null, Objects.requireNonNull(binary, \"'binary' cannot be null.\"));",
                "}")));
        model.getMethodsByName("toJsonMergePatch").forEach(method -> method.remove());
        model.getMethodsByName("toJson")
            .get(0)
            .setBody(StaticJavaParser.parseBlock(String.join("\n", "{", "    jsonWriter.writeStartObject();",
                "    if (this.text != null) {", "        jsonWriter.writeStringField(\"text\", this.text);",
                "    } else {", "        jsonWriter.writeBinaryField(\"binary\", this.binary);", "    }",
                "    return jsonWriter.writeEndObject();", "}")));
        model.getMethodsByName("fromJson")
            .get(0)
            .setBody(StaticJavaParser.parseBlock(String.join("\n", "{", "    return jsonReader.readObject(reader -> {",
                "        String text = null;", "        byte[] binary = null;",
                "        while (reader.nextToken() != JsonToken.END_OBJECT) {",
                "            String fieldName = reader.getFieldName();", "            reader.nextToken();",
                "            if (\"text\".equals(fieldName)) {", "                text = reader.getString();",
                "            } else if (\"binary\".equals(fieldName)) {",
                "                binary = reader.getBinary();", "            } else {",
                "                reader.skipChildren();", "            }", "        }",
                "        if ((text == null) == (binary == null)) {",
                "            throw LOGGER.logExceptionAsError(new IllegalStateException(",
                "                \"Message content must contain exactly one non-null text or binary value.\"));",
                "        }", "        return text != null ? fromText(text) : fromBinary(binary);", "    });", "}")));
        model.getMethodsByName("fromJson")
            .get(0)
            .setJavadocComment(String.join("\n", "Reads text or binary message content from JSON.", "",
                "@param jsonReader the JSON reader.", "@return the message content, or null if the JSON value is null.",
                "@throws IllegalStateException if both representations are non-null or neither is non-null.",
                "@throws IOException if an error occurs while reading the content."));
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
