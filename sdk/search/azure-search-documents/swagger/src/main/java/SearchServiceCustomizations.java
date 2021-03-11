// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;

import java.lang.reflect.Modifier;
import java.util.Locale;

public class SearchServiceCustomizations extends Customization {
    // Common modifier combinations
    private static final int PUBLIC_ABSTRACT = Modifier.PUBLIC | Modifier.ABSTRACT;
    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private static final String VARARG_METHOD_TEMPLATE =
        "public %s %s(%s... %s) {" +
        "    this.%s = (%s == null) ? null : java.util.Arrays.asList(%s);\n" +
        "    return this;\n" +
        "}";

    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.indexes.implementation.models";
    private static final String MODELS = "com.azure.search.documents.indexes.models";

    // Classes
    private static final String MAGNITUDE_SCORING_PARAMETERS = "MagnitudeScoringParameters";
    private static final String SCORING_FUNCTION = "ScoringFunction";
    private static final String SEARCH_FIELD_DATA_TYPE = "SearchFieldDataType";

    private static final String SIMILARITY_ALGORITHM = "SimilarityAlgorithm";
    private static final String BM_25_SIMILARITY_ALGORITHM = "BM25SimilarityAlgorithm";
    private static final String CLASSIC_SIMILARITY_ALGORITHM = "ClassicSimilarityAlgorithm";

    private static final String DATA_CHANGE_DETECTION_POLICY = "DataChangeDetectionPolicy";
    private static final String HIGH_WATER_MARK_CHANGE_DETECTION_POLICY = "HighWaterMarkChangeDetectionPolicy";
    private static final String SQL_INTEGRATED_CHANGE_TRACKING_POLICY = "SqlIntegratedChangeTrackingPolicy";

    private static final String DATA_DELETION_DETECTION_POLICY = "DataDeletionDetectionPolicy";
    private static final String SOFT_DELETE_COLUMN_DELETION_DETECTION_POLICY = "SoftDeleteColumnDeletionDetectionPolicy";

    private static final String CHAR_FILTER = "CharFilter";
    private static final String MAPPING_CHAR_FILTER = "MappingCharFilter";
    private static final String PATTERN_REPLACE_CHAR_FILTER = "PatternReplaceCharFilter";

    private static final String COGNITIVE_SERVICES_ACCOUNT = "CognitiveServicesAccount";
    private static final String DEFAULT_COGNITIVE_SERVICES_ACCOUNT = "DefaultCognitiveServicesAccount";
    private static final String COGNITIVE_SERVICES_ACCOUNT_KEY = "CognitiveServicesAccountKey";

    private static final String INPUT_FIELD_MAPPING_ENTRY = "InputFieldMappingEntry";

    private static final String SCORING_PROFILE = "ScoringProfile";

    @Override
    public void customize(LibraryCustomization libraryCustomization) {
        customizeImplementationModelsPackage(libraryCustomization.getPackage(IMPLEMENTATION_MODELS));
        customizeModelsPackage(libraryCustomization.getPackage(MODELS));
    }

    private void customizeImplementationModelsPackage(PackageCustomization packageCustomization) {
        packageCustomization.getClass("AnalyzeRequest")
            .getMethod("public AnalyzeRequest setTokenFilters(List<TokenFilterName> tokenFilters) {");
    }

    private void customizeModelsPackage(PackageCustomization packageCustomization) {
        // Change class modifiers to 'public abstract'.
        bulkChangeClassModifiers(packageCustomization, PUBLIC_ABSTRACT, SCORING_FUNCTION, DATA_CHANGE_DETECTION_POLICY,
            DATA_DELETION_DETECTION_POLICY, CHAR_FILTER, COGNITIVE_SERVICES_ACCOUNT);

        // Change class modifiers to 'public final'.
        bulkChangeClassModifiers(packageCustomization, PUBLIC_FINAL, BM_25_SIMILARITY_ALGORITHM,
            CLASSIC_SIMILARITY_ALGORITHM, HIGH_WATER_MARK_CHANGE_DETECTION_POLICY,
            SQL_INTEGRATED_CHANGE_TRACKING_POLICY, SOFT_DELETE_COLUMN_DELETION_DETECTION_POLICY, MAPPING_CHAR_FILTER,
            PATTERN_REPLACE_CHAR_FILTER, DEFAULT_COGNITIVE_SERVICES_ACCOUNT);

        // Add vararg overloads to list setters.
        addVarArgsOverload(packageCustomization.getClass(INPUT_FIELD_MAPPING_ENTRY), "inputs",
            "InputFieldMappingEntry");
        addVarArgsOverload(packageCustomization.getClass(SCORING_PROFILE), "functions", "ScoringFunction");

        // Customize MagnitudeScoringParameters.
        customizeMagnitudeScoringParameters(packageCustomization.getClass(MAGNITUDE_SCORING_PARAMETERS));

        // Customize SearchFieldDataTypes.
        customizeSearchFieldDataType(packageCustomization.getClass(SEARCH_FIELD_DATA_TYPE));

        // Customize SimilarityAlgorithm.
        customizeSimilarityAlgorithm(packageCustomization.getClass(SIMILARITY_ALGORITHM));

        // Customize CognitiveServicesAccountKey.
        customizeCognitiveServicesAccountKey(packageCustomization.getClass(COGNITIVE_SERVICES_ACCOUNT_KEY));
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {\n" +
            "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));\n" +
            "}")
            .addAnnotation("@JsonCreator")
            .getJavadoc()
            .setDescription("Returns a collection of a specific SearchFieldDataType")
            .setParam("dataType", "the corresponding SearchFieldDataType")
            .setReturn("a Collection of the corresponding SearchFieldDataType");
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        classCustomization.getMethod("isShouldBoostBeyondRangeByConstant")
            .rename("shouldBoostBeyondRangeByConstant");
    }

    private void customizeSimilarityAlgorithm(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_ABSTRACT);
        classCustomization.removeAnnotation("@JsonTypeName");
        classCustomization.addAnnotation("@JsonTypeName(\"Similarity\")");
    }

    private void customizeCognitiveServicesAccountKey(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        classCustomization.addMethod(
            "/**\n" +
            " * Set the key property: The key used to provision the cognitive service\n" +
            " * resource attached to a skillset.\n" +
            " *\n" +
            " * @param key the key value to set.\n" +
            " * @return the CognitiveServicesAccountKey object itself.\n" +
            " */\n" +
            "public CognitiveServicesAccountKey setKey(String key) {\n" +
            "    this.key = key;\n" +
            "    return this;\n" +
            "}"
        );
    }

    private static void bulkChangeClassModifiers(PackageCustomization packageCustomization, int modifier,
        String... classNames) {
        if (classNames == null) {
            return;
        }

        for (String className : classNames) {
            changeClassModifier(packageCustomization.getClass(className), modifier);
        }
    }

    private static void changeClassModifier(ClassCustomization classCustomization, int modifier) {
        classCustomization.setModifier(modifier);
    }

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static void addVarArgsOverload(ClassCustomization classCustomization, String parameterName,
        String parameterType) {
        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        // Add the '@JsonSetter' annotation to indicate to Jackson to use the List setter.
        JavadocCustomization copyJavadocs = classCustomization.getMethod(methodName)
            .addAnnotation("@JsonSetter")
            .getJavadoc();

        String varargMethod = String.format(VARARG_METHOD_TEMPLATE, classCustomization.getClassName(), methodName,
            parameterType, parameterName, parameterName, parameterName, parameterName);

        JavadocCustomization newJavadocs = classCustomization.addMethod(varargMethod).getJavadoc();

        newJavadocs.setDescription(copyJavadocs.getDescription())
            .setReturn(copyJavadocs.getReturn())
            .setSince(copyJavadocs.getSince())
            .setDeprecated(copyJavadocs.getDeprecated());

        copyJavadocs.getParams().forEach(newJavadocs::setParam);
        copyJavadocs.getThrows().forEach(newJavadocs::addThrows);
        copyJavadocs.getSees().forEach(newJavadocs::addSee);
    }
}
