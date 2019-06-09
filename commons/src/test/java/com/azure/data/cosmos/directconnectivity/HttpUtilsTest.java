/*
 * The MIT License (MIT)
 * Copyright (c) 2019 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos.directconnectivity;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.testng.annotations.Test;

import com.azure.data.cosmos.internal.HttpConstants;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;

public class HttpUtilsTest {

    private static final String OWNER_FULL_NAME_VALUE = "dbs/RxJava.SDKTest.SharedDatabase_20190304T121302_iZc/colls/+%20-_,:.%7C~b2d67001-9000-454e-a140-abceb1756c48%20+-_,:.%7C~";
    
    @Test(groups = { "unit" })
    public void verifyConversionOfHttpResponseHeadersToMap() throws UnsupportedEncodingException {
        HttpHeaders headersMap = new DefaultHttpHeaders();
        headersMap.add(HttpConstants.HttpHeaders.OWNER_FULL_NAME, OWNER_FULL_NAME_VALUE);

        HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_0,
                HttpResponseStatus.ACCEPTED,
                headersMap);
        HttpResponseHeaders httpResponseHeaders = new HttpClientResponse(httpResponse, null).getHeaders();
        Set<Entry<String, String>> resultHeadersSet = HttpUtils.asMap(httpResponseHeaders).entrySet();
        
        assertThat(resultHeadersSet.size()).isEqualTo(1);
        Entry<String, String> entry = resultHeadersSet.iterator().next();
        assertThat(entry.getKey()).isEqualTo(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));
        
        List<Entry<String, String>> resultHeadersList = HttpUtils.unescape(httpResponseHeaders.entries());
        assertThat(resultHeadersList.size()).isEqualTo(1);
        entry = resultHeadersSet.iterator().next();
        assertThat(entry.getKey()).isEqualTo(HttpConstants.HttpHeaders.OWNER_FULL_NAME);
        assertThat(entry.getValue()).isEqualTo(HttpUtils.urlDecode(OWNER_FULL_NAME_VALUE));
    }
}
