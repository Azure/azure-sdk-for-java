// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.StreamSupport;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
    // Common modifier combinations
    private static final int PUBLIC_ABSTRACT = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.ABSTRACT;
    private static final int PUBLIC_FINAL = java.lang.reflect.Modifier.PUBLIC | java.lang.reflect.Modifier.FINAL;

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

        // Customize implementation.

        // Customize models.
        // Change class modifiers to 'public abstract'.
        bulkChangeClassModifiers(publicCustomization, PUBLIC_ABSTRACT, "ScoringFunction", "DataChangeDetectionPolicy",
            "DataDeletionDetectionPolicy", "CharFilter", "CognitiveServicesAccount", "SearchIndexerSkill",
            "LexicalAnalyzer", "SearchIndexerKnowledgeStoreProjectionSelector",
            "SearchIndexerKnowledgeStoreBlobProjectionSelector");

        // Change class modifiers to 'public final'.
        bulkChangeClassModifiers(publicCustomization, PUBLIC_FINAL, "BM25SimilarityAlgorithm",
            "ClassicSimilarityAlgorithm", "HighWaterMarkChangeDetectionPolicy", "SqlIntegratedChangeTrackingPolicy",
            "SoftDeleteColumnDeletionDetectionPolicy", "MappingCharFilter", "PatternReplaceCharFilter",
            "DefaultCognitiveServicesAccount", "ConditionalSkill", "ConditionalSkill", "KeyPhraseExtractionSkill",
            "LanguageDetectionSkill", "ShaperSkill", "MergeSkill", "SentimentSkill", "SplitSkill",
            "TextTranslationSkill", "DocumentExtractionSkill", "WebApiSkill");

        bulkRemoveMethod(publicCustomization, "getOdataType", "BM25SimilarityAlgorithm", "ClassicSimilarityAlgorithm",
            "ConditionalSkill", "DefaultCognitiveServicesAccount", "DocumentExtractionSkill", "EntityRecognitionSkill",
            "HighWaterMarkChangeDetectionPolicy", "KeyPhraseExtractionSkill", "LanguageDetectionSkill",
            "MappingCharFilter", "MergeSkill", "PatternReplaceCharFilter", "SentimentSkill", "ShaperSkill",
            "SoftDeleteColumnDeletionDetectionPolicy", "SplitSkill", "SqlIntegratedChangeTrackingPolicy",
            "TextTranslationSkill", "WebApiSkill");

        bulkRemoveMethod(publicCustomization, "getType", "DistanceScoringFunction", "FreshnessScoringFunction",
            "MagnitudeScoringFunction", "TagScoringFunction");

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
        customizeSearchIndexerSkill(publicCustomization.getClass("SearchIndexerSkill"));
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
        removeMethod(classCustomization, "getOdataType");
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
        removeMethod(classCustomization, "getOdataType");

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

    private void customizeSearchIndexerSkill(ClassCustomization classCustomization) {
        classCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration searchIndexerSkillClass = compilationUnit.getClassByName("SearchIndexerSkill")
                .get();

            // Add the modifier 'abstract' to SearchIndexerSkill.
            searchIndexerSkillClass.addModifier(Modifier.Keyword.ABSTRACT);

            // Get the JsonSubTypes annotation.
            AnnotationExpr jsonSubTypes = searchIndexerSkillClass.getAnnotationByName("JsonSubTypes").get();

            // JsonSubTypes only has a single annotation value which is an array of Types.
            ArrayInitializerExpr jsonSubTypesTypes = (ArrayInitializerExpr) jsonSubTypes.getChildNodes().get(1);

            // All JsonSubTypes.Type with names
            //
            // - #Microsoft.Skills.Text.V3.EntityRecognitionSkill
            // - #Microsoft.Skills.Text.V3.EntityLinkingSkill
            // - #Microsoft.Skills.Text.PIIDetectionSkill
            // - #Microsoft.Skills.Text.V3.SentimentSkill
            //
            // need to be removed as they aren't supported.
            List<Node> nodesToRemove = new ArrayList<>();
            for (Node jsonSubTypesType : jsonSubTypesTypes.getChildNodes()) {
                jsonSubTypesType.getChildNodes().stream()
                    .filter(childNode -> childNode instanceof MemberValuePair)
                    .map(childNode -> (MemberValuePair) childNode)
                    .filter(mvp -> "name".equals(mvp.getName().asString()))
                    .filter(mvp -> {
                        String mvpValue = mvp.getValue().asStringLiteralExpr().asString();
                        return "#Microsoft.Skills.Text.V3.SentimentSkill".equals(mvpValue)
                            || "#Microsoft.Skills.Text.V3.EntityRecognitionSkill".equals(mvpValue)
                            || "#Microsoft.Skills.Text.PIIDetectionSkill".equals(mvpValue)
                            || "#Microsoft.Skills.Text.V3.EntityLinkingSkill".equals(mvpValue);
                    })
                    .findFirst()
                    .ifPresent(mvp -> nodesToRemove.add(mvp.getParentNode().get()));
            }

            nodesToRemove.forEach(Node::remove);
        });
    }

    private void customizeEntityRecognitionSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        classCustomization.getMethod("setIncludeTypelessEntities").rename("setTypelessEntitiesIncluded");
        classCustomization.getMethod("isIncludeTypelessEntities").rename("areTypelessEntitiesIncluded");
        addVarArgsOverload(classCustomization, "categories", "EntityCategory");
    }

    private void customizeImageAnalysisSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "visualFeatures", "VisualFeature");
        addVarArgsOverload(classCustomization, "details", "ImageDetail");
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "inlineEntitiesDefinition", "CustomEntity");
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
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilterName");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilterName");
    }

    private void customizePatternAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        removeMethod(classCustomization, "getOdataType");
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
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeStopAnalyzer(ClassCustomization classCustomization) {
        changeClassModifier(classCustomization, PUBLIC_FINAL);
        removeMethod(classCustomization, "getOdataType");
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

    private static void bulkRemoveMethod(PackageCustomization packageCustomization, String methodName,
        String... classNames) {
        for (String className : classNames) {
            removeMethod(packageCustomization.getClass(className), methodName);
        }
    }

    private static void removeMethod(ClassCustomization classCustomization, String methodName) {
        classCustomization.removeMethod(methodName);
    }
}
