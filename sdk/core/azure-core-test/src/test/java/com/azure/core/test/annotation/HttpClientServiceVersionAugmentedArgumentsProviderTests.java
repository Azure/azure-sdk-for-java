// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.PreconditionViolationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.getServiceVersions;
import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.invokeSupplierMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link HttpClientServiceVersionAugmentedArgumentsProvider}.
 */
public class HttpClientServiceVersionAugmentedArgumentsProviderTests {
    /**
     * Tests {@link HttpClientServiceVersionAugmentedArgumentsProvider#getServiceVersions(String[], boolean, Class)}.
     */
    @ParameterizedTest
    @MethodSource("getServiceVersionsSupplier")
    public void getServiceVersionsTest(String[] serviceVersionStrings, boolean useLatestServiceVersionOnly,
        Class<? extends ServiceVersion> serviceVersionType, List<? extends ServiceVersion> expectedServiceVersions)
        throws ReflectiveOperationException {
        List<? extends ServiceVersion> actualServiceVersions = getServiceVersions(serviceVersionStrings,
            useLatestServiceVersionOnly, serviceVersionType);

        assertEquals(expectedServiceVersions.size(), actualServiceVersions.size());
        for (int i = 0; i < expectedServiceVersions.size(); i++) {
            assertEquals(expectedServiceVersions.get(i), actualServiceVersions.get(i));
        }
    }

    private static Stream<Arguments> getServiceVersionsSupplier() {
        String[] noServiceVersions = new String[0];
        Class<? extends ServiceVersion> serviceVersionType = AzureTestingServiceVersion.class;

        List<? extends ServiceVersion> gaOnly = Collections.singletonList(AzureTestingServiceVersion.GA);
        List<? extends ServiceVersion> alphaAndBeta = Arrays.asList(AzureTestingServiceVersion.ALPHA,
            AzureTestingServiceVersion.BETA);

        String[] alphaAndBetaStrings = alphaAndBeta.stream().map(ServiceVersion::getVersion).toArray(String[]::new);

        return Stream.of(
            // No service versions and not using latest only defaults to latest.
            Arguments.of(noServiceVersions, false, serviceVersionType, gaOnly),

            // Service versions are correctly mapped to their values.
            Arguments.of(alphaAndBetaStrings, false, serviceVersionType, alphaAndBeta),

            // Use latest only overrides passed service version strings
            Arguments.of(alphaAndBetaStrings, true, serviceVersionType, gaOnly)
        );
    }

    @Test
    public void nonEnumServiceVersionTypeThrows() {
        assertThrows(NoSuchMethodException.class, () -> getServiceVersions(new String[] { "ignored" }, false,
            ServiceVersion.class));
    }

    @Test
    public void serviceVersionTypeWithoutGetLatestThrows() {
        assertThrows(NoSuchMethodException.class, () -> getServiceVersions(null, false, ServiceVersion.class));
        assertThrows(NoSuchMethodException.class, () -> getServiceVersions(new String[] { "ignored" }, true,
            ServiceVersion.class));
    }

    @ParameterizedTest
    @MethodSource("invokeSupplierMethodSupplier")
    public void invokeSupplierMethodTests(ExtensionContext context, String sourceSupplier, Object expected)
        throws Exception {
        // Realistically these methods should return either a Stream or Iterable.
        // But for testing purposes just make sure it invokes properly.
        assertEquals(expected, invokeSupplierMethod(context, sourceSupplier));
    }

    private static Stream<Arguments> invokeSupplierMethodSupplier() {
        return Stream.of(
            // Using a fully-qualified source.
            Arguments.of(null, "com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProviderTests"
                + "#invokeMethodSupplierHelper", invokeMethodSupplierHelper()),

            // Using a relative source.
            Arguments.of(getMockExtensionContext(HttpClientServiceVersionAugmentedArgumentsProviderTests.class),
                "invokeMethodSupplierHelper", invokeMethodSupplierHelper())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFullyQualifiedSourceSupplierThrowsSupplier")
    public void invalidFullyQualifiedSourceSupplierThrows(String sourceSupplier,
        Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> invokeSupplierMethod(null, sourceSupplier));
    }

    private static Stream<Arguments> invalidFullyQualifiedSourceSupplierThrowsSupplier() {
        return Stream.of(
            // No class or method.
            Arguments.of("#", PreconditionViolationException.class),

            // Missing method.
            Arguments.of("com.azure.core.test.TestBase#", PreconditionViolationException.class),

            // Missing class.
            Arguments.of("#supplierMethod", PreconditionViolationException.class),

            // Non-existent class.
            Arguments.of("supplierClass#supplierMethod", ClassNotFoundException.class),

            // Non-existent method.
            // This one is odd as the internal tooling wraps this into an optional and this is what is thrown
            // when Optional.get() is called.
            Arguments.of("com.azure.core.test.TestBase#notARealMethod", NoSuchElementException.class),

            // Method isn't static.
            Arguments.of("com.azure.core.test.TestBase#getTestMode", IllegalArgumentException.class)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidRelativelyQualifiedSourceSupplierThrows")
    public void invalidRelativelyQualifiedSourceSupplierThrows(ExtensionContext extensionContext, String sourceSupplier,
        Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, () -> invokeSupplierMethod(extensionContext, sourceSupplier));
    }

    private static Stream<Arguments> invalidRelativelyQualifiedSourceSupplierThrows() {
        return Stream.of(
            // Test class isn't present in extension context.
            Arguments.of(getMockExtensionContext(null), "notARealMethod", PreconditionViolationException.class),

            // Source method doesn't exist.
            Arguments.of(getMockExtensionContext(TestBase.class), "notARealMethod", NoSuchElementException.class),

            // Source method isn't static.
            Arguments.of(getMockExtensionContext(TestBase.class), "getTestMode", IllegalArgumentException.class)
        );
    }

    private static ExtensionContext getMockExtensionContext(Class<?> testClass) {
        // Falls back to calling the real methods if a mock isn't configured.
        ExtensionContext mockExtensionContext = mock(ExtensionContext.class, CALLS_REAL_METHODS);

        if (testClass == null) {
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.empty());
        } else {
            when(mockExtensionContext.getTestClass()).thenReturn(Optional.of(testClass));
        }

        return mockExtensionContext;
    }

//    static List<Arguments> convertSupplierSourceToArguments(Object source) {
//        List<Arguments> arguments = new ArrayList<>();
//        if (source instanceof BaseStream) {
//            Iterator<?> it = ((BaseStream<?, ?>) source).iterator();
//
//            while (it.hasNext()) {
//                Object sourceValue = it.next();
//                arguments.add(convertToArguments(sourceValue));
//            }
//        } else if (source.getClass().isArray()) {
//            for (Object sourceValue : (Object[]) source) {
//                arguments.add(convertToArguments(sourceValue));
//            }
//        } else {
//            throw new RuntimeException("'sourceSupplier' returned an unsupported type:" + source.getClass());
//        }
//
//        return arguments;
//    }

//    static Arguments convertToArguments(Object value) {
//        if (value instanceof Arguments) {
//            return (Arguments) value;
//        }
//
//        if (value instanceof Object[]) {
//            return Arguments.of((Object[]) value);
//        }
//
//        return Arguments.of(value);
//    }

//    static List<Arguments> createHttpServiceVersionPermutations(List<HttpClient> httpClients,
//        List<? extends ServiceVersion> serviceVersions) {
//        List<Arguments> arguments = new ArrayList<>();
//
//        for (HttpClient httpClient : httpClients) {
//            for (ServiceVersion serviceVersion : serviceVersions) {
//                arguments.add(Arguments.of(httpClient, serviceVersion));
//            }
//        }
//
//        return arguments;
//    }

//    static List<Arguments> createNonHttpPermutations(List<? extends ServiceVersion> serviceVersions,
//        List<Arguments> parameterizedTestingValues) {
//        List<Arguments> arguments = new ArrayList<>();
//
//        for (ServiceVersion serviceVersion : serviceVersions) {
//            for (Arguments parameterizedTestingValue : parameterizedTestingValues) {
//                arguments.add(prependArguments(serviceVersion, parameterizedTestingValue));
//            }
//        }
//
//        return arguments;
//    }

//    static List<Arguments> createFullPermutations(List<HttpClient> httpClients,
//        List<? extends ServiceVersion> serviceVersions, List<Arguments> parameterizedTestingValues) {
//        List<Arguments> arguments = new ArrayList<>();
//
//        List<Arguments> nonHttpArguments = createNonHttpPermutations(serviceVersions, parameterizedTestingValues);
//
//        for (HttpClient httpClient : httpClients) {
//            for (Arguments nonHttpArgument : nonHttpArguments) {
//                arguments.add(prependArguments(httpClient, nonHttpArgument));
//            }
//        }
//
//        return arguments;
//    }

//    static Arguments prependArguments(Object prepend, Arguments arguments) {
//        Object[] previousArgs = arguments.get();
//        Object[] newArgs = new Object[previousArgs.length + 1];
//        newArgs[0] = prepend;
//        System.arraycopy(previousArgs, 0, newArgs, 1, previousArgs.length);
//
//        return Arguments.of(newArgs);
//    }

    static TestMode invokeMethodSupplierHelper() {
        return TestMode.PLAYBACK;
    }
}
