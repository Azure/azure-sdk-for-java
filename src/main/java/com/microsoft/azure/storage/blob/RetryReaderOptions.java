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

/**
 * {@code RetryReaderOptions} contains properties which help a {@link RetryReader} determine when to retry.
 */
public final class RetryReaderOptions {
    /**
     * Specifies the maximum number of additional HTTP Get requests that will be made while reading the data from a
     * {@link RetryReader}. Note that if no initial response is provided, the {@code RetryReader} will call the getter
     * to obtain an initial response to read from; this initial response does not count as a retry. A value of {@code 0}
     *  is the default and means that no additional HTTP requests will be made once the initial request has been made or
     *  provided.
     */
    public int maxRetryRequests = 0;
}
