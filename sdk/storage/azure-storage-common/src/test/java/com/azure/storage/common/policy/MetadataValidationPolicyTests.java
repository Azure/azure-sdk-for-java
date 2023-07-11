// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.test.TestProxyTestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MetadataValidationPolicyTests extends TestProxyTestBase {

    @ParameterizedTest
    @MethodSource("invalidMetadataWhitespaceSupplier")
    public void invalidMetadataWhitespace(HttpHeaders headers) {
        assertThrows(IllegalArgumentException.class, () -> MetadataValidationPolicy.validateMetadataHeaders(headers));
    }

    private static Stream<Arguments> invalidMetadataWhitespaceSupplier() {
        return Stream.of(
            Arguments.of(new HttpHeaders().add("x-ms-meta- ", "value")),
            Arguments.of(new HttpHeaders().add("x-ms-meta- nameleadspace", "value")),
            Arguments.of(new HttpHeaders().add("x-ms-meta-nametrailspace ", "value")),
            Arguments.of(new HttpHeaders().add("x-ms-meta-valueleadspace", " value")),
            Arguments.of(new HttpHeaders().add("x-ms-meta-valuetrailspace", "value "))
        );
    }


    @Test
    public void emptyMetadataNameIsValid() { // sort of, this is passed to be handled by the service
        assertDoesNotThrow(() ->
            MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-", "emptyname")));

    }

    @Test
    public void emptyMetadataValueIsValid() {
        assertDoesNotThrow(() ->
            MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-emptyvalue", "")));
    }

    @Test
    public void validMetadata() {
        assertDoesNotThrow(() ->
            MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-something", "value")));
        assertDoesNotThrow(() ->
            MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-anothersomething", "value")));
    }

    @ParameterizedTest
    @MethodSource("noMetadataValuesSupplier")
    public void noMetadataValues(HttpHeaders headers) {
        assertDoesNotThrow(() -> MetadataValidationPolicy.validateMetadataHeaders(headers));
    }

    private static Stream<Arguments> noMetadataValuesSupplier() {
        return Stream.of(
            Arguments.of(new HttpHeaders().add("", "notmetadataheaderemptyname")),
            Arguments.of(new HttpHeaders().add(" ", "notmetadataheaderspacename")),
            Arguments.of(new HttpHeaders().add(" notmetadataheaderwithleadingspace", "value")),
            Arguments.of(new HttpHeaders().add("notmetadataheaderwithtrailingspace ", "value")),
            Arguments.of(new HttpHeaders().add("notmetadatavaluewithtrailingspace", " value")),
            Arguments.of(new HttpHeaders().add("notmetadatavaluewithtrailingspace", "value ")),
            Arguments.of(new HttpHeaders().add("notmetadatavaluewithspacevalue", " "))
        );
    }
}
