/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 */
class RuntimeVersionManager {
    private final RuntimeVersionProtocolClient protocolClient;
    private final List<RuntimeClientFactory> supportedVersionList;

    RuntimeVersionManager(RuntimeVersionProtocolClient protocolClient) {
        this.protocolClient = protocolClient;

        this.supportedVersionList = new ArrayList<RuntimeClientFactory>(1);

        this.supportedVersionList.add(new RuntimeClientFactory() {
            public String getVersion() {
                return "2011-03-08";
            }

            public RuntimeClient createRuntimeClient(String path) {
                RuntimeKernel kernel = RuntimeKernel.getKernel();

                return new Protocol1RuntimeClient(kernel
                        .getProtocol1RuntimeGoalStateClient(), kernel
                        .getProtocol1RuntimeCurrentStateClient(), path);
            }
        });
    }

    public RuntimeClient getRuntimeClient(String versionEndpoint) {
        Map<String, String> versionMap = protocolClient
                .getVersionMap(versionEndpoint);

        for (RuntimeClientFactory factory : supportedVersionList) {
            if (versionMap.containsKey(factory.getVersion())) {
                return factory.createRuntimeClient(versionMap.get(factory
                        .getVersion()));
            }
        }

        throw new RuntimeException(
                "Server does not support any known protocol versions.");
    }
}
