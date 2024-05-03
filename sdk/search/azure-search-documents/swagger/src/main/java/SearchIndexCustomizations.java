// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Contains customizations for Azure Search's index swagger code generation.
 */
public class SearchIndexCustomizations extends Customization {
    private static final String VARARG_METHOD_TEMPLATE = joinWithNewline(
        "{",
        "    this.%1$s = (%1$s == null) ? null : java.util.Arrays.asList(%1$s);",
        "    return this;",
        "}");

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeModelsPackage(libraryCustomization.getPackage("com.azure.search.documents.models"));
        customizeImplementationModelsPackage(libraryCustomization.getPackage("com.azure.search.documents.implementation.models"));

        // Remove all GET-based documents APIs as the SDK doesn't use them.
        libraryCustomization.getPackage("com.azure.search.documents.implementation")
            .getClass("DocumentsImpl")
            .customizeAst(ast -> {
                ClassOrInterfaceDeclaration clazz = ast.getClassByName("DocumentsImpl").get();
                clazz.getMethodsByName("searchGetWithResponseAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("searchGetWithResponse").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("searchGetAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("searchGet").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("suggestGetWithResponseAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("suggestGetWithResponse").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("suggestGetAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("suggestGet").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("autocompleteGetWithResponseAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("autocompleteGetWithResponse").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("autocompleteGetAsync").forEach(MethodDeclaration::remove);
                clazz.getMethodsByName("autocompleteGet").forEach(MethodDeclaration::remove);

                clazz.getMembers().stream()
                    .filter(BodyDeclaration::isClassOrInterfaceDeclaration)
                    .filter(member -> "DocumentsService".equals(member.asClassOrInterfaceDeclaration().getNameAsString()))
                    .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                    .findFirst()
                    .ifPresent(interfaceClazz -> {
                        interfaceClazz.getMethodsByName("searchGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("searchGetSync").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("suggestGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("suggestGetSync").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("autocompleteGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("autocompleteGetSync").forEach(MethodDeclaration::remove);
                    });
            });
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeAutocompleteOptions(packageCustomization.getClass("AutocompleteOptions"));
        customizeSuggestOptions(packageCustomization.getClass("SuggestOptions"));
        customizeIndexingResult(packageCustomization.getClass("IndexingResult"));
        customizeVectorQuery(packageCustomization.getClass("VectorQuery"));
        customizeVectorizedQuery(packageCustomization.getClass("VectorizedQuery"));
        customizeVectorizableTextQuery(packageCustomization.getClass("VectorizableTextQuery"));
        customizeVectorizableImageUrlQuery(packageCustomization.getClass("VectorizableImageUrlQuery"));
        customizeVectorizableImageBinaryQuery(packageCustomization.getClass("VectorizableImageBinaryQuery"));
        customizeSearchScoreThreshold(packageCustomization.getClass("SearchScoreThreshold"));

        packageCustomization.getClass("QueryAnswerResult").removeMethod("setAdditionalProperties");
        packageCustomization.getClass("QueryCaptionResult").removeMethod("setAdditionalProperties");
    }

    private void customizeAutocompleteOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isUseFuzzyMatching").get(0).setName("useFuzzyMatching");
            addVarArgsOverload(clazz, "searchFields", "String");
        });
    }

    private void customizeSuggestOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isUseFuzzyMatching").get(0).setName("useFuzzyMatching");

            addVarArgsOverload(clazz, "orderBy", "String");
            addVarArgsOverload(clazz, "searchFields", "String");
            addVarArgsOverload(clazz, "select", "String");
        });
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchOptions(packageCustomization.getClass("SearchOptions"));
        customizeIndexAction(packageCustomization.getClass("IndexAction"));
        // customizeSearchError(packageCustomization.getClass("SearchError"));
    }

    private void customizeSearchOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {;
            clazz.getMethodsByName("isIncludeTotalCount").get(0).setName("isTotalCountIncluded");

            addVarArgsOverload(clazz, "facets", "String");
            addVarArgsOverload(clazz, "orderBy", "String");
            addVarArgsOverload(clazz, "searchFields", "String");
            addVarArgsOverload(clazz, "select", "String");
            addVarArgsOverload(clazz, "highlightFields", "String");
        });

        // Can't be done right now as setScoringParameters uses String.
//        // Scoring parameters are slightly different as code generates them as String.
//        classCustomization.getMethod("setScoringParameters").replaceParameters("ScoringParameter... scoringParameters")
//            .replaceBody(
//                "this.scoringParameters = (scoringParameters == null)"
//                    + "    ? null"
//                    + "    : java.util.Arrays.stream(scoringParameters)"
//                    + "        .map(ScoringParameter::toString)"
//                    + "        .collect(java.util.stream.Collectors.toList());"
//                    + "return this;");
//
//        classCustomization.getMethod("getScoringParameters")
//            .setReturnType("List<ScoringParameter>",
//                "this.scoringParameters.stream().map(ScoringParameter::new).collect(java.util.stream.Collectors.toList())");
    }


private void customizeVectorQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").get(0)
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                "{",
                "    this.fields = (fields == null) ? null : String.join(\",\", fields);",
                "    return this;",
                "}"
            ))));
}

    private void customizeVectorizedQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").get(0)
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true))));
    }

    private void customizeVectorizableTextQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").get(0)
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true))));
    }

    private void customizeIndexAction(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("String", "rawDocument");
            clazz.addMethod("getRawDocument", Modifier.Keyword.PUBLIC).setType("String")
                .setBody(new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement("return this.rawDocument;"))))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "/**",
                    " * Gets the raw JSON document.",
                    " * @return The raw JSON document.",
                    " */"
                )));

            clazz.addMethod("setRawDocument", Modifier.Keyword.PUBLIC).setType("IndexAction")
                .addParameter("String", "rawDocument")
                .setBody(new BlockStmt(new NodeList<>(
                    StaticJavaParser.parseStatement("this.rawDocument = rawDocument;"),
                    StaticJavaParser.parseStatement("return this;")
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "/**",
                    " * Sets the raw JSON document.",
                    " * @param rawDocument The raw JSON document.",
                    " * @return the IndexAction object itself.",
                    " */"
                )));
        });
    }

    private void customizeIndexingResult(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addImplementedType(Serializable.class);
            clazz.addFieldWithInitializer("long", "serialVersionUID", new LongLiteralExpr("-8604424005271188140L"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

            FieldDeclaration field = clazz.getFieldByName("key").get();
            field.setJavadocComment(field.getComment().get().asBlockComment().getContent());

            field = clazz.getFieldByName("errorMessage").get();
            field.setJavadocComment(field.getComment().get().asBlockComment().getContent());

            field = clazz.getFieldByName("succeeded").get();
            field.setJavadocComment(field.getComment().get().asBlockComment().getContent());

            field = clazz.getFieldByName("statusCode").get();
            field.setJavadocComment(field.getComment().get().asBlockComment().getContent());
        });
    }

    private void customizeSearchError(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            MethodDeclaration fromJson = clazz.getMethodsByName("fromJson").get(0);

            clazz.addMethod("readSearchError", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC)
                .setType("SearchError")
                .addParameter("JsonReader", "jsonReader")
                .addThrownException(IOException.class)
                .setBody(fromJson.getBody().get());

            fromJson.setBody(StaticJavaParser.parseBlock(joinWithNewline(
                "{",
                "return jsonReader.readObject(reader -> {",
                "    // Buffer the next JSON object as SearchError can take two forms:",
                "    //",
                "    // - A SearchError object",
                "    // - A SearchError object wrapped in an \"error\" node.",
                "    JsonReader bufferedReader = reader.bufferObject();",
                "    bufferedReader.nextToken(); // Get to the START_OBJECT token.",
                "    while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {",
                "        String fieldName = bufferedReader.getFieldName();",
                "        bufferedReader.nextToken();",
                "",
                "        if (\"error\".equals(fieldName)) {",
                "            // If the SearchError was wrapped in the \"error\" node begin reading it now.",
                "            return readSearchError(bufferedReader);",
                "        } else {",
                "            bufferedReader.skipChildren();",
                "        }",
                "    }",
                "",
                "    // Otherwise reset the JsonReader and read the whole JSON object.",
                "    return readSearchError(bufferedReader.reset());",
                "});",
                "}"
            )));
        });
    }

    private void customizeVectorizableImageUrlQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").get(0)
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock("{\n" +
                "        super.setFields(fields);\n" +
                "        return this;\n" +
                "    }")));
    }

    private void customizeVectorizableImageBinaryQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").get(0)
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock("{\n" +
                "        super.setFields(fields);\n" +
                "        return this;\n" +
                "    }")));
    }

    private void customizeSearchScoreThreshold(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("getValue").get(0).setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                "/**",
                " * Get the value property: The threshold will filter based on the '@search.score' value. Note this is the",
                " *",
                " * `@search.score` returned as part of the search response. The threshold direction will be chosen for higher",
                " * `@search.score`.",
                " *",
                " * @return the value.",
                " */"
            )));
        });
    }

    private static void customizeAst(ClassCustomization classCustomization, Consumer<ClassOrInterfaceDeclaration> consumer) {
        classCustomization.customizeAst(ast -> consumer.accept(ast.getClassByName(classCustomization.getClassName())
            .orElseThrow(() -> new RuntimeException("Class not found. " + classCustomization.getClassName()))));
    }

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static void addVarArgsOverload(ClassOrInterfaceDeclaration clazz, String parameterName, String parameterType) {
        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        String varargMethod = String.format(VARARG_METHOD_TEMPLATE, parameterName);

        Javadoc copyJavadoc = clazz.getMethodsByName(methodName).get(0).getJavadoc().get();
        clazz.addMethod(methodName, Modifier.Keyword.PUBLIC).setType(clazz.getNameAsString())
            .addParameter(StaticJavaParser.parseParameter(parameterType + "... " + parameterName))
            .setBody(StaticJavaParser.parseBlock(varargMethod))
            .setJavadocComment(copyJavadoc);
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
