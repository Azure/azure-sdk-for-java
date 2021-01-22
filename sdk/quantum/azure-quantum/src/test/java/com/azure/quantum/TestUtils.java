package com.azure.quantum;

public class TestUtils {

    public static QuantumClient getClient() {
        return new QuantumClient("subscriptionId",
            "resourceGroupName", "workspaceName", "https://www.contoso.com");
    }
}
