// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.AzureMethodSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.ServiceVersion;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An implementation of {@link ArgumentsProvider} enabling the ability to parameterize a test using the {@link
 * AzureMethodSource} annotation.
 */
public final class AzureMethodSourceArgumentsProvider
    implements ArgumentsProvider, AnnotationConsumer<AzureMethodSource> {
    private static final TestMode TEST_MODE = TestingHelpers.getTestMode();

    private static final Map<Class<? extends ServiceVersion>, ServiceVersion> CLASS_TO_LATEST_SERVICE_VERSION
        = new ConcurrentHashMap<>();

    private static final Map<Class<? extends ServiceVersion>, Map<String, ServiceVersion>>
        CLASS_TO_MAP_STRING_SERVICE_VERSION = new ConcurrentHashMap<>();

    private static final String MUST_BE_STATIC = "Source supplier method is required to be static. Method: %s.";

    private static final String MUST_BE_STREAM_ARGUMENTS =
        "Source supplier method is required to return Stream<Arguments>. Return type: %s.";

    private final ClientLogger logger = new ClientLogger(AzureMethodSourceArgumentsProvider.class);

    private String minimumServiceVersion;
    private String maximumServiceVersion;
    private Class<? extends ServiceVersion> serviceVersionType;
    private String sourceSupplier;
    private boolean useHttpClientPermutation;

    @Override
    public void accept(AzureMethodSource annotation) {
        this.minimumServiceVersion = annotation.minimumServiceVersion();
        this.maximumServiceVersion = annotation.maximumServiceVersion();
        this.serviceVersionType = annotation.serviceVersionType();

        if (!Enum.class.isAssignableFrom(serviceVersionType)) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("'serviceVersionType' isn't an instance of Enum."));
        }

        this.sourceSupplier = annotation.sourceSupplier();
        this.useHttpClientPermutation = annotation.useHttpClientPermutation();
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
        // If the TEST_MODE is PLAYBACK or HttpClients are being ignored don't use HttpClients.
        List<HttpClient> httpClientsToUse = Collections.singletonList(null);
        if (!useHttpClientPermutation && TEST_MODE != TestMode.PLAYBACK) {
            httpClientsToUse = TestBase.getHttpClients().collect(Collectors.toList());
        }

        boolean testAllServiceVersions = Configuration.getGlobalConfiguration()
            .get("AZURE_TEST_ALL_SERVICE_VERSIONS", false);
        List<? extends ServiceVersion> serviceVersionsToUse = getServiceVersions(minimumServiceVersion,
            maximumServiceVersion, serviceVersionType, TEST_MODE, testAllServiceVersions);

        // If the sourceSupplier isn't provided don't retrieve parameterized testing values.
        List<Arguments> testValues = null;
        if (!CoreUtils.isNullOrEmpty(sourceSupplier)) {
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
        if (!useHttpClientPermutation && testValues == null) {
            return serviceVersionsToUse.stream().map(Arguments::arguments);
        } else if (!useHttpClientPermutation) {
            return createNonHttpPermutations(serviceVersionsToUse, testValues).stream();
        } else if (testValues == null) {
            return createHttpServiceVersionPermutations(httpClientsToUse, serviceVersionsToUse).stream();
        } else if (CoreUtils.isNullOrEmpty(httpClientsToUse)) {
            return createFullPermutations(Collections.singletonList(null), serviceVersionsToUse, testValues).stream();
        } else {
            return createFullPermutations(httpClientsToUse, serviceVersionsToUse, testValues).stream();
        }
    }

    /*
     * Gets the service versions that will be used during testing.
     *
     * If the test class is annotated with TestingServiceVersions that will be used to determine which version(s) will
     * be tested. When the TEST_MODE is PLAYBACK or RECORD the recording service version will be used. Otherwise, the
     * live service versions that the test supports will be used.
     *
     * If the test class is not annotated with TestingServiceVersions the latest service version will be used for
     * testing.
     */
    static List<? extends ServiceVersion> getServiceVersions(String minimumServiceVersion, String maximumServiceVersion,
        Class<? extends ServiceVersion> serviceVersionType, TestMode testMode, boolean testAllServiceVersions) {
        loadServiceVersion(serviceVersionType);

        // If all ServiceVersions are to be tested, and the test mode is LIVE, create the intersection of all service
        // versions and the minimum and maximum service versions supported by the test.
        if (testAllServiceVersions && testMode == TestMode.LIVE) {

            // Otherwise compute all service versions which fall in the intersection of the class and method service
            // version range intersection.
            int minimumOrdinal = getServiceVersionRangeBound(minimumServiceVersion, serviceVersionType, false);
            int maximumOrdinal = getServiceVersionRangeBound(maximumServiceVersion, serviceVersionType, true);

            return CLASS_TO_MAP_STRING_SERVICE_VERSION.get(serviceVersionType).values().stream()
                .filter(sv -> {
                    int ordinal = ((Enum<?>) sv).ordinal();
                    return ordinal >= minimumOrdinal && ordinal <= maximumOrdinal;
                }).collect(Collectors.toList());
        } else {
            // Otherwise, use either the latest service version or the maximum service version, whichever is lesser.
            Enum<?> maximumServiceVersionEnum = getEnumOrNull(maximumServiceVersion, serviceVersionType);
            ServiceVersion latestServiceVersion = CLASS_TO_LATEST_SERVICE_VERSION.get(serviceVersionType);

            if (maximumServiceVersionEnum == null) {
                // If the maximum service version isn't configured use the latest service version.
                return Collections.singletonList(latestServiceVersion);
            } else {
                // Otherwise use the lesser of maximum and latest service version.
                if (maximumServiceVersionEnum.ordinal() >= ((Enum<?>) latestServiceVersion).ordinal()) {
                    return Collections.singletonList(latestServiceVersion);
                } else {
                    return Collections.singletonList(CLASS_TO_MAP_STRING_SERVICE_VERSION.get(serviceVersionType)
                        .get(maximumServiceVersion));
                }
            }
        }
    }

    private static int getServiceVersionRangeBound(String serviceVersion,
        Class<? extends ServiceVersion> serviceVersionType, boolean isMaximum) {
        Enum<?> serviceVersionEnum = getEnumOrNull(serviceVersion, serviceVersionType);

        if (serviceVersionEnum == null) {
            // If the ServiceVersion isn't set, or is invalid, return the int32 bound based on min or max.
            return isMaximum ? Integer.MAX_VALUE : Integer.MIN_VALUE;
        } else {
            // Otherwise, return the ordinal of the ServiceVersion enum.
            return serviceVersionEnum.ordinal();
        }
    }

    private static Enum<?> getEnumOrNull(String serviceVersion, Class<? extends ServiceVersion> serviceVersionType) {
        return CoreUtils.isNullOrEmpty(serviceVersion)
            ? null
            : (Enum<?>) CLASS_TO_MAP_STRING_SERVICE_VERSION.get(serviceVersionType).get(serviceVersion);
    }

    /*
     * Helper method that loads all values and the latest ServiceVersion of the passed serviceVersionType.
     */
    private static void loadServiceVersion(Class<? extends ServiceVersion> serviceVersionType) {
        CLASS_TO_MAP_STRING_SERVICE_VERSION.computeIfAbsent(serviceVersionType, type -> {
            try {
                ServiceVersion[] serviceVersions = (ServiceVersion[]) serviceVersionType.getMethod("values")
                    .invoke(serviceVersionType);

                Map<String, ServiceVersion> stringServiceVersionMap = new TreeMap<>();
                for (ServiceVersion serviceVersion : serviceVersions) {
                    stringServiceVersionMap.put(serviceVersion.getVersion(), serviceVersion);
                }

                return stringServiceVersionMap;
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        });

        CLASS_TO_LATEST_SERVICE_VERSION.computeIfAbsent(serviceVersionType, type -> {
            try {
                return (ServiceVersion) serviceVersionType.getMethod("getLatest").invoke(serviceVersionType);
            } catch (ReflectiveOperationException ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    @SuppressWarnings("SimplifyOptionalCallChains")
    static Object invokeSupplierMethod(ExtensionContext context, String sourceSupplier) throws Exception {
        Class<?> sourceSupplierClass;
        Optional<Method> sourceSupplierMethod;

        // The method is fully qualified.
        if (sourceSupplier.contains("#")) {
            String[] classAndMethod = sourceSupplier.split("#", 2);

            sourceSupplierClass = ReflectionSupport.tryToLoadClass(classAndMethod[0]).get();
            sourceSupplierMethod = ReflectionSupport.findMethod(sourceSupplierClass, classAndMethod[1]);
        } else {
            sourceSupplierClass = context.getRequiredTestClass();
            sourceSupplierMethod = ReflectionSupport.findMethod(sourceSupplierClass, sourceSupplier);
        }

        if (!sourceSupplierMethod.isPresent()) {
            throw new IllegalArgumentException(String.format("Unable to find 'sourceSupplier' method %s.",
                sourceSupplierMethod));
        }

        validateSourceSupplier(sourceSupplierMethod.get());

        return ReflectionSupport.invokeMethod(sourceSupplierMethod.get(), sourceSupplier);
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
            return ((Stream<?>) source).map(AzureMethodSourceArgumentsProvider::convertToArguments)
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
