// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
    // Common modifier combinations
    private static final int PUBLIC_ABSTRACT = Modifier.PUBLIC | Modifier.ABSTRACT;
    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private static final String VARARG_METHOD_TEMPLATE =
        "public %s %s(%s... %s) {"
            + "    this.%s = (%s == null) ? null : java.util.Arrays.asList(%s);\n"
            + "    return this;\n"
            + "}";

    // Packages
    private static final String IMPLEMENTATION_MODELS = "com.azure.search.documents.indexes.implementation.models";
    private static final String MODELS = "com.azure.search.documents.indexes.models";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization implCustomization = libraryCustomization.getPackage(IMPLEMENTATION_MODELS);
        PackageCustomization publicCustomization = libraryCustomization.getPackage(MODELS);

        // Customize implementation models.

        // Customize models.
        // Change class modifiers to 'public abstract'.
        bulkChangeClassModifiers(publicCustomization, PUBLIC_ABSTRACT, "ScoringFunction", "DataChangeDetectionPolicy",
            "DataDeletionDetectionPolicy", "CharFilter", "CognitiveServicesAccount", "SearchIndexerSkill");

        // Change class modifiers to 'public final'.
        bulkChangeClassModifiers(publicCustomization, PUBLIC_FINAL, "BM25SimilarityAlgorithm",
            "ClassicSimilarityAlgorithm", "HighWaterMarkChangeDetectionPolicy", "SqlIntegratedChangeTrackingPolicy",
            "SoftDeleteColumnDeletionDetectionPolicy", "MappingCharFilter", "PatternReplaceCharFilter",
            "DefaultCognitiveServicesAccount", "ConditionalSkill", "ConditionalSkill", "KeyPhraseExtractionSkill",
            "LanguageDetectionSkill", "ShaperSkill", "MergeSkill", "SentimentSkill", "SplitSkill",
            "TextTranslationSkill", "DocumentExtractionSkill", "WebApiSkill");


        // Add vararg overloads to list setters.
        addVarArgsOverload(publicCustomization.getClass("InputFieldMappingEntry"), "inputs", "InputFieldMappingEntry");
        addVarArgsOverload(publicCustomization.getClass("ScoringProfile"), "functions", "ScoringFunction");

        // More complex customizations.
        customizeMagnitudeScoringParameters(publicCustomization.getClass("MagnitudeScoringParameters"));
        customizeSearchFieldDataType(publicCustomization.getClass("SearchFieldDataType"));
        customizeSimilarityAlgorithm(publicCustomization.getClass("SimilarityAlgorithm"));
        customizeCognitiveServicesAccountKey(publicCustomization.getClass("CognitiveServicesAccountKey"));
        customizeOcrSkill(publicCustomization.getClass("OcrSkill"));
        customizeImageAnalysisSkill(publicCustomization.getClass("ImageAnalysisSkill"));
        customizeEntityRecognitionSkill(publicCustomization.getClass("EntityRecognitionSkill"));
        customizeCustomEntityLookupSkill(publicCustomization.getClass("CustomEntityLookupSkill"));
        customizeCustomNormalizer(publicCustomization.getClass("CustomNormalizer"));
        customizeSearchField(publicCustomization.getClass("SearchField"));
        customizeSynonymMap(publicCustomization.getClass("SynonymMap"));
        customizeSearchResourceEncryptionKey(publicCustomization.getClass("SearchResourceEncryptionKey"),
            implCustomization.getClass("AzureActiveDirectoryApplicationCredentials"));
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {\n"
                + "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));\n"
                + "}")
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
            "/**\n"
                + " * Set the key property: The key used to provision the cognitive service\n"
                + " * resource attached to a skillset.\n"
                + " *\n"
                + " * @param key the key value to set.\n"
                + " * @return the CognitiveServicesAccountKey object itself.\n"
                + " */\n"
                + "public CognitiveServicesAccountKey setKey(String key) {\n"
                + "    this.key = key;\n"
                + "    return this;\n"
                + "}");
    }

    private void customizeOcrSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);

        JavadocCustomization javadocToCopy = classCustomization.getMethod("isShouldDetectOrientation")
            .getJavadoc();

        JavadocCustomization newJavadoc = classCustomization.addMethod(
            "public Boolean setShouldDetectOrientation() {\n"
                + "    return this.shouldDetectOrientation;\n"
                + "}")
            .addAnnotation("@Deprecated")
            .getJavadoc();

        copyJavadocs(javadocToCopy, newJavadoc)
            .setDeprecated("Use {@link #isShouldDetectOrientation()} instead.");
    }

    private void customizeEntityRecognitionSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        classCustomization.getMethod("setIncludeTypelessEntities").rename("setTypelessEntitiesIncluded");
        classCustomization.getMethod("isIncludeTypelessEntities").rename("areTypelessEntitiesIncluded");
        addVarArgsOverload(classCustomization, "categories", "EntityCategory");
    }

    private void customizeImageAnalysisSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "visualFeatures", "VisualFeature");
        addVarArgsOverload(classCustomization, "details", "ImageDetail");
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "inlineEntitiesDefinition", "CustomEntity");
    }

    private void customizeCustomNormalizer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilterName");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilterName");
    }

    private void customizeSearchField(ClassCustomization classCustomization) {
        classCustomization.getMethod("setHidden")
            .replaceBody(
                "this.hidden = (hidden == null) ? null : !hidden;\n"
                    + "return this;"
            );

        classCustomization.getMethod("isHidden")
            .replaceBody("return (this.hidden == null) ? null : !this.hidden;");

        addVarArgsOverload(classCustomization, "fields", "SearchField");
        addVarArgsOverload(classCustomization, "synonymMapNames", "String");
    }

    private void customizeSynonymMap(ClassCustomization classCustomization) {
        classCustomization.getMethod("getFormat").setModifier(Modifier.PRIVATE);
        classCustomization.getMethod("setFormat").setModifier(Modifier.PRIVATE);
        classCustomization.getMethod("setName").setModifier(Modifier.PRIVATE);

        classCustomization.addConstructor("public SynonymMap(String name) {\n"
            + "    this(name, null);\n"
            + "}")
            .getJavadoc()
            .setDescription("Constructor of {@link SynonymMap}.")
            .setParam("name", "The name of the synonym map.");

        classCustomization.addConstructor("public SynonymMap(@JsonProperty(value = \"name\") String name, @JsonProperty(value = \"synonyms\") String synonyms) {\n"
            + "    this.format = \"solr\";\n"
            + "    this.name = name;\n"
            + "    this.synonyms = synonyms;\n"
            + "}")
            .addAnnotation("@JsonCreator")
            .getJavadoc()
            .setDescription("Constructor of {@link SynonymMap}.")
            .setParam("name", "The name of the synonym map.")
            .setParam("synonyms", "A series of synonym rules in the specified synonym map format. The rules must be separated by newlines.");
    }

    private void customizeSearchResourceEncryptionKey(ClassCustomization keyCustomization,
        ClassCustomization credentialCustomization) {
        keyCustomization.getMethod("getAccessCredentials").setModifier(Modifier.PRIVATE);
        String setterReturnJavadoc = keyCustomization.getMethod("setAccessCredentials").setModifier(Modifier.PRIVATE)
            .getJavadoc().getReturn();

        JavadocCustomization javadoc = keyCustomization.addMethod("public String getApplicationId() {\n"
            + "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationId();\n"
            + "}")
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("getApplicationId").getJavadoc(), javadoc);

        javadoc = keyCustomization.addMethod("public SearchResourceEncryptionKey setApplicationId(String applicationId) {\n"
            + "    if (this.accessCredentials == null) {\n"
            + "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();\n"
            + "    }\n"
            + "\n"
            + "    this.accessCredentials.setApplicationId(applicationId);\n"
            + "    return this;\n"
            + "}")
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("setApplicationId").getJavadoc(), javadoc)
            .setReturn(setterReturnJavadoc);

        javadoc = keyCustomization.addMethod("public String getApplicationSecret() {\n"
            + "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationSecret();\n"
            + "}")
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("getApplicationSecret").getJavadoc(), javadoc);

        javadoc = keyCustomization.addMethod("public SearchResourceEncryptionKey setApplicationSecret(String applicationSecret) {\n"
            + "    if (this.accessCredentials == null) {\n"
            + "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();\n"
            + "    }\n"
            + "\n"
            + "    this.accessCredentials.setApplicationSecret(applicationSecret);\n"
            + "    return this;\n"
            + "}")
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("setApplicationSecret").getJavadoc(), javadoc)
            .setReturn(setterReturnJavadoc);
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
        copyJavadocs(copyJavadocs, newJavadocs);
    }

    /*
     * This helper function copies Javadocs from one customization to another.
     */
    private static JavadocCustomization copyJavadocs(JavadocCustomization from, JavadocCustomization to) {
        to.setDescription(from.getDescription())
            .setReturn(from.getReturn())
            .setSince(from.getSince())
            .setDeprecated(from.getDeprecated());

        from.getParams().forEach(to::setParam);
        from.getThrows().forEach(to::addThrows);
        from.getSees().forEach(to::addSee);

        return to;
    }
}
