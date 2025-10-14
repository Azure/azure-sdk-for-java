// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Contains customizations for Azure Search's index swagger code generation.
 */
public class SearchIndexCustomizations extends Customization {
    private static final String VARARG_METHOD_TEMPLATE
        = "{ this.%1$s = (%1$s == null) ? null : Arrays.asList(%1$s); return this; }";

    @Override
    public void customize(LibraryCustomization customization, Logger logger) {
        customizeModelsPackage(customization.getPackage("com.azure.search.documents.models"));
        customizeImplementationModelsPackage(customization.getPackage("com.azure.search.documents.implementation.models"));

        // Remove all GET-based documents APIs as the SDK doesn't use them.
        customization.getClass("com.azure.search.documents.implementation", "DocumentsImpl").customizeAst(ast ->
            ast.getClassByName("DocumentsImpl").ifPresent(clazz -> {
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
                    .map(BodyDeclaration::asClassOrInterfaceDeclaration)
                    .filter(member -> "DocumentsService".equals(member.getNameAsString()))
                    .findFirst()
                    .ifPresent(interfaceClazz -> {
                        interfaceClazz.getMethodsByName("searchGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("searchGetSync").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("suggestGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("suggestGetSync").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("autocompleteGet").forEach(MethodDeclaration::remove);
                        interfaceClazz.getMethodsByName("autocompleteGetSync").forEach(MethodDeclaration::remove);
                    });
            }));
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

        customizeAst(packageCustomization.getClass("QueryAnswerResult"),
            clazz -> clazz.getMethodsByName("setAdditionalProperties").forEach(Node::remove));
        customizeAst(packageCustomization.getClass("QueryCaptionResult"),
            clazz -> clazz.getMethodsByName("setAdditionalProperties").forEach(Node::remove));
    }

    private void customizeAutocompleteOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.tryAddImportToParentCompilationUnit(JsonSerializable.class);
            clazz.tryAddImportToParentCompilationUnit(JsonReader.class);
            clazz.tryAddImportToParentCompilationUnit(JsonWriter.class);
            clazz.tryAddImportToParentCompilationUnit(JsonToken.class);
            clazz.tryAddImportToParentCompilationUnit(IOException.class);

            clazz.addImplementedType("JsonSerializable<AutocompleteOptions>");

            clazz.getMethodsByName("isUseFuzzyMatching").forEach(method -> method.setName("useFuzzyMatching"));
            addVarArgsOverload(clazz, "searchFields");

            clazz.addMethod("toJson", Modifier.Keyword.PUBLIC)
                .addMarkerAnnotation("Override")
                .setType("JsonWriter")
                .addParameter("JsonWriter", "jsonWriter")
                .addThrownException(IOException.class)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline("{", "jsonWriter.writeStartObject();",
                    "jsonWriter.writeStringField(\"autocompleteMode\", this.autocompleteMode == null ? null : this.autocompleteMode.toString());",
                    "jsonWriter.writeStringField(\"$filter\", this.filter);",
                    "jsonWriter.writeBooleanField(\"UseFuzzyMatching\", this.useFuzzyMatching);",
                    "jsonWriter.writeStringField(\"highlightPostTag\", this.highlightPostTag);",
                    "jsonWriter.writeStringField(\"highlightPreTag\", this.highlightPreTag);",
                    "jsonWriter.writeNumberField(\"minimumCoverage\", this.minimumCoverage);",
                    "jsonWriter.writeArrayField(\"searchFields\", this.searchFields, (writer, element) -> writer.writeString(element));",
                    "jsonWriter.writeNumberField(\"$top\", this.top);", "return jsonWriter.writeEndObject();", "}")))
                .setJavadocComment("{@inheritDoc}");

            clazz.addMethod("fromJson", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("AutocompleteOptions")
                .addParameter("JsonReader", "jsonReader")
                .addThrownException(IOException.class)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline("{", "return jsonReader.readObject(reader -> {",
                    "    AutocompleteOptions deserializedAutocompleteOptions = new AutocompleteOptions();",
                    "    while (reader.nextToken() != JsonToken.END_OBJECT) {",
                    "        String fieldName = reader.getFieldName();", "        reader.nextToken();",
                    "        if (\"autocompleteMode\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.autocompleteMode = AutocompleteMode.fromString(reader.getString());",
                    "        } else if (\"$filter\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.filter = reader.getString();",
                    "        } else if (\"UseFuzzyMatching\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.useFuzzyMatching = reader.getNullable(JsonReader::getBoolean);",
                    "        } else if (\"highlightPostTag\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.highlightPostTag = reader.getString();",
                    "        } else if (\"highlightPreTag\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.highlightPreTag = reader.getString();",
                    "        } else if (\"minimumCoverage\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.minimumCoverage = reader.getNullable(JsonReader::getDouble);",
                    "        } else if (\"searchFields\".equals(fieldName)) {",
                    "            List<String> searchFields = reader.readArray(reader1 -> reader1.getString());",
                    "            deserializedAutocompleteOptions.searchFields = searchFields;",
                    "        } else if (\"$top\".equals(fieldName)) {",
                    "            deserializedAutocompleteOptions.top = reader.getNullable(JsonReader::getInt);",
                    "        } else {", "            reader.skipChildren();", "        }", "    }",
                    "    return deserializedAutocompleteOptions;", "});", "}")))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                    "Reads an instance of AutocompleteOptions from the JsonReader."))
                    .addBlockTag("param", "jsonReader", "The JsonReader being read.")
                    .addBlockTag("return",
                        "An instance of AutocompleteOptions if the JsonReader was pointing to an instance of it, or null if it was pointing to JSON null.")
                    .addBlockTag("throws", "IOException If an error occurs while reading the AutocompleteOptions."));
        });
    }

    private void customizeSuggestOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.tryAddImportToParentCompilationUnit(JsonSerializable.class);
            clazz.tryAddImportToParentCompilationUnit(JsonReader.class);
            clazz.tryAddImportToParentCompilationUnit(JsonWriter.class);
            clazz.tryAddImportToParentCompilationUnit(JsonToken.class);
            clazz.tryAddImportToParentCompilationUnit(IOException.class);

            clazz.addImplementedType("JsonSerializable<SuggestOptions>");

            clazz.getMethodsByName("isUseFuzzyMatching").forEach(method -> method.setName("useFuzzyMatching"));

            addVarArgsOverload(clazz, "orderBy");
            addVarArgsOverload(clazz, "searchFields");
            addVarArgsOverload(clazz, "select");

            clazz.addMethod("toJson", Modifier.Keyword.PUBLIC)
                .addMarkerAnnotation("Override")
                .setType("JsonWriter")
                .addParameter("JsonWriter", "jsonWriter")
                .addThrownException(IOException.class)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline("{", "jsonWriter.writeStartObject();",
                    "jsonWriter.writeStringField(\"$filter\", this.filter);",
                    "jsonWriter.writeBooleanField(\"UseFuzzyMatching\", this.useFuzzyMatching);",
                    "jsonWriter.writeStringField(\"highlightPostTag\", this.highlightPostTag);",
                    "jsonWriter.writeStringField(\"highlightPreTag\", this.highlightPreTag);",
                    "jsonWriter.writeNumberField(\"minimumCoverage\", this.minimumCoverage);",
                    "jsonWriter.writeArrayField(\"OrderBy\", this.orderBy, (writer, element) -> writer.writeString(element));",
                    "jsonWriter.writeArrayField(\"searchFields\", this.searchFields, (writer, element) -> writer.writeString(element));",
                    "jsonWriter.writeArrayField(\"$select\", this.select, (writer, element) -> writer.writeString(element));",
                    "jsonWriter.writeNumberField(\"$top\", this.top);", "return jsonWriter.writeEndObject();", "}")))
                .setJavadocComment("{@inheritDoc}");

            clazz.addMethod("fromJson", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("SuggestOptions")
                .addParameter("JsonReader", "jsonReader")
                .addThrownException(IOException.class)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline("{", "return jsonReader.readObject(reader -> {",
                    "    SuggestOptions deserializedSuggestOptions = new SuggestOptions();",
                    "    while (reader.nextToken() != JsonToken.END_OBJECT) {",
                    "        String fieldName = reader.getFieldName();", "        reader.nextToken();",
                    "        if (\"$filter\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.filter = reader.getString();",
                    "        } else if (\"UseFuzzyMatching\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.useFuzzyMatching = reader.getNullable(JsonReader::getBoolean);",
                    "        } else if (\"highlightPostTag\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.highlightPostTag = reader.getString();",
                    "        } else if (\"highlightPreTag\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.highlightPreTag = reader.getString();",
                    "        } else if (\"minimumCoverage\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.minimumCoverage = reader.getNullable(JsonReader::getDouble);",
                    "        } else if (\"OrderBy\".equals(fieldName)) {",
                    "            List<String> orderBy = reader.readArray(reader1 -> reader1.getString());",
                    "            deserializedSuggestOptions.orderBy = orderBy;",
                    "        } else if (\"searchFields\".equals(fieldName)) {",
                    "            List<String> searchFields = reader.readArray(reader1 -> reader1.getString());",
                    "            deserializedSuggestOptions.searchFields = searchFields;",
                    "        } else if (\"$select\".equals(fieldName)) {",
                    "            List<String> select = reader.readArray(reader1 -> reader1.getString());",
                    "            deserializedSuggestOptions.select = select;",
                    "        } else if (\"$top\".equals(fieldName)) {",
                    "            deserializedSuggestOptions.top = reader.getNullable(JsonReader::getInt);",
                    "        } else {", "            reader.skipChildren();", "        }", "    }",
                    "    return deserializedSuggestOptions;", "});", "}")))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText(
                    "Reads an instance of SuggestOptions from the JsonReader.")).addBlockTag("param", "jsonReader",
                        "The JsonReader being read.")
                    .addBlockTag("return",
                        "An instance of SuggestOptions if the JsonReader was pointing to an instance of it, or null if it was pointing to JSON null.")
                    .addBlockTag("throws", "IOException If an error occurs while reading the SuggestOptions."));
        });
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchOptions(packageCustomization.getClass("SearchOptions"));
        customizeIndexAction(packageCustomization.getClass("IndexAction"));
    }

    private void customizeSearchOptions(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isIncludeTotalCount").forEach(method -> method.setName("isTotalCountIncluded"));

            addVarArgsOverload(clazz, "facets");
            addVarArgsOverload(clazz, "orderBy");
            addVarArgsOverload(clazz, "searchFields");
            addVarArgsOverload(clazz, "select");
            addVarArgsOverload(clazz, "highlightFields");
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
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").forEach(method -> method
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock("{ this.fields = (fields == null) ? null : String.join(\",\", fields);"
                    + "return this; }"))));
    }

    private void customizeVectorizedQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").forEach(method -> method
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))));
    }

    private void customizeVectorizableTextQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("setFields").forEach(method -> method
                .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true))));
            clazz.getFieldByName("queryRewrites").ifPresent(field -> field.getVariable(0).setType("QueryRewrites"));
            clazz.getMethodsByName("getQueryRewrites").forEach(method -> method.setType("QueryRewrites"));
            clazz.getMethodsByName("setQueryRewrites").forEach(method ->
                method.setParameters(new NodeList<>(new Parameter().setType("QueryRewrites").setName("queryRewrites"))));

            clazz.getMethodsByName("fromJson").forEach(method -> method.getBody().ifPresent(body ->
                method.setBody(StaticJavaParser.parseBlock(body.toString().replace("QueryRewritesType", "QueryRewrites")))));
        });
    }

    private void customizeIndexAction(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("String", "rawDocument");
            clazz.addMethod("getRawDocument", Modifier.Keyword.PUBLIC)
                .setType("String")
                .setBody(new BlockStmt(new NodeList<>(StaticJavaParser.parseStatement("return this.rawDocument;"))))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Gets the raw JSON document."))
                    .addBlockTag("return", "The raw JSON document."));

            clazz.addMethod("setRawDocument", Modifier.Keyword.PUBLIC)
                .setType("IndexAction")
                .addParameter("String", "rawDocument")
                .setBody(StaticJavaParser.parseBlock("{ this.rawDocument = rawDocument; return this; }"))
                .setJavadocComment(new Javadoc(JavadocDescription.parseText("Sets the raw JSON document."))
                    .addBlockTag("param", "rawDocument", "The raw JSON document.")
                    .addBlockTag("return", "the IndexAction object itself."));
        });
    }

    private void customizeIndexingResult(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addImplementedType(Serializable.class);
            clazz.addFieldWithInitializer("long", "serialVersionUID", new LongLiteralExpr("-8604424005271188140L"),
                Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL);

            for (String name : Arrays.asList("key", "errorMessage", "succeeded", "statusCode")) {
                clazz.getFieldByName(name).ifPresent(field -> field.getComment()
                    .ifPresent(doc -> field.setJavadocComment(doc.asBlockComment().getContent())));
            }
        });
    }

    private void customizeVectorizableImageUrlQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").forEach(method -> method
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock("{ super.setFields(fields); return this; }"))));
    }

    private void customizeVectorizableImageBinaryQuery(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("setFields").forEach(method -> method
            .setParameters(new NodeList<>(new Parameter().setType("String").setName("fields").setVarArgs(true)))
            .setBody(StaticJavaParser.parseBlock("{ super.setFields(fields); return this; }"))));
    }

    private void customizeSearchScoreThreshold(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("getValue").forEach(method ->
            method.setJavadocComment(new Javadoc(JavadocDescription.parseText("Get the value property: The threshold "
                + "will filter based on the '@search.score' value. Note this is the `@search.score` returned as part "
                + "of the search response. The threshold direction will be chosen for higher `@search.score`."))
                .addBlockTag("return", "the value."))));
    }

    private static void customizeAst(ClassCustomization classCustomization,
        Consumer<ClassOrInterfaceDeclaration> consumer) {
        classCustomization.customizeAst(ast -> consumer.accept(ast.getClassByName(classCustomization.getClassName())
            .orElseThrow(() -> new RuntimeException("Class not found. " + classCustomization.getClassName()))));
    }

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static void addVarArgsOverload(ClassOrInterfaceDeclaration clazz, String parameterName) {
        clazz.tryAddImportToParentCompilationUnit(Arrays.class);

        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        String varargMethod = String.format(VARARG_METHOD_TEMPLATE, parameterName);

        Javadoc copyJavadoc = clazz.getMethodsByName(methodName).get(0).getJavadoc().get();
        clazz.addMethod(methodName, Modifier.Keyword.PUBLIC)
            .setType(clazz.getNameAsString())
            .addParameter(StaticJavaParser.parseParameter("String" + "... " + parameterName))
            .setBody(StaticJavaParser.parseBlock(varargMethod))
            .setJavadocComment(copyJavadoc);
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
