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

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.services.queue.implementation.QueueExceptionProcessor;
import com.microsoft.windowsazure.services.queue.implementation.QueueRestProxy;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.queue.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {
        registry.add(QueueContract.class, QueueExceptionProcessor.class);
        registry.add(QueueExceptionProcessor.class);
        registry.add(QueueRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
        registry.add(SharedKeyFilter.class);
        registry.add(UserAgentFilter.class);
    }
}
