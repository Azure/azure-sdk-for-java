// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
    private static final String VARARG_METHOD_BLOCK_TEMPLATE = joinWithNewline(
        "{",
        "    this.%1$s = (%1$s == null) ? null : java.util.Arrays.asList(%1$s);",
        "    return this;",
        "}"
    );

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
        customizeAst(publicCustomization.getClass("InputFieldMappingEntry"),
            clazz -> addVarArgsOverload(clazz, "inputs", "InputFieldMappingEntry"));
        customizeAst(publicCustomization.getClass("ScoringProfile"),
            clazz -> addVarArgsOverload(clazz, "functions", "ScoringFunction"));

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
        customizeSemanticPrioritizedFields(publicCustomization.getClass("SemanticPrioritizedFields"));
        customizeVectorSearch(publicCustomization.getClass("VectorSearch"));

        bulkRemoveFromJsonMethods(publicCustomization.getClass("SearchIndexerKnowledgeStoreProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"));
    }

    private static void customizeAst(ClassCustomization classCustomization, Consumer<ClassOrInterfaceDeclaration> consumer) {
        classCustomization.customizeAst(ast -> consumer.accept(ast.getClassByName(classCustomization.getClassName())
            .orElseThrow(() -> new RuntimeException("Class not found. " + classCustomization.getClassName()))));
    }

    private void customizeSearchIndex(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("List<SearchField>", "fields")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.name = name;",
                    "    this.fields = fields;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Constructor of {@link SearchIndex}.",
                    "@param name The name of the index.",
                    "@param fields The fields of the index."
                )));

            addVarArgsOverload(clazz, "fields", "SearchField");
            addVarArgsOverload(clazz, "scoringProfiles", "ScoringProfile");
            addVarArgsOverload(clazz, "suggesters", "SearchSuggester");
            addVarArgsOverload(clazz, "analyzers", "LexicalAnalyzer");
            addVarArgsOverload(clazz, "tokenizers", "LexicalTokenizer");
            addVarArgsOverload(clazz, "tokenFilters", "TokenFilter");
            addVarArgsOverload(clazz, "charFilters", "CharFilter");
            addVarArgsOverload(clazz, "normalizers", "LexicalNormalizer");
        });
    }

    private void customizeSearchIndexer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("String", "dataSourceName")
                .addParameter("String", "targetIndexName")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.name = name;",
                    "    this.dataSourceName = dataSourceName;",
                    "    this.targetIndexName = targetIndexName;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Constructor of {@link SearchIndexer}.",
                    "",
                    "@param name The name of the indexer.",
                    "@param dataSourceName The name of the datasource from which this indexer reads data.",
                    "@param targetIndexName The name of the index to which this indexer writes data."
                )));

            addVarArgsOverload(clazz, "fieldMappings", "FieldMapping");
            addVarArgsOverload(clazz, "outputFieldMappings", "FieldMapping");
        });
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addMethod("collection", com.github.javaparser.ast.Modifier.Keyword.PUBLIC, com.github.javaparser.ast.Modifier.Keyword.STATIC)
                .setType(classCustomization.getClassName())
                .addParameter(classCustomization.getClassName(), "dataType")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    return fromString(String.format(\"Collection(%s)\", dataType.toString()));",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Returns a collection of a specific SearchFieldDataType.",
                    "",
                    "@param dataType the corresponding SearchFieldDataType",
                    "@return a Collection of the corresponding SearchFieldDataType"
                )));
        });
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
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isShouldBoostBeyondRangeByConstant").get(0)
            .setName("shouldBoostBeyondRangeByConstant"));
    }

    private void customizeCognitiveServicesAccountKey(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getFieldByName("key").get().setModifiers(com.github.javaparser.ast.Modifier.Keyword.PRIVATE);
            clazz.addMethod("setKey", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType(classCustomization.getClassName())
                .addParameter("String", "key")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.key = key;",
                    "    return this;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Set the key property: The key used to provision the cognitive service resource attached to a skillset.",
                    "",
                    "@param key the key value to set.",
                    "@return the CognitiveServicesAccountKey object itself."
                )));
        });
    }

    private void customizeOcrSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addMethod("setShouldDetectOrientation", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType("Boolean")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    return this.shouldDetectOrientation;",
                    "}"
                )))
                .addAnnotation("Deprecated")
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Get the shouldDetectOrientation property: A value indicating to turn orientation detection on or not. Default is",
                    "false.",
                    "",
                    "@return the shouldDetectOrientation value.",
                    "@deprecated Use {@link #isShouldDetectOrientation()} instead."
                )));
        });
    }

    private void customizeImageAnalysisSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            addVarArgsOverload(clazz, "visualFeatures", "VisualFeature");
            addVarArgsOverload(clazz, "details", "ImageDetail");
        });
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "inlineEntitiesDefinition", "CustomEntity"));
    }

    private void customizeCustomNormalizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            addVarArgsOverload(clazz, "tokenFilters", "TokenFilterName");
            addVarArgsOverload(clazz, "charFilters", "CharFilterName");
        });
    }

    private void customizeSearchField(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("setHidden").get(0)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.hidden = (hidden == null) ? null : !hidden;",
                    "    return this;",
                    "}"
                )));

            clazz.getMethodsByName("isHidden").get(0)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    return (this.hidden == null) ? null : !this.hidden;",
                    "}"
                )));

            addVarArgsOverload(clazz, "fields", "SearchField");
            addVarArgsOverload(clazz, "synonymMapNames", "String");
        });
    }

    private void customizeSynonymMap(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("getFormat").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setFormat").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setName").forEach(MethodDeclaration::remove);


            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Constructor of {@link SynonymMap}.",
                    "@param name The name of the synonym map."
                )))
                .addParameter("String", "name")
                .getBody()
                .addStatement(StaticJavaParser.parseExplicitConstructorInvocationStmt("this(name,null);"));

            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("String", "synonyms")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.format = \"solr\";",
                    "    this.name = name;",
                    "    this.synonyms = synonyms;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Constructor of {@link SynonymMap}.",
                    "@param name The name of the synonym map.",
                    "@param synonyms A series of synonym rules in the specified synonym map format. The rules must be separated by newlines."
                )));

            clazz.addMethod("createFromFile", com.github.javaparser.ast.Modifier.Keyword.PUBLIC, com.github.javaparser.ast.Modifier.Keyword.STATIC)
                .setType("SynonymMap")
                .addParameter("String", "name")
                .addParameter("java.nio.file.Path", "filePath")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    String synonyms = com.azure.search.documents.implementation.util.Utility.readSynonymsFromFile(filePath);",
                    "    return new SynonymMap(name, synonyms);",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Creates a new instance of SynonymMap with synonyms read from the passed file.",
                    "",
                    "@param name The name of the synonym map.",
                    "@param filePath The path to the file where the formatted synonyms are read.",
                    "@return A SynonymMap.",
                    "@throws java.io.UncheckedIOException If reading {@code filePath} fails."
                )));
        });
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
        customizeAst(classCustomization, clazz -> {
            clazz.getConstructors().get(0)
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.searchMode = \"analyzingInfixMatching\";",
                    "    this.sourceFields = sourceFields;",
                    "    this.name = name;",
                    "}"
                )));

            clazz.getMethodsByName("setSearchMode").forEach(MethodDeclaration::remove);
        });
    }

    private void customizeCustomAnalyzer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            addVarArgsOverload(clazz, "tokenFilters", "TokenFilterName");
            addVarArgsOverload(clazz, "charFilters", "CharFilterName");
        });
    }

    private void customizePatternAnalyzer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isLowerCaseTerms").get(0).setName("areLowerCaseTerms");
            addVarArgsOverload(clazz, "stopwords", "String");

            clazz.getMethodsByName("getFlags").get(0)
                .setType("List<RegexFlags>")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (this.flags == null) {",
                    "        return null;",
                    "    } else {",
                    "        String[] flagStrings = this.flags.toString().split(\"\\\\|\");",
                    "        return java.util.Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList());",
                    "    }",
                    "}"
                )));

            clazz.tryAddImportToParentCompilationUnit(Collectors.class);

            clazz.getMethodsByName("setFlags").get(0)
                .setParameters(new NodeList<>(new Parameter().setType("List<RegexFlags>").setName("flags")))
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (flags == null) {",
                    "        this.flags = null;",
                    "    } else {",
                    "        String flagString = flags.stream().map(RegexFlags::toString).collect(Collectors.joining(\"|\"));",
                    "        this.flags = RegexFlags.fromString(flagString);",
                    "    }",
                    "",
                    "    return this;",
                    "}"
                )));

            addVarArgsOverload(clazz, "flags", "RegexFlags")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (flags == null) {",
                    "        this.flags = null;",
                    "        return this;",
                    "    } else {",
                    "        return setFlags(java.util.Arrays.asList(flags));",
                    "    }",
                    "}"
                )));
        });
    }

    private void customizeLuceneStandardAnalyzer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "stopwords", "String"));
    }

    private void customizeStopAnalyzer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "stopwords", "String"));
    }

    private void customizeSearchIndexerSkillset(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("List<SearchIndexerSkill>", "skills")
                .setBody(new BlockStmt()
                    .addStatement(StaticJavaParser.parseExplicitConstructorInvocationStmt("this(name);"))
                    .addStatement(StaticJavaParser.parseStatement("this.skills = skills;")))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "/**",
                    " * Creates an instance of SearchIndexerSkillset class.",
                    " * @param name The name of the skillset.",
                    " * @param skills The skills in the skillset.",
                    " */"
                )));

            addVarArgsOverload(clazz, "skills", "SearchIndexerSkill");
        });
    }

    private void customizeCjkBigramTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isOutputUnigrams").get(0).setName("areOutputUnigrams");
            addVarArgsOverload(clazz, "ignoreScripts", "CjkBigramTokenFilterScripts");
        });
    }

    private void customizeKeepTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz ->
            clazz.getMethodsByName("isLowerCaseKeepWords").get(0).setName("areLowerCaseKeepWords"));
    }

    private void customizeSynonymTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isExpand").get(0).setName("getExpand"));
    }

    private void customizeShingleTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isOutputUnigrams").get(0).setName("areOutputUnigrams");
            clazz.getMethodsByName("isOutputUnigramsIfNoShingles").get(0).setName("areOutputUnigramsIfNoShingles");
        });
    }

    private void customizeLimitTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz ->
            clazz.getMethodsByName("isAllTokensConsumed").get(0).setName("areAllTokensConsumed"));
    }

    private void customizePhoneticTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz ->
            clazz.getMethodsByName("isOriginalTokensReplaced").get(0).setName("areOriginalTokensReplaced"));
    }

    private void customizeStopwordsTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isTrailingStopWordsRemoved").get(0).setName("areTrailingStopWordsRemoved");

            addVarArgsOverload(clazz, "stopwords", "String");
        });
    }

    private void customizeNGramTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "tokenChars", "TokenCharacterKind"));
    }

    private void customizeEdgeNGramTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "tokenChars", "TokenCharacterKind"));
    }

    private void customizeWordDelimiterTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isGenerateWordParts").get(0).setName("generateWordParts");
            clazz.getMethodsByName("isGenerateNumberParts").get(0).setName("generateNumberParts");
            clazz.getMethodsByName("isWordsCatenated").get(0).setName("areWordsCatenated");
            clazz.getMethodsByName("isNumbersCatenated").get(0).setName("areNumbersCatenated");
            clazz.getMethodsByName("isCatenateAll").get(0).setName("catenateAll");
            clazz.getMethodsByName("isSplitOnCaseChange").get(0).setName("splitOnCaseChange");
            clazz.getMethodsByName("isSplitOnNumerics").get(0).setName("splitOnNumerics");

            addVarArgsOverload(clazz, "protectedWords", "String");
        });
    }

    private void customizeElisionTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "articles", "String"));
    }

    private void customizeMicrosoftLanguageStemmingTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz ->
            clazz.getMethodsByName("isSearchTokenizerUsed").get(0).setName("isSearchTokenizer"));
    }

    private void customizePatternTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("getFlags").get(0)
                .setType("List<RegexFlags>")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (this.flags == null) {",
                    "        return null;",
                    "    } else {",
                    "        String[] flagStrings = this.flags.toString().split(\"\\\\|\");",
                    "        return java.util.Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList());",
                    "    }",
                    "}"
                )));

            clazz.tryAddImportToParentCompilationUnit(Collectors.class);

            clazz.getMethodsByName("setFlags").get(0)
                .setParameters(new NodeList<>(new Parameter().setType("List<RegexFlags>").setName("flags")))
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (flags == null) {",
                    "        this.flags = null;",
                    "    } else {",
                    "        String flagString = flags.stream().map(RegexFlags::toString).collect(Collectors.joining(\"|\"));",
                    "        this.flags = RegexFlags.fromString(flagString);",
                    "    }",
                    "",
                    "    return this;",
                    "}"
                )));

            addVarArgsOverload(clazz, "flags", "RegexFlags")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (flags == null) {",
                    "        this.flags = null;",
                    "        return this;",
                    "    } else {",
                    "        return setFlags(java.util.Arrays.asList(flags));",
                    "    }",
                    "}"
                )));
        });
    }

    private void customizeIndexingParameters(ClassCustomization classCustomization, Editor editor) {
        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("Map<String, Object>", "configurationMap");
            clazz.tryAddImportToParentCompilationUnit(Map.class);

            clazz.getMethodsByName("getConfiguration").get(0).setName("getIndexingParametersConfiguration");
            clazz.getMethodsByName("setConfiguration").get(0)
                .setName("setIndexingParametersConfiguration")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.configuration = configuration;",
                    "    this.configurationMap = MappingUtils.indexingParametersConfigurationToMap(configuration);",
                    "    return this;",
                    "}"
                )));

            clazz.findAncestor(CompilationUnit.class)
                .ifPresent(p -> p.addImport("com.azure.search.documents.implementation.util.MappingUtils"));

            clazz.addMethod("getConfiguration", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType("Map<String, Object>")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    return this.configurationMap;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Get the configuration property: A dictionary of indexer-specific configuration properties. " + "Each name is the name of a specific property. Each value must be of a primitive type.",
                    "",
                    "@return the configuration value."
                )));

            clazz.addMethod("setConfiguration", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType("IndexingParameters")
                .addParameter("Map<String, Object>", "configuration")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.configurationMap = configuration;",
                    "    this.configuration = MappingUtils.mapToIndexingParametersConfiguration(configuration);",
                    "    return this;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Set the configuration property: A dictionary of indexer-specific configuration properties. " + "Each name is the name of a specific property. Each value must be of a primitive type.",
                    "",
                    "@param configuration the configuration value to set.",
                    "@return the IndexingParameters object itself."
                )));
        });

        String replacement = editor
            .getFileContent(classCustomization.getFileName())
            .replace("deserializedValue.configuration = configuration;",
                "deserializedValue.setIndexingParametersConfiguration(configuration);");
        editor.replaceFile(classCustomization.getFileName(), replacement);
    }

    private void customizeSearchIndexerDataSourceConnection(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("SearchIndexerDataSourceType", "type")
                .addParameter("String", "connectionString")
                .addParameter("SearchIndexerDataContainer", "container")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    this.name = name;",
                    "    this.type = type;",
                    "    this.credentials = (connectionString == null) ? null : new DataSourceCredentials().setConnectionString(connectionString);",
                    "    this.container = container;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Constructor of {@link SearchIndexerDataSourceConnection}.",
                    "",
                    "@param name The name of the datasource.",
                    "@param type The type of the datasource.",
                    "@param connectionString The connection string for the datasource.",
                    "@param container The data container for the datasource."
                )));

            clazz.getMethodsByName("getCredentials").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setCredentials").forEach(MethodDeclaration::remove);

            clazz.addMethod("getConnectionString", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType("String")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    return (credentials == null) ? null : credentials.getConnectionString();",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Get the connectionString property: The connection string for the datasource.",
                    "",
                    "@return the connectionString value."
                )));

            clazz.addMethod("setConnectionString", com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
                .setType("SearchIndexerDataSourceConnection")
                .addParameter("String", "connectionString")
                .setBody(StaticJavaParser.parseBlock(joinWithNewline(
                    "{",
                    "    if (connectionString == null) {",
                    "        this.credentials = null;",
                    "    } else if (credentials == null) {",
                    "        this.credentials = new DataSourceCredentials().setConnectionString(connectionString);",
                    "    } else {",
                    "        credentials.setConnectionString(connectionString);",
                    "    }",
                    "    return this;",
                    "}"
                )))
                .setJavadocComment(StaticJavaParser.parseJavadoc(joinWithNewline(
                    "Set the connectionString property: The connection string for the datasource.",
                    "",
                    "@param connectionString the connectionString value to set.",
                    "@return the SearchIndexerDataSourceConnection object itself."
                )));
        });
    }

    private static void customizeSemanticPrioritizedFields(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.tryAddImportToParentCompilationUnit(Arrays.class);

            addVarArgsOverload(clazz, "contentFields", "SemanticField");
            addVarArgsOverload(clazz, "keywordsFields", "SemanticField");
        });
    }

    private static void customizeVectorSearch(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.tryAddImportToParentCompilationUnit(Arrays.class);

            addVarArgsOverload(clazz, "profiles", "VectorSearchProfile");
            addVarArgsOverload(clazz, "algorithms", "VectorSearchAlgorithmConfiguration");
        });
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
    private static MethodDeclaration addVarArgsOverload(ClassOrInterfaceDeclaration clazz, String parameterName,
        String parameterType) {
        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        MethodDeclaration nonVarArgOverload = clazz.getMethodsByName(methodName).get(0);

        return clazz.addMethod(methodName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
            .setType(clazz.getNameAsString())
            .addParameter(new Parameter().setType(parameterType).setName(parameterName).setVarArgs(true))
            .setBody(StaticJavaParser.parseBlock(String.format(VARARG_METHOD_BLOCK_TEMPLATE, parameterName)))
            .setJavadocComment(nonVarArgOverload.getJavadoc().get());
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
