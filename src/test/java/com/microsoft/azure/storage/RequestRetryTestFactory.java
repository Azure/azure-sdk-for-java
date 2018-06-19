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

package com.microsoft.azure.storage;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.policy.RequestPolicy;
import io.reactivex.Single;

public class RequestRetryTestFactory {
    public static final int  RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS = 1;

    public static final int  RETRY_TEST_SCENARIO_RETRY_UNTIL_OPERATION_CANCEL = 2;

    public static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES = 3;

    private static final String RETRY_TEST_PRIMARY_HOST = "PrimaryDC";

    private static final String RETRY_TEST_SECONDARY_HOST = "SecondaryDC";

    private int retryTestScenario;

    private int maxRetries;

    private int tryNumber;

    private final class RetryTestPolicy implements RequestPolicy{
        private RequestPolicy nextPolicy;
        private RequestRetryTestFactory factory;


        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            this.factory.tryNumber++;
            if (this.factory.tryNumber > this.factory.maxRetries) {
                return Single.error(new IllegalArgumentException("Try number has exceeded max tries"));
            }

            // Validate the expected preconditions for each try: The correct host is used.
            String expectedHost = RETRY_TEST_PRIMARY_HOST;
            if (this.factory.tryNumber%2 == 0) {
                /*
                 The retry until success scenario fail's on the 4th try with a 404 on the secondary, so we never expect
                 it to check the secondary after that. All other tests should continue to check the secondary.
                 */
                if (this.factory.retryTestScenario != RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS ||
                        this.factory.tryNumber <= 4) {
                    expectedHost = RETRY_TEST_SECONDARY_HOST;
                }
            }

            if (!request.url().getPath().equals(expectedHost)) {
                throw new IllegalArgumentException("The host does not match the expected host");
            }
        }
    }

    private final class retryError {
        private boolean temporary;
        private boolean timeout;

        public boolean getTemporary() {
            return this.temporary;
        }

        public boolean getTimeout() {
            return this.temporary;
        }

        public String toString() {
            return String.format("Temporary=%b, Timeout = %b", this.temporary, this.timeout);
        }
    }


}
