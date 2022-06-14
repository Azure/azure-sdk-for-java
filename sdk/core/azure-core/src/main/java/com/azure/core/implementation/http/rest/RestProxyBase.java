package com.azure.core.implementation.http.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;

import java.lang.reflect.Method;
import java.util.EnumSet;
import java.util.function.Consumer;

public abstract class RestProxyBase {
    static final String MUST_IMPLEMENT_PAGE_ERROR =
        "Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class;

    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final SerializerAdapter serializer;
    final SwaggerInterfaceParser interfaceParser;
    final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline    the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer      the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     *                        this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger interface that this
     * RestProxy was created to "implement".
     *
     * @param method the method to get a SwaggerMethodParser for
     * @return the SwaggerMethodParser for the provided method
     */
    public SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    public abstract Object invoke(Object proxy, final Method method, RequestOptions options, EnumSet<ErrorOptions> errorOptions,
                                  Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser, HttpRequest request,
                                  Context context);
}
