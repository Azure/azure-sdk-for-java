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

package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.AnonymousCredentials
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.RequestRetryFactory
import com.microsoft.azure.storage.blob.RequestRetryOptions
import com.microsoft.azure.storage.blob.RetryPolicyType
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.SignedIdentifier
import com.microsoft.azure.storage.blob.models.StorageErrorCode
import com.microsoft.azure.storage.blob.models.StorageErrorException
import com.microsoft.rest.v2.http.HttpHeaders
import com.microsoft.rest.v2.http.HttpMethod
import com.microsoft.rest.v2.http.HttpPipeline
import com.microsoft.rest.v2.http.HttpRequest
import com.microsoft.rest.v2.http.HttpResponse
import com.microsoft.rest.v2.policy.RequestPolicyFactory
import io.reactivex.Flowable
import spock.lang.Shared

import java.time.OffsetDateTime

class HelperTest extends APISpec {

    static URL retryTestURL = new URL("http://" + RequestRetryTestFactory.RETRY_TEST_PRIMARY_HOST)
    static RequestRetryOptions retryTestOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 6,
            2,1000, 4000, RequestRetryTestFactory.RETRY_TEST_SECONDARY_HOST)

    def "responseError"() {
        when:
        cu.listBlobsFlatSegment("garbage", null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.errorCode() == StorageErrorCode.INVALID_QUERY_PARAMETER_VALUE
        e.statusCode() == 400
        e.message().contains("Value for one of the query parameters specified in the request URI is invalid.")
        e.getMessage().contains("<?xml") // Ensure that the details in the payload are printable
    }

    def "Retries until success"() {
        setup:
        HttpPipeline pipeline = HttpPipeline.build(new RequestRetryFactory(retryTestOptions),
                new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS,
                        retryTestOptions.maxTries))

        when:
        HttpResponse response = pipeline.sendRequestAsync(new HttpRequest(null, HttpMethod.GET,
                retryTestURL, new HttpHeaders(),
                Flowable.just(RequestRetryTestFactory.RETRY_TEST_DEFAULT_DATA), null)).blockingGet()

        then:
        response != null
        response.statusCode() == 200
    }

    def "Retries until max retries"() {
        setup:
        RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES, retryTestOptions.maxTries)
        HttpPipeline pipeline = HttpPipeline.build(new RequestRetryFactory(retryTestOptions), retryTestFactory)

        when:
        pipeline.sendRequestAsync(new HttpRequest(null, HttpMethod.GET, retryTestURL, new HttpHeaders(),
                Flowable.just(RequestRetryTestFactory.RETRY_TEST_DEFAULT_DATA), null)).blockingGet()

        then:
        def e = thrown(StorageErrorException)
        e.response().statusCode() == 500
        retryTestFactory.tryNumber == retryTestOptions.maxTries
    }
}
