// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
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

        removeTrailingJavadocWhitespace(libraryCustomization);
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
