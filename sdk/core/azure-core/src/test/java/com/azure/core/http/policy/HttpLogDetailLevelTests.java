// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.util.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link HttpLogDetailLevel}.
 */
public class HttpLogDetailLevelTests {
    @ParameterizedTest
    @MethodSource("fromConfigurationSupplier")
    public void fromConfiguration(Configuration configuration, HttpLogDetailLevel expected) {
        assertEquals(expected, HttpLogDetailLevel.fromConfiguration(configuration));
    }

    private static Stream<Arguments> fromConfigurationSupplier() {
        // Inserting a null value into Configuration throws, so mock it.
        Configuration returnsNullMock = mock(Configuration.class);
        when(returnsNullMock.get(Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL)).thenReturn(null);

        return Stream.of(
            // null turns into NONE
            Arguments.of(returnsNullMock, HttpLogDetailLevel.NONE),

            // Empty string turns into NONE
            Arguments.of(makeConfiguration(""), HttpLogDetailLevel.NONE),

            // Unknown values turn into NONE
            Arguments.of(makeConfiguration("unknown"), HttpLogDetailLevel.NONE),

            // basic turns into BASIC
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BASIC_VALUE), HttpLogDetailLevel.BASIC),

            // BASIC turns into BASIC
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BASIC.name()), HttpLogDetailLevel.BASIC),

            // bAsIc turns into BASIC
            Arguments.of(makeConfiguration("bAsIc"), HttpLogDetailLevel.BASIC),

            // headers turns into HEADERS
            Arguments.of(makeConfiguration(HttpLogDetailLevel.HEADERS_VALUE), HttpLogDetailLevel.HEADERS),

            // HEADERS turns into HEADERS
            Arguments.of(makeConfiguration(HttpLogDetailLevel.HEADERS.name()), HttpLogDetailLevel.HEADERS),

            // hEaDeRs turns into HEADERS
            Arguments.of(makeConfiguration("hEaDeRs"), HttpLogDetailLevel.HEADERS),

            // body turns into BODY
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BODY_VALUE), HttpLogDetailLevel.BODY),

            // BODY turns into BODY
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BODY.name()), HttpLogDetailLevel.BODY),

            // bOdY turns into BODY
            Arguments.of(makeConfiguration("bOdY"), HttpLogDetailLevel.BODY),

            // body_and_headers turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BODY_AND_HEADERS_VALUE),
                HttpLogDetailLevel.BODY_AND_HEADERS),

            // BODY_AND_HEADERS turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BODY_AND_HEADERS.name()),
                HttpLogDetailLevel.BODY_AND_HEADERS),

            // bOdY_aNd_HeAdErS turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration("bOdY_aNd_HeAdErS"), HttpLogDetailLevel.BODY_AND_HEADERS),

            // bodyandheaders turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration(HttpLogDetailLevel.BODYANDHEADERS_VALUE),
                HttpLogDetailLevel.BODY_AND_HEADERS),

            // BODYANDHEADERS turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration("BODYANDHEADERS"), HttpLogDetailLevel.BODY_AND_HEADERS),

            // bOdYaNdHeAdErS turns into BODY_AND_HEADERS
            Arguments.of(makeConfiguration("bOdYaNdHeAdErS"), HttpLogDetailLevel.BODY_AND_HEADERS)
        );
    }

    private static Configuration makeConfiguration(String detailLevelValue) {
        return new Configuration().put(Configuration.PROPERTY_AZURE_HTTP_LOG_DETAIL_LEVEL, detailLevelValue);
    }
}
