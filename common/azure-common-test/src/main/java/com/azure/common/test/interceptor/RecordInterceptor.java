// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.interceptor;

import com.azure.common.test.models.NetworkCallRecord;
import com.azure.common.test.models.RecordedData;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.azure.common.test.utils.DataTransformer.applyReplacementRule;
import static com.azure.common.test.utils.DataTransformer.extractResponseData;

/**
 * The interceptor for newly recorded data.
 */
public class RecordInterceptor implements Interceptor {

    private final RecordedData recordedData;
    private final Map<String, String> textReplacementRules;

    /**
     * The constructor of RecordInterceptor
     *
     * @param recordedData The recorded data to intercept with.
     * @param textReplacementRules The text replacement rule of intercepting the recorded data.
     */
    public RecordInterceptor(final RecordedData recordedData, final Map<String, String> textReplacementRules) {
        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules;
    }

    /**
     * Get response from Interceptor chain request.
     * Meanwhile, build a new {@link NetworkCallRecord} using the response and then put it into {@link RecordedData}.
     *
     * @param chain The network call chain with rules
     * @return The response after interception
     * @throws IOException Throw IOException if no response received from new network request call.
     */
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        NetworkCallRecord networkCallRecord = new NetworkCallRecord();

        networkCallRecord.headers(new HashMap<>());

        if (request.header("Content-Type") != null) {
            networkCallRecord.headers().put("Content-Type", request.header("Content-Type"));
        }
        if (request.header("x-ms-version") != null) {
            networkCallRecord.headers().put("x-ms-version", request.header("x-ms-version"));
        }
        if (request.header("User-Agent") != null) {
            networkCallRecord.headers().put("User-Agent", request.header("User-Agent"));
        }

        networkCallRecord.method(request.method());
        networkCallRecord.uri(applyReplacementRule(request.url().toString().replaceAll("\\?$", ""), textReplacementRules));

        Response response = chain.proceed(request);

        networkCallRecord.response(new HashMap<>());
        networkCallRecord.response().put("StatusCode", Integer.toString(response.code()));
        extractResponseData(networkCallRecord.response(), response, textReplacementRules);

        // remove pre-added header if this is a waiting or redirection
        if (!(networkCallRecord.response().containsKey("Body") && networkCallRecord.response().get("Body").contains("<Status>InProgress</Status>"))
                && !(Integer.parseInt(networkCallRecord.response().get("StatusCode")) == HttpStatus.SC_TEMPORARY_REDIRECT)) {
            synchronized (recordedData.getNetworkCallRecords()) {
                recordedData.getNetworkCallRecords().add(networkCallRecord);
            }
        }

        return response;
    }
}
