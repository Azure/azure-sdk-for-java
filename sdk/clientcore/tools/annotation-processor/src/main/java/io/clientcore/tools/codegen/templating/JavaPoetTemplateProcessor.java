// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.codegen.templating;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
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
import io.clientcore.tools.codegen.models.HttpRequestContext;
import io.clientcore.tools.codegen.models.TemplateInput;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static io.clientcore.tools.codegen.utils.ResponseBodyModeGeneration.generateResponseHandling;

/**
 * This class generates the implementation of the service interface.
 */
public class JavaPoetTemplateProcessor implements TemplateProcessor {
    private static final ClassName HTTP_HEADER_NAME = ClassName.bestGuess(HttpHeaderName.class.getName());
    private static final ClassName HTTP_HEADERS = ClassName.bestGuess(HttpHeaders.class.getName());
    private static final ClassName CONTENT_TYPE = ClassName.bestGuess(ContentType.class.getName());

    private final ClassName HTTP_REQUEST = ClassName.bestGuess(HttpRequest.class.getName());
    private final ClassName RESPONSE = ClassName.bestGuess(Response.class.getName());
    private final ClassName HTTP_METHOD = ClassName.bestGuess(HttpMethod.class.getName());

    private TypeSpec.Builder classBuilder;

    private final ClassName HTTP_PIPELINE = ClassName.bestGuess(HttpPipeline.class.getName());
    private static ClassName SERVICE_VERSION_TYPE;
    private final ClassName CLIENT_LOGGER_NAME = ClassName.bestGuess(ClientLogger.class.getName());
    private ClassName OBJECT_SERIALIZER = ClassName.bestGuess(ObjectSerializer.class.getName());

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
        //FieldSpec endpoint = FieldSpec.builder(String.class, "endpoint", Modifier.PRIVATE, Modifier.FINAL)
        //    .build();

        // Create the serviceVersion field
        String serviceVersionPackageName = packageName.substring(0, packageName.lastIndexOf("."));
        SERVICE_VERSION_TYPE = ClassName.get(serviceVersionPackageName, serviceInterfaceShortName.substring(0, serviceInterfaceShortName.indexOf("ClientService")) + "ServiceVersion");
        FieldSpec serviceVersion =
            FieldSpec.builder(SERVICE_VERSION_TYPE, "serviceVersion", Modifier.PRIVATE).build();

        // Create the constructor
        MethodSpec constructor = getServiceImplConstructor();

        FieldSpec apiVersion = FieldSpec.builder(String.class, "apiVersion")
            .addModifiers(Modifier.PRIVATE)
            .build();

        // Add instance field
        FieldSpec instanceField = FieldSpec.builder(ClassName.get(packageName, serviceInterfaceImplShortName),
            "instance", Modifier.PRIVATE, Modifier.STATIC).build();

        // Add the static getInstance method
        MethodSpec getInstanceMethod = MethodSpec.methodBuilder("getInstance")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
            .returns(ClassName.get(packageName, serviceInterfaceImplShortName))
            .addParameter(HTTP_PIPELINE, "pipeline")
            .addParameter(OBJECT_SERIALIZER, "serializer")
            .addParameter(SERVICE_VERSION_TYPE, "serviceVersion")
            .beginControlFlow("if (instance == null)")
            .addStatement("instance = new $T(pipeline, serializer, serviceVersion)", ClassName.get(packageName, serviceInterfaceImplShortName))
            .endControlFlow()
            .addStatement("return instance")
            .build();

        // Add reset instance method
        MethodSpec resetInstanceMethod = MethodSpec.methodBuilder("reset")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
            .addStatement("instance = null")
            .build();

        classBuilder = TypeSpec.classBuilder(serviceInterfaceImplShortName)
            .addModifiers(Modifier.PUBLIC)
            .addSuperinterface(interfaceType)
            .addField(loggerField)
            .addField(defaultPipeline)
            .addField(serializer)
            .addField(serviceVersion)
            .addField(apiVersion)
            .addMethod(getPipelineMethod())
            .addMethod(getServiceVersionMethod())
            .addMethod(constructor)
            .addField(instanceField)
            .addMethod(getInstanceMethod)
            .addMethod(resetInstanceMethod);

        getGeneratedServiceMethods(templateInput);

        TypeSpec typeSpec = classBuilder.build();

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
            boolean generateInternalOnly = method.getParameters().isEmpty() || method.getParameters().stream()
                .anyMatch(parameter -> !(parameter.getName().equals("endpoint") || parameter.getName().equals("apiVersion")));

            if (generateInternalOnly) {
                generateInternalMethod(method); // Generate the internal method
            } else {
                classBuilder.addMethod(generatePublicMethod(method));
                generateInternalMethod(method);
            }
        }
    }

    FieldSpec getLoggerField(String packageName, String serviceInterfaceShortName) {
        return FieldSpec.builder(CLIENT_LOGGER_NAME, "LOGGER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
            .initializer("new $T($T.class)", CLIENT_LOGGER_NAME, ClassName.get(packageName, serviceInterfaceShortName))
            .build();
    }

    MethodSpec getServiceImplConstructor() {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(HTTP_PIPELINE, "defaultPipeline")
            .addStatement("this.defaultPipeline = defaultPipeline")
            .addParameter(OBJECT_SERIALIZER, "serializer")
            .addParameter(SERVICE_VERSION_TYPE, "serviceVersion")
            .addStatement("this.serializer = serializer == null ? new $T() : serializer", JsonSerializer.class)
            .addStatement("this.serviceVersion = serviceVersion == null ? $T.getLatest() : serviceVersion", SERVICE_VERSION_TYPE)
            .addStatement("this.apiVersion = this.serviceVersion.getVersion()")
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
        MethodSpec.Builder methodBuilder = createMethodBuilder(method, returnTypeName);

        addMethodParameters(methodBuilder, method);
        initializeHttpRequest(methodBuilder, method);
        addHeadersToRequest(methodBuilder, method);
        addRequestBody(methodBuilder, method);

        finalizeHttpRequest(methodBuilder, returnTypeName, method);

        classBuilder.addMethod(methodBuilder.build());
    }

    // Helper methods
    private MethodSpec.Builder createMethodBuilder(HttpRequestContext method, TypeName returnTypeName) {
        return MethodSpec.methodBuilder(method.getMethodName())
            .addModifiers(Modifier.PUBLIC)
            .returns(returnTypeName);
    }

    private void addMethodParameters(MethodSpec.Builder methodBuilder, HttpRequestContext method) {
        for (HttpRequestContext.MethodParameter parameter : method.getParameters()) {
            methodBuilder.addParameter(TypeName.get(parameter.getTypeMirror()), parameter.getName());
        }
        methodBuilder.addStatement("HttpPipeline pipeline = this.getPipeline()");
    }

    private void initializeHttpRequest(MethodSpec.Builder methodBuilder, HttpRequestContext method) {
        methodBuilder.addStatement("String host = $L", method.getHost())
            .addCode("\n")
            .addComment("Create the HTTP request")
            .addStatement("$T httpRequest = new $T($T.$L, host)", HTTP_REQUEST, HTTP_REQUEST, HTTP_METHOD, method.getHttpMethod());
    }

    private void addHeadersToRequest(MethodSpec.Builder methodBuilder, HttpRequestContext method) {
        if (!method.getHeaders().isEmpty()) {
            methodBuilder
                .addCode("\n")
                .addComment("set the headers")
                .addStatement("$T headers = new $T()", HTTP_HEADERS, HTTP_HEADERS);
            for (Map.Entry<String, String> header : method.getHeaders().entrySet()) {
                String enumHeaderKey = header.getKey().toUpperCase().replace("-", "_");
                boolean isEnumExists = false;
                for (HttpHeaderName httpHeaderName : HttpHeaderName.values()) {
                    if (httpHeaderName.getCaseInsensitiveName().equals(header.getKey().toLowerCase())) {
                        isEnumExists = true;
                        break;
                    }
                }

                boolean isStringType = method.getParameters().stream()
                    .anyMatch(parameter -> parameter.getName().equals(header.getValue()) && TypeName.get(parameter.getTypeMirror()).equals(TypeName.get(String.class)));
                String value = isStringType ? header.getValue() : "String.valueOf(" + header.getValue() + ")";

                if (isEnumExists) {
                    methodBuilder.addStatement("headers.add($T.$L, $L)", HTTP_HEADER_NAME, enumHeaderKey, value);
                } else {
                    methodBuilder.addStatement("headers.add($T.fromString($S), $L)", HTTP_HEADER_NAME, header.getKey(), value);
                }
            }
            methodBuilder.addStatement("httpRequest.setHeaders(headers)");
        }
    }

    private void addRequestBody(MethodSpec.Builder methodBuilder, HttpRequestContext method) {
        methodBuilder.addCode("\n").addComment("Set the request body");
        HttpRequestContext.Body body = method.getBody();
        boolean isContentTypeSetInHeaders = method.getParameters().stream()
            .anyMatch(parameter -> parameter.getName().equals("contentType"));

        if (body != null) {
            configureRequestWithBodyAndContentType(methodBuilder, body.getParameterType(), body.getContentType(),
                body.getParameterName(), isContentTypeSetInHeaders);
        } else {
            methodBuilder.addStatement("httpRequest.getHeaders().set($T.CONTENT_LENGTH, $S)", HttpHeaderName.class, "0");
        }
    }

    private void finalizeHttpRequest(MethodSpec.Builder methodBuilder, TypeName returnTypeName, HttpRequestContext method) {
        methodBuilder.addCode("\n").addComment("Send the request through the pipeline")
            .addStatement("$T<?> response = pipeline.send(httpRequest)", RESPONSE);

        if (!method.getExpectedStatusCodes().isEmpty()) {
            validateResponseStatus(methodBuilder, method);
        }

        // requestOptions is not used in the generated code for RestProxyTests
        generateResponseHandling(methodBuilder, returnTypeName, false);
    }

    private void validateResponseStatus(MethodSpec.Builder methodBuilder, HttpRequestContext method) {
        if (!method.getExpectedStatusCodes().isEmpty()) {
            methodBuilder
                .addCode("\n")
                .addStatement("int responseCode = response.getStatusCode()");
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
    }

    public void configureRequestWithBodyAndContentType(MethodSpec.Builder methodBuilder, String parameterType, String contentType, String parameterName, boolean isContentTypeSetInHeaders) {
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
            // Set the content type header if it is not already set in the headers
            if (!isContentTypeSetInHeaders) {
                setContentTypeHeader(methodBuilder, contentType);
            }
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
                    HTTP_HEADER_NAME,
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_JSON");
                break;
            case ContentType.APPLICATION_OCTET_STREAM:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    HTTP_HEADER_NAME,
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_OCTET_STREAM");
                break;
            case ContentType.APPLICATION_X_WWW_FORM_URLENCODED:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    HTTP_HEADER_NAME,
                    "CONTENT_TYPE",
                    CONTENT_TYPE,
                    "APPLICATION_X_WWW_FORM_URLENCODED");
                break;
            case ContentType.TEXT_EVENT_STREAM:
                methodBuilder.addStatement("httpRequest.getHeaders().set($T.$L, $T.$L)",
                    HTTP_HEADER_NAME,
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
        if (typeString == null) {
            return TypeName.VOID;
        }
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
