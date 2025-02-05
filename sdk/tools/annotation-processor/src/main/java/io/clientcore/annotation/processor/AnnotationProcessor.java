// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.Substitution;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.annotation.processor.templating.TemplateProcessor;
import io.clientcore.annotation.processor.utils.PathBuilder;
import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.HeaderParam;
import io.clientcore.core.http.annotation.HostParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.annotation.PathParam;
import io.clientcore.core.http.annotation.QueryParam;
import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.util.Context;
import io.clientcore.core.util.binarydata.BinaryData;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Annotation processor that generates client code based on annotated interfaces.
 */
@SupportedAnnotationTypes("io.clientcore.core.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

    /**
     * Creates a new instance of the AnnotationProcessor.
     */
    public AnnotationProcessor() {
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Reflective fallback if SourceVersion.RELEASE_8 isn't available at compile time
        try {
            return SourceVersion.valueOf("RELEASE_8");
        } catch (IllegalArgumentException e) {
            // Fallback to the latest supported version
            return SourceVersion.latest();
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // We iterate through each interface annotated with @ServiceInterface separately.
        // This outer for-loop is not strictly necessary, as we only have one annotation that we care about
        // (@ServiceInterface), but we'll leave it here for now
        annotations.stream()
            .map(roundEnv::getElementsAnnotatedWith)
            .flatMap(Set::stream)
            .filter(element -> element.getKind().isInterface())
            .forEach(element -> {
                if (element.getAnnotation(ServiceInterface.class) != null) {
                    this.processServiceInterface(element);
                }
            });

        return true;
    }

    private void processServiceInterface(Element serviceInterface) {
        if (serviceInterface == null || serviceInterface.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Invalid service interface provided.");
        }

        TemplateInput templateInput = new TemplateInput();

        // Determine the fully qualified name (FQN) and package name
        final String serviceInterfaceFQN = serviceInterface.asType().toString();
        int lastDot = serviceInterfaceFQN.lastIndexOf('.');
        String packageName = (lastDot > 0) ? serviceInterfaceFQN.substring(0, lastDot) : "default.generated";

        final String serviceInterfaceShortName = serviceInterfaceFQN.substring(lastDot + 1);
        final String serviceInterfaceImplFQN = serviceInterfaceFQN + "Impl";
        final String serviceInterfaceImplShortName = serviceInterfaceImplFQN.substring(lastDot + 1);

        templateInput.setPackageName(packageName);
        templateInput.setServiceInterfaceFQN(serviceInterfaceFQN);
        templateInput.setServiceInterfaceShortName(serviceInterfaceShortName);
        templateInput.setServiceInterfaceImplShortName(serviceInterfaceImplShortName);

        // Read the ServiceInterface.host() value from annotations
        ServiceInterface annotation = serviceInterface.getAnnotation(ServiceInterface.class);
        if (annotation != null && annotation.host() != null) {
            templateInput.setHost(annotation.host());
        }

        // Add all required imports
        addImports(templateInput);

        // Collect methods annotated with @HttpRequestInformation
        List<ExecutableElement> httpRequestMethods = serviceInterface.getEnclosedElements()
            .stream()
            .filter(element -> element.getKind() == ElementKind.METHOD)
            .filter(element -> element.getAnnotation(HttpRequestInformation.class) != null)
            .map(ExecutableElement.class::cast)
            .collect(Collectors.toList());

        // Generate HTTP request contexts
        templateInput.setHttpRequestContexts(httpRequestMethods.stream()
            .map(e -> createHttpRequestContext(e, templateInput))
            .filter(Objects::nonNull) // Exclude null contexts
            .collect(Collectors.toList()));

        // Set UnexpectedResponseExceptionDetails
        templateInput.setUnexpectedResponseExceptionDetails(httpRequestMethods.stream()
            .map(e -> e.getAnnotation(UnexpectedResponseExceptionDetail.class))
            .filter(Objects::nonNull) // Exclude null annotations
            .collect(Collectors.toList()));

        // Process the template
        TemplateProcessor.getInstance().process(templateInput, processingEnv);

        // Additional formatting or logging if necessary
    }

    private void addImports(TemplateInput templateInput) {
        templateInput.addImport(Context.class.getName());
        templateInput.addImport(BinaryData.class.getName());
        templateInput.addImport(HttpHeaders.class.getName());
        templateInput.addImport(HttpPipeline.class.getName());
        templateInput.addImport(HttpHeaderName.class.getName());
        templateInput.addImport(HttpMethod.class.getName());
        templateInput.addImport(HttpResponse.class.getName());
        templateInput.addImport(HttpRequest.class.getName());
        templateInput.addImport(Response.class.getName());
        templateInput.addImport(Map.class.getName());
        templateInput.addImport(HashMap.class.getName());
        templateInput.addImport(Arrays.class.getName());
        templateInput.addImport(Void.class.getName());
        templateInput.addImport(List.class.getName());
    }

    private HttpRequestContext createHttpRequestContext(ExecutableElement requestMethod, TemplateInput templateInput) {
        HttpRequestContext method = new HttpRequestContext();
        method.setHost(templateInput.getHost());
        method.setMethodName(requestMethod.getSimpleName().toString());

        // Extract @HttpRequestInformation annotation details
        final HttpRequestInformation httpRequestInfo = requestMethod.getAnnotation(HttpRequestInformation.class);
        method.setPath(httpRequestInfo.path());
        method.setHttpMethod(httpRequestInfo.method());
        method.setExpectedStatusCodes(httpRequestInfo.expectedStatusCodes());

        // Add return type as an import
        setReturnTypeFormMethod(method, requestMethod, templateInput);
        boolean isEncoded = false;
        // Process parameters
        for (VariableElement param : requestMethod.getParameters()) {
            // Cache annotations for each parameter
            HostParam hostParam = param.getAnnotation(HostParam.class);
            PathParam pathParam = param.getAnnotation(PathParam.class);
            HeaderParam headerParam = param.getAnnotation(HeaderParam.class);
            QueryParam queryParam = param.getAnnotation(QueryParam.class);
            BodyParam bodyParam = param.getAnnotation(BodyParam.class);

            // Switch based on annotations
            if (hostParam != null) {
                method.addSubstitution(
                    new Substitution(hostParam.value(), param.getSimpleName().toString(), hostParam.encoded()));
            } else if (pathParam != null) {
                if (pathParam.encoded()) {
                    isEncoded = true;
                }
                method.addSubstitution(
                    new Substitution(pathParam.value(), param.getSimpleName().toString(), pathParam.encoded()));
            } else if (headerParam != null) {
                method.addHeader(headerParam.value(), param.getSimpleName().toString());
            } else if (queryParam != null) {
                method.addQueryParam(queryParam.value(), param.getSimpleName().toString());
                // TODO: Add support for multipleQueryParams and encoded handling
            } else if (bodyParam != null) {
                method.setBody(new HttpRequestContext.Body(bodyParam.value(), param.asType().toString(),
                    param.getSimpleName().toString()));
            }

            // Add parameter details to method context
            String shortParamName = templateInput.addImport(param.asType());
            method.addParameter(new HttpRequestContext.MethodParameter(param.asType(), shortParamName,
                param.getSimpleName().toString()));
        }

        // Pre-compute host substitutions
        method.setHost(getHost(templateInput, method, isEncoded));

        return method;
    }

    private void setReturnTypeFormMethod(HttpRequestContext method, ExecutableElement requestMethod,
        TemplateInput templateInput) {
        // Get the return type from the method
        TypeMirror returnType = requestMethod.getReturnType();

        // If the return type is a declared type (e.g., Response<InputStream>)
        if (returnType.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) returnType;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            String fullTypeName = typeElement.getQualifiedName().toString();

            // Handle generic arguments for declared types
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (!typeArguments.isEmpty()) {
                StringBuilder typeWithArguments = new StringBuilder(fullTypeName);
                typeWithArguments.append("<");

                for (int i = 0; i < typeArguments.size(); i++) {
                    TypeMirror typeArgument = typeArguments.get(i);
                    // Add the type argument to the final type string
                    typeWithArguments.append(typeArgument.toString());
                    if (i < typeArguments.size() - 1) {
                        typeWithArguments.append(", ");
                    }
                }

                typeWithArguments.append(">");
                method.setMethodReturnType(typeWithArguments.toString());
            } else {
                // If no generic arguments, set the return type to the base type
                method.setMethodReturnType(fullTypeName);
            }
        } else {
            // For non-declared types (simple types like String, int, etc.)
            String returnTypeShortName = templateInput.addImport(requestMethod.getReturnType());
            method.setMethodReturnType(returnTypeShortName);
        }

    }

    private static String getHost(TemplateInput templateInput, HttpRequestContext method, boolean isEncoded) {
        String rawHost;
        if (isEncoded) {
            rawHost = method.getPath();
        } else {
            String host = templateInput.getHost();
            String path = method.getPath();
            if (!host.endsWith("/") && !path.startsWith("/")) {
                rawHost = host + "/" + path;
            } else {
                rawHost = host + path;
            }
        }
        return PathBuilder.buildPath(rawHost, method);
    }
}
