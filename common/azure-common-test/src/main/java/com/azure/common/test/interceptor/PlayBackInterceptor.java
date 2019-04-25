// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.interceptor;

import com.azure.common.test.models.NetworkCallRecord;
import com.azure.common.test.models.RecordedData;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static com.azure.common.test.utils.DataTransformer.applyReplacementRule;
import static com.azure.common.test.utils.DataTransformer.removeHost;

/**
 * The network interceptor for the existing test session recorder.
 */
public class PlayBackInterceptor implements Interceptor {
    private RecordedData recordedData;
    private Map<String, String> textReplacementRules;

    /**
     * Constructor of PlayBackInterceptor
     *
     * @param recordedData The recorded data to intercept.
     * @param textReplacementRules The text replacement rule of intercepting the recorded data.
     */
    public PlayBackInterceptor(final RecordedData recordedData, final Map<String, String> textReplacementRules) {
        this.recordedData = recordedData;
        this.textReplacementRules = textReplacementRules;
    }

    /**
     * Find the {@link NetworkCallRecord} from {@link RecordedData} which matches the request parsing URI.
     * Build and return the response from the {@link NetworkCallRecord}
     *
     * @param chain The network call chain with rules
     * @return The response after interception
     * @throws IOException Throw IOException if no network call found in session-records.
     */
    @Override
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        String incomingUrl = applyReplacementRule(request.url().toString(), textReplacementRules);
        String incomingMethod = request.method();

        incomingUrl = removeHost(incomingUrl);
        NetworkCallRecord networkCallRecord = null;
        synchronized (recordedData) {
            for (Iterator<NetworkCallRecord> iterator = recordedData.getNetworkCallRecords().iterator(); iterator.hasNext();) {
                NetworkCallRecord record = iterator.next();
                if (record.method().equalsIgnoreCase(incomingMethod) && removeHost(record.uri()).equalsIgnoreCase(incomingUrl)) {
                    networkCallRecord = record;
                    iterator.remove();
                    break;
                }
            }
        }

        if (networkCallRecord == null) {
            System.out.println("NOT FOUND - " + incomingMethod + " " + incomingUrl);
            System.out.println("Remaining records " + recordedData.getNetworkCallRecords().size());
            throw new IOException("==> Unexpected request: " + incomingMethod + " " + incomingUrl);
        }

        int recordStatusCode = Integer.parseInt(networkCallRecord.response().get("StatusCode"));

        Response originalResponse = chain.proceed(request);
        originalResponse.body().close();

        Response.Builder responseBuilder = originalResponse.newBuilder()
                                               .code(recordStatusCode).message("-");

        for (Map.Entry<String, String> pair : networkCallRecord.response().entrySet()) {
            if (!pair.getKey().equals("StatusCode") && !pair.getKey().equals("Body") && !pair.getKey().equals("Content-Length")) {
                String rawHeader = pair.getValue();
                for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                    if (rule.getValue() != null) {
                        rawHeader = rawHeader.replaceAll(rule.getKey(), rule.getValue());
                    }
                }
                responseBuilder.addHeader(pair.getKey(), rawHeader);
            }
        }

        String rawBody = networkCallRecord.response().get("Body");
        if (rawBody != null) {
            for (Map.Entry<String, String> rule : textReplacementRules.entrySet()) {
                if (rule.getValue() != null) {
                    rawBody = rawBody.replaceAll(rule.getKey(), rule.getValue());
                }
            }

            String rawContentType = networkCallRecord.response().get("content-type");
            String contentType =  rawContentType == null
                                      ? "application/json; charset=utf-8"
                                      : rawContentType;

            ResponseBody responseBody = ResponseBody.create(MediaType.parse(contentType), rawBody.getBytes(StandardCharsets.UTF_8));
            responseBuilder.body(responseBody);
            responseBuilder.addHeader("Content-Length", String.valueOf(rawBody.getBytes(StandardCharsets.UTF_8).length));
        }

        Response newResponce = responseBuilder.build();

        return newResponce;
    }
}
