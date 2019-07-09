package com.azure.core.implementation.service;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Utility class to support service
 */
public final class ServiceHelper {

    // single method that returns mono
    static Mono<Context> withContext() {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext);
    }

    // method that returns mono
    public static <T> Mono<T> callWithContextGetSingle(Function<Context, Mono<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext)
            .flatMap(serviceCall::apply);
    }

    // method that returns Flux. If we need to return PagedFlux, that will be another method
    public static <T> Flux<T> callWithContextGetCollection(Function<Context, Flux<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext)
            .flatMapMany(serviceCall::apply);
    }

    // single method to call with context and return type is a wrapper containing flux and mono types
    public static <T> Mono<ResponseHolder<T>> callWithContext(Function<Context, Publisher<T>> serviceCall) {
        Mono<ResponseHolder<T>> result = withContext()
            .map(context -> getResponseHolder(context, serviceCall));
        return result;

//        Context context = withContext().block(); // This is empty
//        Publisher<T> publisher = withContext().flatMapMany(serviceCall::apply); // this doesn't work as this will always return a Flux
//        return getResponseHolder(context, serviceCall);
    }

    public static <T> ResponseHolder<T> getResponseHolder(Context context, Function<Context, Publisher<T>> serviceCall) {
        Publisher<T> publisher = serviceCall.apply(context);
        ResponseHolder response = new ResponseHolder();
        if (publisher instanceof Mono) {
            response.single((Mono) publisher);
        } else if (publisher instanceof Flux) {
            response.collection((Flux) publisher);
        }
        return response;
    }

//    public static <T, S extends Publisher<T>> S callWithContext(Function<Context, S> serviceCall) {
//        Mono<Context> context = Mono.subscriberContext()
//            .map(ServiceHelper::toAzureContext);
//        S type = serviceCall.apply(context.block());
//        if (type instanceof Flux) {
//            return (Flux) type;
//        }
//
//        if (type instanceof Mono) {
//            return (Mono) type;
//        }
//        return type;
//    }

    private static Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        if (ImplUtils.isNullOrEmpty(keyValues)) {
            return Context.NONE;
        }
        return Context.of(keyValues);
    }
}
