// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WebPubSubChatMessageContentCustomizationTests {
    @Test
    public void removesRegeneratedMutableMembersAndIsIdempotent() throws IOException {
        CompilationUnit ast = readModel();
        ClassOrInterfaceDeclaration original = ast.getClassByName("MessageContent").get();
        original.addMember(StaticJavaParser.parseBodyDeclaration(
            "private final java.util.Set<String> updatedProperties = new java.util.HashSet<>();"));
        original.addMember(
            StaticJavaParser.parseBodyDeclaration("public MessageContent setText(String text) { return this; }"));
        original.addMember(
            StaticJavaParser.parseBodyDeclaration("public MessageContent setBinary(byte[] binary) { return this; }"));
        original.addMember(StaticJavaParser
            .parseBodyDeclaration("private JsonWriter toJsonMergePatch(JsonWriter writer) { return writer; }"));

        WebPubSubChatCustomization.customizeMessageContent(ast);
        ClassOrInterfaceDeclaration model = ast.getClassByName("MessageContent").get();
        assertEquals(1, model.getConstructors().size());
        assertTrue(model.getConstructors().get(0).isPrivate());
        assertTrue(model.getMethodsByName("setText").isEmpty());
        assertTrue(model.getMethodsByName("setBinary").isEmpty());
        assertTrue(model.getMethodsByName("fromText").get(0).isStatic());
        assertTrue(model.getMethodsByName("fromBinary").get(0).isStatic());
        assertTrue(model.getFieldByName("text").get().isFinal());
        assertTrue(model.getFieldByName("binary").get().isFinal());
        assertFalse(model.getFieldByName("updatedProperties").isPresent());
        assertTrue(model.getAnnotationByName("Immutable").isPresent());

        String customized = ast.toString();
        WebPubSubChatCustomization.customizeMessageContent(ast);
        assertEquals(customized, ast.toString());
    }

    @Test
    public void checkedInModelIncludesCustomization() throws IOException {
        CompilationUnit expected = readModel();
        ClassOrInterfaceDeclaration model = expected.getClassByName("MessageContent").get();
        for (String name : new String[] { "toJson", "fromJson", "fromText", "fromBinary" }) {
            assertFalse(model.getMethodsByName(name).get(0).getAnnotationByName("Generated").isPresent());
        }
        for (String name : new String[] { "text", "binary" }) {
            assertFalse(model.getFieldByName(name).get().getAnnotationByName("Generated").isPresent());
        }
        CompilationUnit actual = expected.clone();
        WebPubSubChatCustomization.customizeMessageContent(actual);
        assertEquals(expected.toString(), actual.toString());
    }

    private static CompilationUnit readModel() throws IOException {
        String source = new String(Files.readAllBytes(Paths.get("..", "src", "main", "java", "com", "azure",
            "messaging", "webpubsub", "chat", "models", "MessageContent.java")), StandardCharsets.UTF_8);
        return StaticJavaParser.parse(source);
    }
}
