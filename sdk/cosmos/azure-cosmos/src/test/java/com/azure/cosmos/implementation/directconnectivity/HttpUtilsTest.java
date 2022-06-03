// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpResponse;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpUtilsTest {

    private static final String OWNER_FULL_NAME_VALUE = "dbs/RxJava.SDKTest.SharedDatabase_20190304T121302_iZc/colls/+%20-_,:.%7C~b2d67001-9000-454e-a140-abceb1756c48%20+-_,:.%7C~";
    
    @Test(groups = { "unit" })
    public void verifyConversionOfHttpResponseHeadersToMap() {
        HttpHeaders headersMap = new HttpHeaders(1);
        headersMap.set(HttpConstants.HttpHeaders.OWNER_FULL_NAME, OWNER_FULL_NAME_VALUE);

        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.headers()).thenReturn(headersMap);
        HttpHeaders httpResponseHeaders = httpResponse.headers();
        Set<Entry<String, String>> resultHeadersSet = HttpUtils.asMap(httpResponseHeaders).entrySet();
        
        assertThat(resultHeadersSet.size()).isEqualTo(1);
        Entry<String, String> entry = resultHeadersSet.iterator().next();
        assertThat(entry.getKey()).isEqualTo(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));

        List<Entry<String, String>> resultHeadersList = HttpUtils.unescape(httpResponseHeaders.toMap().entrySet());
        assertThat(resultHeadersList.size()).isEqualTo(1);
        entry = resultHeadersSet.iterator().next();
        assertThat(entry.getKey()).isEqualTo(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));
    }
}
