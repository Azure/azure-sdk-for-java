/**
 * 
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
