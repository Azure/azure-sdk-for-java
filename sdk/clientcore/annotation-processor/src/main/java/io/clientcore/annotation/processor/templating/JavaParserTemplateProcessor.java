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
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.annotation.processor.utils.RequestBodyHandler;
import io.clientcore.annotation.processor.utils.TypeConverter;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

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

        writeFile(packageName, serviceInterfaceImplShortName, processingEnv);
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

        addDeserializeHelperMethod(
            classBuilder.addMethod("decodeNetworkResponse", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC));
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

    private void addDeserializeHelperMethod(MethodDeclaration deserializeHelperMethod) {
        deserializeHelperMethod.setType("Object")
            .addParameter("BinaryData", "data")
            .addParameter(ObjectSerializer.class, "serializer")
            .addParameter("ParameterizedType", "returnType");
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(IOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(UncheckedIOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(ParameterizedType.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(Type.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(List.class);
        deserializeHelperMethod
            .setJavadocComment("Decodes the body of an {@link Response} into the type returned by the called API.\n"
                + "@param data The BinaryData to decode.\n" + "@param serializer The serializer to use.\n"
                + "@param returnType The type of the ParameterizedType return value.\n" + "@return The decoded value.\n"
                + "@throws IOException If the deserialization fails.");

        deserializeHelperMethod.setBody(new BlockStmt()
            .addStatement(StaticJavaParser.parseStatement("if (data == null) { return null; }"))
            .addStatement(StaticJavaParser
                .parseStatement("try { if (List.class.isAssignableFrom((Class<?>) returnType.getRawType())) { "
                    + " return serializer.deserializeFromBytes(data.toBytes(), returnType); } "
                    + "Type token = returnType.getRawType(); if (Response.class.isAssignableFrom((Class<?>) token)) { "
                    + " token = returnType.getActualTypeArguments()[0]; } "
                    + "return serializer.deserializeFromBytes(data.toBytes(), token); } catch (IOException e) { "
                    + "    throw LOGGER.logThrowableAsError(new UncheckedIOException(e)); }")));

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
        // TODO (alzimmer): For now throw @SuppressWarnings({"unchecked", "cast"}) on generated methods while we
        //  improve / fix the generated code to no longer need it.
        internalMethod.setName(method.getMethodName())
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"),
                new ArrayInitializerExpr(
                    new NodeList<>(new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast")))))
            .addMarkerAnnotation(Override.class)
            .setType(TypeConverter.getAstType(method.getMethodReturnType()));
        method.getParameters()
            .forEach(param -> internalMethod
                .addParameter(new Parameter(StaticJavaParser.parseType(param.getShortTypeName()), param.getName())));

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
            e.printStackTrace();
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

        // Fix for use the URI passed to the method, if provided
        boolean useProvidedUri = method.getParameters()
            .stream()
            .anyMatch(parameter -> "uri".equals(parameter.getName()) && "String".equals(parameter.getShortTypeName()));

        body.tryAddImportToParentCompilationUnit(UriEscapers.class);
        String urlStatement = useProvidedUri
            ? String.format("String url = uri + \"/\" + %s;", method.getHost())
            : String.format("String url = %s;", method.getHost());

        body.addStatement(StaticJavaParser.parseStatement(urlStatement));

        appendQueryParams(body, method);

        Statement statement
            = StaticJavaParser.parseStatement("HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod."
                + method.getHttpMethod() + ").setUri(url);");

        statement.setLineComment("\n Create the HTTP request");
        body.addStatement(statement);
        addHeadersToRequest(body, method);
    }

    private void appendQueryParams(BlockStmt body, HttpRequestContext method) {
        // Iterate through the query parameters and append them to the url string if they are not null
        if (!method.getQueryParams().isEmpty()) {
            // Declare newUrl once
            Statement newUrlDeclaration = StaticJavaParser.parseStatement("String newUrl;");
            newUrlDeclaration.setComment(new LineComment("\n Append non-null query parameters"));
            body.addStatement(newUrlDeclaration);
            body.tryAddImportToParentCompilationUnit(LinkedHashMap.class);
            body.addStatement("LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();");

            method.getQueryParams().entrySet().forEach(entry -> {
                String key = entry.getKey();
                HttpRequestContext.QueryParameter value = entry.getValue();
                boolean isValueTypeString = method.getParameters()
                    .stream()
                    .anyMatch(parameter -> parameter.getName().equals(value.getValue())
                        && "String".equals(parameter.getShortTypeName()));
                if (value.shouldEncode()) {
                    if (isValueTypeString) {
                        String encodedKey = "UriEscapers.QUERY_ESCAPER.escape(\"" + key + "\")";
                        String encodedValue = "UriEscapers.QUERY_ESCAPER.escape(" + value.getValue() + ")";
                        body.addStatement("queryParamMap.put(" + encodedKey + ", " + encodedValue + ");");
                    } else {
                        body.addStatement("queryParamMap.put(\"" + key + "\", " + value.getValue() + ");");
                    }
                } else {
                    body.addStatement("queryParamMap.put(\"" + key + "\", " + value.getValue() + ");");
                }
            });
            body.addStatement("newUrl = CoreUtils.appendQueryParams(url, queryParamMap);");
            body.addStatement("if (newUrl != null) { url = newUrl; }");
        }
    }

    private void addHeadersToRequest(BlockStmt body, HttpRequestContext method) {
        if (method.getHeaders().isEmpty()) {
            return;
        }

        body.tryAddImportToParentCompilationUnit(HttpHeaderName.class);
        StringBuilder httpRequestBuilder = new StringBuilder("httpRequest.getHeaders()"); // Start the header chaining
        for (Map.Entry<String, String> header : method.getHeaders().entrySet()) {
            boolean isStringType = method.getParameters()
                .stream()
                .anyMatch(parameter -> parameter.getName().equals(header.getValue())
                    && "String".equals(parameter.getShortTypeName()));
            String value = isStringType ? header.getValue() : "String.valueOf(" + header.getValue() + ")";
            String constantName
                = LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.get(header.getKey().toLowerCase(Locale.ROOT));
            if (constantName != null) {
                httpRequestBuilder.append(".add(HttpHeaderName.")
                    .append(constantName)
                    .append(", ")
                    .append(value)
                    .append(")");
            } else {
                httpRequestBuilder.append(".add(HttpHeaderName.fromString(\"")
                    .append(header.getKey())
                    .append("\"), ")
                    .append(value)
                    .append(")");

            }
        }
        // Finalize the statement
        body.addStatement(StaticJavaParser.parseStatement(httpRequestBuilder + ";"));
    }

    private void finalizeHttpRequest(BlockStmt body, TypeMirror returnTypeName, HttpRequestContext method,
        boolean serializationFormatSet) {
        body.tryAddImportToParentCompilationUnit(Response.class);

        Statement statement = StaticJavaParser
            .parseStatement("Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);");
        statement.setLineComment("\n Send the request through the httpPipeline");
        body.addStatement(statement);

        if (!method.getExpectedStatusCodes().isEmpty()) {
            validateResponseStatus(body, method);
        }

        generateResponseHandling(body, returnTypeName, method, serializationFormatSet);
    }

    private void validateResponseStatus(BlockStmt body, HttpRequestContext method) {
        if (method.getExpectedStatusCodes().isEmpty()) {
            return;
        }

        body.addStatement(StaticJavaParser.parseStatement("int responseCode = networkResponse.getStatusCode();"));
        String expectedResponseCheck;
        if (method.getExpectedStatusCodes().size() == 1) {
            expectedResponseCheck = "responseCode == " + method.getExpectedStatusCodes().get(0) + ";";
        } else {
            String statusCodes = method.getExpectedStatusCodes()
                .stream()
                .map(code -> "responseCode == " + code)
                .collect(Collectors.joining(" || "));
            expectedResponseCheck = "(" + statusCodes + ");";
        }
        body.addStatement(StaticJavaParser.parseStatement("boolean expectedResponse = " + expectedResponseCheck));

        body.tryAddImportToParentCompilationUnit(RuntimeException.class);
        body.addStatement(StaticJavaParser.parseStatement("if (!expectedResponse) {"
            + " throw new RuntimeException(\"Unexpected response code: \" + responseCode); }"));
    }
}
