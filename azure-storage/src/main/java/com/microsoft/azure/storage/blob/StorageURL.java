/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.implementation.StorageClientImpl;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import com.microsoft.rest.v2.policy.DecodingPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Represents a URL to a Azure storage object.
 */
public abstract class StorageURL {

    protected final StorageClientImpl storageClient;

    protected StorageURL(URL url, HttpPipeline pipeline) {
        if (url == null) {
            throw new IllegalArgumentException("url cannot be null.");
        }
        if (pipeline == null) {
            throw new IllegalArgumentException("pipeline cannot be null.");
        }

        this.storageClient = new StorageClientImpl(pipeline).withVersion("2016-05-31");
        this.storageClient.withUrl(url.toString());
    }

    @Override
    public String toString() {
        return this.storageClient.url();
    }

    /**
     * @return
     *      The underlying url to the resource.
     */
    public URL toURL() {
        try {
            return new URL(this.storageClient.url());
        } catch (MalformedURLException e) {
            // TODO: remove and update getLeaseId.
        }
        return null;
    }

    /**
     * Appends a string to the end of a URL's path (prefixing the string with a '/' if required).
     * @param baseURL
     *      A {@code java.net.URL} to which the name should be appended.
     * @param name
     *      A {@code String} with the name to be appended.
     * @return
     *      A {@code String} with the name appended to the URL.
     */
    protected static URL appendToURLPath(URL baseURL, String name) throws MalformedURLException {
        UrlBuilder url = UrlBuilder.parse(baseURL.toString());
        if(url.path() == null) {
            url.withPath("/"); // .path() will return null if it is empty, so we have to process separately from below.
        }
        else if (url.path().charAt(url.path().length() - 1) != '/') {
            url.withPath(url.path() + '/');
        }
        url.withPath(url.path() + name);
        return new URL(url.toString()); // TODO: modify when toURL is released.
    }

    // TODO: Move this? Not discoverable.

    /**
     * Creates an pipeline to process the HTTP requests and Responses.
     *
     * @param credentials
     *      The credentials the pipeline will use to authenticate the requests.
     * @param pipelineOptions
     *      Configurations for each policy in the pipeline.
     * @return
     *      The pipeline.
     */
    public static HttpPipeline createPipeline(ICredentials credentials, PipelineOptions pipelineOptions) {
        /*
        PipelineOptions is mutable, but its fields refer to immutable objects. This method can pass the fields to other
        methods, but the PipelineOptions object itself can only be used for the duration of this call; it must not be
        passed to anything with a longer lifetime.
         */
        if (credentials == null) {
            throw new IllegalArgumentException(
                    "Credentials cannot be null. For anonymous access use Anonymous Credentials.");
        }

        // Closest to API goes first, closest to wire goes last.
        ArrayList<RequestPolicyFactory> factories = new ArrayList<>();
        factories.add(new TelemetryFactory(pipelineOptions.telemetryOptions));
        factories.add(new RequestIDFactory());
        factories.add(new RequestRetryFactory(pipelineOptions.requestRetryOptions));
        factories.add(new AddDatePolicy());
        if (!(credentials instanceof AnonymousCredentials)) {
            factories.add(credentials);
        }
        factories.add(new DecodingPolicyFactory());
        factories.add(new LoggingFactory(pipelineOptions.loggingOptions));

        return HttpPipeline.build(pipelineOptions.client,
                factories.toArray(new RequestPolicyFactory[factories.size()]));
    }

    // TODO: revisit.
    private static class AddDatePolicy implements RequestPolicyFactory {

        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
            return new AddDate(next);
        }

        public final class AddDate implements RequestPolicy {

            private final RequestPolicy next;
            public AddDate(RequestPolicy next) {
                this.next = next;
            }

            @Override
            public Single<HttpResponse> sendAsync(HttpRequest request) {
                request.headers().set(Constants.HeaderConstants.DATE, Utility.RFC1123GMTDateFormat.format(new Date()));
                return this.next.sendAsync(request);
            }
        }
    }
}
