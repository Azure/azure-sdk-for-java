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
package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.ISO8601DateConverter;
import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.services.blob.implementation.BlobExceptionProcessor;
import com.microsoft.windowsazure.services.blob.implementation.BlobRestProxy;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyFilter;
import com.microsoft.windowsazure.services.blob.implementation.SharedKeyLiteFilter;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {
        registry.add(BlobContract.class, BlobExceptionProcessor.class);
        registry.add(BlobExceptionProcessor.class);
        registry.add(BlobRestProxy.class);
        registry.add(SharedKeyLiteFilter.class);
        registry.add(SharedKeyFilter.class);
        registry.add(ISO8601DateConverter.class);
        registry.add(UserAgentFilter.class);
    }
}
