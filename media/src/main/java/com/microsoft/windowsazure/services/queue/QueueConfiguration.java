/**
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
package com.microsoft.windowsazure.services.queue;

/**
 * A class that contains static strings used to identify parts of a service
 * configuration instance associated with the Windows Azure Queue service.
 * <p>
 * These values must not be altered.
 */
public abstract class QueueConfiguration {
    public static final String ACCOUNT_NAME = "queue.accountName";
    public static final String ACCOUNT_KEY = "queue.accountKey";
    public static final String URI = "queue.uri";
}
