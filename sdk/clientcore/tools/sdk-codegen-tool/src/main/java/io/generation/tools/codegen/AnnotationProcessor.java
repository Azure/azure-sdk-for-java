package io.generation.tools.codegen;

import io.generation.tools.codegen.models.HttpRequestContext;
import io.generation.tools.codegen.models.Substitution;
import io.generation.tools.codegen.models.TemplateInput;
import io.generation.tools.codegen.templating.TemplateProcessor;
import io.generation.tools.codegen.utils.PathBuilder;
import io.clientcore.core.annotation.ServiceInterface;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.HeaderParam;
import io.clientcore.core.http.annotation.HostParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.annotation.PathParam;
import io.clientcore.core.http.annotation.QueryParam;

import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.clientcore.core.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AnnotationProcessor extends AbstractProcessor {

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
        TemplateInput templateInput = new TemplateInput();

        // work out some global details about this service interface
        final String serviceInterfaceFQN = serviceInterface.asType().toString();

        String packageName = null;
        int lastDot = serviceInterfaceFQN.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = serviceInterfaceFQN.substring(0, lastDot);
        }

        final String serviceInterfaceShortName = serviceInterfaceFQN.substring(lastDot + 1);
        final String serviceInterfaceImplFQN = serviceInterfaceFQN + "Impl";
        final String serviceInterfaceImplShortName = serviceInterfaceImplFQN.substring(lastDot + 1);

        templateInput.setPackageName(packageName);
        templateInput.setServiceInterfaceFQN(serviceInterfaceFQN);
        templateInput.setServiceInterfaceShortName(serviceInterfaceShortName);
        templateInput.setServiceInterfaceImplShortName(serviceInterfaceImplShortName);

        // Read ServiceInterface.host() value from any enclosed interfaces
        if (serviceInterface.getAnnotation(ServiceInterface.class) != null) {
            templateInput.setHost(serviceInterface.getAnnotation(ServiceInterface.class).host());
        }

        // add all imports
        addImports(templateInput);

        // iterate through all methods in the interface, collecting all that are @HttpRequestInformation
        // and then generating a method implementation for each one
        templateInput.setHttpRequestContexts(serviceInterface.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .filter(element -> element.getAnnotation(HttpRequestInformation.class) != null)
                .map(ExecutableElement.class::cast)
                .map(e -> createHttpRequestContext(e, templateInput))
                .collect(Collectors.toList()));

        // template input set UnexpectedResponseExceptionDetails
        templateInput.setUnexpectedResponseExceptionDetails(serviceInterface.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .filter(element -> element.getAnnotation(HttpRequestInformation.class) != null)
                .map(ExecutableElement.class::cast)
                .map(e -> e.getAnnotation(UnexpectedResponseExceptionDetail.class))
                .collect(Collectors.toList()));

        TemplateProcessor.getInstance().process(templateInput, processingEnv);

        // format the generated code
    }

    private void addImports(TemplateInput templateInput) {
        templateInput.addImport("io.clientcore.core.util.Context");
        templateInput.addImport("io.clientcore.core.util.binarydata.BinaryData");
        templateInput.addImport("io.clientcore.core.http.models.HttpHeaders");
        templateInput.addImport("io.clientcore.core.http.pipeline.HttpPipeline");
        templateInput.addImport("io.clientcore.core.http.models.HttpHeaderName");
        templateInput.addImport("io.clientcore.core.http.models.HttpMethod");
        templateInput.addImport("io.clientcore.core.http.models.HttpResponse");
        templateInput.addImport("io.clientcore.core.http.models.HttpRequest");
        templateInput.addImport("io.clientcore.core.http.models.Response");
        templateInput.addImport("java.util.Map");
        templateInput.addImport("java.util.HashMap");
        templateInput.addImport("java.util.Arrays");
        templateInput.addImport("java.lang.Void");
        templateInput.addImport("java.util.List");
    }

    private HttpRequestContext createHttpRequestContext(ExecutableElement requestMethod, TemplateInput templateInput) {
        HttpRequestContext method = new HttpRequestContext();
        method.setHost(templateInput.getHost());
        method.setMethodName(requestMethod.getSimpleName().toString());

        final HttpRequestInformation httpRequestInfo = requestMethod.getAnnotation(HttpRequestInformation.class);
        method.setPath(httpRequestInfo.path());
        method.setHttpMethod(httpRequestInfo.method());
        method.setExpectedStatusCodes(httpRequestInfo.expectedStatusCodes());

        templateInput.addImport(requestMethod.getReturnType());
        method.setMethodReturnType(requestMethod.getReturnType().toString());
        requestMethod.getParameters().forEach(param -> {
            HostParam hostParam = param.getAnnotation(HostParam.class);
            PathParam pathParam = param.getAnnotation(PathParam.class);
            HeaderParam headerParam = param.getAnnotation(HeaderParam.class);
            QueryParam queryParam = param.getAnnotation(QueryParam.class);
            BodyParam bodyParam = param.getAnnotation(BodyParam.class);

            // check if the parameter has an annotation, and if so, add it to the appropriate list
            if (hostParam != null) {
                // a HostParam is a substitution into the global @Host value. Depending on the @Host value,
                // there may be zero or more {} delimited substitutions, so we need to handle this
                // appropriately
                method.addSubstitution(new Substitution(
                        hostParam.value(),
                        param.getSimpleName().toString(),
                        hostParam.encoded()));
            } else if (pathParam != null) {
                // a PathParam is a substitution into the path value. Depending on the path value,
                // there may be zero or more {} delimited substitutions, so we need to handle this
                // appropriately
                method.addSubstitution(new Substitution(
                        pathParam.value(),
                        param.getSimpleName().toString(),
                        pathParam.encoded()));
            } else if (headerParam != null) {
                method.addHeader(headerParam.value(), param.getSimpleName().toString());
            } else if (queryParam != null) {
                // we do not support query param substitutions, so we just add the query param name and value
                method.addQueryParam(queryParam.value(), param.getSimpleName().toString());
            } else if (false) {
                // TODO support FormParam
            } else if (bodyParam != null) {
                // This is the content type as specified in the @BodyParam annotation
                String contentType = bodyParam.value();

                // This is the type of the parameter that has been annotated with @BodyParam.
                // This is used to determine which setBody method to call on HttpRequest.
                String parameterType = param.asType().toString();

                // This is the parameter name, so we can refer to it when setting the body on the HttpRequest.
                String parameterName = param.getSimpleName().toString();

                method.setBody(new HttpRequestContext.Body(contentType, parameterType, parameterName));
            }

            String shortImportName = templateInput.addImport(param.asType());
            method.addParameter(new HttpRequestContext.MethodParameter(param.asType(), shortImportName, param.getSimpleName().toString()));
        });

        // we can reduce the amount of regex matching on the host by doing as many substitutions as possible here
        String rawHost = getHost(templateInput, method);
        method.setHost(rawHost);

        return method;
    }

    private static String getHost(TemplateInput templateInput, HttpRequestContext method) {
        String rawHost = templateInput.getHost() + method.getPath();

        return PathBuilder.buildPath(rawHost, method);
    }
}
