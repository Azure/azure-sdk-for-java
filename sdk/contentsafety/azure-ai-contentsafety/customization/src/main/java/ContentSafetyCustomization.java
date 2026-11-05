// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Node;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import static com.github.javaparser.javadoc.description.JavadocDescription.parseText;

/**
 * Code customizations for the Azure AI Content Safety SDK.
 */
public class ContentSafetyCustomization extends Customization {
    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        PackageCustomization models = customization.getPackage("com.azure.ai.contentsafety.models");

        customizeDetectProvenanceOptions(models);
        customizeAnalyzeImageExamples(customization.getRawEditor());
    }

    private static void customizeDetectProvenanceOptions(PackageCustomization models) {
        models.getClass("DetectProvenanceOptions")
            .customizeAst(ast -> ast.getClassByName("DetectProvenanceOptions")
                .ifPresent(clazz -> clazz.getConstructors().forEach(constructor -> {
                    if (constructor.getParameters().size() == 1
                        && "content".equals(constructor.getParameter(0).getNameAsString())) {
                        constructor.setJavadocComment(
                            new Javadoc(parseText("Creates an instance of DetectProvenanceOptions class."))
                                .addBlockTag("param", "content", "Source content to inspect."));
                        constructor.getAnnotationByName("Generated").ifPresent(Node::remove);
                    }
                })));
    }

    private static void customizeAnalyzeImageExamples(Editor editor) {
        String byteArrayExpression = "\"Y29udGVudDE=\".getBytes()";
        String binaryDataExpression = "com.azure.core.util.BinaryData.fromBytes(\"Y29udGVudDE=\".getBytes())";

        for (String path : new String[] {
            "src/samples/java/com/azure/ai/contentsafety/generated/AnalyzeImage.java",
            "src/test/java/com/azure/ai/contentsafety/generated/AnalyzeImageTests.java" }) {
            editor.replaceFile(path, editor.getFileContent(path).replace(byteArrayExpression, binaryDataExpression));
        }
    }
}
