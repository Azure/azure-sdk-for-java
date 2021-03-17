package com.azure.iot.deviceupdate.sample;

public class Program {
    public static void main(String[] args) throws Exception {
        String tenantId = System.getenv("AZURE_TENANT_ID");
        String clientId = System.getenv("AZURE_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_CLIENT_SECRET");
        String storageName = System.getenv("AZURE_STORAGE_NAME");
        String storageKey = System.getenv("AZURE_STORAGE_KEY");
        String accountEndpoint = System.getenv("AZURE_ACCOUNT_ENDPOINT");
        String instanceId = System.getenv("AZURE_INSTANCE_ID");
        String deviceId = System.getenv("AZURE_DEVICE_ID");
        String deviceTag = deviceId;

        System.out.println("Device Update for IoT Hub client library for Java sample");
        System.out.println();

        Runner runner = new Runner(tenantId, clientId, clientSecret, accountEndpoint, instanceId, storageName, storageKey, deviceId, deviceTag, false);
        runner.Run();

        System.out.println("Finished.");
    }
}
