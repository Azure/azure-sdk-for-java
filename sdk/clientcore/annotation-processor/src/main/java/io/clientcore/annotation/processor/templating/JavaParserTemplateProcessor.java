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
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.json.JsonSerializer;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static io.clientcore.annotation.processor.utils.ResponseBodyModeGeneration.generateResponseHandling;

/**
 * This class generates the implementation of the service interface.
 */
public class JavaParserTemplateProcessor implements TemplateProcessor {
    private static final String APPLICATION_JSON = "application/json";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

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
            HttpHeaderName httpHeaderName = null;
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
        addOrphanComments();
        setPackageDeclaration(packageName);
        createClass(serviceInterfaceImplShortName, serviceInterfaceShortName, templateInput);

        try (Writer fileWriter = processingEnv.getFiler()
            .createSourceFile(packageName + "." + serviceInterfaceImplShortName)
            .openWriter()) {
            fileWriter.write(compilationUnit.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addImports(TemplateInput templateInput) {
        templateInput.getImports().keySet().forEach(compilationUnit::addImport);
    }

    void addOrphanComments() {
        compilationUnit.addOrphanComment(new LineComment("Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment("Licensed under the MIT License."));
    }

    void setPackageDeclaration(String packageName) {
        compilationUnit.setPackageDeclaration(packageName);
    }

    void createClass(String serviceInterfaceImplShortName, String serviceInterfaceShortName,
        TemplateInput templateInput) {
        classBuilder = compilationUnit.addClass(serviceInterfaceImplShortName, Modifier.Keyword.PUBLIC);

        String serviceInterfacePackage = templateInput.getServiceInterfaceFQN()
            .substring(0, templateInput.getServiceInterfaceFQN().lastIndexOf('.'));
        compilationUnit.addImport(serviceInterfacePackage + "." + serviceInterfaceShortName);
        classBuilder.addImplementedType(serviceInterfaceShortName);

        configureLoggerField(classBuilder.addField("ClientLogger", "LOGGER", Modifier.Keyword.PRIVATE,
            Modifier.Keyword.STATIC, Modifier.Keyword.FINAL), serviceInterfaceShortName);

        classBuilder.addField(HttpPipeline.class, "defaultPipeline", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);
        classBuilder.addField(ObjectSerializer.class, "serializer", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        compilationUnit.addImport(JsonSerializer.class);
        classBuilder.addConstructor(Modifier.Keyword.PRIVATE)
            .addParameter(HttpPipeline.class, "defaultPipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser.parseBlock(
                "{ this.defaultPipeline = defaultPipeline; this.serializer = serializer == null ? new JsonSerializer() : serializer; }"));

        classBuilder.addMethod("getNewInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType(serviceInterfaceShortName)
            .addParameter(HttpPipeline.class, "pipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser
                .parseBlock("{ return new " + serviceInterfaceImplShortName + "(pipeline, serializer); }"))
            .setJavadocComment("Creates an instance of " + serviceInterfaceShortName
                + " that is capable of sending requests to the service.\n"
                + "@param pipeline The HTTP pipeline to use for sending requests.\n"
                + "@param serializer The serializer to use for serializing and deserializing request and response bodies.\n"
                + "@return An instance of `" + serviceInterfaceShortName + "`;");

        configurePipelineMethod(classBuilder.addMethod("getPipeline", Modifier.Keyword.PRIVATE));

        for (HttpRequestContext method : templateInput.getHttpRequestContexts()) {
            if (!method.isConvenience()) {
                configureInternalMethod(classBuilder.addMethod(method.getMethodName(), Modifier.Keyword.PUBLIC),
                    method);
            }
        }

        addDeserializeHelperMethod(
            classBuilder.addMethod("decodeByteArray", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC));
        addDefaultResponseHandlingMethod(
            classBuilder.addMethod("getOrDefaultResponseBodyMode", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC));
    }

    /**
     * Get the compilation unit
     * @return the compilation unit
     */
    CompilationUnit getCompilationUnit() {
        return this.compilationUnit;
    }

    private void addDefaultResponseHandlingMethod(MethodDeclaration defaultResponseHandlingMethod) {
        defaultResponseHandlingMethod.setType(ResponseBodyMode.class)
            .addParameter(RequestOptions.class, "requestOptions");
        defaultResponseHandlingMethod
            .setJavadocComment("Retrieve the ResponseBodyMode from RequestOptions or use the default "
                + "ResponseBodyMode.BUFFER.\n" + "@param requestOptions the request options set on the HttpRequest\n"
                + "@return the ResponseBodyMode from RequestOptions or ResponseBodyMode.BUFFER");
        defaultResponseHandlingMethod.setBody(StaticJavaParser.parseBlock("{ ResponseBodyMode responseBodyMode;"
            + " if (requestOptions != null && requestOptions.getResponseBodyMode() != null) {"
            + " responseBodyMode = requestOptions.getResponseBodyMode();" + " } else {"
            + " responseBodyMode = ResponseBodyMode.BUFFER;" + " }" + " return responseBodyMode; }"));

    }

    private void addDeserializeHelperMethod(MethodDeclaration deserializeHelperMethod) {
        deserializeHelperMethod.setType("Object")
            .addParameter("byte[]", "bytes")
            .addParameter(ObjectSerializer.class, "serializer")
            .addParameter("ParameterizedType", "returnType");
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(IOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(UncheckedIOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(ParameterizedType.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(Type.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(List.class);
        deserializeHelperMethod
            .setJavadocComment("Decodes the body of an {@link Response} into the type returned by the called API.\n"
                + "@param bytes The bytes to decode.\n" + "@param serializer The serializer to use.\n"
                + "@param returnType The type of the ParameterizedType return value.\n" + "@return The decoded value.\n"
                + "@throws IOException If the deserialization fails.");

        deserializeHelperMethod.setBody(new BlockStmt().addStatement(StaticJavaParser
            .parseStatement("try { " + "if (List.class.isAssignableFrom((Class<?>) returnType.getRawType())) { "
                + "    return serializer.deserializeFromBytes(bytes, returnType); " + "} "
                + "Type token = returnType.getRawType(); " + "if (Response.class.isAssignableFrom((Class<?>) token)) { "
                + "    token = returnType.getActualTypeArguments()[0]; " + "} "
                + "return serializer.deserializeFromBytes(bytes, token); " + "} catch (IOException e) { "
                + "    throw LOGGER.logThrowableAsError(new UncheckedIOException(e)); " + "}")));

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

    void configurePipelineMethod(MethodDeclaration pipelineMethod) {
        pipelineMethod.tryAddImportToParentCompilationUnit(HttpPipeline.class);
        pipelineMethod.setName("getPipeline")
            .setModifiers(Modifier.Keyword.PRIVATE)
            .setType(HttpPipeline.class)
            .setBody(new BlockStmt().addStatement(new ReturnStmt("defaultPipeline")));
    }

    /**
     * Configures the request with the body content and content type.
     *
     * @param body The method builder to add the statements to
     * @param parameterType The type of the parameter
     * @param contentType The content type of the request
     * @param parameterName The name of the parameter
     * @param isContentTypeSetInHeaders Whether the content type is set in the headers
     */
    void configureBodyContentType(BlockStmt body, String parameterType, String contentType, String parameterName,
        boolean isContentTypeSetInHeaders) {
        if (parameterType == null) {
            // No body content to set
            body.addStatement(
                StaticJavaParser.parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"));
        } else {

            if (contentType == null || contentType.isEmpty()) {
                // TODO (alzimmer): Why is String octet-stream?
                if ("byte[]".equals(parameterType) || "String".equals(parameterType)) {
                    contentType = APPLICATION_OCTET_STREAM;
                } else {
                    contentType = APPLICATION_JSON;
                }
            }
            // Set the content type header if it is not already set in the headers
            if (!isContentTypeSetInHeaders) {
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, \"" + contentType + "\");"));
            }
            if ("io.clientcore.core.models.binarydata.BinaryData".equals(parameterType)) {
                body.tryAddImportToParentCompilationUnit(BinaryData.class);
                body.addStatement(
                    StaticJavaParser.parseStatement("BinaryData binaryData = (BinaryData) " + parameterName + ";"));
                body.addStatement(StaticJavaParser.parseStatement("if (binaryData.getLength() != null) {"
                    + "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));"
                    + "httpRequest.setBody(binaryData); }"));
                return;
            }

            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");

            for (final String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(APPLICATION_JSON)) {
                    isJson = true;

                    break;
                }
            }
            updateRequestWithBodyContent(body, isJson, parameterType, parameterName);
        }
    }

    private void configureInternalMethod(MethodDeclaration internalMethod, HttpRequestContext method) {
        // TODO (alzimmer): For now throw @SuppressWarnings({"unchecked", "cast"}) on generated methods while we
        //  improve / fix the generated code to no longer need it.
        internalMethod.setName(method.getMethodName())
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"),
                new ArrayInitializerExpr(
                    new NodeList<>(new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast")))))
            .addMarkerAnnotation(Override.class)
            .setType(method.getMethodReturnType());

        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            internalMethod.addParameter(new com.github.javaparser.ast.body.Parameter(
                StaticJavaParser.parseType(parameter.getShortTypeName()), parameter.getName()));
        }

        BlockStmt body = internalMethod.getBody().get();
        body.addStatement(StaticJavaParser.parseStatement("HttpPipeline pipeline = this.getPipeline();"));

        initializeHttpRequest(body, method);
        addHeadersToRequest(body, method);
        addRequestBody(body, method);
        addRequestOptionsToRequestIfPresent(body, method);
        finalizeHttpRequest(body, method.getMethodReturnType(), method);

        internalMethod.setBody(body);
    }

    private void addRequestOptionsToRequestIfPresent(BlockStmt body, HttpRequestContext method) {
        // Check if any parameter in the method is of type RequestOptions
        boolean hasRequestOptions = method.getParameters()
            .stream()
            .anyMatch(parameter -> "options".equals(parameter.getName())
                && "RequestOptions".equals(parameter.getShortTypeName()));

        if (hasRequestOptions) {
            // Create a statement for setting request options
            ExpressionStmt statement = new ExpressionStmt(new MethodCallExpr(new NameExpr("httpRequest"),
                "setRequestOptions", NodeList.nodeList(new NameExpr("options"))));

            statement.setComment(new LineComment("\n Set the Request Options"));
            body.addStatement(statement);
        }

    }

    // Helper methods
    private void initializeHttpRequest(BlockStmt body, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(HttpRequest.class);
        body.tryAddImportToParentCompilationUnit(HttpMethod.class);

        // Fix for use the URI passed to the method, if provided
        boolean useProvidedUri = method.getParameters()
            .stream()
            .anyMatch(parameter -> "uri".equals(parameter.getName()) && "String".equals(parameter.getShortTypeName()));

        if (useProvidedUri) {
            body.addStatement(
                StaticJavaParser.parseStatement("String host = uri + \"/\" + \"" + method.getPath() + "\";"));
        } else {
            body.addStatement(StaticJavaParser.parseStatement("String host = " + method.getHost() + ";"));
        }

        Statement statement
            = StaticJavaParser.parseStatement("HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod."
                + method.getHttpMethod() + ").setUri(host);");

        statement.setLineComment("\n Create the HTTP request");
        body.addStatement(statement);
    }

    private void addHeadersToRequest(BlockStmt body, HttpRequestContext method) {
        if (method.getHeaders().isEmpty()) {
            return;
        }

        body.tryAddImportToParentCompilationUnit(HttpHeaderName.class);

        for (Map.Entry<String, String> header : method.getHeaders().entrySet()) {
            boolean isStringType = method.getParameters()
                .stream()
                .anyMatch(parameter -> parameter.getName().equals(header.getValue())
                    && "String".equals(parameter.getShortTypeName()));
            String value = isStringType ? header.getValue() : "String.valueOf(" + header.getValue() + ")";

            String constantName
                = LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.get(header.getKey().toLowerCase(Locale.ROOT));
            if (constantName != null) {
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().add(HttpHeaderName." + constantName + ", " + value + ");"));
            } else {
                body.addStatement(
                    StaticJavaParser.parseStatement("httpRequest.getHeaders().add(HttpHeaderName.fromString(\""
                        + header.getKey() + "\"), " + value + ");"));
            }
        }
    }

    private void addRequestBody(BlockStmt body, HttpRequestContext method) {
        int index = body.getStatements().size();

        HttpRequestContext.Body requestBody = method.getBody();
        boolean isContentTypeSetInHeaders
            = method.getParameters().stream().anyMatch(parameter -> parameter.getName().equals("contentType"));

        if (requestBody != null) {
            configureBodyContentType(body, requestBody.getParameterType(), requestBody.getContentType(),
                requestBody.getParameterName(), isContentTypeSetInHeaders);
        } else {
            body.addStatement(
                StaticJavaParser.parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"));
        }
        body.getStatements().get(index).setLineComment("\n Set the request body");
    }

    private void finalizeHttpRequest(BlockStmt body, com.github.javaparser.ast.type.Type returnTypeName,
        HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(Response.class);

        Statement statement = StaticJavaParser.parseStatement("Response<?> response = pipeline.send(httpRequest);");

        // Check if the return type is Response
        if (returnTypeName instanceof ClassOrInterfaceType
            && returnTypeName.asClassOrInterfaceType().getNameAsString().equals("Response")) {

            // Extract the variable declaration
            if (statement.isExpressionStmt()) {
                statement.asExpressionStmt().getExpression().ifVariableDeclarationExpr(variableDeclarationExpr -> {
                    variableDeclarationExpr.getVariables().forEach(variable -> {
                        // Parse the full response type
                        ClassOrInterfaceType responseType = returnTypeName.asClassOrInterfaceType();

                        // Set the new type for the variable
                        variable.setType(responseType);

                        // Ensure the initializer is correctly casted
                        variable.getInitializer().ifPresent(initializer -> {
                            CastExpr castExpression = new CastExpr(responseType, initializer);
                            variable.setInitializer(castExpression);
                        });
                    });
                });
            }
        }
        statement.setLineComment("\n Send the request through the pipeline");
        body.addStatement(statement);

        if (!method.getExpectedStatusCodes().isEmpty()) {
            validateResponseStatus(body, method);
        }

        generateResponseHandling(body, returnTypeName, method);
    }

    private void validateResponseStatus(BlockStmt body, HttpRequestContext method) {
        if (method.getExpectedStatusCodes().isEmpty()) {
            return;
        }

        body.addStatement(StaticJavaParser.parseStatement("int responseCode = response.getStatusCode();"));
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

    private void updateRequestWithBodyContent(BlockStmt body, boolean isJson, String parameterType,
        String parameterName) {
        if (parameterType == null) {
            return;
        }
        if (isJson) {
            body.addStatement(StaticJavaParser
                .parseStatement("httpRequest.setBody(BinaryData.fromObject(" + parameterName + ", serializer));"));
        } else if ("byte[]".equals(parameterType)) {
            body.addStatement(StaticJavaParser
                .parseStatement("httpRequest.setBody(BinaryData.fromBytes((byte[]) " + parameterName + "));"));
        } else if ("String".equals(parameterType)) {
            body.addStatement(StaticJavaParser
                .parseStatement("httpRequest.setBody(BinaryData.fromString((String) " + parameterName + "));"));
        } else if ("ByteBuffer".equals(parameterType)) {
            // TODO: confirm behavior
            //if (((ByteBuffer) bodyContentObject).hasArray()) {
            //    methodBuilder
            //            .addStatement("httpRequest.setBody($T.fromBytes(((ByteBuffer) $L).array()))", BinaryData.class, parameterName);
            //} else {
            //    byte[] array = new byte[((ByteBuffer) bodyContentObject).remaining()];
            //
            //    ((ByteBuffer) bodyContentObject).get(array);
            //    methodBuilder
            //            .addStatement("httpRequest.setBody($T.fromBytes($L))", BinaryData.class, array);
            //}
            body.tryAddImportToParentCompilationUnit(ByteBuffer.class);
            body.addStatement(StaticJavaParser.parseStatement(
                "httpRequest.setBody(BinaryData.fromBytes(((ByteBuffer) " + parameterName + ").array()));"));
        } else {
            body.addStatement(StaticJavaParser
                .parseStatement("httpRequest.setBody(BinaryData.fromObject(" + parameterName + ", serializer));"));
        }
    }
}
