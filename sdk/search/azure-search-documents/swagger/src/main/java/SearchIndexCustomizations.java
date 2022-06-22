// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * Contains customizations for Azure Search's index swagger code generation.
 */
public class SearchIndexCustomizations extends Customization {
    private static final String VARARG_METHOD_TEMPLATE = joinWithNewline(
        "public %s %s(%s... %s) {",
        "    this.%s = (%s == null) ? null : java.util.Arrays.asList(%s);",
        "    return this;",
        "}");

    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.implementation.models";
    private static final String MODELS = "com.azure.search.documents.models";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
        customizeImplementationModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        customizeAutocompleteOptions(packageCustomization.getClass("AutocompleteOptions"));
        customizeSuggestOptions(packageCustomization.getClass("SuggestOptions"));
    }

    private void customizeAutocompleteOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");
        addVarArgsOverload(classCustomization, "searchFields", "String");
    }

    private void customizeSuggestOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isUseFuzzyMatching").rename("useFuzzyMatching");

        addVarArgsOverload(classCustomization, "orderBy", "String");
        addVarArgsOverload(classCustomization, "searchFields", "String");
        addVarArgsOverload(classCustomization, "select", "String");
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        customizeSearchOptions(packageCustomization.getClass("SearchOptions"));
        customizeIndexAction(packageCustomization.getClass("IndexAction"));
    }

    private void customizeSearchOptions(ClassCustomization classCustomization) {
        classCustomization.getMethod("isIncludeTotalCount").rename("isTotalCountIncluded");

        addVarArgsOverload(classCustomization, "facets", "String");
        addVarArgsOverload(classCustomization, "orderBy", "String");
        addVarArgsOverload(classCustomization, "searchFields", "String");
        addVarArgsOverload(classCustomization, "select", "String");
        addVarArgsOverload(classCustomization, "highlightFields", "String");

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
        classCustomization
            .customizeAst(ast -> ast.getClassByName("IndexAction").get()
                .addPrivateField(String.class, "rawDocument"))
            .getProperty("rawDocument")
            .generateGetterAndSetter();

        classCustomization.getMethod("getRawDocument")
            .getJavadoc()
            .setDescription("Gets the raw JSON document.")
            .setReturn("The raw JSON document.");

        classCustomization.getMethod("setRawDocument")
            .getJavadoc()
            .setDescription("Sets the raw JSON document.")
            .setParam("rawDocument", "The raw JSON document.")
            .setReturn("the IndexAction object itself.");
    }

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static void addVarArgsOverload(ClassCustomization classCustomization, String parameterName,
        String parameterType) {
        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        String varargMethod = String.format(VARARG_METHOD_TEMPLATE, classCustomization.getClassName(), methodName,
            parameterType, parameterName, parameterName, parameterName, parameterName);

        classCustomization.addMethod(varargMethod).getJavadoc()
            .replace(classCustomization.getMethod(methodName).getJavadoc());
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
