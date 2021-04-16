// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.implementation.ImplUtils;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class HttpClientServiceVersionAugmentedArgumentsProvider
    implements ArgumentsProvider, AnnotationConsumer<HttpClientServiceVersionAugmentedSource> {
    private static final TestMode TEST_MODE = ImplUtils.getTestMode();

    private static final Map<Class<? extends ServiceVersion>, ServiceVersion> CLASS_TO_LATEST_SERVICE_VERSION
        = new ConcurrentHashMap<>();

    private static final Map<Class<? extends ServiceVersion>, Map<String, ServiceVersion>>
        CLASS_TO_MAP_STRING_SERVICE_VERSION = new ConcurrentHashMap<>();

    private static final String MUST_BE_STATIC = "Source supplier method is required to be static. Method: %s.";

    private static final String MUST_BE_STREAM_ARGUMENTS =
        "Source supplier method is required to return Stream<Arguments>. Return type: %s.";

    private String sourceSupplier;
    private boolean noSourceSupplier;

    private String[] serviceVersions;
    private boolean useAllServiceVersions;
    private boolean useLatestServiceVersionOnly;
    private Class<? extends ServiceVersion> serviceVersionType;

    private boolean ignoreHttpClients;
    private boolean usePlaybackClient;

    @Override
    public void accept(HttpClientServiceVersionAugmentedSource annotation) {
        this.sourceSupplier = annotation.sourceSupplier();
        this.noSourceSupplier = CoreUtils.isNullOrEmpty(sourceSupplier);

        this.serviceVersions = annotation.serviceVersions();
        this.useAllServiceVersions = annotation.useAllServiceVersions();
        this.useLatestServiceVersionOnly = CoreUtils.isNullOrEmpty(serviceVersions) || TEST_MODE == TestMode.PLAYBACK;
        this.serviceVersionType = annotation.serviceVersionType();

        this.ignoreHttpClients = annotation.ignoreHttpClients();
        this.usePlaybackClient = TEST_MODE == TestMode.PLAYBACK;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        // If the TEST_MODE is PLAYBACK or HttpClients are being ignored don't use HttpClients.
        List<HttpClient> httpClientsToUse = Collections.singletonList(null);
        if (!ignoreHttpClients && !usePlaybackClient) {
            httpClientsToUse = TestBase.getHttpClients().collect(Collectors.toList());
        }

        List<? extends ServiceVersion> serviceVersionsToUse = getServiceVersions(serviceVersions, useAllServiceVersions,
            useLatestServiceVersionOnly, serviceVersionType);

        // If the sourceSupplier isn't provided don't retrieve parameterized testing values.
        List<Arguments> testValues = null;
        if (!noSourceSupplier) {
            Object source = invokeSupplierMethod(context, sourceSupplier);
            testValues = convertSupplierSourceToArguments(source);
        }

        /*
         * Create a stream of arguments for the test using the following logic.
         *
         * 1) HTTP clients are being ignored and there is no source supplier.
         *    - Use only service versions as the test permutation.
         *
         * 2) HTTP clients are being ignored.
         *    - Use a permutation of service versions X parameterized testing values.
         *
         * 3) There is no source supplier.
         *    - Use a permutation of HTTP client X service versions.
         *
         * 4) There are no HTTP clients.
         *    - Use a permutation of null HTTP client X service versions X parameterized testing values.
         *
         * 5) HTTP clients exist.
         *    - Use a permutation of HTTP client X service versions X parameterized testing values.
         */
        if (ignoreHttpClients && noSourceSupplier) {
            return serviceVersionsToUse.stream().map(Arguments::arguments);
        } else if (ignoreHttpClients) {
            return createNonHttpPermutations(serviceVersionsToUse, testValues).stream();
        } else if (noSourceSupplier) {
            return createHttpServiceVersionPermutations(httpClientsToUse, serviceVersionsToUse).stream();
        } else if (CoreUtils.isNullOrEmpty(httpClientsToUse)) {
            return createFullPermutations(Collections.singletonList(null), serviceVersionsToUse, testValues).stream();
        } else {
            return createFullPermutations(httpClientsToUse, serviceVersionsToUse, testValues).stream();
        }
    }

    /*
     * Get the ServiceVersions to use in testing.
     *
     * This uses reflection to convert the string values into their enum representation. If the string set is null
     * 'getLatest' will be the only value returned.
     */
    static List<? extends ServiceVersion> getServiceVersions(String[] serviceVersionStrings,
        boolean useAllServiceVersions, boolean useLatestServiceVersionOnly,
        Class<? extends ServiceVersion> serviceVersionType) throws ReflectiveOperationException {

        if (useLatestServiceVersionOnly || (CoreUtils.isNullOrEmpty(serviceVersionStrings) && !useAllServiceVersions)) {
            return Collections.singletonList(CLASS_TO_LATEST_SERVICE_VERSION.computeIfAbsent(serviceVersionType,
                type -> {
                    try {
                        return (ServiceVersion) serviceVersionType.getMethod("getLatest").invoke(serviceVersionType);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new IllegalStateException("ServiceVersion class doesn't have a getLatest method. Class: "
                            + serviceVersionType);
                    }
                }));
        }

        // Assumption that the service version type is an enum.
        Method values = serviceVersionType.getMethod("values");
        ServiceVersion[] serviceVersions = (ServiceVersion[]) values.invoke(serviceVersionType);

        Map<String, ServiceVersion> stringToServiceVersion = CLASS_TO_MAP_STRING_SERVICE_VERSION
            .computeIfAbsent(serviceVersionType, type -> Arrays.stream(serviceVersions)
                .collect(Collectors.toMap(ServiceVersion::getVersion, sv -> sv)));

        if (useAllServiceVersions) {
            return new ArrayList<>(stringToServiceVersion.values());
        }

        return Arrays.stream(serviceVersionStrings)
            .map(stringToServiceVersion::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    static Object invokeSupplierMethod(ExtensionContext context, String sourceSupplier) throws Exception {
        Class<?> sourceSupplierClass;
        Method sourceSupplierMethod;

        // The method is fully qualified.
        if (sourceSupplier.contains("#")) {
            String[] classAndMethod = sourceSupplier.split("#", 2);

            sourceSupplierClass = ReflectionSupport.tryToLoadClass(classAndMethod[0]).get();
            sourceSupplierMethod = ReflectionSupport.findMethod(sourceSupplierClass, classAndMethod[1]).get();
        } else {
            sourceSupplierClass = context.getRequiredTestClass();
            sourceSupplierMethod = ReflectionSupport.findMethod(sourceSupplierClass, sourceSupplier).get();
        }

        validateSourceSupplier(sourceSupplierMethod);

        return ReflectionSupport.invokeMethod(sourceSupplierMethod, sourceSupplier);
    }

    static void validateSourceSupplier(Method sourceMethod) {
        int modifiers = sourceMethod.getModifiers();
        if ((modifiers | Modifier.STATIC) != modifiers) {
            throw new IllegalArgumentException(String.format(MUST_BE_STATIC, sourceMethod.getName()));
        }

        Type returnType = sourceMethod.getGenericReturnType();
        boolean validReturnType = returnType instanceof ParameterizedType;

        if (!validReturnType) {
            throw new IllegalArgumentException(String.format(MUST_BE_STREAM_ARGUMENTS, returnType));
        }

        ParameterizedType parameterizedType = (ParameterizedType) returnType;
        validReturnType = parameterizedType.getRawType() instanceof Class
            && Stream.class.isAssignableFrom((Class<?>) parameterizedType.getRawType())
            && parameterizedType.getActualTypeArguments().length == 1
            && parameterizedType.getActualTypeArguments()[0] instanceof Class
            && Arguments.class.isAssignableFrom((Class<?>) parameterizedType.getActualTypeArguments()[0]);

        if (!validReturnType) {
            throw new IllegalArgumentException(String.format(MUST_BE_STREAM_ARGUMENTS, returnType));
        }
    }

    static List<Arguments> convertSupplierSourceToArguments(Object source) {
        if (source instanceof Stream) {
            return ((Stream<?>) source).map(HttpClientServiceVersionAugmentedArgumentsProvider::convertToArguments)
                .collect(Collectors.toList());
        } else {
            throw new IllegalStateException("'sourceSupplier' returned an unsupported type: " + source.getClass());
        }
    }

    static Arguments convertToArguments(Object value) {
        if (value instanceof Arguments) {
            return (Arguments) value;
        } else {
            throw new IllegalStateException("Test parameterized source is an unsupported type: " + value.getClass());
        }
    }

    static List<Arguments> createHttpServiceVersionPermutations(List<HttpClient> httpClients,
        List<? extends ServiceVersion> serviceVersions) {
        List<Arguments> arguments = new ArrayList<>();

        for (HttpClient httpClient : httpClients) {
            for (ServiceVersion serviceVersion : serviceVersions) {
                arguments.add(Arguments.of(httpClient, serviceVersion));
            }
        }

        return arguments;
    }

    static List<Arguments> createNonHttpPermutations(List<? extends ServiceVersion> serviceVersions,
        List<Arguments> parameterizedTestingValues) {
        List<Arguments> arguments = new ArrayList<>();

        for (ServiceVersion serviceVersion : serviceVersions) {
            for (Arguments parameterizedTestingValue : parameterizedTestingValues) {
                arguments.add(prependArguments(serviceVersion, null, parameterizedTestingValue));
            }
        }

        return arguments;
    }

    static List<Arguments> createFullPermutations(List<HttpClient> httpClients,
        List<? extends ServiceVersion> serviceVersions, List<Arguments> parameterizedTestingValues) {
        List<Arguments> arguments = new ArrayList<>();

        for (HttpClient httpClient : httpClients) {
            for (ServiceVersion serviceVersion : serviceVersions) {
                for (Arguments parameterizedTestingValue : parameterizedTestingValues) {
                    arguments.add(prependArguments(httpClient, serviceVersion, parameterizedTestingValue));
                }
            }
        }

        return arguments;
    }

    private static Arguments prependArguments(Object prepend, Object optionalPrepend, Arguments arguments) {
        boolean hasOptionalPrepend = optionalPrepend != null;

        Object[] previousArgs = arguments.get();
        Object[] newArgs = new Object[previousArgs.length + 1 + (hasOptionalPrepend ? 1 : 0)];

        newArgs[0] = prepend;
        if (hasOptionalPrepend) {
            newArgs[1] = optionalPrepend;
        }

        System.arraycopy(previousArgs, 0, newArgs, hasOptionalPrepend ? 2 : 1, previousArgs.length);

        return Arguments.of(newArgs);
    }
}
