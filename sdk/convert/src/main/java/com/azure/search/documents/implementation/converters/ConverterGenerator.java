// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class ConverterGenerator {
    // TODO
    private static final String CONVERTER_PATH = "com.azure.search.documents.implementation.converters";
    void generate(String externalModelPath,
        String internalModelPath,
        List<String> classNames) {
        Path dir = getPath();
        System.out.println("Write to " + dir);
        classNames.forEach(className ->
            generateConverter(externalModelPath,
                internalModelPath,
                className,
                dir));
    }

    private static void generateConverter(String externalModelPath,
        String internalModelPath,
        String className,
        Path outputPath) {
        ClassName externalModelClass = ClassName.get(externalModelPath, className);
        ClassName internalModelClass = ClassName.get(internalModelPath, className);

        MethodSpec mapToMethod = generateMapMethod(externalModelClass, internalModelClass);
        MethodSpec mapFromMethod = generateMapMethod(internalModelClass, externalModelClass);

        TypeSpec classConverterType = TypeSpec.classBuilder(className + "Converter")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addMethod(mapToMethod)
            .addMethod(mapFromMethod)
            .addJavadoc("Auto generated code for default converter.\n")
            .addJavadoc("Update {@code convert} methods if {@link $T} and \n{@link $T} mismatch.",
                externalModelClass, internalModelClass)
            .build();

        JavaFile javaFile = JavaFile.builder(CONVERTER_PATH, classConverterType)
            .indent("    ")
            .build();

        try {
//            javaFile.writeTo(System.out);
            System.out.println("To: " + outputPath + "\nFor " + className);
            javaFile.writeTo(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    Path getPath() {
        String path = getClass()
            .getClassLoader().getResource("output.json").getPath()
            .replace("output.json", "output");
        File dir = new File(path);
        dir.mkdir();
        return new File(path).toPath().getParent();
    }

    private static MethodSpec generateMapMethod(ClassName fromClass, ClassName toClass) {
        return MethodSpec.methodBuilder("convert")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(toClass)
            .addParameter(fromClass, "obj")
            .addStatement("return $T.convert(obj, $T.class)", DefaultConverter.class,
                toClass)
            .build();
    }

    public static void main(String[] args) {
        // TODO
        //List<String> classNames = List.of("AutocompleteOptions");
        // Read className under folder

        List<String> results = new ArrayList<String>();


        File[] files = new File("C://azure-sdk-for-java/sdk/search/azure-search-documents/src/main/java/com/azure/search/documents/models").listFiles();
//If this pathname does not denote a directory, then listFiles() returns null.

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName().replace(".java", ""));
            }
        }
        // TODO
        String externalModelPath = "com.azure.search.documents.models";
        // TODO
        String internalModelPath = "com.azure.search.documents.implementation.models";
        new ConverterGenerator().generate(externalModelPath, internalModelPath, results);
    }
}
