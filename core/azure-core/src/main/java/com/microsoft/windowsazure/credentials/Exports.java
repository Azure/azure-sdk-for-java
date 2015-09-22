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
package com.microsoft.windowsazure.credentials;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Registry;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.util.Map;

public class Exports implements Builder.Exports {

    @Override
    public void register(Registry registry) {
        registry.add(SubscriptionCloudCredentials.class, CertificateCloudCredentials.class);
        registry.add(SubscriptionCloudCredentials.class, TokenCloudCredentials.class);

        registry.add(new Builder.Factory<SubscriptionCloudCredentials>() {
            @Override
            public <S> SubscriptionCloudCredentials create(String profile,
                                                    Class<S> service, Builder builder,
                                                    Map<String, Object> properties) {
                SubscriptionCloudCredentials credential = (SubscriptionCloudCredentials)properties.get(profile);
                if (!ManagementConfiguration.isPlayback()) {
                    credential.applyConfig(profile, properties);
                }
                return credential;
            }
        });
    }
}
