// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.annotation;

import com.azure.core.test.TestBase;
import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.PreconditionViolationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.convertToArguments;
import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.getServiceVersions;
import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.invokeSupplierMethod;
import static com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProvider.validateSourceSupplier;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    @ParameterizedTest(name = "[{index}] {displayName}")
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

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("invokeSupplierMethodSupplier")
    public void invokeSupplierMethodTests(ExtensionContext context, String sourceSupplier, Stream<Arguments> expected)
        throws Exception {
        Object actual = invokeSupplierMethod(context, sourceSupplier);

        assertTrue(actual instanceof Stream);

        Iterator<?> actualIterator = ((Stream<?>) actual).iterator();
        Iterator<Arguments> expectedIterator = expected.iterator();

        while (actualIterator.hasNext()) {
            assertTrue(expectedIterator.hasNext());

            Object actualNext = actualIterator.next();
            Arguments expectedNext = expectedIterator.next();

            assertTrue(actualNext instanceof Arguments);
            assertArrayEquals(expectedNext.get(), ((Arguments) actualNext).get());
        }

        assertFalse(expectedIterator.hasNext());
    }

    private static Stream<Arguments> invokeSupplierMethodSupplier() {
        return Stream.of(
            // Using a fully-qualified source that's in this class.
            Arguments.of(null, "com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProviderTests"
                + "#staticAndValidReturnType", staticAndValidReturnType()),

            // Using a fully-qualified source that's in another class.
            Arguments.of(null,
                "com.azure.core.test.annotation.FullyQualifiedSourceSupplierTestHelper#staticAndValidReturnType",
                FullyQualifiedSourceSupplierTestHelper.staticAndValidReturnType()),

            // Using a relative source.
            Arguments.of(getMockExtensionContext(HttpClientServiceVersionAugmentedArgumentsProviderTests.class),
                "staticAndValidReturnType", staticAndValidReturnType())
        );
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
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

            // Valid return types but have parameters.
            Arguments.of("com.azure.core.test.annotation.HttpClientServiceVersionAugmentedArgumentsProviderTests"
                + "#staticAndValidReturnTypeButHasParameters", NoSuchElementException.class),
            Arguments.of("com.azure.core.test.annotation.FullyQualifiedSourceSupplierTestHelper"
                + "#staticAndValidReturnTypeButHasParameters", NoSuchElementException.class)
        );
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("validateSourceSupplierTestSupplier")
    public void validateSourceSupplierTest(Method validSourceSupplier) {
        assertDoesNotThrow(() -> validateSourceSupplier(validSourceSupplier));
    }

    private static Stream<Arguments> validateSourceSupplierTestSupplier() throws NoSuchMethodException {
        Class<HttpClientServiceVersionAugmentedArgumentsProviderTests> thisClass =
            HttpClientServiceVersionAugmentedArgumentsProviderTests.class;

        Class<FullyQualifiedSourceSupplierTestHelper> anotherClass = FullyQualifiedSourceSupplierTestHelper.class;

        return Stream.of(
            Arguments.of(thisClass.getMethod("staticAndValidReturnType")),
            Arguments.of(anotherClass.getMethod("staticAndValidReturnType"))
        );
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("invalidSourceSupplerSupplier")
    public void invalidSourceSupplier(Method invalidSourceSupplier) {
        assertThrows(IllegalArgumentException.class, () -> validateSourceSupplier(invalidSourceSupplier));
    }

    private static Stream<Arguments> invalidSourceSupplerSupplier() throws NoSuchMethodException {
        Class<HttpClientServiceVersionAugmentedArgumentsProviderTests> thisClass =
            HttpClientServiceVersionAugmentedArgumentsProviderTests.class;

        Class<FullyQualifiedSourceSupplierTestHelper> anotherClass = FullyQualifiedSourceSupplierTestHelper.class;

        return Stream.of(
            Arguments.of(thisClass.getMethod("nonStaticAndInvalidReturnTypeMethod")),
            Arguments.of(thisClass.getMethod("anotherNonStaticAndInvalidReturnTypeMethod")),
            Arguments.of(thisClass.getMethod("nonStaticMethod")),
            Arguments.of(thisClass.getMethod("staticButInvalidReturnTypeMethod")),
            Arguments.of(thisClass.getMethod("anotherStaticButInvalidReturnTypeMethod")),

            Arguments.of(anotherClass.getMethod("nonStaticAndInvalidReturnTypeMethod")),
            Arguments.of(anotherClass.getMethod("anotherNonStaticAndInvalidReturnTypeMethod")),
            Arguments.of(anotherClass.getMethod("nonStaticMethod")),
            Arguments.of(anotherClass.getMethod("staticButInvalidReturnTypeMethod")),
            Arguments.of(anotherClass.getMethod("anotherStaticButInvalidReturnTypeMethod"))
        );
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
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

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("convertToArgumentsTestSupplier")
    public void convertToArgumentsTest(Object value, Arguments expected) {
        Arguments actual = convertToArguments(value);

        assertArrayEquals(expected.get(), actual.get());
    }

    private static Stream<Arguments> convertToArgumentsTestSupplier() {
        Arguments emptyArgumentsMock = mock(Arguments.class);
        when(emptyArgumentsMock.get()).thenReturn(null);

        Arguments nonEmptyArguments = Arguments.of("1", 1, null, new byte[0]);

        return Stream.of(
            Arguments.of(emptyArgumentsMock, emptyArgumentsMock),
            Arguments.of(nonEmptyArguments, nonEmptyArguments)
        );
    }

    @ParameterizedTest(name = "[{index}] {displayName}")
    @MethodSource("invalidArgumentTypesSupplier")
    public void invalidArgumentTypes(Object argument) {
        assertThrows(IllegalStateException.class, () -> convertToArguments(argument));
    }

    private static Stream<Arguments> invalidArgumentTypesSupplier() {
        return Stream.of(
            Arguments.of(1),
            Arguments.of("1"),
            Arguments.of(1.0),
            Arguments.of(true),
            Arguments.of(new Object())
        );
    }

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

    /**
     * Dummy method for testing that a non-static, non-{@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer.
     */
    public int nonStaticAndInvalidReturnTypeMethod() {
        return 1;
    }

    /**
     * Dummy method for testing that a non-static, non-Arguments Stream supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer Stream.
     */
    public Stream<Integer> anotherNonStaticAndInvalidReturnTypeMethod() {
        return Stream.of(1);
    }

    /**
     * Dummy method for testing that a non-static, {@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy Arguments Stream.
     */
    public Stream<Arguments> nonStaticMethod() {
        return Stream.of(Arguments.of(1));
    }

    /**
     * Dummy method for testing that a static, non-{@code Stream<Arguments>} supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer.
     */
    public static int staticButInvalidReturnTypeMethod() {
        return 1;
    }

    /**
     * Dummy method for testing that a static, non-Arguments stream supplier method results in an {@link
     * IllegalArgumentException}.
     *
     * @return Dummy integer Stream.
     */
    public static Stream<Integer> anotherStaticButInvalidReturnTypeMethod() {
        return Stream.of(1);
    }

    /**
     * Dummy method for testing that a static, {@code Stream<Arguments>} supplier method that has any parameters results
     * in a {@link RuntimeException}.
     *
     * @param dummyParam Dummy parameter.
     * @return Dummy Arguements Stream.
     */
    public static Stream<Arguments> staticAndValidReturnTypeButHasParameters(Object dummyParam) {
        return Stream.of(Arguments.of(dummyParam));
    }

    /**
     * Dummy method for testing that a static, {@code Stream<Arguments>} supplier method is valid and returns the
     * expected Stream.
     *
     * @return A single Arguments Stream whose only value is the integer {@code 1}.
     */
    public static Stream<Arguments> staticAndValidReturnType() {
        return Stream.of(Arguments.of(1));
    }
}
