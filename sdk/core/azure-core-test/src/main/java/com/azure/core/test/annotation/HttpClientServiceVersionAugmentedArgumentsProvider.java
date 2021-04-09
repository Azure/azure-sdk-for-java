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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class HttpClientServiceVersionAugmentedArgumentsProvider
    implements ArgumentsProvider, AnnotationConsumer<HttpClientServiceVersionAugmentedSource> {
    private static final TestMode TEST_MODE = ImplUtils.getTestMode();

    private static final Map<Class<? extends ServiceVersion>, Map<String, ServiceVersion>>
        CLASS_TO_MAP_STRING_SERVICE_VERSION = new ConcurrentHashMap<>();

    private String sourceSupplier;
    private boolean noSourceSupplier;

    private String[] serviceVersions;
    private boolean useLatestServiceVersionOnly;
    private Class<? extends ServiceVersion> serviceVersionType;

    private boolean ignoreHttpClients;
    private boolean useNullHttpClient;

    @Override
    public void accept(HttpClientServiceVersionAugmentedSource annotation) {
        this.sourceSupplier = annotation.sourceSupplier();
        this.noSourceSupplier = CoreUtils.isNullOrEmpty(sourceSupplier);

        this.serviceVersions = annotation.serviceVersions();
        this.useLatestServiceVersionOnly = CoreUtils.isNullOrEmpty(serviceVersions) || TEST_MODE == TestMode.PLAYBACK;
        this.serviceVersionType = annotation.serviceVersionType();

        this.ignoreHttpClients = annotation.ignoreHttpClients();
        this.useNullHttpClient = TEST_MODE == TestMode.PLAYBACK;
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        // If the TEST_MODE is PLAYBACK or HttpClients are being ignored don't use HttpClients.
        List<HttpClient> httpClientsToUse = Collections.singletonList(null);
        if (!ignoreHttpClients && !useNullHttpClient) {
            httpClientsToUse = TestBase.getHttpClients().collect(Collectors.toList());
        }

        List<? extends ServiceVersion> serviceVersionsToUse = getServiceVersions(serviceVersions,
            useLatestServiceVersionOnly, serviceVersionType);

        // If the sourceSupplier isn't provided don't retrieve parameterized testing values.
        List<Arguments> parameterizedTestingValues = null;
        if (!noSourceSupplier) {
            parameterizedTestingValues = getParameterizedTestingValues(context, sourceSupplier);
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
            return serviceVersionsToUse.stream().map(Arguments::of);
        } else if (ignoreHttpClients) {
            return createNonHttpPermutations(serviceVersionsToUse, parameterizedTestingValues).stream();
        } else if (noSourceSupplier) {
            return createHttpServiceVersionPermutations(httpClientsToUse, serviceVersionsToUse).stream();
        } else if (CoreUtils.isNullOrEmpty(httpClientsToUse)) {
            return createFullPermutations(Collections.singletonList(null), serviceVersionsToUse,
                parameterizedTestingValues).stream();
        } else {
            return createFullPermutations(httpClientsToUse, serviceVersionsToUse, parameterizedTestingValues)
                .stream();
        }
    }

    /*
     * Get the ServiceVersions to use in testing.
     *
     * This uses reflection to convert the string values into their enum representation. If the string set is null
     * 'getLatest' will be the only value returned.
     */
    private static List<? extends ServiceVersion> getServiceVersions(String[] serviceVersionStrings,
        boolean useLatestServiceVersionOnly, Class<? extends ServiceVersion> serviceVersionType)
        throws ReflectiveOperationException {
        if (CoreUtils.isNullOrEmpty(serviceVersionStrings) || useLatestServiceVersionOnly) {
            Method getLatest = serviceVersionType.getMethod("getLatest");
            return Collections.singletonList((ServiceVersion) getLatest.invoke(serviceVersionType));
        }

        // Assumption that the service version type is an enum.
        Method values = serviceVersionType.getMethod("values");
        ServiceVersion[] serviceVersions = (ServiceVersion[]) values.invoke(serviceVersionType);

        Map<String, ServiceVersion> stringToServiceVersion = CLASS_TO_MAP_STRING_SERVICE_VERSION
            .computeIfAbsent(serviceVersionType, type -> Arrays.stream(serviceVersions)
                .collect(Collectors.toMap(ServiceVersion::getVersion, sv -> sv)));

        return Arrays.stream(serviceVersionStrings)
            .map(stringToServiceVersion::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static List<Arguments> getParameterizedTestingValues(ExtensionContext context, String sourceSupplier)
        throws ReflectiveOperationException {
        // The method is fully qualified.

        Class<?> sourceSupplierClass;
        Method sourceSupplierMethod;
        if (sourceSupplier.contains("#")) {
            String[] classAndMethod = sourceSupplier.split("#", 2);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }

            sourceSupplierClass = classLoader.loadClass(classAndMethod[0]);
            sourceSupplierMethod = sourceSupplierClass.getMethod(classAndMethod[1]);
        } else {
            sourceSupplierClass = context.getRequiredTestClass();
            sourceSupplierMethod = sourceSupplierClass.getMethod(sourceSupplier);
        }

        Object source = sourceSupplierMethod.invoke(sourceSupplierClass);

        List<Arguments> arguments = new ArrayList<>();
        if (source instanceof BaseStream) {
            Iterator<?> it = ((BaseStream<?, ?>) source).iterator();

            while (it.hasNext()) {
                Object sourceValue = it.next();
                arguments.add(convertToArguments(sourceValue));
            }
        } else if (source.getClass().isArray()) {
            for (Object sourceValue : (Object[]) source) {
                arguments.add(convertToArguments(sourceValue));
            }
        } else {
            throw new RuntimeException("'sourceSupplier' returned an unsupported type:" + source.getClass());
        }

        return arguments;
    }

    private static Arguments convertToArguments(Object value) {
        if (value instanceof Arguments) {
            return (Arguments) value;
        }

        if (value instanceof Object[]) {
            return Arguments.of((Object[]) value);
        }

        return Arguments.of(value);
    }

    private static List<Arguments> createHttpServiceVersionPermutations(List<HttpClient> httpClients,
        List<? extends ServiceVersion> serviceVersions) {
        List<Arguments> arguments = new ArrayList<>();

        for (HttpClient httpClient : httpClients) {
            for (ServiceVersion serviceVersion : serviceVersions) {
                arguments.add(Arguments.of(httpClient, serviceVersion));
            }
        }

        return arguments;
    }

    private static List<Arguments> createNonHttpPermutations(List<? extends ServiceVersion> serviceVersions,
        List<Arguments> parameterizedTestingValues) {
        List<Arguments> arguments = new ArrayList<>();

        for (ServiceVersion serviceVersion : serviceVersions) {
            for (Arguments parameterizedTestingValue : parameterizedTestingValues) {
                arguments.add(prependArguments(serviceVersion, parameterizedTestingValue));
            }
        }

        return arguments;
    }

    private static List<Arguments> createFullPermutations(List<HttpClient> httpClients,
        List<? extends ServiceVersion> serviceVersions, List<Arguments> parameterizedTestingValues) {
        List<Arguments> arguments = new ArrayList<>();

        List<Arguments> nonHttpArguments = createNonHttpPermutations(serviceVersions, parameterizedTestingValues);

        for (HttpClient httpClient : httpClients) {
            for (Arguments nonHttpArgument : nonHttpArguments) {
                arguments.add(prependArguments(httpClient, nonHttpArgument));
            }
        }

        return arguments;
    }

    private static Arguments prependArguments(Object prepend, Arguments arguments) {
        Object[] previousArgs = arguments.get();
        Object[] newArgs = new Object[previousArgs.length + 1];
        newArgs[0] = prepend;
        System.arraycopy(previousArgs, 0, newArgs, 1, previousArgs.length);

        return Arguments.of(newArgs);
    }
}
