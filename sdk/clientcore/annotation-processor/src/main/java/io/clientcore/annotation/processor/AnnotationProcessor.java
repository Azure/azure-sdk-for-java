// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor;

import io.clientcore.annotation.processor.models.HttpRequestContext;
import io.clientcore.annotation.processor.models.Substitution;
import io.clientcore.annotation.processor.models.TemplateInput;
import io.clientcore.annotation.processor.templating.TemplateProcessor;
import io.clientcore.annotation.processor.utils.PathBuilder;
import io.clientcore.core.annotations.ServiceInterface;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.CoreUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Annotation processor that generates client code based on annotated interfaces.
 */
@SupportedAnnotationTypes("io.clientcore.core.annotations.*")
public class AnnotationProcessor extends AbstractProcessor {

    /**
     * Creates a new instance of the AnnotationProcessor.
     */
    public AnnotationProcessor() {
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Gather all elements to process
        Set<? extends Element> elementsToProcess = annotations.stream()
            .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
            .filter(element -> element.getKind().isInterface())
            .collect(Collectors.toSet());

        if (elementsToProcess.isEmpty()) {
            // No interfaces to process in this round; skip logging
            return false;
        }

        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE,
                "[Clientcore SDK AnnotationProcessor] Starting annotation processing for service interfaces.");

        for (Element element : elementsToProcess) {
            this.processServiceInterface(element);
        }

        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE,
                "[Clientcore SDK AnnotationProcessor] Completed annotation processing.");

        return true;
    }

    private void processServiceInterface(Element serviceInterface) {
        if (serviceInterface == null || serviceInterface.getKind() != ElementKind.INTERFACE) {
            throw new IllegalArgumentException("Invalid service interface provided.");
        }
        processingEnv.getMessager()
            .printMessage(Diagnostic.Kind.NOTE,
                "Generating client implementation for: " + serviceInterface.asType().toString());
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
    }

    private void addImports(TemplateInput templateInput) {
        templateInput.addImport(BinaryData.class.getName());
        templateInput.addImport(HttpPipeline.class.getName());
        templateInput.addImport(HttpHeaderName.class.getName());
        templateInput.addImport(HttpMethod.class.getName());
        templateInput.addImport(HttpRequest.class.getName());
        templateInput.addImport(Response.class.getName());
        templateInput.addImport(Void.class.getName());
        templateInput.addImport(UriEscapers.class.getTypeName());
    }

    private HttpRequestContext createHttpRequestContext(ExecutableElement requestMethod, TemplateInput templateInput) {
        HttpRequestContext method = new HttpRequestContext();
        method.setTemplateHasHost(!CoreUtils.isNullOrEmpty(templateInput.getHost()));
        method.setHost(templateInput.getHost());
        method.setMethodName(requestMethod.getSimpleName().toString());
        method.setIsConvenience(requestMethod.isDefault());

        // Extract @HttpRequestInformation annotation details
        final HttpRequestInformation httpRequestInfo = requestMethod.getAnnotation(HttpRequestInformation.class);
        method.setPath(httpRequestInfo.path());
        method.setHttpMethod(httpRequestInfo.method());
        method.setExpectedStatusCodes(httpRequestInfo.expectedStatusCodes());
        method.addStaticHeaders(httpRequestInfo.headers());
        method.addStaticQueryParams(httpRequestInfo.queryParams());
        TypeMirror returnValueWireType = null;
        try {
            // This will throw MirroredTypeException at compile time
            // The assignment to clazz is only there to trigger the exception and use the TypeMirror from the exception.
            Class<?> clazz = httpRequestInfo.returnValueWireType();
        } catch (MirroredTypeException mte) {
            TypeMirror typeMirror = mte.getTypeMirror();
            returnValueWireType = typeMirror;

        }
        method.setReturnValueWireType(returnValueWireType);
        templateInput.addImport(requestMethod.getReturnType());
        method.setMethodReturnType(requestMethod.getReturnType());
        List<UnexpectedResponseExceptionDetail> details = getUnexpectedResponseExceptionDetails(requestMethod);
        TypeMirror defaultExceptionBodyType = null;
        // For each detail, map statusCode -> exceptionBodyClass
        for (UnexpectedResponseExceptionDetail detail : details) {
            TypeMirror exceptionBodyType = null;
            boolean isDefaultObject = false;
            try {
                // This will throw MirroredTypeException at compile time
                // The assignment to clazz is only there to trigger the exception and use the TypeMirror from the exception.
                Class<?> clazz = detail.exceptionBodyClass();
            } catch (MirroredTypeException mte) {
                TypeMirror typeMirror = mte.getTypeMirror();
                String typeStr = typeMirror.toString();
                if ("void".equals(typeStr) || "java.lang.Void".equals(typeStr)) {
                    continue; // skip void types
                } else if ("java.lang.Object".equals(typeStr)) {
                    isDefaultObject = true;
                } else {
                    exceptionBodyType = typeMirror;
                }
            }
            HttpRequestContext.ExceptionBodyTypeInfo info
                = new HttpRequestContext.ExceptionBodyTypeInfo(exceptionBodyType, isDefaultObject);

            if (detail.statusCode().length == 0) {
                // This is the default for all unmapped status codes
                defaultExceptionBodyType = exceptionBodyType;
            } else {
                for (int code : detail.statusCode()) {
                    method.addExceptionBodyMapping(code, info);
                }
            }
        }
        // Store the default type in your context
        method.setDefaultExceptionBodyType(defaultExceptionBodyType);
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
                    new Substitution(hostParam.value(), param.getSimpleName().toString(), !hostParam.encoded()));
            } else if (pathParam != null) {
                if (pathParam.value() == null) {
                    throw new IllegalArgumentException(
                        "Path parameter '" + param.getSimpleName().toString() + "' must not be null.");
                }
                method.addSubstitution(
                    new Substitution(pathParam.value(), param.getSimpleName().toString(), !pathParam.encoded()));
            } else if (headerParam != null) {
                // Only add header param if the key is not already present (e.g., set by static header params)
                String key = headerParam.value();
                if (!method.getHeaders().containsKey(key)) {
                    method.addHeader(headerParam.value(), param.getSimpleName().toString());
                }
            } else if (queryParam != null) {
                // Only add query param if the key is not already present (e.g., set by static query params)
                String key = queryParam.value();
                if (!method.getQueryParams().containsKey(key)) {
                    method.addQueryParam(key, param.getSimpleName().toString(), queryParam.multipleQueryParams(),
                        !queryParam.encoded(), false);
                }
            } else if (bodyParam != null) {
                method.setBody(
                    new HttpRequestContext.Body(bodyParam.value(), param.asType(), param.getSimpleName().toString()));
            }

            // Add parameter details to method context
            String shortParamName = templateInput.addImport(param.asType());
            method.addParameter(new HttpRequestContext.MethodParameter(param.asType(), shortParamName,
                param.getSimpleName().toString(), param));
        }
        // Needed in PathBuilder
        templateInput.addImport(UriEscapers.class.getSimpleName());
        // Pre-compute host substitutions
        method.setHost(getHost(method));

        return method;
    }

    private String getHost(HttpRequestContext method) {
        String path = method.getPath();
        String hostPath = method.getHost();

        // If hostPath is set (from @ServiceInterface), always use it as the base.
        if (hostPath != null && !hostPath.isEmpty() && !"/".equals(hostPath)) {
            if (path == null || path.isEmpty() || "/".equals(path)) {
                // Path is "/" or empty, so append "/" to the host
                method.setPath(hostPath + "/");
            } else if (path.contains("://")) {
                // Path is a full URL, use as is
                method.setPath(path);
            } else if (path.startsWith("/")) {
                method.setPath(hostPath + path);
            } else {
                method.setPath(hostPath + "/" + path);
            }
        }
        // else: hostPath is empty, use the path as is
        return PathBuilder.buildPath(method.getPath(), method);
    }

    private List<UnexpectedResponseExceptionDetail>
        getUnexpectedResponseExceptionDetails(ExecutableElement requestMethod) {
        List<UnexpectedResponseExceptionDetail> details = new ArrayList<>();
        // Single annotation
        UnexpectedResponseExceptionDetail singleDetail
            = requestMethod.getAnnotation(UnexpectedResponseExceptionDetail.class);
        if (singleDetail != null) {
            details.add(singleDetail);
        }
        // Repeatable annotation
        io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetails multiDetail
            = requestMethod.getAnnotation(io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetails.class);
        if (multiDetail != null) {
            Collections.addAll(details, multiDetail.value());
        }
        return details;
    }
}
