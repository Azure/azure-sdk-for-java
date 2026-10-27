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
    public void customizesGeneratedModelAndIsIdempotent() {
        CompilationUnit ast = StaticJavaParser
            .parse(String.join("\n", "import com.azure.core.annotation.Fluent;", "import java.util.Set;",
                "import java.util.HashSet;", "@Fluent public final class MessageContent {", "    private String text;",
                "    private byte[] binary;", "    private final Set<String> updatedProperties = new HashSet<>();",
                "    public MessageContent() {}", "    public MessageContent setText(String text) { return this; }",
                "    public MessageContent setBinary(byte[] binary) { return this; }",
                "    public JsonWriter toJson(JsonWriter writer) { return writer; }",
                "    private JsonWriter toJsonMergePatch(JsonWriter writer) { return writer; }",
                "    public static MessageContent fromJson(JsonReader reader) { return null; }", "}"));

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
        String source = new String(Files.readAllBytes(Paths.get("..", "src", "main", "java", "com", "azure",
            "messaging", "webpubsub", "chat", "models", "MessageContent.java")), StandardCharsets.UTF_8);
        CompilationUnit expected = StaticJavaParser.parse(source);
        CompilationUnit actual = expected.clone();
        WebPubSubChatCustomization.customizeMessageContent(actual);
        assertEquals(expected.toString(), actual.toString());
    }
}
