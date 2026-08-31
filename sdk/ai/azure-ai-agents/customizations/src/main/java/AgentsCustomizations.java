import com.azure.autorest.customization.ClassCustomization;
import com.azure.autorest.customization.Customization;
import com.azure.autorest.customization.LibraryCustomization;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;

/**
 * This class contains the customization code to customize the AutoRest generated code for the Agents Client library
 * Reference: https://github.com/Azure/autorest.java/blob/main/customization-base/README.md
 */
public class AgentsCustomizations extends Customization {

    @Override
    public void customize(LibraryCustomization libraryCustomization, Logger logger) {
        renameImageGenToolSize(libraryCustomization, logger);
        modifyPollingStrategies(libraryCustomization, logger);
        makeRealtimeMessageDiscriminatorsFinal(libraryCustomization);
        applyUnionTypeWrappers(libraryCustomization, logger);
        annotateBetaClients(libraryCustomization, logger);
        annotateBetaFields(libraryCustomization, loadBetaAnnotations(logger), logger);
    }

    private static final String MODELS_PACKAGE = "com.azure.ai.agents.models";

    private static final String UNION_MARKER = "AI Tooling: union type";

    private static final String DEDUP_MARKER = "AI Tooling: openai-java de-dup";

    private static final String OPENAI_JSON_HELPER = "com.azure.ai.agents.implementation.OpenAIJsonHelper";

    // A union variant is described by a flat String[] rather than a helper type: the customization runner only loads
    // the AgentsCustomizations class file, so no nested, local or anonymous class may be introduced here.
    private static final int V_SETTER_TYPE = 0;

    private static final int V_GETTER_TYPE = 1;

    private static final int V_TO_OBJECT_ARG = 2;

    private static final int V_SUFFIX = 3;

    private static final int V_FACTORY = 4;

    private static final int V_PARAM_DOC = 5;

    private static final int V_RETURN_DOC = 6;

    private static final int V_TOKEN_GUARD = 7;

    private static final int V_EXTRA_IMPORT = 8;

    private static final int V_SETTER_NULLABLE = 9;

    private static final int V_OPENAI_IMPORT = 10;

    private static final int V_OPENAI_UNION_IMPORT = 11;

    private static final int V_OPENAI_UNION_TYPE = 12;

    private static final int V_OPENAI_METHOD_SUFFIX = 13;

    private static final int V_OPENAI_IS_METHOD = 14;

    private static final int V_OPENAI_AS_METHOD = 15;

    private static final int V_OPENAI_DOC = 16;

    private static final int V_SIZE = 17;

    /**
     * Re-applies the typed union accessors on the generated models whose TypeSpec union properties are emitted as
     * {@code BinaryData}. The generated {@code BinaryData} accessors are hidden from the public API surface (kept
     * package-private for {@code fromJson}/{@code toJson} and tests) and one typed setter/getter pair is added per
     * union variant.
     */
    private void applyUnionTypeWrappers(LibraryCustomization customization, Logger logger) {
        List<String[]> requireApproval = Arrays.asList(
            stringUnionVariant("the approval setting string to set (e.g., \"always\" or \"never\")"),
            modelUnionVariant("McpToolRequireApproval", "the {@link McpToolRequireApproval} filter to set"));
        List<String[]> toolChoice = Arrays.asList(
            stringUnionVariant(
                "the tool-selection mode to set, one of {@code \"none\"}, {@code \"auto\"} or {@code \"required\"}"),
            openAiUnionVariant("com.openai.models.responses.ToolChoiceFunction",
                "com.openai.models.responses.ResponseCreateParams", "ResponseCreateParams.ToolChoice",
                "FunctionToolChoice", "isFunction", "asFunction", "a specific function tool"),
            openAiUnionVariant("com.openai.models.responses.ToolChoiceMcp",
                "com.openai.models.responses.ResponseCreateParams", "ResponseCreateParams.ToolChoice",
                "McpToolChoice", "isMcp", "asMcp", "a specific MCP tool"));
        List<String[]> maxOutputTokens = Arrays.asList(
            numberUnionVariant("the maximum number of output tokens to set"),
            stringUnionVariant(
                "the token-limit string to set, for example {@code \"inf\"} for an unlimited count"));
        List<String[]> allowedTools
            = Arrays.asList(stringListUnionVariant("the list of tool name strings to set"),
                modelUnionVariant("McpToolFilter", "the {@link McpToolFilter} to set"));

        customizeUnionProperty(customization, "VoiceAgentDefinition", "maxOutputTokens",
            "The maximum output-token count for one response.", maxOutputTokens, true, false, logger);
        customizeUnionProperty(customization, "VoiceAgentDefinition", "toolChoice",
            "How the model chooses tools for generated responses.", toolChoice, true, false, logger);
        customizeUnionProperty(customization, "VoiceAgentLlmGeneratedGreetingConfig", "toolChoice",
            "The tool-selection policy for the opening response. Defaults to `none`.", toolChoice, true, false,
            logger);
        customizeUnionProperty(customization, "VoiceAgentMcpTool", "allowedTools", "The allowed_tools property.",
            allowedTools, true, false, logger);
        customizeUnionProperty(customization, "VoiceAgentMcpTool", "requireApproval",
            "The require_approval property.", requireApproval, true, false, logger);
        customizeUnionProperty(customization, "WebIqPreviewTool", "requireApproval",
            "Whether the agent requires approval before executing actions. When omitted, the service defaults to"
                + " \"always\".",
            requireApproval, true, false, logger);
        customizeUnionProperty(customization, "WebIqPreviewToolboxTool", "requireApproval",
            "Whether the agent requires approval before executing actions. When omitted, the service defaults to"
                + " \"always\".",
            requireApproval, true, false, logger);

        // VoiceResponseBase is an immutable output model: no public setters, and the typed getters read through the
        // (overridable) BinaryData accessor so that VoiceResponse, which shadows the field, is handled by the same
        // inherited public API.
        customizeUnionProperty(customization, "VoiceResponseBase", "maxOutputTokens",
            "Maximum number of output tokens for a single assistant response, inclusive of tool calls, that was used"
                + " in this response.",
            maxOutputTokens, false, true, logger);
        // VoiceResponse only re-declares the shadowed accessor; the typed getters are inherited from
        // VoiceResponseBase, so no public API is duplicated here.
        customizeUnionProperty(customization, "VoiceResponse", "maxOutputTokens",
            "Maximum number of output tokens for a single assistant response, inclusive of tool calls, that was used"
                + " in this response.",
            new ArrayList<>(), false, false, logger);
    }

    private void customizeUnionProperty(LibraryCustomization customization, String className, String property,
                                        String description, List<String[]> variants, boolean addSetters,
                                        boolean readThroughAccessor, Logger logger) {
        ClassCustomization classCustomization;
        try {
            classCustomization = customization.getClass(MODELS_PACKAGE, className);
        } catch (IllegalArgumentException ex) {
            logger.warn("{}.{} does not exist; skipping union type wrappers.", className, property);
            return;
        }

        String capitalized = Character.toUpperCase(property.charAt(0)) + property.substring(1);
        String getterName = "get" + capitalized;
        String setterName = "set" + capitalized;

        classCustomization.customizeAst(ast -> ast.getClassByName(className).ifPresent(clazz -> {
            boolean hidAccessor = false;
            for (MethodDeclaration method : clazz.getMethodsByName(getterName)) {
                if (method.getParameters().isEmpty() && isBinaryData(method.getType().asString())) {
                    hideAccessor(method);
                    hidAccessor = true;
                }
            }
            for (MethodDeclaration method : clazz.getMethodsByName(setterName)) {
                if (addSetters && method.getParameters().size() == 1
                    && isBinaryData(method.getParameter(0).getType().asString())) {
                    hideAccessor(method);
                }
            }

            if (!hidAccessor) {
                logger.error("Could not find a BinaryData {}() accessor on {}", getterName, className);
                throw new IllegalStateException(
                    "Could not find a BinaryData " + getterName + "() accessor on " + className + ".");
            }

            for (String[] variant : variants) {
                if (variant[V_OPENAI_UNION_TYPE] != null) {
                    addOpenAIVariant(clazz, className, property, capitalized, description, variant, addSetters);
                    continue;
                }
                if (addSetters) {
                    // Reference variants must assign null directly: BinaryData.fromObject(null) yields a non-null
                    // wrapper whose toString() throws and which serializes an explicit JSON null.
                    String assignment = "true".equals(variant[V_SETTER_NULLABLE])
                        ? property + " == null ? null : BinaryData." + variant[V_FACTORY] + "(" + property + ")"
                        : "BinaryData." + variant[V_FACTORY] + "(" + property + ")";
                    addMethod(clazz,
                        "Set the " + property + " property: " + description + "\n\n@param " + property + " "
                            + variant[V_PARAM_DOC] + ".\n@return the " + className + " object itself.",
                        "public " + className + " " + setterName + "(" + variant[V_SETTER_TYPE] + " " + property
                            + ") {\n" + "    // " + UNION_MARKER + "\n" + "    this." + property + " = " + assignment
                            + ";\n" + "    return this;\n" + "}");
                }

                String body = readThroughAccessor
                    ? "    BinaryData value = " + getterName + "();\n" + "    if (value == null) {\n"
                        + "        return null;\n" + "    }\n" + "    String json = value.toString().trim();\n"
                        + "    if (!(" + variant[V_TOKEN_GUARD] + ")) {\n" + "        return null;\n" + "    }\n"
                        + "    return value.toObject(" + variant[V_TO_OBJECT_ARG] + ");\n"
                    : "    if (this." + property + " == null) {\n" + "        return null;\n" + "    }\n"
                        + "    String json = this." + property + ".toString().trim();\n" + "    if (!("
                        + variant[V_TOKEN_GUARD] + ")) {\n" + "        return null;\n" + "    }\n"
                        + "    return this." + property + ".toObject(" + variant[V_TO_OBJECT_ARG] + ");\n";
                if (variant[V_EXTRA_IMPORT] != null) {
                    ast.addImport(variant[V_EXTRA_IMPORT]);
                }
                addMethod(clazz,
                    "Get the " + property + " property: " + description + "\n\n@return the " + property + " value as "
                        + variant[V_RETURN_DOC] + ".",
                    "public " + (readThroughAccessor ? "final " : "") + variant[V_GETTER_TYPE] + " " + getterName
                        + "As" + variant[V_SUFFIX] + "() {\n" + "    // " + UNION_MARKER + "\n" + body + "}");
            }

            String[] openAiVariant = null;
            for (String[] variant : variants) {
                if (variant[V_OPENAI_UNION_TYPE] != null) {
                    openAiVariant = variant;
                }
            }
            if (openAiVariant != null) {
                ast.addImport(openAiVariant[V_OPENAI_UNION_IMPORT]);
                addMethod(clazz, null,
                    "private " + openAiVariant[V_OPENAI_UNION_TYPE] + " getOpenAI" + capitalized + "() {\n" + "    // "
                        + DEDUP_MARKER + "\n" + "    if (this." + property + " == null) {\n"
                        + "        return null;\n" + "    }\n" + "    String json = this." + property
                        + ".toString().trim();\n" + "    if (!json.startsWith(\"{\")) {\n" + "        return null;\n"
                        + "    }\n" + "    return " + OPENAI_JSON_HELPER + ".fromBinaryData(this." + property + ", "
                        + openAiVariant[V_OPENAI_UNION_TYPE] + ".class);\n" + "}");
            }
        }));
    }

    /**
     * Adds the distinctly named openai-java accessors for one union variant that is represented by an openai-java
     * model (the Azure equivalents are suppressed through {@code @@alternateType} in TypeSpec). Overloads are
     * deliberately avoided because {@code null} arguments would be ambiguous between variants.
     */
    private static void addOpenAIVariant(ClassOrInterfaceDeclaration clazz, String className, String property,
                                         String capitalized, String description, String[] variant,
                                         boolean addSetters) {
        clazz.findCompilationUnit().ifPresent(unit -> unit.addImport(variant[V_OPENAI_IMPORT]));
        if (addSetters) {
            addMethod(clazz,
                "Set the " + property + " property to " + variant[V_OPENAI_DOC] + ": " + description + "\n\n@param "
                    + property + " " + variant[V_PARAM_DOC] + ".\n@return the " + className + " object itself.",
                "public " + className + " set" + variant[V_OPENAI_METHOD_SUFFIX] + "(" + variant[V_SETTER_TYPE] + " "
                    + property + ") {\n" + "    // " + DEDUP_MARKER + "\n" + "    this." + property + " = "
                    + OPENAI_JSON_HELPER + ".toBinaryData(" + property + ");\n" + "    return this;\n" + "}");
        }
        addMethod(clazz,
            "Get the " + property + " property as an openai-java {@link " + variant[V_GETTER_TYPE] + "}: "
                + description + "\n\n@return the " + property + " value as " + variant[V_RETURN_DOC]
                + ", or {@code null} if it is not set or holds another variant.",
            "public " + variant[V_GETTER_TYPE] + " get" + variant[V_OPENAI_METHOD_SUFFIX] + "() {\n" + "    // "
                + DEDUP_MARKER + "\n" + "    " + variant[V_OPENAI_UNION_TYPE] + " choice = getOpenAI" + capitalized
                + "();\n" + "    if (choice == null || !choice." + variant[V_OPENAI_IS_METHOD] + "()) {\n"
                + "        return null;\n" + "    }\n" + "    return choice." + variant[V_OPENAI_AS_METHOD]
                + "();\n" + "}");
    }

    private static boolean isBinaryData(String type) {
        return "BinaryData".equals(type) || "com.azure.core.util.BinaryData".equals(type);
    }

    /**
     * Removes {@code @Generated}, drops the {@code public} modifier and stamps the union marker comment on a
     * generated {@code BinaryData} accessor so that it is no longer part of the published API surface.
     */
    private static void hideAccessor(MethodDeclaration method) {
        method.getAnnotationByName("Generated").ifPresent(AnnotationExpr::remove);
        method.setModifiers(new NodeList<>());
        method.getBody()
            .filter(body -> !body.getStatements().isEmpty())
            .ifPresent(body -> body.getStatement(0).setLineComment(" " + UNION_MARKER));
    }

    private static void addMethod(ClassOrInterfaceDeclaration clazz, String javadoc, String methodSource) {
        MethodDeclaration method = StaticJavaParser.parseMethodDeclaration(methodSource);
        if (javadoc != null) {
            method.setJavadocComment(javadoc);
        }
        clazz.addMember(method);
    }

    /**
     * Describes one variant of a TypeSpec union: how it is written to and read back from the backing
     * {@code BinaryData} field. The slots are addressed through the {@code V_*} constants.
     *
     * @param setterType the parameter type of the generated setter.
     * @param getterType the return type of the generated getter.
     * @param toObjectArg the argument passed to {@code BinaryData.toObject}.
     * @param suffix the {@code get<Property>As<Suffix>} accessor suffix.
     * @param factory the {@code BinaryData} factory method used by the setter.
     * @param paramDoc the setter {@code @param} description.
     * @param returnDoc the getter {@code @return} description.
     * @param tokenGuard a boolean expression over the local {@code json} variable that is {@code true} only when the
     * stored JSON token belongs to this variant, so cross-variant reads return {@code null} instead of throwing.
     * @param extraImport an additional import the getter needs, or {@code null}.
     * @param setterNullable whether the setter parameter is a reference type, in which case a {@code null} argument
     * clears the property instead of being wrapped by {@code BinaryData.fromObject}.
     * @return the union variant.
     */
    private static String[] unionVariant(String setterType, String getterType, String toObjectArg, String suffix,
                                         String factory, String paramDoc, String returnDoc, String tokenGuard,
                                         String extraImport, boolean setterNullable) {
        String[] variant = new String[V_SIZE];
        variant[V_SETTER_TYPE] = setterType;
        variant[V_GETTER_TYPE] = getterType;
        variant[V_TO_OBJECT_ARG] = toObjectArg;
        variant[V_SUFFIX] = suffix;
        variant[V_FACTORY] = factory;
        variant[V_PARAM_DOC] = paramDoc;
        variant[V_RETURN_DOC] = returnDoc;
        variant[V_TOKEN_GUARD] = tokenGuard;
        variant[V_EXTRA_IMPORT] = extraImport;
        variant[V_SETTER_NULLABLE] = String.valueOf(setterNullable);
        return variant;
    }

    private static String[] stringUnionVariant(String paramDoc) {
        // BinaryData.fromObject is used for strings as well: fromString stores raw text, which the matching
        // toObject(String.class) read back cannot parse as JSON. Both factories write the same quoted JSON token.
        return unionVariant("String", "String", "String.class", "String", "fromObject", paramDoc,
            "a String, or {@code null} when it is not set or holds another variant", "json.startsWith(\"\\\"\")",
            null, true);
    }

    private static String[] numberUnionVariant(String paramDoc) {
        return unionVariant("int", "Integer", "Integer.class", "Integer", "fromObject", paramDoc,
            "an Integer, or {@code null} when it is not set or holds another variant",
            "!json.isEmpty() && (Character.isDigit(json.charAt(0)) || json.charAt(0) == '-')", null, false);
    }

    private static String[] stringListUnionVariant(String paramDoc) {
        return unionVariant("List<String>", "List<String>", "new TypeReference<List<String>>() { }", "StringList",
            "fromObject", paramDoc, "a list of Strings, or {@code null} when it is not set or holds another variant",
            "json.startsWith(\"[\")", "com.azure.core.util.serializer.TypeReference", true);
    }

    private static String[] modelUnionVariant(String type, String paramDoc) {
        return unionVariant(type, type, type + ".class", type, "fromObject", paramDoc,
            "a {@link " + type + "}, or {@code null} when it is not set or holds another variant",
            "json.startsWith(\"{\")", null, true);
    }

    /**
     * Declares a union variant that is represented by an openai-java model rather than a generated Azure model.
     * Values are bridged through {@code OpenAIJsonHelper} and read back by discriminating on the openai-java
     * union wrapper (for example {@code ResponseCreateParams.ToolChoice}).
     *
     * @param openAiImport the fully qualified openai-java variant class.
     * @param unionImport the fully qualified openai-java union class used to discriminate the stored value.
     * @param unionType the (possibly nested) name the union class is referenced by in source.
     * @param methodSuffix the distinct accessor suffix, e.g. {@code FunctionToolChoice}.
     * @param isMethod the union predicate method, e.g. {@code isFunction}.
     * @param asMethod the union accessor method, e.g. {@code asFunction}.
     * @param doc a short description of the variant used in the generated javadoc.
     * @return the union variant.
     */
    private static String[] openAiUnionVariant(String openAiImport, String unionImport, String unionType,
                                               String methodSuffix, String isMethod, String asMethod, String doc) {
        String simpleName = openAiImport.substring(openAiImport.lastIndexOf('.') + 1);
        String[] variant = unionVariant(simpleName, simpleName, simpleName + ".class", simpleName, "fromObject",
            "the openai-java {@link " + simpleName + "} to set, or null to clear", "a " + simpleName,
            "json.startsWith(\"{\")", null, true);
        variant[V_OPENAI_IMPORT] = openAiImport;
        variant[V_OPENAI_UNION_IMPORT] = unionImport;
        variant[V_OPENAI_UNION_TYPE] = unionType;
        variant[V_OPENAI_METHOD_SUFFIX] = methodSuffix;
        variant[V_OPENAI_IS_METHOD] = isMethod;
        variant[V_OPENAI_AS_METHOD] = asMethod;
        variant[V_OPENAI_DOC] = doc;
        return variant;
    }

    private void makeRealtimeMessageDiscriminatorsFinal(LibraryCustomization customization) {
        for (String className : new String[] {
            "RealtimeConversationItemMessage",
            "RealtimeConversationItemMessageSystem",
            "RealtimeConversationItemMessageUser",
            "RealtimeConversationItemMessageAssistant" }) {
            customization.getClass("com.azure.ai.agents.models", className)
                .customizeAst(ast -> ast.getClassByName(className)
                    .flatMap(clazz -> clazz.getFieldByName("type"))
                    .ifPresent(field -> field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL)));
        }

        for (String className : new String[] {
            "RealtimeConversationItemMessageSystem",
            "RealtimeConversationItemMessageUser",
            "RealtimeConversationItemMessageAssistant" }) {
            customization.getClass("com.azure.ai.agents.models", className).customizeAst(ast -> ast
                .getClassByName(className)
                .ifPresent(clazz -> {
                    clazz.getFieldByName("role")
                        .ifPresent(field -> field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL));
                    clazz.getMethodsByName("fromJson")
                        .forEach(method -> method.findAll(AssignExpr.class).stream()
                            .filter(assignment -> assignment.getTarget().toString().endsWith(".role"))
                            .forEach(assignment -> assignment.findAncestor(ExpressionStmt.class)
                                .ifPresent(ExpressionStmt::remove)));
                }));
        }
    }

    private void renameImageGenToolSize(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1024"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_ZERO_TWO_FOURX_ONE_FIVE_THREE_SIX".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1024_X_1536"))));

        customization.getClass("com.azure.ai.agents.models", "ImageGenToolSize").customizeAst(ast -> ast.getEnumByName("ImageGenToolSize")
            .ifPresent(clazz -> clazz.getEntries().stream()
                .filter(entry -> "ONE_FIVE_THREE_SIXX_ONE_ZERO_TWO_FOUR".equals(entry.getName().getIdentifier()))
                .forEach(entry -> entry.setName("RESOLUTION_1536_X_1024"))));
    }

    private void modifyPollingStrategies(LibraryCustomization customization, Logger logger) {
        customization.getClass("com.azure.ai.agents.implementation", "OperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("OperationLocationPollingStrategy")
                .ifPresent(clazz -> clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public Mono<PollResponse<T>> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return super.poll(pollingContext, pollResponseType).map(AgentsServicePollUtils::remapStatus); }"))));

        customization.getClass("com.azure.ai.agents.implementation", "SyncOperationLocationPollingStrategy")
            .customizeAst(ast -> ast.getClassByName("SyncOperationLocationPollingStrategy")
                .ifPresent(clazz -> clazz.addMember(StaticJavaParser.parseMethodDeclaration("@Override public PollResponse<T> poll(PollingContext<T> pollingContext, TypeReference<T> pollResponseType) { return AgentsServicePollUtils.remapStatus(super.poll(pollingContext, pollResponseType)); }"))));
    }

    private void annotateBetaClients(LibraryCustomization customization, Logger logger) {
        customization.getPackage("com.azure.ai.agents")
            .listClasses()
            .stream()
            .filter(cc -> cc.getClassName().startsWith("Beta") && cc.getClassName().endsWith("Client"))
            .forEach(classCustomization -> {
                String simpleName = classCustomization.getClassName();
                logger.info("Annotating {} with @Beta", simpleName);
                classCustomization.customizeAst(ast -> ast.getClassByName(simpleName).ifPresent(clazz -> {
                    ast.addImport("com.azure.ai.agents.implementation.utils.Beta");
                    clazz.addAnnotation(betaAnnotation("This class is in preview and may change in future releases."));
                }));
            });
    }

    private void annotateBetaFields(LibraryCustomization customization, List<String[]> betaAnnotations,
                                    Logger logger) {
        for (String[] entry : betaAnnotations) {
            String className = entry[0];
            String member = entry[1];
            String description = entry[2];
            int lastDot = className.lastIndexOf('.');
            String packageName = className.substring(0, lastDot);
            String simpleName = className.substring(lastDot + 1);

            logger.info("Annotating {}{} with @Beta", className, member == null ? "" : "#" + member);

            ClassCustomization classCustomization = null;
            try {
                classCustomization = customization.getClass(packageName, simpleName);
            } catch (IllegalArgumentException ex) {
                logger.info(packageName + simpleName + " does not exit.");
                continue;
            }

            classCustomization.customizeAst(ast -> ast.getTypes().stream()
                .filter(type -> type.getNameAsString().equals(simpleName))
                .findFirst()
                .ifPresent(type -> {
                    ast.addImport("com.azure.ai.agents.implementation.utils.Beta");
                    if (member == null) {
                        type.addAnnotation(betaAnnotation(description));
                    } else {
                        annotateMember(type, member, description, logger);
                    }
                }));
        }
    }

    private void annotateMember(TypeDeclaration<?> type, String member, String description, Logger logger) {
        String fieldName = toCamelCase(member);
        boolean found = false;

        for (FieldDeclaration field : type.getFields()) {
            if (field.getVariables().stream().anyMatch(v -> v.getNameAsString().equals(fieldName))) {
                field.addAnnotation(betaAnnotation(description));
                found = true;
            }
        }

        if (!found) {
            logger.error("Could not find field '{}' on type {}", fieldName, type.getNameAsString());
            throw new IllegalStateException(
                "Could not find field '" + fieldName + "' on type " + type.getNameAsString() + ".");
        }

        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        for (String accessor : new String[] { "get" + capitalized, "is" + capitalized, "set" + capitalized }) {
            for (MethodDeclaration method : type.getMethodsByName(accessor)) {
                method.addAnnotation(betaAnnotation(description));
            }
        }
    }

    private static AnnotationExpr betaAnnotation(String description) {
        StringLiteralExpr warningText = new StringLiteralExpr();
        warningText.setString(description);
        NormalAnnotationExpr annotation = new NormalAnnotationExpr();
        annotation.setName("Beta");
        annotation.addPair("warningText", warningText);
        return annotation;
    }

    private static String toCamelCase(String name) {
        if (name.indexOf('_') < 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name.length());
        boolean upperNext = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : c);
                upperNext = false;
            }
        }
        return sb.toString();
    }

    private static final String CSV_FILE_NAME = "beta-annotations.csv";

    /**
     * Loads the {@code @Beta} annotation entries from {@code beta-annotations.csv}. This file is the single source of
     * truth and is produced/updated by external tooling.
     * <p>
     * Format: a header row followed by {@code ;}-separated entries of
     * {@code type;class_name;annotation_description;member_name}. {@code type} is {@code class} (no member) or
     * {@code field} (member required). Blank lines and lines starting with {@code #} are ignored.
     */
    private List<String[]> loadBetaAnnotations(Logger logger) {
        Path csvPath = locateBetaCsv(logger);
        if (csvPath == null) {
            return new ArrayList<>();
        }
        logger.info("Loading @Beta annotations from {}", csvPath);

        List<String> lines;
        try {
            lines = Files.readAllLines(csvPath, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            logger.error("Failed to read @Beta annotations from {}", csvPath, ex);
            throw new UncheckedIOException("Failed to read @Beta annotations from " + csvPath, ex);
        }

        List<String[]> annotations = new ArrayList<>();
        int lineNumber = 0;
        for (String line : lines) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.charAt(0) == '#') {
                continue;
            }

            // Skip the header row (type;class_name;annotation_description;member_name).
            if (lineNumber == 1) {
                continue;
            }

            String[] columns = line.split(";", 4);
            if (columns.length < 3) {
                logger.error("Line {} of {} must have ';'-separated columns"
                    + " (type;class_name;annotation_description;member_name): {}", lineNumber, CSV_FILE_NAME, line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " must have ';'-separated columns (type;class_name;annotation_description;member_name): "
                    + line);
            }

            String type = columns[0].trim();
            String className = columns[1].trim();
            String description = columns[2];
            String member = columns.length >= 4 ? columns[3].trim() : "";
            if (className.isEmpty() || description.isEmpty()) {
                logger.error("Line {} of {} requires a class_name and an annotation_description: {}", lineNumber,
                    CSV_FILE_NAME, line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " requires a class_name and an annotation_description: " + line);
            }
            if ("field".equals(type) && member.isEmpty()) {
                logger.error("Line {} of {} is a field entry but has no member_name: {}", lineNumber, CSV_FILE_NAME,
                    line);
                throw new IllegalStateException("Line " + lineNumber + " of " + CSV_FILE_NAME
                    + " is a field entry but has no member_name: " + line);
            }

            annotations.add(new String[] { className, member.isEmpty() ? null : member, description });
        }

        logger.info("Loaded {} @Beta annotation entries", annotations.size());
        return annotations;
    }

    /**
     * Resolves the {@code beta-annotations.csv} path. {@code tsp-client update} launches the customization with its
     * working directory set to the library module (so the file lives at {@code <module>/customizations/...}), while
     * spec SDK-generation launches from the repo root; both locations are checked. Returns {@code null} (rather than
     * failing) when the file cannot be found, in which case {@code @Beta} annotations are skipped.
     */
    private Path locateBetaCsv(Logger logger) {
        // tsp-client update launches the customization from the module folder (user.dir = module).
        Path modulePath = Paths.get(System.getProperty("user.dir"), "customizations", CSV_FILE_NAME).toAbsolutePath();
        if (Files.isRegularFile(modulePath)) {
            return modulePath;
        }
        // spec SDK-generation (spec-gen-sdk) launches from the repo root (user.dir = repo root).
        Path repoRootPath
            = Paths.get(System.getProperty("user.dir"), "sdk", "ai", "azure-ai-agents", "customizations", CSV_FILE_NAME)
                .toAbsolutePath();
        if (Files.isRegularFile(repoRootPath)) {
            return repoRootPath;
        }
        logger.warn("Could not locate {} at {} or {} (user.dir={}); skipping @Beta annotations.", CSV_FILE_NAME,
            modulePath, repoRootPath, System.getProperty("user.dir"));
        return null;
    }
}
