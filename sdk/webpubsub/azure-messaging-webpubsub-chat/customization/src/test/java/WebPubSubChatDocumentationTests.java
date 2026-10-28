// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebPubSubChatDocumentationTests {
    private static final String REQUEST = "<p><strong>Request Body Schema</strong></p>";
    private static final String RESPONSE = "<p><strong>Response Body Schema</strong></p>";
    private static final Path SOURCE
        = Paths.get("..", "src", "main", "java", "com", "azure", "messaging", "webpubsub", "chat");

    @Test
    public void generatedDocumentationMatchesCustomization() throws IOException {
        for (String file : Arrays.asList("WebPubSubChatServiceClient.java", "WebPubSubChatServiceAsyncClient.java",
            "implementation/WebPubSubChatServiceClientImpl.java")) {
            String source = new String(Files.readAllBytes(SOURCE.resolve(file)), StandardCharsets.UTF_8);
            assertEquals(source, WebPubSubChatCustomization.correctLifecycleDocumentation(source), file);
        }
    }

    @Test
    public void allWriteSchemasRespectVisibility() throws IOException {
        for (String file : Arrays.asList("WebPubSubChatServiceClient.java", "WebPubSubChatServiceAsyncClient.java",
            "implementation/WebPubSubChatServiceClientImpl.java")) {
            List<MethodDeclaration> methods
                = StaticJavaParser.parse(SOURCE.resolve(file)).findAll(MethodDeclaration.class);
            int requests = 0;
            for (MethodDeclaration method : methods) {
                String doc = method.getJavadocComment().map(comment -> comment.getContent()).orElse("");
                if (doc.contains(RESPONSE)) {
                    assertFalse(doc.substring(doc.indexOf(RESPONSE)).contains("Optional, Required on create"),
                        method.getNameAsString());
                }
                if (!doc.contains(REQUEST)) {
                    continue;
                }
                requests++;
                String request = doc.substring(doc.indexOf(REQUEST), doc.indexOf(RESPONSE));
                assertFalse(request.contains("etag:"), method.getNameAsString());
                assertFalse(request.contains("Required on create"), method.getNameAsString());
                for (String readOnly : Arrays.asList("id:", "name:", "userId:", "createdAt:", "defaultConversation:",
                    "conversationId:")) {
                    assertFalse(java.util.regex.Pattern.compile("(?m)^\\h*\\*\\h+" + readOnly).matcher(request).find(),
                        method.getNameAsString() + ": " + readOnly);
                }
                String name = method.getNameAsString();
                if (name.startsWith("updateMessage")) {
                    assertTrue(request.contains("createdBy: String (Optional)"));
                    assertTrue(request.contains("content (Optional)"));
                    assertTrue(request.contains("text: String (Optional)"));
                    assertTrue(request.contains("binary: byte[] (Optional)"));
                    assertTrue(doc.substring(doc.indexOf(RESPONSE)).contains("content (Required)"));
                } else if (name.startsWith("updateTopic") || name.startsWith("createOrReplaceTopic")) {
                    assertFalse(request.contains("createdBy:"));
                    assertTrue(request
                        .contains("title: String (" + (name.startsWith("update") ? "Optional" : "Required") + ")"));
                    assertTrue(request.contains("state: String(Normal/Archived/SoftDeleted) (Optional)"));
                    assertTrue(doc.substring(doc.indexOf(RESPONSE)).contains("title: String (Required)"));
                    assertTrue(doc.substring(doc.indexOf(RESPONSE))
                        .contains("state: String(Normal/Archived/SoftDeleted) (Required)"));
                } else if (name.startsWith("createOrReplaceRole")) {
                    assertTrue(request.contains("permissions (Required)"));
                } else if (name.startsWith("createOrReplaceRoomMember")) {
                    assertTrue(request.contains("roleName: String (Required)"));
                } else if (name.startsWith("createOrReplaceRoom")) {
                    assertTrue(request.contains("title: String (Required)"));
                } else if (name.startsWith("createOrReplaceUser")) {
                    assertTrue(request.contains("kind: String(Human) (Required)"));
                    assertTrue(request.contains("nickname: String (Required)"));
                }
                assertTrue(doc.contains("@param resource"));
                assertTrue(doc.contains("If-Match"));
            }
            assertEquals(file.startsWith("implementation") ? 14 : 7, requests, file);
        }
    }

    @Test
    public void repairsEmitterDocumentationWithoutChangingCode() {
        String source = "class Example {\n" + "/**\n * " + REQUEST + "\n" + " *     id: String (Required)\n"
            + " *     title: String (Optional, Required on create)\n"
            + " *     state: String(Normal/Archived/SoftDeleted) (Optional, Required on create)\n"
            + " *     etag: String (Required)\n" + " * " + RESPONSE + "\n" + " *     id: String (Required)\n"
            + " *     title: String (Optional, Required on create)\n"
            + " *     state: String(Normal/Archived/SoftDeleted) (Optional, Required on create)\n"
            + " * @param roomId Room identifier.\n */\n"
            + "public void updateTopicWithResponse(String roomId) { System.out.println(roomId); }\n}";
        String corrected = WebPubSubChatCustomization.correctLifecycleDocumentation(source);
        assertFalse(corrected.equals(source));
        assertTrue(corrected.contains("title: String (Optional)"));
        assertTrue(corrected.contains("title: String (Required)"));
        assertEquals(corrected, WebPubSubChatCustomization.correctLifecycleDocumentation(corrected));
        assertEquals(withoutComments(source), withoutComments(corrected));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "updateMessage",
            "updateTopic",
            "createOrReplaceTopic",
            "createOrReplaceRole",
            "createOrReplaceRoom",
            "createOrReplaceRoomMember",
            "createOrReplaceUser" })
    public void repairsAllWriteOperations(String operation) {
        String fields;
        String readOnly;
        switch (operation) {
            case "updateMessage":
                fields = "createdBy: String (Optional, Required on create)\n"
                    + " *     content (Optional, Required on create): {\n"
                    + " *         text: String (Optional)\n *     }";
                readOnly = "id,createdAt,etag";
                break;

            case "updateTopic":
            case "createOrReplaceTopic":
                fields = "title: String (Optional, Required on create)\n"
                    + " *     state: String(Normal/Archived/SoftDeleted) (Optional, Required on create)";
                readOnly = "id,createdAt,createdBy,conversationId,etag";
                break;

            case "createOrReplaceRole":
                fields = "permissions (Required): []";
                readOnly = "name,etag";
                break;

            case "createOrReplaceRoom":
                fields = "title: String (Required)";
                readOnly = "id,defaultConversation,etag";
                break;

            case "createOrReplaceRoomMember":
                fields = "roleName: String (Required)";
                readOnly = "userId,etag";
                break;

            case "createOrReplaceUser":
                fields = "nickname: String (Required)\n *     kind: String(Human) (Required)";
                readOnly = "id,etag";
                break;

            default:
                throw new IllegalArgumentException(operation);
        }
        String schema = " *     " + fields + "\n";
        for (String field : readOnly.split(",")) {
            schema += " *     " + field + ": String (Required)\n";
        }
        for (String suffix : Arrays.asList("WithResponse", "WithResponseAsync")) {
            String source = "class Example {\n/**\n * " + REQUEST + "\n" + schema + " * " + RESPONSE + "\n" + schema
                + " * @param resource The resource.\n */\nvoid " + operation + suffix + "(Object resource) {}\n}";
            String corrected = WebPubSubChatCustomization.correctLifecycleDocumentation(source);
            String request = corrected.substring(corrected.indexOf(REQUEST), corrected.indexOf(RESPONSE));
            String response = corrected.substring(corrected.indexOf(RESPONSE));
            for (String field : readOnly.split(",")) {
                assertFalse(request.contains(" *     " + field + ":"), operation + ": " + field);
                assertTrue(response.contains(" *     " + field + ":"), operation + ": " + field);
            }
            assertFalse(corrected.contains("Optional, Required on create"), operation);
            if (operation.equals("updateMessage")) {
                assertTrue(request.contains("content (Optional)"));
                assertTrue(response.contains("content (Required)"));
                assertTrue(response.contains("text: String (Optional)"));
            } else if (operation.endsWith("Topic")) {
                assertTrue(request
                    .contains("title: String (" + (operation.startsWith("update") ? "Optional" : "Required") + ")"));
                assertTrue(request.contains("state: String(Normal/Archived/SoftDeleted) (Optional)"));
                assertTrue(response.contains("title: String (Required)"));
                assertTrue(response.contains("state: String(Normal/Archived/SoftDeleted) (Required)"));
            } else {
                assertTrue(request.contains(fields));
            }
            assertEquals(corrected, WebPubSubChatCustomization.correctLifecycleDocumentation(corrected), operation);
            assertEquals(withoutComments(source), withoutComments(corrected), operation);
        }
    }

    @Test
    public void stateSetterPreservesHandwrittenDocumentation() throws IOException {
        MethodDeclaration setter = StaticJavaParser.parse(SOURCE.resolve("models/ChatTopic.java"))
            .findAll(MethodDeclaration.class)
            .stream()
            .filter(method -> method.getNameAsString().equals("setState"))
            .findFirst()
            .get();
        assertFalse(setter.getAnnotationByName("Generated").isPresent());
        String doc = setter.getJavadocComment().get().getContent().replaceAll("\\s*\\R\\s*\\*\\s*", " ");
        assertTrue(doc.contains("When creating a topic, omit this property"));
        assertTrue(doc.contains("{@link ChatTopicState#NORMAL}"));
        assertTrue(doc.contains("Other states are only valid when updating an existing topic."));
        assertFalse(doc.contains("Required when create"));
    }

    @Test
    public void topicCreationDocumentsAllowedInitialState() throws IOException {
        for (String file : Arrays.asList("WebPubSubChatServiceClient.java", "WebPubSubChatServiceAsyncClient.java",
            "implementation/WebPubSubChatServiceClientImpl.java")) {
            for (MethodDeclaration method : StaticJavaParser.parse(SOURCE.resolve(file))
                .findAll(MethodDeclaration.class)) {
                if (method.getNameAsString().startsWith("createOrReplaceTopic")
                    && method.getJavadocComment().isPresent()) {
                    String doc = method.getJavadocComment().get().getContent().replaceAll("\\s*\\R\\s*\\*\\s*", " ");
                    assertTrue(doc.contains("When creating a topic, omit state or set it to Normal."), file);
                    assertTrue(doc.contains("Other states are only valid when updating an existing topic."), file);
                }
            }
        }
    }

    private static String withoutComments(String source) {
        com.github.javaparser.ast.CompilationUnit ast = StaticJavaParser.parse(source);
        ast.getAllContainedComments().forEach(comment -> comment.remove());
        return ast.toString();
    }
}
