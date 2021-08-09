// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Locale;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
    // Common modifier combinations
    private static final int PUBLIC_ABSTRACT = Modifier.PUBLIC | Modifier.ABSTRACT;
    private static final int PUBLIC_FINAL = Modifier.PUBLIC | Modifier.FINAL;

    private static final String VARARG_METHOD_TEMPLATE = joinWithNewline(
        "public %s %s(%s... %s) {",
        "    this.%s = (%s == null) ? null : java.util.Arrays.asList(%s);",
        "    return this;",
        "}");

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
            "DataDeletionDetectionPolicy", "CharFilter", "CognitiveServicesAccount", "SearchIndexerSkill",
            "LexicalAnalyzer", "SearchIndexerKnowledgeStoreProjectionSelector",
            "SearchIndexerKnowledgeStoreBlobProjectionSelector", "SearchIndexerDataIdentity");

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
        customizeSearchSuggester(publicCustomization.getClass("SearchSuggester"));
        customizeCustomAnalyzer(publicCustomization.getClass("CustomAnalyzer"));
        customizePatternAnalyzer(publicCustomization.getClass("PatternAnalyzer"));
        customizeLuceneStandardAnalyzer(publicCustomization.getClass("LuceneStandardAnalyzer"));
        customizeStopAnalyzer(publicCustomization.getClass("StopAnalyzer"));
        customizeSearchIndexerSkillset(publicCustomization.getClass("SearchIndexerSkillset"));

        addKnowledgeStoreProjectionFluentSetterOverrides(
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreFileProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreObjectProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreTableProjectionSelector"));
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(joinWithNewline(
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {",
            "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));",
            "}"))
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
        classCustomization.addMethod(joinWithNewline(
            "/**",
            " * Set the key property: The key used to provision the cognitive service",
            " * resource attached to a skillset.",
            " *",
            " * @param key the key value to set.",
            " * @return the CognitiveServicesAccountKey object itself.",
            " */",
            "public CognitiveServicesAccountKey setKey(String key) {",
            "    this.key = key;",
            "    return this;",
            "}"));
    }

    private void customizeOcrSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);

        JavadocCustomization javadocToCopy = classCustomization.getMethod("isShouldDetectOrientation")
            .getJavadoc();

        JavadocCustomization newJavadoc = classCustomization.addMethod(joinWithNewline(
            "public Boolean setShouldDetectOrientation() {",
            "    return this.shouldDetectOrientation;",
            "}"))
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
        classCustomization.getMethod("setHidden").replaceBody(joinWithNewline(
            "this.hidden = (hidden == null) ? null : !hidden;",
            "return this;"
        ));

        classCustomization.getMethod("isHidden")
            .replaceBody("return (this.hidden == null) ? null : !this.hidden;");

        addVarArgsOverload(classCustomization, "fields", "SearchField");
        addVarArgsOverload(classCustomization, "synonymMapNames", "String");
    }

    private void customizeSynonymMap(ClassCustomization classCustomization) {
        classCustomization.removeMethod("getFormat");
        classCustomization.removeMethod("setFormat");
        classCustomization.removeMethod("setName");

        classCustomization.addConstructor(joinWithNewline(
            "public SynonymMap(String name) {",
            "    this(name, null);",
            "}"))
            .getJavadoc()
            .setDescription("Constructor of {@link SynonymMap}.")
            .setParam("name", "The name of the synonym map.");

        classCustomization.addConstructor(joinWithNewline(
            "public SynonymMap(@JsonProperty(value = \"name\") String name, @JsonProperty(value = \"synonyms\") String synonyms) {",
            "    this.format = \"solr\";",
            "    this.name = name;",
            "    this.synonyms = synonyms;",
            "}"))
            .addAnnotation("@JsonCreator")
            .getJavadoc()
            .setDescription("Constructor of {@link SynonymMap}.")
            .setParam("name", "The name of the synonym map.")
            .setParam("synonyms", "A series of synonym rules in the specified synonym map format. The rules must be separated by newlines.");

        classCustomization.addMethod(joinWithNewline(
            "/**",
            " * Creates a new instance of SynonymMap with synonyms read from the passed file.",
            " *",
            " * @param name The name of the synonym map.",
            " * @param filePath The path to the file where the formatted synonyms are read.",
            " * @return A SynonymMap.",
            " * @throws java.io.UncheckedIOException If reading {@code filePath} fails.",
            " */",
            "public static SynonymMap createFromFile(String name, java.nio.file.Path filePath) {",
            "    String synonyms = com.azure.search.documents.implementation.util.Utility.readSynonymsFromFile(filePath);",
            "    return new SynonymMap(name, synonyms);",
            "}"));
    }

    private void customizeSearchResourceEncryptionKey(ClassCustomization keyCustomization,
        ClassCustomization credentialCustomization) {
        keyCustomization.removeMethod("getAccessCredentials");

        String setterReturnJavadoc = keyCustomization.getMethod("setAccessCredentials").getJavadoc().getReturn();
        keyCustomization.removeMethod("setAccessCredentials");

        JavadocCustomization javadoc = keyCustomization.addMethod(joinWithNewline(
            "public String getApplicationId() {",
            "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationId();",
            "}"))
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("getApplicationId").getJavadoc(), javadoc);

        javadoc = keyCustomization.addMethod(joinWithNewline(
            "public SearchResourceEncryptionKey setApplicationId(String applicationId) {",
            "    if (this.accessCredentials == null) {",
            "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();",
            "    }",
            "",
            "    this.accessCredentials.setApplicationId(applicationId);",
            "    return this;",
            "}"))
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("setApplicationId").getJavadoc(), javadoc)
            .setReturn(setterReturnJavadoc);

        javadoc = keyCustomization.addMethod(joinWithNewline(
            "public String getApplicationSecret() {",
            "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationSecret();",
            "}"))
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("getApplicationSecret").getJavadoc(), javadoc);

        javadoc = keyCustomization.addMethod(joinWithNewline(
            "public SearchResourceEncryptionKey setApplicationSecret(String applicationSecret) {",
            "    if (this.accessCredentials == null) {",
            "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();",
            "    }",
            "",
            "    this.accessCredentials.setApplicationSecret(applicationSecret);",
            "    return this;",
            "}"))
            .getJavadoc();
        copyJavadocs(credentialCustomization.getMethod("setApplicationSecret").getJavadoc(), javadoc)
            .setReturn(setterReturnJavadoc);
    }

    private void customizeSearchSuggester(ClassCustomization classCustomization) {
        classCustomization.getConstructor("SearchSuggester").replaceBody(joinWithNewline(
            "this.searchMode = \"analyzingInfixMatching\";",
            "this.name = name;",
            "this.sourceFields = sourceFields;"));

        classCustomization.removeMethod("setSearchMode");
    }

    private void customizeCustomAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilterName");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilterName");
    }

    private void customizePatternAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        classCustomization.getMethod("isLowerCaseTerms").rename("areLowerCaseTerms");
        addVarArgsOverload(classCustomization, "stopwords", "String");

        classCustomization.getMethod("getFlags").setReturnType("List<RegexFlags>", "%s")
            .replaceBody(joinWithNewline(
                "if (this.flags == null) {",
                "    return null;",
                "} else {",
                "    String[] flagStrings = this.flags.toString().split(\"\\\\|\");",
                "    return java.util.Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList());",
                "}"));

        classCustomization.getMethod("setFlags").replaceParameters("List<RegexFlags> flags")
            .replaceBody(joinWithNewline(
                "if (flags == null) {",
                "    this.flags = null;",
                "} else {",
                "    String flagString = flags.stream().map(RegexFlags::toString).collect(Collectors.joining(\"|\"));",
                "    this.flags = RegexFlags.fromString(flagString);",
                "}",
                "",
                "return this;"));
        addVarArgsOverload(classCustomization, "flags", "RegexFlags");
        classCustomization.getMethod("setFlags(RegexFlags... flags)")
            .replaceBody(joinWithNewline(
                "if (flags == null) {",
                "    this.flags = null;",
                "    return this;",
                "} else {",
                "    return setFlags(java.util.Arrays.asList(flags));",
                "}"));
    }

    private void customizeLuceneStandardAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeStopAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeSearchIndexerSkillset(ClassCustomization classCustomization) {
        JavadocCustomization originalConstructorJavadocs = classCustomization.getConstructor("SearchIndexerSkillset")
            .replaceParameters("@JsonProperty(value = \"name\") String name, @JsonProperty(value = \"skills\") List<SearchIndexerSkill> skills")
            .getJavadoc();

        JavadocCustomization additionalConstructorJavadocs = classCustomization.addConstructor(joinWithNewline(
            "public SearchIndexerSkillset(String name) {",
            "    this(name, null);",
            "}"))
            .getJavadoc();

        additionalConstructorJavadocs.setDescription(originalConstructorJavadocs.getDescription());
        additionalConstructorJavadocs.setParam("name", originalConstructorJavadocs.getParams().get("name"));

        classCustomization.addMethod(joinWithNewline(
            "/**",
            " * Sets the skills property: A list of skills in the skillset.",
            " *",
            " * @param skills the skills value to set.",
            " * @return the SearchIndexerSkillset object itself.",
            " */",
            "public SearchIndexerSkillset setSkills(List<SearchIndexerSkill> skills) {",
            "    this.skills = skills;",
            "    return this;",
            "}"
        ));
        addVarArgsOverload(classCustomization, "skills", "SearchIndexerSkill");
    }

    private void addKnowledgeStoreProjectionFluentSetterOverrides(ClassCustomization... classCustomizations) {
        for (ClassCustomization classCustomization : classCustomizations) {
            String className = classCustomization.getClassName();

            classCustomization.addMethod(joinWithNewline(
                String.format("public %s setReferenceKeyName(String referenceKeyName) {", className),
                "    super.setReferenceKeyName(referenceKeyName);",
                "    return this;",
                "}")).addAnnotation("@Override");

            classCustomization.addMethod(joinWithNewline(
                String.format("public %s setGeneratedKeyName(String generatedKeyName) {", className),
                "    super.setGeneratedKeyName(generatedKeyName);",
                "    return this;",
                "}")).addAnnotation("@Override");

            classCustomization.addMethod(joinWithNewline(
                String.format("public %s setSource(String source) {", className),
                "    super.setSource(source);\n",
                "    return this;\n",
                "}")).addAnnotation("@Override");

            classCustomization.addMethod(joinWithNewline(
                String.format("public %s setSourceContext(String sourceContext) {", className),
                "    super.setSourceContext(sourceContext);",
                "    return this;",
                "}")).addAnnotation("@Override");

            classCustomization.addMethod(joinWithNewline(
                String.format("public %s setInputs(List<InputFieldMappingEntry> inputs) {", className),
                "    super.setInputs(inputs);",
                "    return this;",
                "}"), Collections.singletonList("java.util.List"))
                .addAnnotation("@Override");
        }
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

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
