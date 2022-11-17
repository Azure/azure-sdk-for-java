// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
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
        bulkSetClassModifier(publicCustomization, Modifier.PUBLIC | Modifier.ABSTRACT, "ScoringFunction",
            "DataChangeDetectionPolicy", "DataDeletionDetectionPolicy", "CharFilter", "CognitiveServicesAccount",
            "LexicalAnalyzer", "SearchIndexerKnowledgeStoreProjectionSelector", "SimilarityAlgorithm",
            "SearchIndexerKnowledgeStoreBlobProjectionSelector", "SearchIndexerDataIdentity");

        // Add vararg overloads to list setters.
        addVarArgsOverload(publicCustomization.getClass("InputFieldMappingEntry"), "inputs", "InputFieldMappingEntry");
        addVarArgsOverload(publicCustomization.getClass("ScoringProfile"), "functions", "ScoringFunction");

        // More complex customizations.
        customizeSearchIndex(publicCustomization.getClass("SearchIndex"));
        customizeSearchIndexer(publicCustomization.getClass("SearchIndexer"));
        customizeSearchIndexerSkill(publicCustomization.getClass("SearchIndexerSkill"),
            libraryCustomization.getRawEditor());
        customizeTokenFilter(publicCustomization.getClass("TokenFilter"), libraryCustomization.getRawEditor());
        customizeLexicalTokenizer(publicCustomization.getClass("LexicalTokenizer"), libraryCustomization.getRawEditor());
        customizeMagnitudeScoringParameters(publicCustomization.getClass("MagnitudeScoringParameters"));
        customizeSearchFieldDataType(publicCustomization.getClass("SearchFieldDataType"));
        customizeCognitiveServicesAccountKey(publicCustomization.getClass("CognitiveServicesAccountKey"));
        customizeOcrSkill(publicCustomization.getClass("OcrSkill"));
        customizeImageAnalysisSkill(publicCustomization.getClass("ImageAnalysisSkill"));
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
        customizeCjkBigramTokenFilter(publicCustomization.getClass("CjkBigramTokenFilter"));
        customizeKeepTokenFilter(publicCustomization.getClass("KeepTokenFilter"));
        customizeSynonymTokenFilter(publicCustomization.getClass("SynonymTokenFilter"));
        customizeShingleTokenFilter(publicCustomization.getClass("ShingleTokenFilter"));
        customizeLimitTokenFilter(publicCustomization.getClass("LimitTokenFilter"));
        customizePhoneticTokenFilter(publicCustomization.getClass("PhoneticTokenFilter"));
        customizeStopwordsTokenFilter(publicCustomization.getClass("StopwordsTokenFilter"));
        customizeWordDelimiterTokenFilter(publicCustomization.getClass("WordDelimiterTokenFilter"));
        customizeElisionTokenFilter(publicCustomization.getClass("ElisionTokenFilter"));
        customizeNGramTokenizer(publicCustomization.getClass("NGramTokenizer"));
        customizeEdgeNGramTokenizer(publicCustomization.getClass("EdgeNGramTokenizer"));
        customizeMicrosoftLanguageStemmingTokenizer(publicCustomization.getClass("MicrosoftLanguageStemmingTokenizer"));
        customizePatternTokenizer(publicCustomization.getClass("PatternTokenizer"));
        customizeIndexingParameters(publicCustomization.getClass("IndexingParameters"),
            libraryCustomization.getRawEditor());
        customizeSearchIndexerDataSourceConnection(publicCustomization.getClass("SearchIndexerDataSourceConnection"));

        addKnowledgeStoreProjectionFluentSetterOverrides(
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreFileProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreObjectProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreTableProjectionSelector"));

        bulkRemoveFromJsonMethods(publicCustomization.getClass("SearchIndexerKnowledgeStoreProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"));
    }

    private void customizeSearchIndex(ClassCustomization classCustomization) {
        classCustomization.addConstructor(joinWithNewline(
            "/**",
            " * Constructor of {@link SearchIndex}.",
            " * @param name The name of the index.",
            " * @param fields The fields of the index.",
            " */",
            "public SearchIndex(String name, List<SearchField> fields) {",
            "    this.name = name;",
            "    this.fields = fields;",
            "}"
        ));

        addVarArgsOverload(classCustomization, "fields", "SearchField");
        addVarArgsOverload(classCustomization, "scoringProfiles", "ScoringProfile");
        addVarArgsOverload(classCustomization, "suggesters", "SearchSuggester");
        addVarArgsOverload(classCustomization, "analyzers", "LexicalAnalyzer");
        addVarArgsOverload(classCustomization, "tokenizers", "LexicalTokenizer");
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilter");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilter");
        addVarArgsOverload(classCustomization, "normalizers", "LexicalNormalizer");
    }

    private void customizeSearchIndexer(ClassCustomization classCustomization) {
        classCustomization.addConstructor(joinWithNewline(
            "/**",
            " * Constructor of {@link SearchIndexer}.",
            " *",
            " * @param name The name of the indexer.",
            " * @param dataSourceName The name of the datasource from which this indexer reads data.",
            " * @param targetIndexName The name of the index to which this indexer writes data.",
            " */",
            "public SearchIndexer(String name, String dataSourceName, String targetIndexName) {",
            "    this.name = name;",
            "    this.dataSourceName = dataSourceName;",
            "    this.targetIndexName = targetIndexName;",
            "}"
        ));

        addVarArgsOverload(classCustomization, "fieldMappings", "FieldMapping");
        addVarArgsOverload(classCustomization, "outputFieldMappings", "FieldMapping");
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.addMethod(joinWithNewline(
            "/**",
            " * Returns a collection of a specific SearchFieldDataType.",
            " *",
            " * @param dataType the corresponding SearchFieldDataType",
            " * @return a Collection of the corresponding SearchFieldDataType",
            " */",
            "public static SearchFieldDataType collection(SearchFieldDataType dataType) {",
            "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));",
            "}"
        ));
    }

    private void customizeSearchIndexerSkill(ClassCustomization classCustomization, Editor editor) {
        classCustomization.setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);

        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "EntityRecognitionSkillV1", "EntityRecognitionSkill");
        fileContents = updateVersionedDeserialization(fileContents, "EntityRecognitionSkillV3", "EntityRecognitionSkill");
        fileContents = updateVersionedDeserialization(fileContents, "SentimentSkillV1", "SentimentSkill");
        fileContents = updateVersionedDeserialization(fileContents, "SentimentSkillV3", "SentimentSkill");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private void customizeTokenFilter(ClassCustomization classCustomization, Editor editor) {
        classCustomization.setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);

        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "EdgeNGramTokenFilterV1", "EdgeNGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "EdgeNGramTokenFilterV2", "EdgeNGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "NGramTokenFilterV1", "NGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "NGramTokenFilterV2", "NGramTokenFilter");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private void customizeLexicalTokenizer(ClassCustomization classCustomization, Editor editor) {
        classCustomization.setModifier(Modifier.PUBLIC | Modifier.ABSTRACT);

        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "KeywordTokenizerV1", "KeywordTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "KeywordTokenizerV2", "KeywordTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "LuceneStandardTokenizerV1", "LuceneStandardTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "LuceneStandardTokenizerV2", "LuceneStandardTokenizer");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private String updateVersionedDeserialization(String fileContents, String codegenName, String Name) {
        String target = String.format("return %1$s.fromJson(readerToUse);", codegenName);
        String replacement = String.format(joinWithNewline(
            "%1$s codegen = %1$s.fromJson(readerToUse);",
            "return (codegen == null) ? null : new %2$s(codegen);"
        ), codegenName, Name);

        return fileContents.replace(target, replacement);
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        classCustomization.getMethod("isShouldBoostBeyondRangeByConstant")
            .rename("shouldBoostBeyondRangeByConstant");
    }

    private void customizeCognitiveServicesAccountKey(ClassCustomization classCustomization) {
        classCustomization.getProperty("key").setModifier(Modifier.PRIVATE);
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

        JavadocCustomization javadocToCopy = classCustomization.getMethod("isShouldDetectOrientation")
            .getJavadoc();

        classCustomization.addMethod(joinWithNewline(
                "public Boolean setShouldDetectOrientation() {",
                "    return this.shouldDetectOrientation;",
                "}"))
            .addAnnotation("@Deprecated")
            .getJavadoc()
            .replace(javadocToCopy)
            .setDeprecated("Use {@link #isShouldDetectOrientation()} instead.");
    }

    private void customizeImageAnalysisSkill(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "visualFeatures", "VisualFeature");
        addVarArgsOverload(classCustomization, "details", "ImageDetail");
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "inlineEntitiesDefinition", "CustomEntity");
    }

    private void customizeCustomNormalizer(ClassCustomization classCustomization) {
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
            "private SynonymMap() {",
            "    this(null, null);",
            "}"
        ));

        classCustomization.addConstructor(joinWithNewline(
                "public SynonymMap(String name) {",
                "    this(name, null);",
                "}"))
            .getJavadoc()
            .setDescription("Constructor of {@link SynonymMap}.")
            .setParam("name", "The name of the synonym map.");

        classCustomization.addConstructor(joinWithNewline(
                "public SynonymMap(String name, String synonyms) {",
                "    this.format = \"solr\";",
                "    this.name = name;",
                "    this.synonyms = synonyms;",
                "}"))
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

        keyCustomization.addMethod(joinWithNewline(
                "public String getApplicationId() {",
                "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationId();",
                "}"))
            .getJavadoc()
            .replace(credentialCustomization.getMethod("getApplicationId").getJavadoc());

        keyCustomization.addMethod(joinWithNewline(
                "public SearchResourceEncryptionKey setApplicationId(String applicationId) {",
                "    if (this.accessCredentials == null) {",
                "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();",
                "    }",
                "",
                "    this.accessCredentials.setApplicationId(applicationId);",
                "    return this;",
                "}"))
            .getJavadoc()
            .replace(credentialCustomization.getMethod("setApplicationId").getJavadoc())
            .setReturn(setterReturnJavadoc);

        keyCustomization.addMethod(joinWithNewline(
                "public String getApplicationSecret() {",
                "    return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationSecret();",
                "}"))
            .getJavadoc()
            .replace(credentialCustomization.getMethod("getApplicationSecret").getJavadoc());

        keyCustomization.addMethod(joinWithNewline(
                "public SearchResourceEncryptionKey setApplicationSecret(String applicationSecret) {",
                "    if (this.accessCredentials == null) {",
                "        this.accessCredentials = new AzureActiveDirectoryApplicationCredentials();",
                "    }",
                "",
                "    this.accessCredentials.setApplicationSecret(applicationSecret);",
                "    return this;",
                "}"))
            .getJavadoc()
            .replace(credentialCustomization.getMethod("setApplicationSecret").getJavadoc())
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
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilterName");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilterName");
    }

    private void customizePatternAnalyzer(ClassCustomization classCustomization) {
        classCustomization.getMethod("isLowerCaseTerms").rename("areLowerCaseTerms");
        addVarArgsOverload(classCustomization, "stopwords", "String");

        classCustomization.getMethod("getFlags").setReturnType("List<RegexFlags>", "%s")
            .replaceBody(joinWithNewline(
                "if (this.flags == null) {",
                "    return null;",
                "} else {",
                "    String[] flagStrings = this.flags.toString().split(\"\\\\|\");",
                "    return java.util.Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList());",
                "}"), Collections.singletonList(Collectors.class.getName()));

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
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeStopAnalyzer(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeSearchIndexerSkillset(ClassCustomization classCustomization) {
        classCustomization.addConstructor(joinWithNewline(
                "public SearchIndexerSkillset(String name, List<SearchIndexerSkill> skills) {",
                "    this(name);",
                "    this.skills = skills;",
                "}"
            ))
            .getJavadoc()
            .setDescription("Creates an instance of SearchIndexerSkillset class.")
            .setParam("name", "the name value to set.")
            .setParam("skills", "the skills value to set.");

        addVarArgsOverload(classCustomization, "skills", "SearchIndexerSkill");
    }

    private static void addKnowledgeStoreProjectionFluentSetterOverrides(ClassCustomization... classCustomizations) {
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

    private void customizeCjkBigramTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isOutputUnigrams").rename("areOutputUnigrams");
        addVarArgsOverload(classCustomization, "ignoreScripts", "CjkBigramTokenFilterScripts");
    }

    private void customizeKeepTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isLowerCaseKeepWords").rename("areLowerCaseKeepWords");
    }

    private void customizeSynonymTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isExpand").rename("getExpand");
    }

    private void customizeShingleTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isOutputUnigrams").rename("areOutputUnigrams");
        classCustomization.getMethod("isOutputUnigramsIfNoShingles").rename("areOutputUnigramsIfNoShingles");
    }

    private void customizeLimitTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isAllTokensConsumed").rename("areAllTokensConsumed");
    }

    private void customizePhoneticTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isOriginalTokensReplaced").rename("areOriginalTokensReplaced");
    }

    private void customizeStopwordsTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isTrailingStopWordsRemoved").rename("areTrailingStopWordsRemoved");

        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeNGramTokenizer(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "tokenChars", "TokenCharacterKind");
    }

    private void customizeEdgeNGramTokenizer(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "tokenChars", "TokenCharacterKind");
    }

    private void customizeWordDelimiterTokenFilter(ClassCustomization classCustomization) {
        classCustomization.getMethod("isGenerateWordParts").rename("generateWordParts");
        classCustomization.getMethod("isGenerateNumberParts").rename("generateNumberParts");
        classCustomization.getMethod("isWordsCatenated").rename("areWordsCatenated");
        classCustomization.getMethod("isNumbersCatenated").rename("areNumbersCatenated");
        classCustomization.getMethod("isCatenateAll").rename("catenateAll");
        classCustomization.getMethod("isSplitOnCaseChange").rename("splitOnCaseChange");
        classCustomization.getMethod("isSplitOnNumerics").rename("splitOnNumerics");

        addVarArgsOverload(classCustomization, "protectedWords", "String");
    }

    private void customizeElisionTokenFilter(ClassCustomization classCustomization) {
        addVarArgsOverload(classCustomization, "articles", "String");
    }

    private void customizeMicrosoftLanguageStemmingTokenizer(ClassCustomization classCustomization) {
        classCustomization.getMethod("isSearchTokenizerUsed").rename("isSearchTokenizer");
    }

    private void customizePatternTokenizer(ClassCustomization classCustomization) {
        classCustomization.getMethod("getFlags").setReturnType("List<RegexFlags>", "%s")
            .replaceBody(joinWithNewline(
                "if (this.flags == null) {",
                "    return null;",
                "} else {",
                "    String[] flagStrings = this.flags.toString().split(\"\\\\|\");",
                "    return java.util.Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList());",
                "}"), Collections.singletonList(Collectors.class.getName()));

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

    private void customizeIndexingParameters(ClassCustomization classCustomization, Editor editor) {
        classCustomization.customizeAst(ast -> ast.getClassByName("IndexingParameters").get()
            .addPrivateField("Map<String, Object>", "configurationMap"));

        classCustomization.getMethod("getConfiguration").rename("getIndexingParametersConfiguration");
        classCustomization.getMethod("setConfiguration").rename("setIndexingParametersConfiguration")
            .replaceBody(joinWithNewline(
                "this.configuration = configuration;",
                "this.configurationMap = MappingUtils.indexingParametersConfigurationToMap(configuration);",
                "return this;"
            ), Collections.singletonList("com.azure.search.documents.implementation.util.MappingUtils"));

        classCustomization.addMethod(joinWithNewline(
                "public Map<String, Object> getConfiguration() {",
                "    return this.configurationMap;",
                "}"
            ), Collections.singletonList(Map.class.getName()))
            .getJavadoc()
            .setDescription("Get the configuration property: A dictionary of indexer-specific configuration "
                + "properties. Each name is the name of a specific property. Each value must be of a primitive type.")
            .setReturn("the configuration value.");

        classCustomization.addMethod(joinWithNewline(
                "public IndexingParameters setConfiguration(Map<String, Object> configuration) {",
                "    this.configurationMap = configuration;",
                "    this.configuration = MappingUtils.mapToIndexingParametersConfiguration(configuration);",
                "    return this;",
                "}"
            ))
            .getJavadoc()
            .setReturn("Set the configuration property: A dictionary of indexer-specific configuration properties. "
                + "Each name is the name of a specific property. Each value must be of a primitive type.")
            .setParam("configuration", "the configuration value to set.")
            .setReturn("the IndexingParameters object itself.");

        String replacement = editor.getFileContent(classCustomization.getFileName())
            .replace("deserializedValue.configuration = configuration;",
                "deserializedValue.setIndexingParametersConfiguration(configuration);");
        editor.replaceFile(classCustomization.getFileName(), replacement);
    }

    private void customizeSearchIndexerDataSourceConnection(ClassCustomization classCustomization) {
        classCustomization.addConstructor(joinWithNewline(
                "public SearchIndexerDataSourceConnection(String name, SearchIndexerDataSourceType type, String connectionString, SearchIndexerDataContainer container) {",
                "    this.name = name;",
                "    this.type = type;",
                "    this.credentials = (connectionString == null) ? null : new DataSourceCredentials().setConnectionString(connectionString);",
                "    this.container = container;",
                "}"
            ))
            .getJavadoc()
            .setDescription("Constructor of {@link SearchIndexerDataSourceConnection}.")
            .setParam("name", "The name of the datasource.")
            .setParam("type", "The type of the datasource. Possible values include: 'AzureSql', 'CosmosDb', 'AzureBlob', 'AzureTable', 'MySql'")
            .setParam("connectionString", "The connection string for the datasource.")
            .setParam("container", "The data container for the datasource.");

        classCustomization.removeMethod("getCredentials");
        classCustomization.removeMethod("setCredentials");

        classCustomization.addMethod(joinWithNewline(
                "public String getConnectionString() {",
                "    return (credentials == null) ? null : credentials.getConnectionString();",
                "}"
            ))
            .getJavadoc()
            .setDescription("Get the connectionString property: The connection string for the datasource.")
            .setReturn("the connectionString value.");

        classCustomization.addMethod(joinWithNewline(
                "public SearchIndexerDataSourceConnection setConnectionString(String connectionString) {",
                "    if (connectionString == null) {",
                "        this.credentials = null;",
                "    } else if (credentials == null) {",
                "        this.credentials = new DataSourceCredentials().setConnectionString(connectionString);",
                "    } else {",
                "        credentials.setConnectionString(connectionString);",
                "    }",
                "    return this;",
                "}"
            ))
            .getJavadoc()
            .setDescription("Set the connectionString property: The connection string for the datasource.")
            .setParam("connectionString", "the connectionString value to set.")
            .setReturn("the SearchIndexerDataSourceConnection object itself.");
    }

    private static void bulkRemoveFromJsonMethods(ClassCustomization... classCustomizations) {
        for (ClassCustomization classCustomization : classCustomizations) {
            classCustomization.removeMethod("fromJson(JsonReader jsonReader)");
        }
    }

    private static void bulkSetClassModifier(PackageCustomization packageCustomization, int modifier,
        String... classNames) {
        if (classNames == null) {
            return;
        }

        for (String className : classNames) {
            setClassModifier(packageCustomization.getClass(className), modifier);
        }
    }

    private static void setClassModifier(ClassCustomization classCustomization, int modifier) {
        classCustomization.setModifier(modifier);
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
