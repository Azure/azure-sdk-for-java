// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpUtilsTest {

    private static final String OWNER_FULL_NAME_VALUE = "dbs/RxJava.SDKTest.SharedDatabase_20190304T121302_iZc/colls/+%20-_,:.%7C~b2d67001-9000-454e-a140-abceb1756c48%20+-_,:.%7C~";

    @Test(groups = { "unit" })
    public void verifyConversionOfHttpResponseHeadersToMap() {
        HttpHeaders headersMap = new HttpHeaders();
        headersMap.put(HttpConstants.Headers.OWNER_FULL_NAME, OWNER_FULL_NAME_VALUE);

        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.headers()).thenReturn(headersMap);
        HttpHeaders httpResponseHeaders = httpResponse.headers();

        assertThat(httpResponseHeaders.getSize()).isEqualTo(1);
        HttpHeader entry = httpResponseHeaders.iterator().next();
        assertThat(entry.getName()).isEqualTo(HttpConstants.Headers.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));

        HttpUtils.unescapeOwnerFullName(httpResponseHeaders);
        assertThat(httpResponseHeaders.getSize()).isEqualTo(1);
        entry = httpResponseHeaders.iterator().next();
        assertThat(entry.getName()).isEqualTo(HttpConstants.Headers.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));
    }
}
