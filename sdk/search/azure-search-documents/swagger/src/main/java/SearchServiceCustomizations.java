// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.Editor;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
public class SearchServiceCustomizations extends Customization {
    private static final String VARARG_METHOD_BLOCK_TEMPLATE
        = "{ this.%1$s = (%1$s == null) ? null : Arrays.asList(%1$s); return this; }";

    // Packages
    private static final String MODELS = "com.azure.search.documents.indexes.models";

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        PackageCustomization publicCustomization = libraryCustomization.getPackage(MODELS);

        // Customize implementation models.

        // Customize models.

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
        customizeLexicalTokenizer(publicCustomization.getClass("LexicalTokenizer"),
            libraryCustomization.getRawEditor());
        customizeMagnitudeScoringParameters(publicCustomization.getClass("MagnitudeScoringParameters"));
        customizeSearchFieldDataType(publicCustomization.getClass("SearchFieldDataType"));
        customizeCognitiveServicesAccountKey(publicCustomization.getClass("CognitiveServicesAccountKey"));
        customizeOcrSkill(publicCustomization.getClass("OcrSkill"));
        customizeImageAnalysisSkill(publicCustomization.getClass("ImageAnalysisSkill"));
        customizeCustomEntityLookupSkill(publicCustomization.getClass("CustomEntityLookupSkill"));
        customizeSearchField(publicCustomization.getClass("SearchField"));
        customizeSynonymMap(publicCustomization.getClass("SynonymMap"));
        customizeSearchResourceEncryptionKey(publicCustomization.getClass("SearchResourceEncryptionKey"));
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
        customizeAzureOpenAIModelName(publicCustomization.getClass("AzureOpenAIModelName"));
        // customizeSearchError(implCustomization.getClass("SearchError"));
        customizeSplitSkillEncoderModelName(publicCustomization.getClass("SplitSkillEncoderModelName"));

        bulkRemoveFromJsonMethods(publicCustomization.getClass("SearchIndexerKnowledgeStoreProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"));
    }

    private static void customizeAst(ClassCustomization classCustomization,
        Consumer<ClassOrInterfaceDeclaration> consumer) {
        classCustomization.customizeAst(ast -> consumer.accept(ast.getClassByName(classCustomization.getClassName())
            .orElseThrow(() -> new RuntimeException("Class not found. " + classCustomization.getClassName()))));
    }

    private void customizeSearchIndex(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("List<SearchField>", "fields")
                .setBody(StaticJavaParser.parseBlock("{ this.name = name; this.fields = fields; }"))
                .setJavadocComment(newJavadoc("Constructor of {@link SearchIndex}.")
                    .addBlockTag("param", "name", "The name of the index.")
                    .addBlockTag("param", "fields", "The fields of the index."));

            addVarArgsOverload(clazz, "fields", "SearchField");
            addVarArgsOverload(clazz, "scoringProfiles", "ScoringProfile");
            addVarArgsOverload(clazz, "suggesters", "SearchSuggester");
            addVarArgsOverload(clazz, "analyzers", "LexicalAnalyzer");
            addVarArgsOverload(clazz, "tokenizers", "LexicalTokenizer");
            addVarArgsOverload(clazz, "tokenFilters", "TokenFilter");
            addVarArgsOverload(clazz, "charFilters", "CharFilter");
        });
    }

    private void customizeSearchIndexer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("String", "dataSourceName")
                .addParameter("String", "targetIndexName")
                .setBody(StaticJavaParser.parseBlock("{ this.name = name; this.dataSourceName = dataSourceName;"
                    + "this.targetIndexName = targetIndexName; }"))
                .setJavadocComment(newJavadoc("Constructor of {@link SearchIndexer}.")
                    .addBlockTag("param", "name", "The name of the indexer.")
                    .addBlockTag("param", "dataSourceName", "The name of the datasource from which this indexer reads data.")
                    .addBlockTag("param", "targetIndexName", "The name of the index to which this indexer writes data."));

            addVarArgsOverload(clazz, "fieldMappings", "FieldMapping");
            addVarArgsOverload(clazz, "outputFieldMappings", "FieldMapping");
        });
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.addMethod("collection", Modifier.Keyword.PUBLIC,
                Modifier.Keyword.STATIC)
            .setType(classCustomization.getClassName())
            .addParameter(classCustomization.getClassName(), "dataType")
            .setBody(StaticJavaParser.parseBlock("{ return fromString(String.format(\"Collection(%s)\", dataType.toString())); }"))
            .setJavadocComment(newJavadoc("Returns a collection of a specific SearchFieldDataType.")
                .addBlockTag("param", "dataType", "the corresponding SearchFieldDataType")
                .addBlockTag("return", "a Collection of the corresponding SearchFieldDataType")));
    }

    private void customizeSearchIndexerSkill(ClassCustomization classCustomization, Editor editor) {
        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "EntityRecognitionSkillV1",
            "EntityRecognitionSkill");
        fileContents = updateVersionedDeserialization(fileContents, "EntityRecognitionSkillV3",
            "EntityRecognitionSkill");
        fileContents = updateVersionedDeserialization(fileContents, "SentimentSkillV1", "SentimentSkill");
        fileContents = updateVersionedDeserialization(fileContents, "SentimentSkillV3", "SentimentSkill");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private void customizeTokenFilter(ClassCustomization classCustomization, Editor editor) {
        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "EdgeNGramTokenFilterV1", "EdgeNGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "EdgeNGramTokenFilterV2", "EdgeNGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "NGramTokenFilterV1", "NGramTokenFilter");
        fileContents = updateVersionedDeserialization(fileContents, "NGramTokenFilterV2", "NGramTokenFilter");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private void customizeLexicalTokenizer(ClassCustomization classCustomization, Editor editor) {
        String fileContents = editor.getFileContent(classCustomization.getFileName());

        fileContents = updateVersionedDeserialization(fileContents, "KeywordTokenizerV1", "KeywordTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "KeywordTokenizerV2", "KeywordTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "LuceneStandardTokenizerV1",
            "LuceneStandardTokenizer");
        fileContents = updateVersionedDeserialization(fileContents, "LuceneStandardTokenizerV2",
            "LuceneStandardTokenizer");

        editor.replaceFile(classCustomization.getFileName(), fileContents);
    }

    private String updateVersionedDeserialization(String fileContents, String codegenName, String Name) {
        String target = String.format("return %1$s.fromJson(readerToUse);", codegenName);
        String replacement = String.format(joinWithNewline("%1$s codegen = %1$s.fromJson(readerToUse);",
            "return (codegen == null) ? null : new %2$s(codegen);"), codegenName, Name);

        return fileContents.replace(target, replacement);
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isShouldBoostBeyondRangeByConstant")
            .forEach(method -> method.setName("shouldBoostBeyondRangeByConstant")));
    }

    private void customizeCognitiveServicesAccountKey(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getFieldByName("key").ifPresent(field -> field.setModifiers(Modifier.Keyword.PRIVATE));
            clazz.addMethod("setKey", Modifier.Keyword.PUBLIC)
                .setType(classCustomization.getClassName())
                .addParameter("String", "key")
                .setBody(StaticJavaParser.parseBlock("{ this.key = key; return this; }"))
                .setJavadocComment(newJavadoc("Set the key property: The key used to provision the cognitive service resource attached to a skillset.")
                    .addBlockTag("param", "key", "the key value to set.")
                    .addBlockTag("return", "the CognitiveServicesAccountKey object itself."));
        });
    }

    private void customizeOcrSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.addMethod("setShouldDetectOrientation", Modifier.Keyword.PUBLIC)
            .setType("Boolean")
            .setBody(StaticJavaParser.parseBlock("{ return this.shouldDetectOrientation; }"))
            .addMarkerAnnotation("Deprecated")
            .setJavadocComment(newJavadoc("Get the shouldDetectOrientation property: A value indicating to turn orientation detection on or not. Default is false.")
                .addBlockTag("return", "the shouldDetectOrientation value.")
                .addBlockTag("deprecated", "Use {@link #isShouldDetectOrientation()} instead.")));
    }

    private void customizeImageAnalysisSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            addVarArgsOverload(clazz, "visualFeatures", "VisualFeature");
            addVarArgsOverload(clazz, "details", "ImageDetail");
        });
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        customizeAst(classCustomization,
            clazz -> addVarArgsOverload(clazz, "inlineEntitiesDefinition", "CustomEntity"));
    }

    private void customizeSearchField(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("setHidden").forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                "{ this.hidden = (hidden == null) ? null : !hidden; return this; }")));

            clazz.getMethodsByName("isHidden").forEach(method -> method.setBody(StaticJavaParser.parseBlock(
                "{ return (this.hidden == null) ? null : !this.hidden; }")));

            addVarArgsOverload(clazz, "fields", "SearchField");
            addVarArgsOverload(clazz, "synonymMapNames", "String");
        });
    }

    private void customizeSynonymMap(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("getFormat").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setFormat").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setName").forEach(MethodDeclaration::remove);

            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .setJavadocComment(StaticJavaParser.parseJavadoc(
                    joinWithNewline("Constructor of {@link SynonymMap}.", "@param name The name of the synonym map.")))
                .addParameter("String", "name")
                .getBody()
                .addStatement(StaticJavaParser.parseExplicitConstructorInvocationStmt("this(name, null);"));

            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("String", "synonyms")
                .setBody(StaticJavaParser.parseBlock("{ this.format = \"solr\"; this.name = name; this.synonyms = synonyms; }"))
                .setJavadocComment(newJavadoc("Constructor of {@link SynonymMap}.")
                    .addBlockTag("param", "name", "The name of the synonym map.")
                    .addBlockTag("param", "synonyms", "A series of synonym rules in the specified synonym map format. The rules must be separated by newlines."));

            clazz.addMethod("createFromFile", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("SynonymMap")
                .addParameter("String", "name")
                .addParameter("java.nio.file.Path", "filePath")
                .setBody(StaticJavaParser.parseBlock("{"
                    + "String synonyms = com.azure.search.documents.implementation.util.Utility.readSynonymsFromFile(filePath);"
                    + "return new SynonymMap(name, synonyms); }"))
                .setJavadocComment(newJavadoc("Creates a new instance of SynonymMap with synonyms read from the passed file.")
                    .addBlockTag("param", "name", "The name of the synonym map.")
                    .addBlockTag("param", "filePath", "The path to the file where the formatted synonyms are read.")
                    .addBlockTag("return", "A SynonymMap.")
                    .addBlockTag("throws", "java.io.UncheckedIOException", "If reading {@code filePath} fails."));
        });
    }

    private void customizeSearchResourceEncryptionKey(ClassCustomization keyCustomization) {
        customizeAst(keyCustomization, clazz -> {
            clazz.getMethodsByName("getAccessCredentials").forEach(Node::remove);
            clazz.getMethodsByName("setAccessCredentials").forEach(Node::remove);

            clazz.addMethod("getApplicationId", Modifier.Keyword.PUBLIC)
                .setType(String.class)
                .setBody(StaticJavaParser.parseBlock(
                    "{ return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationId(); }"))
                .setJavadocComment(newJavadoc("Get the applicationId property: An AAD Application ID that was granted "
                    + "the required access permissions to the Azure Key Vault that is to be used when encrypting your "
                    + "data at rest. The Application ID should not be confused with the Object ID for your AAD Application.")
                    .addBlockTag("return", "the applicationId value."));

            clazz.addMethod("setApplicationId", Modifier.Keyword.PUBLIC)
                .setType(keyCustomization.getClassName())
                .addParameter(String.class, "applicationId")
                .setBody(StaticJavaParser.parseBlock("{ if (this.accessCredentials == null) {"
                    + "this.accessCredentials = new AzureActiveDirectoryApplicationCredentials(); }"
                    + "this.accessCredentials.setApplicationId(applicationId); return this; }"))
                .setJavadocComment(newJavadoc("Set the applicationId property: An AAD Application ID that was granted "
                    + "the required access permissions to the Azure Key Vault that is to be used when encrypting your "
                    + "data at rest. The Application ID should not be confused with the Object ID for your AAD Application.")
                    .addBlockTag("param", "applicationId", "the applicationId value to set.")
                    .addBlockTag("return", "the SearchResourceEncryptionKey object itself."));

            clazz.addMethod("getApplicationSecret", Modifier.Keyword.PUBLIC)
                .setType(String.class)
                .setBody(StaticJavaParser.parseBlock("{ return (this.accessCredentials == null) ? null : this.accessCredentials.getApplicationSecret(); }"))
                .setJavadocComment(newJavadoc("Get the applicationSecret property: The authentication key of the specified AAD application.")
                    .addBlockTag("return", "the applicationSecret value."));

            clazz.addMethod("setApplicationSecret", Modifier.Keyword.PUBLIC)
                .setType(keyCustomization.getClassName())
                .addParameter(String.class, "applicationSecret")
                .setBody(StaticJavaParser.parseBlock("{ if (this.accessCredentials == null) {"
                    + "this.accessCredentials = new AzureActiveDirectoryApplicationCredentials(); }"
                    + "this.accessCredentials.setApplicationSecret(applicationSecret); return this; }"))
                .setJavadocComment(newJavadoc("Set the applicationSecret property: The authentication key of the specified AAD application.")
                    .addBlockTag("param", "applicationSecret", "the applicationSecret value to set.")
                    .addBlockTag("return", "the SearchResourceEncryptionKey object itself."));
        });
    }

    private void customizeSearchSuggester(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getConstructors().get(0)
                .setBody(StaticJavaParser.parseBlock(
                    "{ this.searchMode = \"analyzingInfixMatching\"; this.sourceFields = sourceFields; this.name = name; }"));

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
            clazz.tryAddImportToParentCompilationUnit(Arrays.class);

            clazz.getMethodsByName("isLowerCaseTerms").forEach(method -> method.setName("areLowerCaseTerms"));
            addVarArgsOverload(clazz, "stopwords", "String");

            clazz.getMethodsByName("getFlags").forEach(method -> method.setType("List<RegexFlags>")
                .setBody(StaticJavaParser.parseBlock("{ if (this.flags == null) { return null; } else {"
                        + "String[] flagStrings = this.flags.toString().split(\"\\\\|\");"
                        + "return Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList()); } }")));

            clazz.tryAddImportToParentCompilationUnit(Collectors.class);

            clazz.getMethodsByName("setFlags").forEach(method -> method
                .setParameters(new NodeList<>(new Parameter().setType("List<RegexFlags>").setName("flags")))
                .setBody(StaticJavaParser.parseBlock("{ if (flags == null) { this.flags = null; } else {"
                        + "String flagString = flags.stream().map(RegexFlags::toString).collect(Collectors.joining(\"|\"));"
                        + "this.flags = RegexFlags.fromString(flagString); } return this; }")));

            addVarArgsOverload(clazz, "flags", "RegexFlags").setBody(StaticJavaParser.parseBlock(
                "{ if (flags == null) { this.flags = null; return this; } else { return setFlags(Arrays.asList(flags)); } }"));
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
            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("List<SearchIndexerSkill>", "skills")
                .setBody(new BlockStmt().addStatement(StaticJavaParser.parseStatement("this(name);"))
                    .addStatement(StaticJavaParser.parseStatement("this.skills = skills;")))
                .setJavadocComment(newJavadoc("Creates an instance of SearchIndexerSkillset class.")
                    .addBlockTag("param", "name", "The name of the skillset.")
                    .addBlockTag("param", "skills", "The skills in the skillset."));

            addVarArgsOverload(clazz, "skills", "SearchIndexerSkill");
        });
    }

    private void customizeCjkBigramTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isOutputUnigrams").forEach(method -> method.setName("areOutputUnigrams"));
            addVarArgsOverload(clazz, "ignoreScripts", "CjkBigramTokenFilterScripts");
        });
    }

    private void customizeKeepTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isLowerCaseKeepWords")
            .forEach(method -> method.setName("areLowerCaseKeepWords")));
    }

    private void customizeSynonymTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isExpand")
            .forEach(method -> method.setName("getExpand")));
    }

    private void customizeShingleTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isOutputUnigrams").forEach(method -> method.setName("areOutputUnigrams"));
            clazz.getMethodsByName("isOutputUnigramsIfNoShingles")
                .forEach(method -> method.setName("areOutputUnigramsIfNoShingles"));
        });
    }

    private void customizeLimitTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isAllTokensConsumed")
            .forEach(method -> method.setName("areAllTokensConsumed")));
    }

    private void customizePhoneticTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isOriginalTokensReplaced")
            .forEach(method -> method.setName("areOriginalTokensReplaced")));
    }

    private void customizeStopwordsTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getMethodsByName("isTrailingStopWordsRemoved")
                .forEach(method -> method.setName("areTrailingStopWordsRemoved"));

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
            clazz.getMethodsByName("isGenerateWordParts").forEach(method -> method.setName("generateWordParts"));
            clazz.getMethodsByName("isGenerateNumberParts").forEach(method -> method.setName("generateNumberParts"));
            clazz.getMethodsByName("isWordsCatenated").forEach(method -> method.setName("areWordsCatenated"));
            clazz.getMethodsByName("isNumbersCatenated").forEach(method -> method.setName("areNumbersCatenated"));
            clazz.getMethodsByName("isCatenateAll").forEach(method -> method.setName("catenateAll"));
            clazz.getMethodsByName("isSplitOnCaseChange").forEach(method -> method.setName("splitOnCaseChange"));
            clazz.getMethodsByName("isSplitOnNumerics").forEach(method -> method.setName("splitOnNumerics"));

            addVarArgsOverload(clazz, "protectedWords", "String");
        });
    }

    private void customizeElisionTokenFilter(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> addVarArgsOverload(clazz, "articles", "String"));
    }

    private void customizeMicrosoftLanguageStemmingTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> clazz.getMethodsByName("isSearchTokenizerUsed")
            .forEach(method -> method.setName("isSearchTokenizer")));
    }

    private void customizePatternTokenizer(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.tryAddImportToParentCompilationUnit(Arrays.class);

            clazz.getMethodsByName("getFlags").forEach(method -> method.setType("List<RegexFlags>")
                .setBody(StaticJavaParser.parseBlock("{ if (this.flags == null) { return null; } else {"
                        + "String[] flagStrings = this.flags.toString().split(\"\\\\|\");"
                        + "return Arrays.stream(flagStrings).map(RegexFlags::fromString).collect(Collectors.toList()); } }")));

            clazz.tryAddImportToParentCompilationUnit(Collectors.class);

            clazz.getMethodsByName("setFlags").forEach(method -> method
                .setParameters(new NodeList<>(new Parameter().setType("List<RegexFlags>").setName("flags")))
                .setBody(StaticJavaParser.parseBlock("{ if (flags == null) { this.flags = null; } else {"
                        + "String flagString = flags.stream().map(RegexFlags::toString).collect(Collectors.joining(\"|\"));"
                        + "this.flags = RegexFlags.fromString(flagString); } return this; }")));

            addVarArgsOverload(clazz, "flags", "RegexFlags").setBody(StaticJavaParser.parseBlock(
                "{ if (flags == null) { this.flags = null; return this; } else { return setFlags(Arrays.asList(flags)); } }"));
        });
    }

    private void customizeIndexingParameters(ClassCustomization classCustomization, Editor editor) {
        customizeAst(classCustomization, clazz -> {
            clazz.addPrivateField("Map<String, Object>", "configurationMap");
            clazz.tryAddImportToParentCompilationUnit(Map.class);

            clazz.getMethodsByName("getConfiguration").forEach(method -> method.setName("getIndexingParametersConfiguration"));
            clazz.getMethodsByName("setConfiguration").forEach(method -> method.setName("setIndexingParametersConfiguration")
                .setBody(StaticJavaParser.parseBlock("{ this.configuration = configuration;"
                    + "this.configurationMap = MappingUtils.indexingParametersConfigurationToMap(configuration);"
                    + "return this; }")));

            clazz.findAncestor(CompilationUnit.class)
                .ifPresent(p -> p.addImport("com.azure.search.documents.implementation.util.MappingUtils"));

            clazz.addMethod("getConfiguration", Modifier.Keyword.PUBLIC)
                .setType("Map<String, Object>")
                .setBody(StaticJavaParser.parseBlock("{ return this.configurationMap; }"))
                .setJavadocComment(newJavadoc("Get the configuration property: A dictionary of indexer-specific "
                    + "configuration properties. Each name is the name of a specific property. Each value must be of a primitive type.")
                    .addBlockTag("return", "the configuration value."));

            clazz.addMethod("setConfiguration", Modifier.Keyword.PUBLIC)
                .setType("IndexingParameters")
                .addParameter("Map<String, Object>", "configuration")
                .setBody(StaticJavaParser.parseBlock("{ this.configurationMap = configuration;"
                    + "this.configuration = MappingUtils.mapToIndexingParametersConfiguration(configuration);"
                    + "return this; }"))
                .setJavadocComment(newJavadoc("Set the configuration property: A dictionary of indexer-specific "
                    + "configuration properties. Each name is the name of a specific property. Each value must be of a primitive type.")
                    .addBlockTag("param", "configuration", "the configuration value to set.")
                    .addBlockTag("return", "the IndexingParameters object itself."));
        });

        String replacement = editor.getFileContent(classCustomization.getFileName())
            .replace("deserializedValue.configuration = configuration;",
                "deserializedValue.setIndexingParametersConfiguration(configuration);");
        editor.replaceFile(classCustomization.getFileName(), replacement);
    }

    private void customizeSearchIndexerDataSourceConnection(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.addConstructor(Modifier.Keyword.PUBLIC)
                .addParameter("String", "name")
                .addParameter("SearchIndexerDataSourceType", "type")
                .addParameter("String", "connectionString")
                .addParameter("SearchIndexerDataContainer", "container")
                .setBody(StaticJavaParser.parseBlock("{ this.name = name; this.type = type;"
                    + "this.credentials = (connectionString == null) ? null : new DataSourceCredentials().setConnectionString(connectionString);"
                    + "this.container = container; }"))
                .setJavadocComment(newJavadoc("Constructor of {@link SearchIndexerDataSourceConnection}.")
                    .addBlockTag("param", "name", "The name of the datasource.")
                    .addBlockTag("param", "type", "The type of the datasource.")
                    .addBlockTag("param", "connectionString", "The connection string for the datasource.")
                    .addBlockTag("param", "container", "The data container for the datasource."));

            clazz.getMethodsByName("getCredentials").forEach(MethodDeclaration::remove);
            clazz.getMethodsByName("setCredentials").forEach(MethodDeclaration::remove);

            clazz.addMethod("getConnectionString", Modifier.Keyword.PUBLIC)
                .setType("String")
                .setBody(StaticJavaParser.parseBlock("{ return (credentials == null) ? null : credentials.getConnectionString(); }"))
                .setJavadocComment(newJavadoc("Get the connectionString property: The connection string for the datasource.")
                    .addBlockTag("return", "the connectionString value."));

            clazz.addMethod("setConnectionString", Modifier.Keyword.PUBLIC)
                .setType("SearchIndexerDataSourceConnection")
                .addParameter("String", "connectionString")
                .setBody(StaticJavaParser.parseBlock("{ if (connectionString == null) { this.credentials = null;"
                    + "} else if (credentials == null) {"
                    + "this.credentials = new DataSourceCredentials().setConnectionString(connectionString); } else {"
                    + "credentials.setConnectionString(connectionString); } return this; }"))
                .setJavadocComment(newJavadoc("Set the connectionString property: The connection string for the datasource.")
                    .addBlockTag("param", "connectionString", "the connectionString value to set.")
                    .addBlockTag("return", "the SearchIndexerDataSourceConnection object itself."));
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
            addVarArgsOverload(clazz, "compressions", "VectorSearchCompression");
            addVarArgsOverload(clazz, "vectorizers", "VectorSearchVectorizer");
        });
    }

    private static void customizeAzureOpenAIModelName(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getFieldByName("TEXT_EMBEDDING_ADA002").ifPresent(f -> f.getVariable(0).setName("TEXT_EMBEDDING_ADA_002"));
            clazz.getFieldByName("TEXT_EMBEDDING3LARGE").ifPresent(f -> f.getVariable(0).setName("TEXT_EMBEDDING_3_LARGE"));
            clazz.getFieldByName("TEXT_EMBEDDING3SMALL").ifPresent(f -> f.getVariable(0).setName("TEXT_EMBEDDING_3_SMALL"));
        });
    }

    private static void customizeSplitSkillEncoderModelName(ClassCustomization classCustomization) {
        customizeAst(classCustomization, clazz -> {
            clazz.getFieldByName("R50KBASE").ifPresent(f -> f.getVariable(0).setName("R_50K_BASE"));
            clazz.getFieldByName("P50KBASE").ifPresent(f -> f.getVariable(0).setName("P_50K_BASE"));
            clazz.getFieldByName("P50KEDIT").ifPresent(f -> f.getVariable(0).setName("P_50K_EDIT"));
            clazz.getFieldByName("CL100KBASE").ifPresent(f -> f.getVariable(0).setName("CL_100K_BASE"));
        });
    }

    private static void bulkRemoveFromJsonMethods(ClassCustomization... classCustomizations) {
        for (ClassCustomization customization : classCustomizations) {
            customizeAst(customization, clazz -> clazz.getMethodsBySignature("fromJson", "JsonReader").forEach(Node::remove));
        }
    }

    /*
     * This helper function adds a varargs overload in addition to a List setter.
     */
    private static MethodDeclaration addVarArgsOverload(ClassOrInterfaceDeclaration clazz, String parameterName,
        String parameterType) {
        clazz.tryAddImportToParentCompilationUnit(Arrays.class);

        String methodName = "set" + parameterName.substring(0, 1).toUpperCase(Locale.ROOT) + parameterName.substring(1);

        MethodDeclaration nonVarArgOverload = clazz.getMethodsByName(methodName).get(0);

        return clazz.addMethod(methodName, com.github.javaparser.ast.Modifier.Keyword.PUBLIC)
            .setType(clazz.getNameAsString())
            .addParameter(new Parameter().setType(parameterType).setName(parameterName).setVarArgs(true))
            .setBody(StaticJavaParser.parseBlock(String.format(VARARG_METHOD_BLOCK_TEMPLATE, parameterName)))
            .setJavadocComment(nonVarArgOverload.getJavadoc().get());
    }

    private static Javadoc newJavadoc(String description) {
        return new Javadoc(JavadocDescription.parseText(description));
    }

    private static String joinWithNewline(String... lines) {
        return String.join("\n", lines);
    }
}
