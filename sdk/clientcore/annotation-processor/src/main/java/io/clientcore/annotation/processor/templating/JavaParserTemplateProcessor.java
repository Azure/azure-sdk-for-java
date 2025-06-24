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
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
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
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.UriBuilder;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

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
        boolean serializationFormatSet = RequestBodyHandler.configureRequestBody(body, method, processingEnv);
        addRequestContextToRequestIfPresent(body, method);
        finalizeHttpRequest(body, method.getMethodReturnType(), method, serializationFormatSet);

        internalMethod.setBody(body);
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

        // Add a statement that initializes the HttpRequest and sets the HTTP method.
        // But don't add it to the body BlockStmt yet, let setHttpRequestUri do that as it may chain setting the URI
        // or require code between this instantiation and setting the URI.
        VariableDeclarator variableDeclarator = new VariableDeclarator().setType(HttpRequest.class)
            .setName("httpRequest")
            .setInitializer(new MethodCallExpr(new ObjectCreationExpr().setType(HttpRequest.class), "setMethod")
                .addArgument(new NameExpr("HttpMethod." + method.getHttpMethod())));
        Expression createHttpRequest = new VariableDeclarationExpr(variableDeclarator);
        createHttpRequest.setLineComment(" Create the HttpRequest.");

        // Then create the HttpRequest URI. This will handle calling 'setUri' on the HttpRequest and adding
        // createHttpRequest to the body BlockStmt.
        setHttpRequestUri(body, createHttpRequest, method);

        addHeadersToRequest(body, method);
    }

    /**
     * Sets the {@link URI} on the {@link HttpRequest} creation {@link Expression} passed to this method.
     *
     * @param body Where the method is being generated.
     * @param createHttpRequest The {@link Expression} that initializes the {@link HttpRequest} and sets the HTTP
     * method.
     * @param method Reflective information about the method being generated.
     */
    void setHttpRequestUri(BlockStmt body, Expression createHttpRequest, HttpRequestContext method) {
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
            if (method.isUriNextLink() || CoreUtils.isNullOrEmpty(concatenatedHostParams)) {
                urlStatement = method.getHost();
            } else {
                urlStatement = concatenatedHostParams + " + \"/\" + " + method.getHost();
            }
        } else {
            urlStatement = method.getHost();
        }
        // If the method doesn't have query parameters to set, inline the call to HttpRequest.setUri and return.
        if (method.getQueryParams().isEmpty()) {
            // The 'createHttpRequest' expression is the scope for the method call expression being added.
            // This will result in 'HttpRequest request = new HttpRequest().setMethod(method).setUri(urlStatement);'
            body.addStatement(
                new ExpressionStmt(new MethodCallExpr(createHttpRequest, "setUri").addArgument(urlStatement)));

            // Return now to reduce the indentation on the complex path, making the code easier to read.
            return;
        }

        // Create the UriBuilder that will be used to create the HttpRequest URI.
        body.tryAddImportToParentCompilationUnit(UriBuilder.class);
        Statement uriBuilderParse = StaticJavaParser
            .parseStatement("UriBuilder " + variableName + " = UriBuilder.parse(" + urlStatement + ");");
        uriBuilderParse.setLineComment(" Append the query parameters.");
        body.addStatement(uriBuilderParse);

        for (Map.Entry<String, HttpRequestContext.QueryParameter> kvp : method.getQueryParams().entrySet()) {
            String key = kvp.getKey();
            HttpRequestContext.QueryParameter queryParameter = kvp.getValue();

            if (CoreUtils.isNullOrEmpty(key)) {
                // Skip null or empty keys
                continue;
            }

            List<String> values = queryParameter.getValues();
            if (values.isEmpty()) {
                // Skip empty values.
                continue;
            }

            Expression valueExpression;
            if (values.size() == 1) {
                String value = values.get(0);
                if (queryParameter.isStatic()) {
                    // For static query parameters the value is a string constant, unless if doesn't have a value.
                    if (value == null) {
                        valueExpression = new NullLiteralExpr();
                    } else {
                        valueExpression = new StringLiteralExpr(value);
                    }
                } else {
                    // For non-static query parameters the value should be the name of the method parameter.
                    valueExpression = StaticJavaParser.parseExpression(value);
                }
            } else {
                body.tryAddImportToParentCompilationUnit(Arrays.class);
                valueExpression = StaticJavaParser
                    .parseExpression("Arrays.asList(" + CodeGenUtils.toJavaArrayInitializer(values, true) + ")");
            }

            body.tryAddImportToParentCompilationUnit(GeneratedCodeUtils.class);

            // This is the manual equivalent of:
            // GeneratedCodeUtils.addQueryParameter(variableName, key, !queryParameter.isStatic(), valueExpression,
            // queryParameter.shouldEncode());
            // Doing this manually avoids a call to StaticJavaParser which is much slower.
            MethodCallExpr addParameterCall
                = new MethodCallExpr(new NameExpr("GeneratedCodeUtils"), "addQueryParameter")
                    .addArgument(new NameExpr(variableName))
                    .addArgument(new StringLiteralExpr(key))
                    .addArgument(new BooleanLiteralExpr(!queryParameter.isStatic()))
                    .addArgument(valueExpression)
                    .addArgument(new BooleanLiteralExpr(queryParameter.shouldEncode()));
            body.addStatement(new ExpressionStmt(addParameterCall));
        }

        body.addStatement(new ExpressionStmt(
            new MethodCallExpr(createHttpRequest, "setUri").addArgument(variableName + ".toString()")));
    }

    /**
     * Adds headers to the HttpRequest using the provided HttpRequestContext. Handles both static and dynamic headers,
     * and applies correct quoting logic for static values.
     * <p>
     * Quoting logic: - If value starts and ends with ", use as-is. - If starts with ", append trailing ". - If ends
     * with ", prepend leading ". - Otherwise, wrap value in quotes.
     * <p>
     * For dynamic headers (parameter-based), values are not quoted. For static headers (literal values), quoting is
     * always applied.
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

            String constantName = LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.get(headerKey.toLowerCase(Locale.ROOT));
            if ("CONTENT_TYPE".equals(constantName)) {
                continue;
            }
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
                    } else if ("OffsetDateTime".equals(paramType)) {
                        // Special case for OffsetDateTime, format it to ISO_INSTANT.
                        body.tryAddImportToParentCompilationUnit(DateTimeFormatter.class);
                        ifCheck = value + " != null";
                        valueExpression = value + ".format(DateTimeFormatter.ISO_INSTANT)";
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
        generateResponseHandling(body, returnTypeName, method, serializationFormatSet);
    }
}
