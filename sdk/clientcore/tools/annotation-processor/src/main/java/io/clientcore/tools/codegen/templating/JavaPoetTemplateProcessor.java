// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.templating;

import io.clientcore.tools.codegen.models.HttpRequestContext;
import io.clientcore.tools.codegen.models.TemplateInput;
import io.clientcore.tools.codegen.utils.ResponseBodyModeGeneration;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

public class JavaPoetTemplateProcessor implements TemplateProcessor {
    private static final ClassName HTTP_HEADER_NAME = ClassName.get("io.clientcore.core.http.models", "HttpHeaderName");
    private static final ClassName CONTENT_TYPE = ClassName.get("io.clientcore.core.http.models", "ContentType");

    private final ClassName HTTP_REQUEST = ClassName.get("io.clientcore.core.http.models", "HttpRequest");
    private final ClassName RESPONSE = ClassName.get("io.clientcore.core.http" +
        ".models", "Response");
    private final ClassName HTTP_METHOD = ClassName.get("io.clientcore.core.http.models", "HttpMethod");

    private TypeSpec.Builder classBuilder;
    final ClassName HTTP_PIPELINE = ClassName.get("io.clientcore.core.http.pipeline", "HttpPipeline");
    static ClassName SERVICE_VERSION_TYPE;
    final ClassName CLIENTLOGGER_NAME = ClassName.get("io.clientcore.core.instrumentation.logging", "ClientLogger");

    @Override
    public void process(TemplateInput templateInput, ProcessingEnvironment processingEnv) {
        String packageName = templateInput.getPackageName();
        String serviceInterfaceImplShortName = templateInput.getServiceInterfaceImplShortName();
        String serviceInterfaceShortName = templateInput.getServiceInterfaceShortName();

        ClassName interfaceType = ClassName.get(packageName, serviceInterfaceShortName);

        // add LoggerField
        FieldSpec loggerField = getLoggerField(packageName, serviceInterfaceShortName);

        // Create the defaultPipeline field
        FieldSpec defaultPipeline =
            FieldSpec.builder(HTTP_PIPELINE, "defaultPipeline", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        // Create the serializer field
        FieldSpec serializer = FieldSpec.builder(ObjectSerializer.class, "serializer", Modifier.PRIVATE, Modifier.FINAL)
            .build();

        // Create the endpoint field
        FieldSpec endpoint = FieldSpec.builder(String.class, "endpoint", Modifier.PRIVATE, Modifier.FINAL)
            .build();

        // Create the serviceVersion field
        ClassName serviceVersionType = getServiceVersionType(packageName, serviceInterfaceShortName);
        FieldSpec serviceVersion =
            FieldSpec.builder(serviceVersionType, "serviceVersion", Modifier.PRIVATE, Modifier.FINAL)
                .build();

        // Create the constructor
        MethodSpec constructor = getServiceImplConstructor(packageName, serviceInterfaceShortName);

        FieldSpec apiVersion = FieldSpec.builder(String.class, "apiVersion")
            .addModifiers(Modifier.PRIVATE)
            .build();

        classBuilder = TypeSpec.classBuilder(serviceInterfaceImplShortName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(interfaceType)
            .addField(loggerField)
            .addField(defaultPipeline)
            .addField(serializer)
            .addField(endpoint)
            .addField(serviceVersion)
            .addField(apiVersion)
            .addMethod(getEndpointMethod())
            .addMethod(getPipelineMethod())
            .addMethod(getServiceVersionMethod())
            .addMethod(constructor);

        getGeneratedServiceMethods(templateInput);

        TypeSpec typeSpec = classBuilder.build();

        // Sets the indentation for the generated source file to four spaces.
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec)
            .indent("    ") // four spaces
            .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void getGeneratedServiceMethods(TemplateInput templateInput) {
        for (HttpRequestContext method : templateInput.getHttpRequestContexts()) {
            classBuilder.addMethod(generatePublicMethod(method));
            generateInternalMethod(method);
        }
    }

    FieldSpec getLoggerField(String packageName, String serviceInterfaceShortName) {
        return FieldSpec.builder(CLIENTLOGGER_NAME, "LOGGER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T($T.class)", CLIENTLOGGER_NAME, ClassName.get(packageName, serviceInterfaceShortName))
            .build();
    }

    MethodSpec getServiceImplConstructor(String packageName, String serviceInterfaceShortName) {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(HTTP_PIPELINE, "defaultPipeline")
            .addStatement("this.defaultPipeline = defaultPipeline")
            .addParameter(ClassName.get("io.clientcore.core.util.serializer", "ObjectSerializer"), "serializer")
            .addStatement("this.serializer = serializer")
            .addParameter(String.class, "endpoint")
            .addStatement("this.endpoint = endpoint")
            .addParameter(getServiceVersionType(packageName, serviceInterfaceShortName),
                "serviceVersion")
            .addStatement("this.apiVersion = serviceVersion.getVersion()")
            .addStatement("this.serviceVersion = serviceVersion")
            .build();
    }

    static ClassName getServiceVersionType(String packageName, String serviceInterfaceShortName) {
        SERVICE_VERSION_TYPE = ClassName.get(packageName, serviceInterfaceShortName.substring(0,
            serviceInterfaceShortName.indexOf("ClientService")) + "ServiceVersion");
        return SERVICE_VERSION_TYPE;
    }

    MethodSpec getEndpointMethod() {
        return MethodSpec.methodBuilder("getEndpoint")
            .addModifiers(Modifier.PUBLIC)
            .returns(String.class)
            .addStatement("return endpoint")
            .build();
    }

    MethodSpec getPipelineMethod() {
        return MethodSpec.methodBuilder("getPipeline")
            .addModifiers(Modifier.PUBLIC)
            .returns(HTTP_PIPELINE)
            .addStatement("return defaultPipeline")
            .build();
    }

    MethodSpec getServiceVersionMethod() {
        return MethodSpec.methodBuilder("getServiceVersion")
            .addModifiers(Modifier.PUBLIC)
            .returns(SERVICE_VERSION_TYPE)
            .addStatement("return serviceVersion")
            .build();
    }

    MethodSpec generatePublicMethod(HttpRequestContext method) {

        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getMethodName())
            .addModifiers(Modifier.PUBLIC)
            .returns(inferTypeNameFromReturnType(method.getMethodReturnType()));

        // add method parameters, with Context at the end
        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            if (parameter.getName().equals("endpoint") || parameter.getName().equals("apiVersion")) {
                continue;
            }
            methodBuilder.addParameter(TypeName.get(parameter.getTypeMirror()), parameter.getName());
        }

        // add call to the overloaded version of this method
        String params = method.getParameters().stream()
            .map(HttpRequestContext.MethodParameter::getName)
            .reduce((a, b) -> a + ", " + b)
            .orElse("");

        if (!"void".equals(method.getMethodReturnType())) {
            methodBuilder.addStatement("return $L($L)",
                method.getMethodName(), params);
        } else {
            methodBuilder.addStatement("$L($L)",
                method.getMethodName(), params);
        }

        return methodBuilder.build();
    }

    private void generateInternalMethod(HttpRequestContext method) {
        TypeName returnTypeName = inferTypeNameFromReturnType(method.getMethodReturnType());
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getMethodName())
            .addModifiers(Modifier.PRIVATE)
            .addAnnotation(Override.class)
            .returns(returnTypeName);

        // add method parameters, as well as the HttpPipeline at the front
        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            methodBuilder.addParameter(TypeName.get(parameter.getTypeMirror()), parameter.getName());
        }

        // add field pipeline
        methodBuilder.addStatement("HttpPipeline pipeline = this.getPipeline()");

        methodBuilder
            .addStatement("String host = $L", method.getHost())
            .addCode("\n")
            .addComment("create the request")
            .addStatement("$T httpRequest = new $T($T.$L, host)", HTTP_REQUEST, HTTP_REQUEST, HTTP_METHOD,
                method.getHttpMethod());

        // add headers
        if (!method.getHeaders().isEmpty()) {
            methodBuilder
                .addCode("\n")
                .addComment("set the headers")
                .addStatement("$T headers = new $T()", ClassName.get("io.clientcore.core.http.models", "HttpHeaders"),
                    ClassName.get("io.clientcore.core.http.models", "HttpHeaders"));
            for (Map.Entry<String, String> header : method.getHeaders().entrySet()) {
                String enumHeaderKey = header.getKey().toUpperCase().replace("-", "_");
                boolean isEnumExists = false;
                for (HttpHeaderName httpHeaderName : HttpHeaderName.values()) {
                    if (httpHeaderName.getCaseInsensitiveName().equals(header.getKey().toLowerCase())) {
                        isEnumExists = true;
                        break;
                    }
                }
                if (isEnumExists) {
                    methodBuilder.addStatement("headers.add($T.$L, $L)",
                        HTTP_HEADER_NAME, enumHeaderKey, header.getValue());
                } else {
                    methodBuilder.addStatement("headers.add($T.fromString($S), $L)",
                        HTTP_HEADER_NAME, header.getKey(), header.getValue());
                }
            }

            methodBuilder.addStatement("httpRequest.setHeaders(headers)");
        }

        methodBuilder
            .addCode("\n")
            .addComment("add RequestOptions to the request")
            .addStatement("httpRequest.setRequestOptions(requestOptions)");

        // [TODO] set SSE listener if available

        // set the body
        methodBuilder
            .addCode("\n")
            .addComment("set the body content if present");
        if (method.getBody() != null) {
            HttpRequestContext.Body body = method.getBody();
            String contentType = body.getContentType();
            String parameterType = body.getParameterType();
            String parameterName = body.getParameterName();

            configureRequestWithBodyAndContentType(methodBuilder, parameterType, contentType, parameterName);
        } else {
            methodBuilder
                .addStatement("httpRequest.getHeaders().set($T.CONTENT_LENGTH, $S)", HttpHeaderName.class, "0");
            methodBuilder.addComment("no body content to set");
        }

        // send request through pipeline
        methodBuilder
            .addCode("\n")
            .addComment("send the request through the pipeline")
            .addStatement("$T<?> response = pipeline.send(httpRequest)", RESPONSE);

        // check for expected status codes
        if (!method.getExpectedStatusCodes().isEmpty()) {
            methodBuilder
                .addCode("\n")
                .addStatement("final int responseCode = response.getStatusCode()");
            if (method.getExpectedStatusCodes().size() == 1) {
                methodBuilder.addStatement("boolean expectedResponse = responseCode == $L",
                    method.getExpectedStatusCodes().get(0));
            } else {
                String statusCodes = method.getExpectedStatusCodes().stream()
                    .map(code -> "responseCode == " + code)
                    .collect(Collectors.joining(" || "));
                methodBuilder.addStatement("boolean expectedResponse = " + statusCodes);
            }
            methodBuilder.beginControlFlow("if (!expectedResponse)")
                .addStatement("throw new $T(\"Unexpected response code: \" + responseCode)", RuntimeException.class)
                .endControlFlow();
        }

        // add return statement if method return type is not "void"
        if (returnTypeName.toString().contains("void") && returnTypeName.toString().contains("Void")) {
            methodBuilder.addStatement("return");
        } else if (returnTypeName.toString().contains("Response")) {
            if (returnTypeName.toString().contains("Void")) {
                methodBuilder.beginControlFlow("try")
                    .addStatement("response.close()")
                    .nextControlFlow("catch ($T e)", IOException.class)
                    .addStatement("throw LOGGER.logThrowableAsError(new $T(e))", UncheckedIOException.class)
                    .endControlFlow();
                createResponseIfNecessary(returnTypeName, methodBuilder);
            } else {
                // Step 1: Generate ResponseBodyMode assignment
                ResponseBodyModeGeneration.generateResponseBodyModeAssignment(methodBuilder);

                // Step 2: Generate DESERIALIZE handling
                ResponseBodyModeGeneration.generateDeserializeResponseHandling(methodBuilder);

                // Step 3: Generate non-DESERIALIZE handling
                ResponseBodyModeGeneration.generateNonDeserializeResponseHandling(methodBuilder);

                // Step 4: Create the response if necessary
                createResponseIfNecessary(returnTypeName, methodBuilder);
            }
        } else {
            handleResponseModeToCreateResponse(method, returnTypeName, methodBuilder);
        }

        classBuilder.addMethod(methodBuilder.build());
    }

    private static void createResponseIfNecessary(TypeName returnTypeName, MethodSpec.Builder methodBuilder) {
        // TODO: Fix me
        methodBuilder.addStatement("return ($T) response", returnTypeName);
    }

    private static void handleResponseModeToCreateResponse(HttpRequestContext method, TypeName returnTypeName,
        MethodSpec.Builder methodBuilder) {
        HttpMethod httpMethod = method.getHttpMethod();
        if (httpMethod == HttpMethod.HEAD &&
            (returnTypeName.toString().contains("Boolean") || returnTypeName.toString().contains("boolean"))) {
            methodBuilder.addStatement("return (responseCode / 100) == 2");
        } else if (returnTypeName.toString().contains("byte[]")) {
            methodBuilder
                .addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null")
                .addStatement(
                    "return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null");
        } else if (returnTypeName.toString().contains("InputStream")) {
            methodBuilder
                .addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("return responseBody.toStream()");
        } else if (returnTypeName.toString().contains("BinaryData")) {
            methodBuilder
                .addStatement("$T responseBody = response.getBody()", BinaryData.class);
        } else {
            methodBuilder
                .addStatement("$T responseBody = response.getBody()", BinaryData.class)
                .addStatement("return decodeByteArray(responseBody.toBytes(), response, serializer, methodParser)");
        }
    }

    public void configureRequestWithBodyAndContentType(MethodSpec.Builder methodBuilder, String parameterType,
        String contentType, String parameterName) {
        if (parameterType == null) {
            // No body content to set
            methodBuilder
                .addStatement("httpRequest.getHeaders().set($T.CONTENT_LENGTH, $S))", HttpHeaderName.class, 0);
        } else {

            if (contentType == null || contentType.isEmpty()) {
                if (parameterType.equals("byte[]") || parameterType.equals("String")) {

                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {

                    contentType = ContentType.APPLICATION_JSON;
                }
            }
            setContentTypeHeader(methodBuilder, contentType);
            if (parameterType.equals("io.clientcore.core.util.binarydata.BinaryData")) {
                methodBuilder
                    .addStatement("$T binaryData = ($T) $L", BinaryData.class, BinaryData.class, parameterName)
                    .beginControlFlow("if (binaryData.getLength() != null)")
                    .addStatement(
                        "httpRequest.getHeaders().set($T.CONTENT_LENGTH, String.valueOf(binaryData.getLength()))",
                        HttpHeaderName.class)
                    .addStatement("httpRequest.setBody(binaryData)")
                    .endControlFlow();
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
            updateRequestWithBodyContent(methodBuilder, isJson, parameterType, parameterName);
        }
    }

    private static void setContentTypeHeader(MethodSpec.Builder methodBuilder, String contentType) {
        switch (contentType) {
            case ContentType.APPLICATION_JSON:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    ClassName.get("io.clientcore.core.http.models", "HttpHeaderName"),
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_JSON");
                break;
            case ContentType.APPLICATION_OCTET_STREAM:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    ClassName.get("io.clientcore.core.http.models", "HttpHeaderName"),
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_OCTET_STREAM");
                break;
            case ContentType.APPLICATION_X_WWW_FORM_URLENCODED:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    ClassName.get("io.clientcore.core.http.models", "HttpHeaderName"),
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_X_WWW_FORM_URLENCODED");
                break;
            case ContentType.TEXT_EVENT_STREAM:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    ClassName.get("io.clientcore.core.http.models", "HttpHeaderName"),
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "TEXT_EVENT_STREAM");
                break;
        }
    }

    private void updateRequestWithBodyContent(MethodSpec.Builder methodBuilder, boolean isJson, String parameterType,
        String parameterName) {
        if (parameterType == null) {
            return;
        }
        if (isJson) {
            methodBuilder
                .addStatement("httpRequest.setBody($T.fromObject($L, serializer))", BinaryData.class, parameterName);
        } else if (parameterType.equals("byte[]")) {
            methodBuilder
                .addStatement("httpRequest.setBody($T.fromBytes((byte[]) $L))", BinaryData.class, parameterName);
        } else if (parameterType.equals("String")) {
            methodBuilder
                .addStatement("httpRequest.setBody($T.fromString((String) $L))", BinaryData.class, parameterName);
        } else if (parameterType.equals("ByteBuffer")) {
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
            methodBuilder
                .addStatement("httpRequest.setBody($T.fromBytes(((ByteBuffer) $L).array()))", BinaryData.class,
                    parameterName);

        } else {
            methodBuilder
                .addStatement("httpRequest.setBody($T.fromObject($L, serializer))", BinaryData.class, parameterName);
        }
    }

    /*
     * Get a TypeName for a parameterized type, given the raw type and type arguments as Class objects.
     */
    private static TypeName inferTypeNameFromReturnType(String typeString) {
        // Split the string into raw type and type arguments
        int angleBracketIndex = typeString.indexOf('<');
        if (angleBracketIndex == -1) {
            // No type arguments
            return ClassName.get("", typeString);
        }
        String rawTypeString = typeString.substring(0, angleBracketIndex);
        String typeArgumentsString = typeString.substring(angleBracketIndex + 1, typeString.length() - 1);

        // Get the Class objects for the raw type and type arguments
        Class<?> rawType;
        Class<?> typeArgument;
        try {
            rawType = Class.forName(rawTypeString);
            typeArgument = Class.forName(typeArgumentsString);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Use the inferTypeNameFromReturnType method to create a ParameterizedTypeName
        return getParameterizedTypeNameFromRawArguments(rawType, typeArgument);
    }

    /*
     * Get a TypeName for a parameterized type, given the raw type and type arguments as Class objects.
     */
    private static ParameterizedTypeName getParameterizedTypeNameFromRawArguments(Class<?> rawType,
        Class<?>... typeArguments) {
        ClassName rawTypeName = ClassName.get(rawType);
        TypeName[] typeArgumentNames = new TypeName[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            typeArgumentNames[i] = ClassName.get(typeArguments[i]);
        }
        return ParameterizedTypeName.get(rawTypeName, typeArgumentNames);
    }
}
