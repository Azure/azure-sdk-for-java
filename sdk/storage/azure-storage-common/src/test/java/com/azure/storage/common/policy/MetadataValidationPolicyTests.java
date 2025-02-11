// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.policy;

import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("deprecation")
public class MetadataValidationPolicyTests {
    @ParameterizedTest
    @MethodSource("invalidMetadataWhitespaceSupplier")
    public void invalidMetadataWhitespace(HttpHeaders headers) {
        assertThrows(IllegalArgumentException.class, () -> MetadataValidationPolicy.validateMetadataHeaders(headers));
    }

    private static Stream<HttpHeaders> invalidMetadataWhitespaceSupplier() {
        return Stream.of(new HttpHeaders().add("x-ms-meta- ", "value"),
            new HttpHeaders().add("x-ms-meta- nameleadspace", "value"),
            new HttpHeaders().add("x-ms-meta-nametrailspace ", "value"),
            new HttpHeaders().add("x-ms-meta-valueleadspace", " value"),
            new HttpHeaders().add("x-ms-meta-valuetrailspace", "value "));
    }

    @Test
    public void emptyMetadataNameIsValid() { // sort of, this is passed to be handled by the service
        assertDoesNotThrow(
            () -> MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-", "emptyname")));
    }

    @Test
    public void emptyMetadataValueIsValid() {
        assertDoesNotThrow(
            () -> MetadataValidationPolicy.validateMetadataHeaders(new HttpHeaders().add("x-ms-meta-emptyvalue", "")));
    }

    @ParameterizedTest
    @MethodSource("validMetadataSupplier")
    public void validMetadata(HttpHeaders headers) {
        assertDoesNotThrow(() -> MetadataValidationPolicy.validateMetadataHeaders(headers));
    }

    private static Stream<HttpHeaders> validMetadataSupplier() {
        return Stream.of(new HttpHeaders().add("x-ms-meta-something", "value"),
            new HttpHeaders().add("x-ms-meta-anothersomething", "value"));
    }

    @ParameterizedTest
    @MethodSource("noMetadataValuesSupplier")
    public void noMetadataValues(HttpHeaders headers) {
        assertDoesNotThrow(() -> MetadataValidationPolicy.validateMetadataHeaders(headers));
    }

    private static Stream<HttpHeaders> noMetadataValuesSupplier() {
        return Stream.of(new HttpHeaders().add("", "notmetadataheaderemptyname"),
            new HttpHeaders().add(" ", "notmetadataheaderspacename"),
            new HttpHeaders().add(" notmetadataheaderwithleadingspace", "value"),
            new HttpHeaders().add("notmetadataheaderwithtrailingspace ", "value"),
            new HttpHeaders().add("notmetadatavaluewithtrailingspace", " value"),
            new HttpHeaders().add("notmetadatavaluewithtrailingspace", "value "),
            new HttpHeaders().add("notmetadatavaluewithspacevalue", " "));
    }
}
