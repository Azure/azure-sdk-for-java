// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.templating;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.annotation.processor.utils.CodeGenUtils;
import io.clientcore.annotation.processor.utils.RequestBodyHandler;
import io.clientcore.annotation.processor.utils.TypeConverter;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.CoreUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static io.clientcore.annotation.processor.utils.ResponseHandler.generateResponseHandling;

/**
 * This class generates the implementation of the service interface.
 */
public class JavaParserTemplateProcessor implements TemplateProcessor {

    private static final Map<String, String> LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT;

    static {
        LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT = new HashMap<>();
        for (Field field : HttpHeaderName.class.getDeclaredFields()) {
            // Only inspect public static final fields (aka, constants)
            if (!java.lang.reflect.Modifier.isPublic(field.getModifiers())
                || !java.lang.reflect.Modifier.isStatic(field.getModifiers())
                || !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            String constantName = field.getName();
            HttpHeaderName httpHeaderName;
            try {
                httpHeaderName = (HttpHeaderName) field.get(null);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.put(httpHeaderName.getCaseInsensitiveName(), constantName);
        }
    }

    /**
     * Initializes a new instance of the {@link JavaParserTemplateProcessor} class.
     */
    public JavaParserTemplateProcessor() {
    }

    private final CompilationUnit compilationUnit = new CompilationUnit();
    private final Map<String, String> httpHeaderNameConstantsToAdd = new TreeMap<>(String::compareToIgnoreCase);
    private ClassOrInterfaceDeclaration classBuilder;

    @Override
    public void process(TemplateInput templateInput, ProcessingEnvironment processingEnv) {
        String packageName = templateInput.getPackageName();
        // Remove the last part of the package name to avoid clash with class name
        packageName = packageName.substring(0, packageName.lastIndexOf('.'));
        String serviceInterfaceImplShortName = templateInput.getServiceInterfaceImplShortName();
        String serviceInterfaceShortName = templateInput.getServiceInterfaceShortName();

        addImports(templateInput);
        addCopyrightComments();
        setPackageDeclaration(packageName);
        createClass(serviceInterfaceImplShortName, serviceInterfaceShortName, templateInput, processingEnv);
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE, "Writing generated source file for: " + serviceInterfaceImplShortName);
        writeFile(packageName, serviceInterfaceImplShortName, processingEnv);
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE, "Completed code generation for: " + serviceInterfaceImplShortName);
    }

    void addImports(TemplateInput templateInput) {
        templateInput.getImports().keySet().forEach(compilationUnit::addImport);
    }

    void addCopyrightComments() {
        compilationUnit.addOrphanComment(new LineComment(" Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment(" Licensed under the MIT License."));
    }

    void setPackageDeclaration(String packageName) {
        compilationUnit.setPackageDeclaration(packageName);
    }

    void createClass(String serviceInterfaceImplShortName, String serviceInterfaceShortName,
        TemplateInput templateInput, ProcessingEnvironment processingEnv) {
        classBuilder = compilationUnit.addClass(serviceInterfaceImplShortName, Modifier.Keyword.PUBLIC);
        classBuilder.setJavadocComment("Initializes a new instance of the " + serviceInterfaceImplShortName + " type.");
        String serviceInterfacePackage = templateInput.getServiceInterfaceFQN()
            .substring(0, templateInput.getServiceInterfaceFQN().lastIndexOf('.'));

        compilationUnit.addImport(serviceInterfacePackage + "." + serviceInterfaceShortName);
        classBuilder.addImplementedType(serviceInterfaceShortName);

        addLoggerField(serviceInterfaceShortName);
        addHttpPipelineField();
        addSerializerFields();
        addConstructor();
        addGetNewInstanceMethod(serviceInterfaceImplShortName, serviceInterfaceShortName);

        for (HttpRequestContext method : templateInput.getHttpRequestContexts()) {
            if (!method.isConvenience()) {
                configureInternalMethod(classBuilder.addMethod(method.getMethodName(), Modifier.Keyword.PUBLIC), method,
                    processingEnv);
            }
        }

        List<FieldDeclaration> headerConstants = new ArrayList<>();
        for (Map.Entry<String, String> e : httpHeaderNameConstantsToAdd.entrySet()) {
            headerConstants.add(new FieldDeclaration()
                .setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL)
                .addVariable(new VariableDeclarator().setType("HttpHeaderName")
                    .setName(e.getKey())
                    .setInitializer("HttpHeaderName.fromString(\"" + e.getValue() + "\")")));
        }

        if (!headerConstants.isEmpty()) {
            classBuilder.getMembers().addAll(0, headerConstants);
        }
    }

    private void addLoggerField(String serviceInterfaceShortName) {
        configureLoggerField(classBuilder.addField("ClientLogger", "LOGGER", Modifier.Keyword.PRIVATE,
            Modifier.Keyword.STATIC, Modifier.Keyword.FINAL), serviceInterfaceShortName);
    }

    private void addHttpPipelineField() {
        classBuilder.addField(HttpPipeline.class, "httpPipeline", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
    }

    private void addSerializerFields() {
        compilationUnit.addImport(JsonSerializer.class);
        classBuilder.addField(JsonSerializer.class, "jsonSerializer", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
        compilationUnit.addImport(XmlSerializer.class);
        classBuilder.addField(XmlSerializer.class, "xmlSerializer", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
    }

    private void addConstructor() {
        classBuilder.addConstructor(Modifier.Keyword.PRIVATE)
            .addParameter(HttpPipeline.class, "httpPipeline")
            .setBody(StaticJavaParser.parseBlock(
                "{ this.httpPipeline = httpPipeline; this.jsonSerializer = JsonSerializer.getInstance(); this.xmlSerializer = XmlSerializer.getInstance(); }"));
    }

    private void addGetNewInstanceMethod(String serviceInterfaceImplShortName, String serviceInterfaceShortName) {
        classBuilder.addMethod("getNewInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType(serviceInterfaceShortName)
            .addParameter(HttpPipeline.class, "httpPipeline")
            .setBody(StaticJavaParser.parseBlock("{ return new " + serviceInterfaceImplShortName + "(httpPipeline); }"))
            .setJavadocComment("Creates an instance of " + serviceInterfaceShortName
                + " that is capable of sending requests to the service.\n"
                + "@param httpPipeline The HTTP pipeline to use for sending requests.\n" + "@return An instance of `"
                + serviceInterfaceShortName + "`;");
    }

    /**
     * Get the compilation unit
     *
     * @return the compilation unit
     */
    CompilationUnit getCompilationUnit() {
        return this.compilationUnit;
    }

    // Pattern for all field and method creation is to mutate the passed declaration.
    // This pattern allows for addition of imports to be done in these methods using
    // 'tryAddImportToParentCompilationUnit', as that method follows the chain of parent nodes to add the import to the
    // creating CompilationUnit (if it exists).
    //
    // So, instead of needing a bunch of 'CompilationUnit.addImport' calls before creating the class, fields, and
    // methods contained by source file, we can add the imports as we create the methods. This works as declarations
    // will look for a parent node, in this case a ClassOrInterfaceDeclaration, and then look for the parent node of
    // that (the CompilationUnit) to add the import to.

    void configureLoggerField(FieldDeclaration field, String serviceInterfaceShortName) {
        field.tryAddImportToParentCompilationUnit(ClientLogger.class);
        field.setModifiers(Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC, Modifier.Keyword.FINAL)
            .setVariables(new NodeList<>(new VariableDeclarator().setType("ClientLogger")
                .setName("LOGGER")
                .setInitializer("new ClientLogger(" + serviceInterfaceShortName + ".class)")));
    }

    // Helper methods
    private void configureInternalMethod(MethodDeclaration internalMethod, HttpRequestContext method,
        ProcessingEnvironment processingEnv) {
        // TODO (alzimmer): For now throw @SuppressWarnings("cast") on generated methods while we
        //  improve / fix the generated code to no longer need it.
        internalMethod.setName(method.getMethodName())
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"), new StringLiteralExpr("cast")))
            .addMarkerAnnotation(Override.class)
            .setType(TypeConverter.getAstType(method.getMethodReturnType()));

        method.getParameters().forEach(param -> {
            if (param.getTypeMirror().getKind() == TypeKind.DECLARED) {
                internalMethod
                    .addParameter(new Parameter(StaticJavaParser.parseType(param.getShortTypeName()), param.getName()));
            } else {
                internalMethod.addParameter(
                    new Parameter(StaticJavaParser.parseType(param.getTypeMirror().toString()), param.getName()));
            }
        });
        BlockStmt body = internalMethod.getBody().get();

        initializeHttpRequest(body, method);
        setContentType(body, method);
        boolean serializationFormatSet = RequestBodyHandler.configureRequestBody(body, method.getBody(), processingEnv);
        addRequestContextToRequestIfPresent(body, method);
        finalizeHttpRequest(body, method.getMethodReturnType(), method, serializationFormatSet);

        internalMethod.setBody(body);
    }

    private void setContentType(BlockStmt body, HttpRequestContext method) {
        final HttpRequestContext.Body requestBody = method.getBody();
        if (requestBody == null || requestBody.getParameterType() == null) {
            return;
        }

        boolean isContentTypeSetInHeaders
            = method.getParameters().stream().anyMatch(p -> "contentType".equals(p.getName()));

        // Header param to have precedence
        if (!isContentTypeSetInHeaders) {
            String contentType = requestBody.getContentType();
            RequestBodyHandler.setContentTypeHeader(body, contentType);
        }
    }

    private void writeFile(String packageName, String serviceInterfaceImplShortName,
        ProcessingEnvironment processingEnv) {
        try (Writer fileWriter = processingEnv.getFiler()
            .createSourceFile(packageName + "." + serviceInterfaceImplShortName)
            .openWriter()) {
            fileWriter.write(compilationUnit.toString());
            fileWriter.flush();
        } catch (IOException e) {
            processingEnv.getMessager()
                .printMessage(Diagnostic.Kind.ERROR, "Failed to write generated source file for "
                    + serviceInterfaceImplShortName + ": " + e.getMessage());
        }
    }

    private void addRequestContextToRequestIfPresent(BlockStmt body, HttpRequestContext method) {
        boolean hasRequestContext = method.getParameters()
            .stream()
            .anyMatch(parameter -> "requestContext".equals(parameter.getName())
                && "RequestContext".equals(parameter.getShortTypeName()));

        if (hasRequestContext) {
            // Create a statement for setting request options
            Statement statement1 = StaticJavaParser.parseStatement("httpRequest.setContext(requestContext);");

            Statement statement2
                = StaticJavaParser.parseStatement("httpRequest.getContext().getRequestCallback().accept(httpRequest);");

            body.addStatement(statement1);
            body.addStatement(statement2);
        }
    }

    void initializeHttpRequest(BlockStmt body, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(HttpRequest.class);
        body.tryAddImportToParentCompilationUnit(HttpMethod.class);

        createUri(body, method);
        appendQueryParams(body, method);

        Statement statement
            = StaticJavaParser.parseStatement("HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod."
                + method.getHttpMethod() + ").setUri(" + method.getUriParameterName() + ");");

        statement.setLineComment("\n Create the HTTP request");
        body.addStatement(statement);
        addHeadersToRequest(body, method);
    }

    /**
     * Creates the basic {@code String} URI.
     *
     * @param body Where the method is being generated.
     * @param method Reflective information about the method being generated.
     */
    void createUri(BlockStmt body, HttpRequestContext method) {
        String variableName = method.getUriParameterName();

        // In rare cases an interface could be created without a 'host' value in 'ServiceInterface'.
        // If that happens, concatenate all 'HostParam' values together as the base endpoint.
        String urlStatement;
        if (!method.isTemplateHasHost()) {
            String concatenatedHostParams = method.getParameters()
                .stream()
                .filter(param -> param.getVariableElement().getAnnotation(HostParam.class) != null)
                .map(HttpRequestContext.MethodParameter::getName)
                .collect(Collectors.joining(" + "));

            if (CoreUtils.isNullOrEmpty(concatenatedHostParams)) {
                urlStatement = method.getHost() + ";";
            } else {
                urlStatement = concatenatedHostParams + " + \"/\" + " + method.getHost() + ";";
            }
        } else {
            urlStatement = method.getHost() + ";";
        }

        body.addStatement(StaticJavaParser.parseStatement("String " + variableName + " = " + urlStatement));
    }

    private void appendQueryParams(BlockStmt body, HttpRequestContext method) {
        if (method.getQueryParams().isEmpty()) {
            return;
        }

        Statement queryParamMapDeclaration
            = StaticJavaParser.parseStatement("LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();");
        queryParamMapDeclaration.setLineComment("\n Append non-null query parameters");
        body.addStatement(queryParamMapDeclaration);
        body.tryAddImportToParentCompilationUnit(LinkedHashMap.class);

        method.getQueryParams().forEach((key, queryParameter) -> {
            if (CoreUtils.isNullOrEmpty(key)) {
                // Skip null or empty keys
                return;
            }

            List<String> values = queryParameter.getValues();
            boolean shouldEncode = queryParameter.shouldEncode();

            if (values.isEmpty()) {
                // If there are no values, put null
                body.addStatement("queryParamMap.put(\"" + key + "\", null);");
            } else if (values.size() == 1) {
                String valueExpr = values.get(0);
                boolean isParam = method.getParameters().stream().anyMatch(p -> p.getName().equals(valueExpr));
                String valueCode;
                String keyExpr = isParam ? "UriEscapers.QUERY_ESCAPER.escape(\"" + key + "\")" : "\"" + key + "\"";

                if (isParam && queryParameter.isMultiple()) {
                    body.tryAddImportToParentCompilationUnit(UriEscapers.class);
                    body.tryAddImportToParentCompilationUnit(Collectors.class);
                    // List<String> parameter
                    valueCode = "(" + valueExpr + " != null ? " + valueExpr
                        + ".stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()) : null)";

                    body.addStatement("queryParamMap.put(" + keyExpr + ", " + valueCode + ");");
                } else if (isParam) {
                    // Single parameter
                    Optional<HttpRequestContext.MethodParameter> paramOpt = method.getParameters()
                        .stream()
                        .filter(parameter -> parameter.getName().equals(valueExpr))
                        .findFirst();
                    boolean isValueTypeString
                        = paramOpt.isPresent() && "String".equals(paramOpt.get().getShortTypeName());
                    if (shouldEncode && isValueTypeString) {
                        body.tryAddImportToParentCompilationUnit(UriEscapers.class);
                        valueCode = "UriEscapers.QUERY_ESCAPER.escape(" + valueExpr + ")";
                        body.addStatement("queryParamMap.put(" + keyExpr + ", " + valueCode + ");");
                    } else {
                        // Non-string param: do NOT encode, just use as is
                        valueCode = valueExpr;
                        body.addStatement("queryParamMap.put(" + keyExpr + ", " + valueCode + ");");
                    }
                } else {
                    // Static/literal value: do NOT escape key or value
                    // If the value is an empty string, treat as "", if null, treat as null
                    if (valueExpr == null) {
                        body.addStatement("queryParamMap.put(\"" + key + "\", null);");
                    } else {
                        valueCode = "\"" + valueExpr.replace("\"", "\\\"") + "\"";
                        body.addStatement("queryParamMap.put(\"" + key + "\", " + valueCode + ");");
                    }
                }
            } else {
                // Multiple values: always add as List<String>
                String joinedValues = CodeGenUtils.toJavaArrayInitializer(values, true);
                boolean isParam = values.stream()
                    .allMatch(v -> method.getParameters().stream().anyMatch(p -> p.getName().equals(v)));
                String keyExpr = isParam ? "UriEscapers.QUERY_ESCAPER.escape(\"" + key + "\")" : "\"" + key + "\"";

                if (!joinedValues.trim().isEmpty()) {
                    body.tryAddImportToParentCompilationUnit(Arrays.class);
                    if (shouldEncode && isParam) {
                        body.tryAddImportToParentCompilationUnit(UriEscapers.class);
                        body.addStatement("queryParamMap.put(" + keyExpr + ", Arrays.asList(" + joinedValues + ")"
                            + ".stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()));");
                    } else {
                        // For static query params, do NOT escape key or value
                        body.addStatement("queryParamMap.put(" + keyExpr + ", Arrays.asList(" + joinedValues + "));");
                    }
                } else {
                    // if joinedValues is empty, put null
                    body.addStatement("queryParamMap.put(\"" + key + "\", null);");
                }
            }
        });

        // Append query parameters to the URL
        body.tryAddImportToParentCompilationUnit(CoreUtils.class);

        // CoreUtils.appendQueryParams never returns null, update the URI with query parameters.
        body.addStatement(method.getUriParameterName() + " = CoreUtils.appendQueryParams("
            + method.getUriParameterName() + ", queryParamMap);");
    }

    /**
     * Adds headers to the HttpRequest using the provided HttpRequestContext.
     * Handles both static and dynamic headers, and applies correct quoting logic for static values.
     * <p>
     * Quoting logic:
     * - If value starts and ends with ", use as-is.
     * - If starts with ", append trailing ".
     * - If ends with ", prepend leading ".
     * - Otherwise, wrap value in quotes.
     * <p>
     * For dynamic headers (parameter-based), values are not quoted.
     * For static headers (literal values), quoting is always applied.
     * <p>
     */
    private void addHeadersToRequest(BlockStmt body, HttpRequestContext method) {
        if (method.getHeaders().isEmpty()) {
            // No headers to add; exit early for clarity and efficiency.
            return;
        }

        for (Map.Entry<String, List<String>> header : method.getHeaders().entrySet()) {
            String headerKey = header.getKey();
            List<String> headerValues = header.getValue();

            // Start building the header addition for the HttpRequest.
            StringBuilder addHeader = new StringBuilder();

            if (headerValues.isEmpty()) {
                // If headerValues is empty, skip adding this header.
                continue;
            }

            body.tryAddImportToParentCompilationUnit(HttpHeaderName.class);
            body.tryAddImportToParentCompilationUnit(HttpHeader.class);

            String ifCheck = null;
            String valueExpression;
            // Handle multiple header values (e.g., for repeated headers).
            if (headerValues.size() > 1) {
                body.tryAddImportToParentCompilationUnit(Arrays.class);

                // For multiple values, always treat as static and apply quoting logic.
                valueExpression = "Arrays.asList(" + CodeGenUtils.toJavaArrayInitializer(headerValues, true) + ")";
            } else {
                String value = headerValues.get(0);
                // Determine if the header value is a String type (for dynamic headers).
                // This is used to avoid quoting parameter-based (dynamic) header values.
                Optional<HttpRequestContext.MethodParameter> paramOpt
                    = method.getParameters().stream().filter(p -> p.getName().equals(value)).findFirst();
                if (paramOpt.isPresent()) {
                    String paramType = paramOpt.get().getShortTypeName();
                    if ("String".equals(paramType)) {
                        // Dynamic header: use parameter name directly.
                        ifCheck = value + " != null";
                        valueExpression = value;
                    } else {
                        if (!paramOpt.get().getTypeMirror().getKind().isPrimitive()) {
                            ifCheck = value + " != null";
                        }
                        valueExpression = "String.valueOf(" + value + ")";
                    }
                } else {
                    // Static header: apply quoting logic.
                    valueExpression = CodeGenUtils.quoteHeaderValue(value);
                }
            }

            String constantName = LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.get(headerKey.toLowerCase(Locale.ROOT));
            if (constantName != null) {
                addHeader.append("httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.")
                    .append(constantName)
                    .append(", ")
                    .append(valueExpression)
                    .append("));");
            } else {
                String headerNameConstant = headerKey.replace('-', '_').toUpperCase(Locale.US);
                httpHeaderNameConstantsToAdd.put(headerNameConstant, headerKey);
                addHeader.append("httpRequest.getHeaders().add(new HttpHeader(")
                    .append(headerNameConstant)
                    .append(", ")
                    .append(valueExpression)
                    .append("));");
            }

            Statement addHeaderStatement = StaticJavaParser.parseStatement(addHeader.toString());
            if (ifCheck != null) {
                addHeaderStatement = new IfStmt().setCondition(StaticJavaParser.parseExpression(ifCheck))
                    .setThenStmt(new BlockStmt().addStatement(addHeaderStatement));
            }

            body.addStatement(addHeaderStatement);
        }
    }

    private void finalizeHttpRequest(BlockStmt body, TypeMirror returnTypeName, HttpRequestContext method,
        boolean serializationFormatSet) {
        body.tryAddImportToParentCompilationUnit(Response.class);
        generateResponseHandling(body, returnTypeName, method, serializationFormatSet);
    }
}
