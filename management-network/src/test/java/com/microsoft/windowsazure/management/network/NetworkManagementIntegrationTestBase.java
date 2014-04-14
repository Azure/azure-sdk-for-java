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
package com.microsoft.windowsazure.management.network;

import com.microsoft.windowsazure.management.configuration.*;
import com.microsoft.windowsazure.*;

public abstract class NetworkManagementIntegrationTestBase {

    protected static NetworkManagementClient networkManagementClient;	

    protected static void createService() throws Exception {
        // reinitialize configuration from known state
        Configuration config = createConfiguration();      
        networkManagementClient = NetworkManagementService.create(config);
    }

    protected static Configuration createConfiguration() throws Exception {
//        return ManagementConfiguration.configure(
//                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
//                System.getenv(ManagementConfiguration.KEYSTORE_PATH),
//                System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD)
//        );        
        return PublishSettingsLoader.createManagementConfiguration("C:\\Users\\xuezhain\\Downloads\\node-Azpad057T7N4266-6-28-2013-credentials.publishsettings","00977cdb-163f-435f-9c32-39ec8ae61f4d");        
    }
}