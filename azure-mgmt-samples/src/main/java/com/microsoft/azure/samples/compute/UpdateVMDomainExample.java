/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.samples.compute;

import com.microsoft.azure.management.network.NetworkResourceProviderClient;
import com.microsoft.azure.management.network.NetworkResourceProviderService;
import com.microsoft.azure.utility.NetworkHelper;
import com.microsoft.windowsazure.Configuration;

public class UpdateVMDomainExample {
    /**
     * To add a simple domain name like {domainPrefix}.{region}.cloudapp.azure.com to an existing publicIp which
     * is one associated with your VM.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Configuration config = CreateVMExample.createConfiguration();
        NetworkResourceProviderClient networkResourceProviderClient = NetworkResourceProviderService.create(config);

        String resourceGroupName = "rgName";
        String publicIpName = "publicIpName";
        String domainPrefix = "domainPrefix";

        try {
            NetworkHelper.updatePublicIpAddressDomainName(
                    networkResourceProviderClient, resourceGroupName, publicIpName, domainPrefix);

            System.out.println(publicIpName + " is updated");
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }
}
