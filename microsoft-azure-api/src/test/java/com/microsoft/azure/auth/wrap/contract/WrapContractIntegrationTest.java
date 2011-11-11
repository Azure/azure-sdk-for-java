package com.microsoft.azure.auth.wrap.contract;

import org.junit.Test;

import com.microsoft.azure.configuration.Configuration;
import com.sun.jersey.api.client.Client;

public class WrapContractIntegrationTest {
    @Test
    public void yaaaaargh() throws Exception {
        Configuration config = new Configuration();

        WrapContract contract = new WrapContractImpl(config.create(Client.class));

        contract.post(
                "https://lodejard-sb.accesscontrol.windows.net/WRAPv0.9",
                "owner",
                "Zo3QCZ5jLlJofibEiifZyz7B3x6a5Suv2YoS1JAWopA=",
                "http://lodejard.servicebus.windows.net");
    }
}
