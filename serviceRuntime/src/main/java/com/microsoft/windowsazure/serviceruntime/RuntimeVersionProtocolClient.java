/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 * 
 */
class RuntimeVersionProtocolClient {
    private final InputChannel inputChannel;

    public RuntimeVersionProtocolClient(InputChannel inputChannel) {
        this.inputChannel = inputChannel;
    }

    public Map<String, String> getVersionMap(String connectionPath) {
        try {
            Map<String, String> versions = new HashMap<String, String>();
            JAXBContext context = JAXBContext
                    .newInstance(RuntimeServerDiscoveryInfo.class.getPackage()
                            .getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            InputStream input = inputChannel.getInputStream(connectionPath);

            @SuppressWarnings("unchecked")
            RuntimeServerDiscoveryInfo discoveryInfo = ((JAXBElement<RuntimeServerDiscoveryInfo>) unmarshaller
                    .unmarshal(input)).getValue();

            for (RuntimeServerEndpointInfo endpointInfo : discoveryInfo
                    .getRuntimeServerEndpoints().getRuntimeServerEndpoint()) {
                versions.put(endpointInfo.getVersion(), endpointInfo.getPath());
            }

            return versions;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
