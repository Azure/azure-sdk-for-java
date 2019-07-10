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

    // alternate approach with no overloading - works
    public static <T> Mono<T> callWithContextGetSingle(Function<Context, Mono<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext)
            .flatMap(serviceCall::apply);
    }

    // alternate approach with no overloading - works
    public static <T> Flux<T> callWithContextGetCollection(Function<Context, Flux<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext)
            .flatMapMany(serviceCall::apply);
    }

    // Does not work - caller (clientlibrary API) has to block to get ResponseHolder which contains Flux or Mono response
    // that will be returned to the consumer
    public static <T> Mono<ResponseHolder<T>> callWithContext(Function<Context, Publisher<T>> serviceCall) {
        return Mono.subscriberContext()
            .map(ServiceHelper::toAzureContext)
            .map(context -> getResponse(context, serviceCall));
    }

    // Does not work - context is empty and calls block
    public static <T> ResponseHolder<T> callWithContextBlock(
        Function<Context, Publisher<T>> serviceCall) {
        // Same issue, calling block (is bad) will result in empty context
        Context context = Mono.subscriberContext().map(ServiceHelper::toAzureContext).block();
        return getResponse(context, serviceCall);
    }

    private static <T> ResponseHolder<T> getResponse(Context context, Function<Context, Publisher<T>> serviceCall) {
        Publisher<T> publisher = serviceCall.apply(context);
        ResponseHolder response = new ResponseHolder();
        if (publisher instanceof Mono) {
            response.single((Mono) publisher);
        } else if (publisher instanceof Flux) {
            response.collection((Flux) publisher);
        }
        return response;
    }

    private static Context toAzureContext(reactor.util.context.Context context) {
        Map<Object, Object> keyValues = context.stream()
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        if (ImplUtils.isNullOrEmpty(keyValues)) {
            return Context.NONE;
        }
        return Context.of(keyValues);
    }

//    Does not compile
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
}
