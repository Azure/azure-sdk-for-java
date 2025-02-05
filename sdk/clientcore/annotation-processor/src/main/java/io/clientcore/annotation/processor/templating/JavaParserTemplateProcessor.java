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
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.util.JsonSerializer;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.stream.Collectors;

import static io.clientcore.annotation.processor.utils.ResponseBodyModeGeneration.generateResponseHandling;

/**
 * This class generates the implementation of the service interface.
 */
public class JavaParserTemplateProcessor implements TemplateProcessor {

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

        // Create the serviceVersion field
        String serviceVersionClassName
            = serviceInterfaceShortName.substring(0, serviceInterfaceShortName.indexOf("ClientService"))
                + "ServiceVersion";
        String serviceVersionFullName
            = packageName.substring(0, packageName.lastIndexOf(".")) + "." + serviceVersionClassName;
        compilationUnit.addImport(serviceVersionFullName);
        classBuilder.addField(serviceVersionClassName, "serviceVersion", Modifier.Keyword.PRIVATE);

        // Create the constructor
        compilationUnit.addImport(JsonSerializer.class);
        classBuilder.addConstructor(Modifier.Keyword.PUBLIC)
            .addParameter(HttpPipeline.class, "defaultPipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser.parseBlock(
                "{ this.defaultPipeline = defaultPipeline; this.serializer = serializer == null ? new JsonSerializer() : serializer; }"));

        classBuilder.addField(String.class, "apiVersion", Modifier.Keyword.PRIVATE);

        // Add instance field
        classBuilder.addField(serviceInterfaceShortName, "instance", Modifier.Keyword.PRIVATE, Modifier.Keyword.STATIC);

        // Add the static getNewInstance method
        classBuilder.addMethod("getNewInstance", Modifier.Keyword.PUBLIC, Modifier.Keyword.STATIC)
            .setType(serviceInterfaceShortName)
            .addParameter(HttpPipeline.class, "pipeline")
            .addParameter(ObjectSerializer.class, "serializer")
            .setBody(StaticJavaParser
                .parseBlock("{ return new " + serviceInterfaceImplShortName + "(pipeline, serializer); }"));

        configurePipelineMethod(classBuilder.addMethod("getPipeline", Modifier.Keyword.PUBLIC));
        configureServiceVersionMethod(classBuilder.addMethod("getServiceVersion", Modifier.Keyword.PUBLIC),
            serviceVersionClassName);

        getGeneratedServiceMethods(templateInput);

        try (Writer fileWriter = processingEnv.getFiler()
            .createSourceFile(packageName + "." + serviceInterfaceImplShortName)
            .openWriter()) {
            fileWriter.write(compilationUnit.toString());
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void getGeneratedServiceMethods(TemplateInput templateInput) {
        for (HttpRequestContext method : templateInput.getHttpRequestContexts()) {
            boolean generateInternalOnly = method.getParameters().isEmpty()
                || method.getParameters()
                    .stream()
                    .anyMatch(parameter -> !(parameter.getName().equals("endpoint")
                        || parameter.getName().equals("apiVersion")));

            if (generateInternalOnly) {
                configureInternalMethod(classBuilder.addMethod(method.getMethodName()), method); // Generate the internal method
            } else {
                configurePublicMethod(classBuilder.addMethod(method.getMethodName()), method);
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
        // TODO (alzimmer): For now throw @SuppressWarnings({"unchecked", "cast"}) on generated methods while we
        //  improve / fix the generated code to no longer need it.
        publicMethod.setName(method.getMethodName())
            .setModifiers(Modifier.Keyword.PUBLIC)
            .addAnnotation(new SingleMemberAnnotationExpr(new Name("SuppressWarnings"),
                new ArrayInitializerExpr(
                    new NodeList<>(new StringLiteralExpr("unchecked"), new StringLiteralExpr("cast")))))
            .addMarkerAnnotation(Override.class)
            .setType(inferTypeNameFromReturnType(method.getMethodReturnType()));

        System.out.println("method.getMethodReturnType(): " + method.getMethodReturnType());
        // add method parameters, with Context at the end
        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            if (parameter.getName().equals("endpoint") || parameter.getName().equals("apiVersion")) {
                continue;
            }
            publicMethod.addParameter(parameter.getShortTypeName(), parameter.getName());
        }

        // add call to the overloaded version of this method
        String params = method.getParameters()
            .stream()
            .map(HttpRequestContext.MethodParameter::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        if (!"void".equals(method.getMethodReturnType())) {
            publicMethod
                .setBody(new BlockStmt().addStatement(new ReturnStmt(method.getMethodName() + "(" + params + ")")));
        } else {
            publicMethod.setBody(StaticJavaParser.parseBlock("{" + method.getMethodName() + "(" + params + ")}"));
        }
        System.out.println("methodBuilder: " + publicMethod);
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
        finalizeHttpRequest(body, returnTypeName, method);

        internalMethod.setBody(body);
    }

    // Helper methods

    private void initializeHttpRequest(BlockStmt body, HttpRequestContext method) {
        body.tryAddImportToParentCompilationUnit(HttpRequest.class);
        body.tryAddImportToParentCompilationUnit(HttpMethod.class);

        body.addStatement(StaticJavaParser.parseStatement("String host = " + method.getHost() + ";"));
        Statement statement = StaticJavaParser.parseStatement(
            "HttpRequest httpRequest = new HttpRequest(HttpMethod." + method.getHttpMethod() + ", host);");
        statement.setLineComment("Create the HTTP request");
        body.addStatement(statement);
    }

    private void addHeadersToRequest(BlockStmt body, HttpRequestContext method) {
        if (method.getHeaders().isEmpty()) {
            return;
        }

        body.tryAddImportToParentCompilationUnit(HttpHeaders.class);
        body.tryAddImportToParentCompilationUnit(HttpHeaderName.class);

        Statement statement = StaticJavaParser.parseStatement("HttpHeaders headers = new HttpHeaders();");
        statement.setLineComment("Set the headers");
        body.addStatement(statement);
        for (Map.Entry<String, String> header : method.getHeaders().entrySet()) {
            String enumHeaderKey = header.getKey().toUpperCase().replace("-", "_");
            boolean isEnumExists = false;
            for (HttpHeaderName httpHeaderName : HttpHeaderName.values()) {
                if (httpHeaderName.getCaseInsensitiveName().equals(header.getKey().toLowerCase())) {
                    isEnumExists = true;
                    break;
                }
            }

            boolean isStringType = method.getParameters()
                .stream()
                .anyMatch(parameter -> parameter.getName().equals(header.getValue())
                    && "String".equals(parameter.getShortTypeName()));
            String value = isStringType ? header.getValue() : "String.valueOf(" + header.getValue() + ")";

            if (isEnumExists) {
                body.addStatement(StaticJavaParser
                    .parseStatement("headers.add(HttpHeaderName." + enumHeaderKey + ", " + value + ");"));
            } else {
                body.addStatement(StaticJavaParser.parseStatement(
                    "headers.add(HttpHeaderName.fromString(\"" + header.getKey() + "\"), " + value + ");"));
            }
        }

        body.addStatement(StaticJavaParser.parseStatement("httpRequest.setHeaders(headers);"));
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
        statement.setLineComment("Send the request through the pipeline");
        body.addStatement(statement);

        if (!method.getExpectedStatusCodes().isEmpty()) {
            validateResponseStatus(body, method);
        }

        // requestOptions is not used in the generated code for RestProxyTests
        generateResponseHandling(body, returnTypeName, false);
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
                setContentTypeHeader(body, contentType);
            }
            if ("io.clientcore.core.util.binarydata.BinaryData".equals(parameterType)) {
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

    private static void setContentTypeHeader(BlockStmt body, String contentType) {
        switch (contentType) {
            case ContentType.APPLICATION_JSON:
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_JSON);"));
                break;

            case ContentType.APPLICATION_OCTET_STREAM:
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_OCTET_STREAM);"));
                break;

            case ContentType.APPLICATION_X_WWW_FORM_URLENCODED:
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.APPLICATION_X_WWW_FORM_URLENCODED);"));
                break;

            case ContentType.TEXT_EVENT_STREAM:
                body.addStatement(StaticJavaParser.parseStatement(
                    "httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, ContentType.TEXT_EVENT_STREAM);"));
                break;

            default:
                body.addStatement(StaticJavaParser
                    .parseStatement("httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, " + contentType + ");"));
                break;
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

        String l = builder.toString();
        System.out.println("ParameterizedTypeName: " + l);
        return l;
    }
}
