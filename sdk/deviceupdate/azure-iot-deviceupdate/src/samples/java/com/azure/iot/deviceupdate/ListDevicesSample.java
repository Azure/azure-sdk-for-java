package com.azure.iot.deviceupdate;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

public class ListDevicesSample {
    public static void main(String[] args) {
        // BEGIN: com.azure.iot.deviceupdate.ManagementAsyncClient.instantiate
        ManagementAsyncClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildManagementAsyncClient();
        // END: com.azure.iot.deviceupdate.ManagementAsyncClient.instantiate

        PagedFlux<BinaryData> response = client.listDevices(null);

        System.out.println(response.toStream().count());
    }
}
