// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PerProtocolPartitionAddressInformationTests {
    private final static DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();

    @Test(groups = "unit", expectedExceptions = GoneException.class)
    public void getPrimaryUri_NoAddress() {
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation> replicaAddresses = new ArrayList<>();
        PerProtocolPartitionAddressInformation protocolPartitionAddressInformation =
                new PerProtocolPartitionAddressInformation(replicaAddresses, Protocol.HTTPS);

        protocolPartitionAddressInformation.getPrimaryAddressUri(request);
    }

    @Test(groups = "unit", expectedExceptions = GoneException.class, expectedExceptionsMessageRegExp =
            ".*\"innerErrorMessage\":\"The requested resource is no longer available at the server. Returned addresses are .*https://cosmos1/,https://cosmos2/}.*")
    public void getPrimaryUri_NoPrimaryAddress() throws Exception {
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos2", Protocol.HTTPS));

        PerProtocolPartitionAddressInformation protocolPartitionAddressInformation =
                new PerProtocolPartitionAddressInformation(replicaAddresses, replicaAddresses.get(0).getProtocol());

        protocolPartitionAddressInformation.getPrimaryAddressUri(request);
    }

    @Test(groups = "unit")
    public void getPrimaryUri() {
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        PerProtocolPartitionAddressInformation protocolPartitionAddressInformation =
                new PerProtocolPartitionAddressInformation(replicaAddresses, replicaAddresses.get(0).getProtocol());

        Uri res = protocolPartitionAddressInformation.getPrimaryAddressUri(request);
        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }

    @Test(groups = "unit")
    public void getPrimaryUri_WithRequestReplicaIndex() {
        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(1).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        PerProtocolPartitionAddressInformation protocolPartitionAddressInformation =
                new PerProtocolPartitionAddressInformation(replicaAddresses, replicaAddresses.get(0).getProtocol());

        Uri res = protocolPartitionAddressInformation.getPrimaryAddressUri(request);
        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }
}
