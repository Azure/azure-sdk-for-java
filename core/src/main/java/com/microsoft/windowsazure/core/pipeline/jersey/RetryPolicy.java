/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.microsoft.windowsazure.core.pipeline.jersey;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceResponseContext;

public abstract class RetryPolicy {
    public static final int DEFAULT_CLIENT_BACKOFF = 1000 * 30;
    public static final int DEFAULT_CLIENT_RETRY_COUNT = 3;
    public static final int DEFAULT_MAX_BACKOFF = 1000 * 90;
    public static final int DEFAULT_MIN_BACKOFF = 100 * 3;

    public abstract boolean shouldRetry(int retryCount,
            ServiceResponseContext response, Exception error);

    public abstract int calculateBackoff(int retryCount,
            ServiceResponseContext response, Exception error);
}
