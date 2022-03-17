// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.JavadocCustomization;
import com.azure.autorest.customization.LibraryCustomization;
import com.azure.autorest.customization.PackageCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.ThisExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExplicitConstructorInvocationStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Contains customizations for Azure Search's service swagger code generation.
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
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
        bulkAddClassModifier(publicCustomization, Modifier.Keyword.ABSTRACT, "ScoringFunction",
            "DataChangeDetectionPolicy", "DataDeletionDetectionPolicy", "CharFilter", "CognitiveServicesAccount",
            "LexicalAnalyzer", "SearchIndexerKnowledgeStoreProjectionSelector",
            "SearchIndexerKnowledgeStoreBlobProjectionSelector", "SearchIndexerDataIdentity");

        // Change class modifiers to 'public final'.
        bulkAddClassModifier(publicCustomization, Modifier.Keyword.FINAL,
            "BM25SimilarityAlgorithm", "ClassicSimilarityAlgorithm", "HighWaterMarkChangeDetectionPolicy",
            "SqlIntegratedChangeTrackingPolicy", "SoftDeleteColumnDeletionDetectionPolicy", "MappingCharFilter",
            "PatternReplaceCharFilter", "DefaultCognitiveServicesAccount", "ConditionalSkill",
            "KeyPhraseExtractionSkill", "LanguageDetectionSkill", "ShaperSkill", "MergeSkill",
            "SplitSkill", "TextTranslationSkill", "DocumentExtractionSkill", "WebApiSkill");

        bulkRemoveMethod(publicCustomization, "getOdataType", "BM25SimilarityAlgorithm", "ClassicSimilarityAlgorithm",
            "ConditionalSkill", "DefaultCognitiveServicesAccount", "DocumentExtractionSkill",
            "EntityLinkingSkill", "HighWaterMarkChangeDetectionPolicy",
            "KeyPhraseExtractionSkill", "LanguageDetectionSkill", "MappingCharFilter", "MergeSkill",
            "PatternReplaceCharFilter", "PiiDetectionSkill", "SearchIndexerDataNoneIdentity",
            "SearchIndexerDataUserAssignedIdentity", "ShaperSkill", "SoftDeleteColumnDeletionDetectionPolicy",
            "SplitSkill", "SqlIntegratedChangeTrackingPolicy", "TextTranslationSkill", "WebApiSkill");

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
        customizeEntityRecognitionSkill(publicCustomization.getClass("EntityRecognitionSkill"),
            implCustomization.getClass("EntityRecognitionSkillV3"));
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
        customizeSearchIndexerSkill(publicCustomization.getClass("SearchIndexerSkill"));
        customizeSentimentSkill(publicCustomization.getClass("SentimentSkill"),
            implCustomization.getClass("SentimentSkillV3"));

        addKnowledgeStoreProjectionFluentSetterOverrides(
            publicCustomization.getClass("SearchIndexerKnowledgeStoreBlobProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreFileProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreObjectProjectionSelector"),
            publicCustomization.getClass("SearchIndexerKnowledgeStoreTableProjectionSelector"));
    }

    private void customizeSearchFieldDataType(ClassCustomization classCustomization) {
        classCustomization.customizeAst(compilationUnit ->
            compilationUnit.getClassByName(classCustomization.getClassName()).get()
                .addMethod("collection", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
                .setType("SearchFieldDataType")
                .addParameter("SearchFieldDataType", "dataType")
                .addMarkerAnnotation("JsonCreator")
                .setBody(new BlockStmt(new NodeList<>(new ReturnStmt("fromString(String.format(\"Collection(%s)\", dataType.toString()))"))))
                .setJavadocComment(new Javadoc(new JavadocDescription(Collections.singletonList(() -> "Returns a collection of a specific SearchFieldDataType")))
                    .addBlockTag(JavadocBlockTag.createParamBlockTag("dataType", "the corresponding SearchFieldDataType"))
                    .addBlockTag("return", "a Collection of the corresponding SearchFieldDataType")));
    }

    private void customizeMagnitudeScoringParameters(ClassCustomization classCustomization) {
        classCustomization.getMethod("isShouldBoostBeyondRangeByConstant")
            .rename("shouldBoostBeyondRangeByConstant");
    }

    private void customizeSimilarityAlgorithm(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.ABSTRACT);
        classCustomization.removeAnnotation("@JsonTypeName");
        classCustomization.addAnnotation("@JsonTypeName(\"Similarity\")");
    }

    private void customizeCognitiveServicesAccountKey(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
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
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");

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
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "visualFeatures", "VisualFeature");
        addVarArgsOverload(classCustomization, "details", "ImageDetail");
    }

    private void customizeCustomEntityLookupSkill(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "inlineEntitiesDefinition", "CustomEntity");
    }

    private void customizeCustomNormalizer(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");
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
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "tokenFilters", "TokenFilterName");
        addVarArgsOverload(classCustomization, "charFilters", "CharFilterName");
    }

    private void customizePatternAnalyzer(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
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
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
        removeMethod(classCustomization, "getOdataType");
        addVarArgsOverload(classCustomization, "stopwords", "String");
    }

    private void customizeStopAnalyzer(ClassCustomization classCustomization) {
        addClassModifier(classCustomization, Modifier.Keyword.FINAL);
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

            // Both #Microsoft.Skills.Text.V3.SentimentSkill and #Microsoft.Skills.Text.V3.EntityRecognitionSkill
            // should use the non-V3 subtype as they were merged into a single class.
            for (Node jsonSubTypesType : jsonSubTypesTypes.getChildNodes()) {
                Optional<MemberValuePair> potentialNameNode = jsonSubTypesType.getChildNodes().stream()
                    .filter(childNode -> childNode instanceof MemberValuePair)
                    .map(childNode -> (MemberValuePair) childNode)
                    .filter(mvp -> "name".equals(mvp.getName().asString()))
                    .filter(mvp -> {
                        String mvpValue = mvp.getValue().asStringLiteralExpr().asString();
                        return "#Microsoft.Skills.Text.V3.SentimentSkill".equals(mvpValue)
                            || "#Microsoft.Skills.Text.V3.EntityRecognitionSkill".equals(mvpValue);
                    }).findFirst();

                if (potentialNameNode.isPresent()) {
                    MemberValuePair valueNode = jsonSubTypesType.getChildNodes().stream()
                        .filter(childNode -> childNode instanceof MemberValuePair)
                        .map(childNode -> (MemberValuePair) childNode)
                        .filter(mvp -> "value".equals(mvp.getName().asString()))
                        .findFirst()
                        .get();

                    MemberValuePair nameNode = potentialNameNode.get();
                    String subTypeName = nameNode.getValue().asStringLiteralExpr().asString();
                    ClassExpr valueClass = valueNode.getValue().asClassExpr();
                    if ("#Microsoft.Skills.Text.V3.SentimentSkill".equals(subTypeName)) {
                        valueClass.setType("SentimentSkill");
                    } else {
                        valueClass.setType("EntityRecognitionSkill");
                    }
                }
            }
        });
    }

    private void customizeEntityRecognitionSkill(ClassCustomization entityRecognitionSkill,
        ClassCustomization entityRecognitionSkillV3) {
        // Get the fields and methods that will be copied from the V3 skill into the V1 skill.
        String modelVersionString = "modelVersion";
        String getModelVersionString = "getModelVersion";
        String setModelVersionString = "setModelVersion";

        Map<String, BodyDeclaration<?>> nodesToCopy = new HashMap<>();
        entityRecognitionSkillV3.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(entityRecognitionSkillV3.getClassName())
                .get();
            nodesToCopy.put(modelVersionString, clazz.getFieldByName(modelVersionString).get().clone());
            nodesToCopy.put(getModelVersionString, clazz.getMethodsByName(getModelVersionString).get(0).clone());
            nodesToCopy.put(setModelVersionString, clazz.getMethodsByName(setModelVersionString).get(0).clone());
        });

        entityRecognitionSkill.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(entityRecognitionSkill.getClassName()).get()
                .addModifier(Modifier.Keyword.FINAL);
            clazz.getMethodsByName("setIncludeTypelessEntities").get(0).setName("setTypelessEntitiesIncluded");
            clazz.getMethodsByName("isIncludeTypelessEntities").get(0).setName("areTypelessEntitiesIncluded");

            addClientLogger(compilationUnit, clazz);
            changeOdataTypeToEnum(clazz, "EntityRecognitionSkillVersion");

            Expression v1Expression = new FieldAccessExpr(new NameExpr("EntityRecognitionSkillVersion"), "V1");
            Expression v3Expression = new FieldAccessExpr(new NameExpr("EntityRecognitionSkillVersion"), "V3");

            addVersionConstructorOverload(clazz, clazz.getConstructors().get(0), "EntityRecognitionSkillVersion",
                v1Expression);

            clazz.getMembers().add(clazz.getFields().size(), nodesToCopy.get(modelVersionString));

            clazz.addMember(nodesToCopy.get(getModelVersionString));
            modifyAndAddCopiedSetter(clazz, nodesToCopy.get(setModelVersionString).asMethodDeclaration(),
                "EntityRecognitionSkill", "modelVersion", "EntityRecognitionSkillVersion", "V1", v1Expression);

            MethodDeclaration setTypelessEntitiesIncluded = clazz.getMethodsByName("setTypelessEntitiesIncluded").get(0);

            Javadoc setTypelessEntitiesIncludedBodyJavadoc = setTypelessEntitiesIncluded.getJavadoc().get();
            setTypelessEntitiesIncludedBodyJavadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.THROWS,
                "IllegalArgumentException If {@code includeTypelessEntities} is supplied when {@link #getSkillVersion()} is {@link EntityRecognitionSkillVersion#V3}."));
            setTypelessEntitiesIncluded.setJavadocComment(setTypelessEntitiesIncludedBodyJavadoc);


            BlockStmt setTypelessEntitiesIncludedBody = setTypelessEntitiesIncluded.getBody().get();
            setTypelessEntitiesIncludedBody.getStatements().add(0, createAndIfThrowStatement("IllegalArgumentException",
                "EntityRecognitionSkill using V3 doesn't support 'includeTypelessEntities'.",
                new BinaryExpr(new NameExpr("includeTypelessEntities"), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS),
                new BinaryExpr(new NameExpr("version"), v3Expression, BinaryExpr.Operator.EQUALS)));
        });

        addVarArgsOverload(entityRecognitionSkill, "categories", "EntityCategory");
    }

    private void customizeSentimentSkill(ClassCustomization sentimentSkillCustomization,
        ClassCustomization sentimentSkillV3Customization) {
        // Get the fields and methods that will be copied from the V3 skill into the V1 skill.
        String includeOpinionMiningString = "includeOpinionMining";
        String modelVersionString = "modelVersion";
        String isIncludeOpinionMiningString = "isIncludeOpinionMining";
        String setIncludeOpinionMiningString = "setIncludeOpinionMining";
        String getModelVersionString = "getModelVersion";
        String setModelVersionString = "setModelVersion";

        Map<String, BodyDeclaration<?>> nodesToCopy = new HashMap<>();
        sentimentSkillV3Customization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(sentimentSkillV3Customization.getClassName())
                .get();
            nodesToCopy.put(includeOpinionMiningString, clazz.getFieldByName(includeOpinionMiningString).get().clone());
            nodesToCopy.put(modelVersionString, clazz.getFieldByName(modelVersionString).get().clone());
            nodesToCopy.put(isIncludeOpinionMiningString, clazz.getMethodsByName(isIncludeOpinionMiningString).get(0).clone());
            nodesToCopy.put(setIncludeOpinionMiningString, clazz.getMethodsByName(setIncludeOpinionMiningString).get(0).clone());
            nodesToCopy.put(getModelVersionString, clazz.getMethodsByName(getModelVersionString).get(0).clone());
            nodesToCopy.put(setModelVersionString, clazz.getMethodsByName(setModelVersionString).get(0).clone());
        });

        sentimentSkillCustomization.customizeAst(compilationUnit -> {
            ClassOrInterfaceDeclaration clazz = compilationUnit.getClassByName(sentimentSkillCustomization.getClassName()).get()
                .addModifier(Modifier.Keyword.FINAL);

            addClientLogger(compilationUnit, clazz);
            changeOdataTypeToEnum(clazz, "SentimentSkillVersion");

            Expression v1Expression = new FieldAccessExpr(new NameExpr("SentimentSkillVersion"), "V1");

            addVersionConstructorOverload(clazz, clazz.getConstructors().get(0), "SentimentSkillVersion", v1Expression);

            int whereToAddAdditionalFields = clazz.getFields().size();
            clazz.getMembers().add(whereToAddAdditionalFields++, nodesToCopy.get(includeOpinionMiningString));
            clazz.getMembers().add(whereToAddAdditionalFields, nodesToCopy.get(modelVersionString));

            clazz.addMember(nodesToCopy.get(isIncludeOpinionMiningString));
            modifyAndAddCopiedSetter(clazz, nodesToCopy.get(setIncludeOpinionMiningString).asMethodDeclaration(),
                "SentimentSkill", "includeOpinionMining", "SentimentSkillVersion", "V1", v1Expression);

            clazz.addMember(nodesToCopy.get(getModelVersionString));
            modifyAndAddCopiedSetter(clazz, nodesToCopy.get(setModelVersionString).asMethodDeclaration(),
                "SentimentSkill", "modelVersion", "SentimentSkillVersion", "V1", v1Expression);
        });
    }

    private static void bulkAddClassModifier(PackageCustomization packageCustomization, Modifier.Keyword modifier,
        String... classNames) {
        if (classNames == null) {
            return;
        }

        for (String className : classNames) {
            addClassModifier(packageCustomization.getClass(className), modifier);
        }
    }

    private static void addClassModifier(ClassCustomization classCustomization, Modifier.Keyword modifier) {
        classCustomization.customizeAst(compilationUnit -> compilationUnit
            .getClassByName(classCustomization.getClassName()).get().addModifier(modifier));
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

        classCustomization.addMethod(varargMethod).getJavadoc().replace(copyJavadocs);
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

    private static void addClientLogger(CompilationUnit compilationUnit, ClassOrInterfaceDeclaration clazz) {
        // Add ClientLogger and JsonIgnore imports, if they are duplicates they will be cleaned up later on.
        compilationUnit.addImport("com.azure.core.util.logging.ClientLogger")
            .addImport("com.fasterxml.jackson.annotation.JsonIgnore");

        FieldDeclaration clientLoggerDeclaration = new FieldDeclaration()
            .addVariable(new VariableDeclarator(StaticJavaParser.parseType("ClientLogger"), "LOGGER")
                .setInitializer("new ClientLogger(" + clazz.getName().asString() + ".class)"))
            .setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL)
            .addMarkerAnnotation("JsonIgnore");

        // Always make ClientLogger the first field declaration in the class.
        clazz.getMembers().add(0, clientLoggerDeclaration);
    }

    private static void changeOdataTypeToEnum(ClassOrInterfaceDeclaration clazz, String versionType) {
        clazz.getFieldByName("odataType").ifPresent(fieldDeclaration -> fieldDeclaration.getVariable(0)
            .setName("version")
            .setType(versionType)
            .removeInitializer());

        MethodDeclaration getVersion = clazz.getMethodsByName("getOdataType").get(0);
        getVersion.setType(versionType).setName("getSkillVersion").createBody()
            .addStatement(new ReturnStmt(new FieldAccessExpr(new ThisExpr(), "version")));
        getVersion.setJavadocComment(createJavadoc(String.format("Gets the version of the {@link %s}.",
                clazz.getName().asString()),
            new JavadocBlockTag(JavadocBlockTag.Type.RETURN, String.format("The version of the {@link %s}.",
                clazz.getName().asString()))));
    }

    private static void addVersionConstructorOverload(ClassOrInterfaceDeclaration clazz,
        ConstructorDeclaration creatorCtor, String versionType, Expression v1Constant) {
        int nextCtorPosition = clazz.getMembers().indexOf(creatorCtor) + 1;
        ConstructorDeclaration versionCtor = creatorCtor.clone();
        versionCtor.getAnnotationByName("JsonCreator").ifPresent(AnnotationExpr::remove);
        versionCtor.getParameters().forEach(param -> param.getAnnotationByName("JsonProperty")
            .ifPresent(AnnotationExpr::remove));
        versionCtor.addParameter(versionType, "version");
        versionCtor.setJavadocComment(versionCtor.getJavadoc().get().addBlockTag(
            JavadocBlockTag.createParamBlockTag("version",
                String.format("the %s value to set.", versionType))));
        versionCtor.getBody().addStatement(new AssignExpr(new FieldAccessExpr().setName("version"),
            new NameExpr("version"), AssignExpr.Operator.ASSIGN));
        clazz.getMembers().add(nextCtorPosition, versionCtor);

        ExplicitConstructorInvocationStmt thisCtorCall = new ExplicitConstructorInvocationStmt()
            .setThis(true)
            .setArguments(new NodeList<>(new NameExpr("inputs"), new NameExpr("outputs"), v1Constant));
        creatorCtor.getBody().setStatements(new NodeList<>(thisCtorCall));
    }

    private static void modifyAndAddCopiedSetter(ClassOrInterfaceDeclaration clazz, MethodDeclaration method,
        String returnType, String setterVariable, String versionType, String version, Expression versionExpression) {
        // Change the method's return type.
        method.setType(returnType);

        Javadoc javadoc = method.getJavadoc().get();
        int returnIndex = -1;
        for (int i = 0; i < javadoc.getBlockTags().size(); i++) {
            if (javadoc.getBlockTags().get(i).getType() == JavadocBlockTag.Type.RETURN) {
                returnIndex = i;
                break;
            }
        }

        // Update the Javadocs.
        javadoc.getBlockTags().set(returnIndex, new JavadocBlockTag(JavadocBlockTag.Type.RETURN,
            String.format("the %s object itself.", returnType)));
        javadoc.addBlockTag(new JavadocBlockTag(JavadocBlockTag.Type.THROWS, String.format(
            "IllegalArgumentException If {@code %s} is supplied when {@link #getSkillVersion()} is {@link %s#%s}.",
            setterVariable, versionType, version)));
        method.setJavadocComment(javadoc);


        BlockStmt body = method.getBody().get();
        body.getStatements().add(0, createAndIfThrowStatement("IllegalArgumentException",
            String.format("%s using %s doesn't support '%s'.", returnType, version, setterVariable),
            new BinaryExpr(new NameExpr(setterVariable), new NullLiteralExpr(), BinaryExpr.Operator.NOT_EQUALS),
            new BinaryExpr(new NameExpr("version"), versionExpression, BinaryExpr.Operator.EQUALS)));

        clazz.addMember(method);
    }

    /**
     * Creates an if-throw statement.
     *
     * <pre>
     * if (a != null && b == c) {
     *     throw LOGGER.logExceptionAsError(new Exception(exceptionMessage));
     * }
     * </pre>
     *
     * Is what this method creates.
     *
     * @param exceptionType Type of the exception.
     * @param exceptionMessage Message of the exception.
     * @param check1 First statement in the conditional.
     * @param check2 Second statement in the conditional.
     * @return A new throw statement.
     */
    private static IfStmt createAndIfThrowStatement(String exceptionType, String exceptionMessage,
        BinaryExpr check1, BinaryExpr check2) {
        // Create check1 && check2
        BinaryExpr conditional = new BinaryExpr(check1, check2, BinaryExpr.Operator.AND);

        // Create throw block in the if.
        BlockStmt throwBlock = new BlockStmt(new NodeList<>(new ThrowStmt(
            createLogAndThrowCall("LOGGER", "logExceptionAsError",
                createExceptionWithMessage(exceptionType, exceptionMessage)))));

        return new IfStmt(conditional, throwBlock, null);
    }

    private static MethodCallExpr createLogAndThrowCall(String loggerName, String logMethod,
        Expression exceptionExpression) {
        return new MethodCallExpr(new NameExpr(loggerName), logMethod, new NodeList<>(exceptionExpression));
    }

    private static ObjectCreationExpr createExceptionWithMessage(String exceptionType, String exceptionMessage) {
        return new ObjectCreationExpr().setType(exceptionType).setArguments(new NodeList<>(
            new StringLiteralExpr(exceptionMessage)));
    }

    private static Javadoc createJavadoc(String description, JavadocBlockTag... tags) {
        Javadoc javadoc = new Javadoc(JavadocDescription.parseText(description));
        for (JavadocBlockTag tag : tags) {
            javadoc.addBlockTag(tag);
        }

        return javadoc;
    }
}
