// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.implementation.ConnectionPolicy;
import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.testng.annotations.Test;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
public class RntbdServiceEndpointTest {

    @Test(groups = { "unit" })
    public void endpointCloseOnIdleEndpointTimeout() throws Exception {
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        directConnectionConfig.setIdleEndpointTimeout(Duration.ofSeconds(20));
        ConnectionPolicy connectionPolicy = new ConnectionPolicy(directConnectionConfig);
        final RntbdTransportClient.Options options = new RntbdTransportClient.Options.Builder(connectionPolicy).build();
        final SslContext sslContext = SslContextBuilder.forClient().build();
        RntbdTransportClient transportClient = new RntbdTransportClient(options, sslContext, null);
        URI uri = new URI(TestConfigurations.HOST);
        List<Uri> uriList = new ArrayList<>();

        RntbdEndpoint.Provider endpointProvider = (RntbdEndpoint.Provider) FieldUtils.readField(transportClient, "endpointProvider", true);
        for(int i = 0;i <10;i++) {
            int port=uri.getPort()+i;
            Uri physicalAddress = new Uri("rntbd://"
                + uri.getHost()+":"+port
                + "/apps/82691bd9-bcdd-44c4-b8e3-0722d357b80d/services/dc15e65d-9fb3-46cb-83e0-765faeec8855/partitions/fd9d234b-cdf5-47a0-a495-99f2fb19e874/replicas/132542487074124709p/"
            );
            uriList.add(physicalAddress);

            //Adding endpoints to provider
            endpointProvider.get(physicalAddress.getURI());
        }
        //Asserting no eviction yet
        assertThat(endpointProvider.evictions()).isEqualTo(0);

        for(int i = 0;i <5;i++) {
            RntbdEndpoint rntbdEndpoint = endpointProvider.get(uriList.get(i).getURI());
            assertThat(rntbdEndpoint.isClosed()).isFalse();
            rntbdEndpoint.close();
            assertThat(rntbdEndpoint.isClosed()).isTrue();
        }

        //5 endpoints were explicitly  closed above
        assertThat(endpointProvider.evictions()).isEqualTo(5);
        Thread.sleep(30000);

        //Remaining 5 were closed due to IdleEndpointTimeout set to 20 sec
        assertThat(endpointProvider.evictions()).isEqualTo(10);
    }
}
