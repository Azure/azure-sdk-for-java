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
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.serialization.JsonSerializer;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.utils.CodegenUtil;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;

import static io.clientcore.annotation.processor.utils.ResponseBodyModeGeneration.generateResponseHandling;

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
        String serviceInterfaceImplShortName = templateInput.getServiceInterfaceImplShortName();
        String serviceInterfaceShortName = templateInput.getServiceInterfaceShortName();

        templateInput.getImports().keySet().forEach(compilationUnit::addImport);

        // For multi-line LineComments they need to be added individually as orphan comments.
        compilationUnit.addOrphanComment(new LineComment("Copyright (c) Microsoft Corporation. All rights reserved."));
        compilationUnit.addOrphanComment(new LineComment("Licensed under the MIT License."));
        compilationUnit.setPackageDeclaration(packageName);
        classBuilder = compilationUnit.addClass(serviceInterfaceImplShortName, Modifier.Keyword.PUBLIC);

        // Import the service interface using the fully qualified name.
        // TODO (alzimmer): Should check if the service interface and implementation are in the same package. If so,
        //  this import isn't needed. But this can be a final touches thing.
        compilationUnit.addImport(templateInput.getServiceInterfaceFQN());

        classBuilder.addImplementedType(serviceInterfaceShortName);

        // Add ClientLogger static instantiation.
        configureLoggerField(classBuilder.addField("ClientLogger", "LOGGER", Modifier.Keyword.PRIVATE,
            Modifier.Keyword.STATIC, Modifier.Keyword.FINAL), serviceInterfaceShortName);

        // Create the defaultPipeline field
        classBuilder.addField(HttpPipeline.class, "defaultPipeline", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        // Create the serializer field
        classBuilder.addField(ObjectSerializer.class, "serializer", Modifier.Keyword.PRIVATE, Modifier.Keyword.FINAL);

        // Create the endpoint field
        //FieldSpec endpoint = FieldSpec.builder(String.class, "endpoint", Modifier.PRIVATE, Modifier.FINAL)
        //    .build();

        // TODO: Disable these features until the Service interface requirements are determined for Service Version
        // Create the serviceVersion field
        // String serviceVersionClassName
        //    = serviceInterfaceShortName.substring(0, serviceInterfaceShortName.indexOf("ClientService"))
        //        + "ServiceVersion";
        //String serviceVersionFullName
        //    = packageName.substring(0, packageName.lastIndexOf(".")) + "." + serviceVersionClassName;
        //compilationUnit.addImport(serviceVersionFullName);
        //classBuilder.addField(serviceVersionClassName, "serviceVersion", Modifier.Keyword.PRIVATE);

        // Create the constructor
        compilationUnit.addImport(JsonSerializer.class);
        classBuilder.addConstructor(Modifier.Keyword.PUBLIC)
            .addParameter(HttpPipeline.class, "defaultPipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser.parseBlock(
                "{ this.defaultPipeline = defaultPipeline; this.serializer = serializer == null ? new JsonSerializer() : serializer; }"));

        // TODO: Disable these features until the Service interface requirements are determined for Service Version
        //classBuilder.addField(String.class, "apiVersion", Modifier.Keyword.PRIVATE);

        // Add the static getNewInstance method
        classBuilder.addMethod("getNewInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType(serviceInterfaceShortName)
            .addParameter(HttpPipeline.class, "pipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser
                .parseBlock("{ return new " + serviceInterfaceImplShortName + "(pipeline, serializer); }"));

        configurePipelineMethod(classBuilder.addMethod("getPipeline", Modifier.Keyword.PUBLIC));
        // TODO: Disable these features until the Service interface requirements are determined for Service Version
        //configureServiceVersionMethod(classBuilder.addMethod("getServiceVersion", Modifier.Keyword.PUBLIC),
        //    serviceVersionClassName);

        getGeneratedServiceMethods(templateInput);
        addDeserializeHelperMethod();

        try (Writer fileWriter = processingEnv.getFiler()
            .createSourceFile(packageName + "." + serviceInterfaceImplShortName)
            .openWriter()) {
            fileWriter.write(compilationUnit.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addDeserializeHelperMethod() {
        MethodDeclaration deserializeHelperMethod
            = classBuilder.addMethod("decodeByteArray", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC)
                .setType("Object")
                .addParameter("byte[]", "bytes")
                .addParameter(ObjectSerializer.class, "serializer")
                .addParameter("String", "returnType");
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(IOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(UncheckedIOException.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(CodegenUtil.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(ParameterizedType.class);
        deserializeHelperMethod.tryAddImportToParentCompilationUnit(Type.class);
        deserializeHelperMethod.setBody(new BlockStmt().addStatement(StaticJavaParser
            .parseStatement("try {" + " ParameterizedType type = CodegenUtil.inferTypeNameFromReturnType(returnType);"
                + " Type token = type.getRawType();" + " if (Response.class.isAssignableFrom((Class<?>) token)) {"
                + "     token = type.getActualTypeArguments()[0];" + " }"
                + " return serializer.deserializeFromBytes(bytes, token);" + " } catch (IOException e) {"
                + " throw LOGGER.logThrowableAsError(new UncheckedIOException(e));" + " }")));
    }

    void getGeneratedServiceMethods(TemplateInput templateInput) {
        for (HttpRequestContext method : templateInput.getHttpRequestContexts()) {
            if (!method.isConvenience()) {
                configureInternalMethod(classBuilder.addMethod(method.getMethodName()), method);
            }
        }
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

    static String getServiceVersionType(String packageName, String serviceInterfaceShortName) {
        return packageName + "."
            + serviceInterfaceShortName.substring(0, serviceInterfaceShortName.indexOf("ClientService"))
            + "ServiceVersion";
    }

    void configureEndpointMethod(MethodDeclaration endpointMethod) {
        endpointMethod.setName("getEndpoint")
            .setModifiers(Modifier.Keyword.PUBLIC)
            .setType(String.class)
            .setBody(new BlockStmt().addStatement(new ReturnStmt("endpoint")));
    }

    void configurePipelineMethod(MethodDeclaration pipelineMethod) {
        pipelineMethod.tryAddImportToParentCompilationUnit(HttpPipeline.class);
        pipelineMethod.setName("getPipeline")
            .setModifiers(Modifier.Keyword.PUBLIC)
            .setType(HttpPipeline.class)
            .setBody(new BlockStmt().addStatement(new ReturnStmt("defaultPipeline")));
    }

    void configureServiceVersionMethod(MethodDeclaration serviceVersionMethod, String serviceVersionType) {
        serviceVersionMethod.setName("getServiceVersion")
            .setModifiers(Modifier.Keyword.PUBLIC)
            .setType(serviceVersionType)
            .setBody(new BlockStmt().addStatement(new ReturnStmt("serviceVersion")));
    }

    void configurePublicMethod(MethodDeclaration publicMethod, HttpRequestContext method) {
        // Set method properties
        publicMethod.setName(method.getMethodName())
            .setModifiers(Modifier.Keyword.PUBLIC)
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"),
                new ArrayInitializerExpr(
                    NodeList.nodeList(new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast")))))
            .addMarkerAnnotation(Override.class)
            .setType(inferTypeNameFromReturnType(method.getMethodReturnType()));

        // Add parameters
        method.getParameters().forEach(param -> publicMethod.addParameter(param.getShortTypeName(), param.getName()));

        // Generate method signature params
        String params = method.getParameters()
            .stream()
            .map(HttpRequestContext.MethodParameter::getName)
            .collect(Collectors.joining(", "));

        if (!isVoidReturnType(method.getMethodReturnType())) {
            // adjust the convenience method naming to remove "Convenience" to existing method name and append Response
            String callerMethodName = method.getMethodName().replace("Convenience", "") + "Response";
            publicMethod.setBody(
                new BlockStmt().addStatement(new ReturnStmt(callerMethodName + "(" + params + ").getValue()")));
        } else {
            publicMethod.setBody(StaticJavaParser.parseBlock("{" + method.getMethodName() + "(" + params + ")}"));
        }
    }

    private boolean isVoidReturnType(String returnType) {
        return "void".equals(returnType) || "java.lang.Void".equals(returnType);
    }

    private void configureInternalMethod(MethodDeclaration internalMethod, HttpRequestContext method) {
        String returnTypeName = inferTypeNameFromReturnType(method.getMethodReturnType());
        // TODO (alzimmer): For now throw @SuppressWarnings({"unchecked", "cast"}) on generated methods while we
        //  improve / fix the generated code to no longer need it.
        internalMethod.setName(method.getMethodName())
            .setModifiers(Modifier.Keyword.PUBLIC)
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"),
                new ArrayInitializerExpr(
                    new NodeList<>(new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast")))))
            .addMarkerAnnotation(Override.class)
            .setType(returnTypeName);

        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            internalMethod.addParameter(parameter.getShortTypeName(), parameter.getName());
        }

        BlockStmt body = internalMethod.getBody().get();
        body.addStatement(StaticJavaParser.parseStatement("HttpPipeline pipeline = this.getPipeline();"));

        initializeHttpRequest(body, method);
        addHeadersToRequest(body, method);
        addRequestBody(body, method);
        addRequestOptionsToRequestIfPresent(body, method);
        finalizeHttpRequest(body, returnTypeName, method);

        internalMethod.setBody(body);
    }

    private void addRequestOptionsToRequestIfPresent(BlockStmt body, HttpRequestContext method) {
        // Check if any parameter in the method is of type RequestOptions
        boolean hasRequestOptions = method.getParameters()
            .stream()
            .anyMatch(parameter -> "options".equals(parameter.getName())
                && "RequestOptions".equals(parameter.getShortTypeName()));

        if (hasRequestOptions) {
            ExpressionStmt statement = new ExpressionStmt(new MethodCallExpr(new NameExpr("httpRequest"),
                "setRequestOptions", NodeList.nodeList(new NameExpr("options"))));
            statement.setLineComment("Set the Request Options");
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
        statement.setLineComment("Create the HTTP request");
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

            String constantName = LOWERCASE_HEADER_TO_HTTPHEADENAME_CONSTANT.get(header.getKey().toLowerCase());
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
            configureRequestWithBodyAndContentType(body, requestBody.getParameterType(), requestBody.getContentType(),
                requestBody.getParameterName(), isContentTypeSetInHeaders);
        } else {
            body.addStatement(
                StaticJavaParser.parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"));
        }

        body.getStatements().get(index).setLineComment("Set the request body");
    }

    private void finalizeHttpRequest(BlockStmt body, String returnTypeName, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(Response.class);

        Statement statement = StaticJavaParser.parseStatement("Response<?> response = pipeline.send(httpRequest);");
        if (!isVoidReturnType(returnTypeName)) {
            if (!isPrimitiveOrWrapper(returnTypeName)
                && returnTypeName.startsWith("io.clientcore.core.http.models.Response<")) {
                // Extract the variable declaration
                if (statement.isExpressionStmt()) {
                    statement.asExpressionStmt().getExpression().ifVariableDeclarationExpr(variableDeclarationExpr -> {
                        variableDeclarationExpr.getVariables().forEach(variable -> {
                            // Parse the full response type with generics from returnTypeName
                            ClassOrInterfaceType responseType
                                = StaticJavaParser.parseClassOrInterfaceType(returnTypeName);

                            // Set the new type for the variable
                            variable.setType(responseType);

                            CastExpr castExpression = new CastExpr(responseType, variable.getInitializer().get());
                            variable.setInitializer(castExpression);

                        });
                    });
                }
            }
        }
        statement.setLineComment("Send the request through the pipeline");
        body.addStatement(statement);

        if (!method.getExpectedStatusCodes().isEmpty()) {
            validateResponseStatus(body, method);
        }

        generateResponseHandling(body, returnTypeName, method);
    }

    /**
     * Helper method to check if a type is a primitive or its wrapper.
     * @param typeName the return type string value
     * @return boolean if the return type string is primitive type
     */
    public static boolean isPrimitiveOrWrapper(String typeName) {
        // TODO: This helper method will be removed once the return type issue is fixed
        return "int".equals(typeName)
            || "java.lang.Integer".equals(typeName)
            || "double".equals(typeName)
            || "java.lang.Double".equals(typeName)
            || "long".equals(typeName)
            || "java.lang.Long".equals(typeName)
            || "short".equals(typeName)
            || "java.lang.Short".equals(typeName)
            || "float".equals(typeName)
            || "java.lang.Float".equals(typeName)
            || "boolean".equals(typeName)
            || "java.lang.Boolean".equals(typeName)
            || "char".equals(typeName)
            || "java.lang.Character".equals(typeName)
            || "byte".equals(typeName)
            || "java.lang.Byte".equals(typeName)
            || typeName.endsWith("[]");  // Catch all array types
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

    /**
     * Configures the request with the body content and content type.
     * @param body The method builder to add the statements to
     * @param parameterType The type of the parameter
     * @param contentType The content type of the request
     * @param parameterName The name of the parameter
     * @param isContentTypeSetInHeaders Whether the content type is set in the headers
     */
    public void configureRequestWithBodyAndContentType(BlockStmt body, String parameterType, String contentType,
        String parameterName, boolean isContentTypeSetInHeaders) {
        if (parameterType == null) {
            // No body content to set
            body.addStatement(
                StaticJavaParser.parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, \"0\");"));
        } else {

            if (contentType == null || contentType.isEmpty()) {
                if ("byte[]".equals(parameterType) || "String".equals(parameterType)) {

                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {

                    contentType = ContentType.APPLICATION_JSON;
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
                if (contentTypePart.trim().equalsIgnoreCase(ContentType.APPLICATION_JSON)) {
                    isJson = true;

                    break;
                }
            }
            updateRequestWithBodyContent(body, isJson, parameterType, parameterName);
        }
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

    /*
     * Get a TypeName for a parameterized type, given the raw type and type arguments as Class objects.
     */
    private static String inferTypeNameFromReturnType(String typeString) {
        if (typeString == null) {
            return "void";
        }
        // Split the string into raw type and type arguments
        int angleBracketIndex = typeString.indexOf('<');
        if (angleBracketIndex == -1) {
            // No type arguments
            return typeString;
        }
        String rawTypeString = typeString.substring(0, angleBracketIndex);
        String typeArgumentsString = typeString.substring(angleBracketIndex + 1, typeString.length() - 1);

        return getParameterizedTypeNameFromRawArguments(rawTypeString, typeArgumentsString);
    }

    /*
     * Get a TypeName for a parameterized type, given the raw type and type arguments as Class objects.
     */
    private static String getParameterizedTypeNameFromRawArguments(String rawType, String... typeArguments) {
        StringBuilder builder = new StringBuilder(rawType).append('<');

        boolean first = true;
        for (String typeArgument : typeArguments) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(typeArgument);
        }
        builder.append('>');

        return builder.toString();
    }
}
