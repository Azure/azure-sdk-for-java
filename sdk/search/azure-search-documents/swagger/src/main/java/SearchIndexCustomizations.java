// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Locale;

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
            });
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeAutocompleteOptions(packageCustomization.getClass("AutocompleteOptions"));
        customizeSuggestOptions(packageCustomization.getClass("SuggestOptions"));
        customizeIndexingResult(packageCustomization.getClass("IndexingResult"));
    }

    private void customizeAutocompleteOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");
        classCustomization.customizeAst(ast -> addVarArgsOverload(
            ast.getClassByName(classCustomization.getClassName()).get(), classCustomization.getClassName(),
            "searchFields", "String"));
    }

    private void customizeSuggestOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");
        classCustomization.customizeAst(ast -> {
            String className = classCustomization.getClassName();
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(className).get();

            addVarArgsOverload(clazz, className, "orderBy", "String");
            addVarArgsOverload(clazz, className, "searchFields", "String");
            addVarArgsOverload(clazz, className, "select", "String");
        });
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchOptions(packageCustomization.getClass("SearchOptions"));
        customizeIndexAction(packageCustomization.getClass("IndexAction"));
    }

    private void customizeSearchOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isIncludeTotalCount").rename("isTotalCountIncluded");
        classCustomization.customizeAst(ast -> {
            String className = classCustomization.getClassName();
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(className).get();

            addVarArgsOverload(clazz, className, "facets", "String");
            addVarArgsOverload(clazz, className, "orderBy", "String");
            addVarArgsOverload(clazz, className, "searchFields", "String");
            addVarArgsOverload(clazz, className, "select", "String");
            addVarArgsOverload(clazz, className, "highlightFields", "String");
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

    private void customizeIndexAction(ClassCustomization classCustomization) {
        classCustomization.customizeAst(ast -> {
            String className = classCustomization.getClassName();
            ClassOrInterfaceDeclaration clazz = ast.getClassByName(className).get();

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
        classCustomization.customizeAst(ast -> {
            ClassOrInterfaceDeclaration clazz = ast.getClassByName("IndexingResult").get();
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

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static void addVarArgsOverload(ClassOrInterfaceDeclaration clazz, String className, String parameterName,
        String parameterType) {
        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        String varargMethod = String.format(VARARG_METHOD_TEMPLATE, parameterName);

        Javadoc copyJavadoc = clazz.getMethodsByName(methodName).get(0).getJavadoc().get();
        clazz.addMethod(methodName, Modifier.Keyword.PUBLIC).setType(className)
            .addParameter(StaticJavaParser.parseParameter(parameterType + "... " + parameterName))
            .setBody(StaticJavaParser.parseBlock(varargMethod))
            .setJavadocComment(copyJavadoc);
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}