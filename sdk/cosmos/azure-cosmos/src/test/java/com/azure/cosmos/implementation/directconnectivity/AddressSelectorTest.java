// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.DiagnosticsClientContext;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static com.azure.cosmos.implementation.TestUtils.mockDocumentServiceRequest;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AddressSelectorTest {
    private final static DiagnosticsClientContext clientContext = mockDiagnosticsClientContext();

    @Test(groups = "unit")
    public void resolvePrimaryUriAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito
                .doReturn(Mono.just(new PartitionAddressInformation(replicaAddresses)))
                .when(addressResolver)
                .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), ArgumentMatchers.eq(false));

        Uri res = selector.resolvePrimaryUriAsync(request, false).block();

        assertThat(res).isEqualTo(Uri.create("https://cosmos2/"));
    }

    @Test(groups = "unit")
    public void resolveAllUriAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(new PartitionAddressInformation(replicaAddresses)))
               .when(addressResolver)
               .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), ArgumentMatchers.eq(false));

        List<Uri> res = selector.resolveAllUriAsync(request, true, false).block();

        assertThat(res).isEqualTo(ImmutableList.of(Uri.create("https://cosmos1/"), Uri.create("https://cosmos2/"), Uri.create("https://cosmos3/")));
    }

    @Test(groups = "unit")
    public void resolveAddressesAsync() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.HTTPS);

        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos4", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos5", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(new PartitionAddressInformation(replicaAddresses)))
               .when(addressResolver)
               .resolveAsync(Mockito.any(RxDocumentServiceRequest.class), ArgumentMatchers.eq(false));

        PerProtocolPartitionAddressInformation res = selector.resolveAddressesAsync(request, false).block();

        assertThat(res.getTransportAddressUris()).isEqualTo(
                replicaAddresses
                    .stream()
                    .filter(a -> a.getProtocolName().equalsIgnoreCase(Protocol.HTTPS.toString()))
                    .map(addressInformation -> addressInformation.getPhysicalUri())
                    .collect(Collectors.toList()));
    }

    @Test(groups = "unit")
    public void resolveAllUriAsync_RNTBD() {
        IAddressResolver addressResolver = Mockito.mock(IAddressResolver.class);
        AddressSelector selector = new AddressSelector(addressResolver, Protocol.TCP);

        RxDocumentServiceRequest request = mockDocumentServiceRequest(clientContext);
        Mockito.doReturn(null).when(request).getDefaultReplicaIndex();

        List<AddressInformation>  replicaAddresses = new ArrayList<>();

        replicaAddresses.add(new AddressInformation(true, false, "rntbd://cosmos1", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, true, "rntbd://cosmos2", Protocol.TCP));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos1", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, true, "https://cosmos2", Protocol.HTTPS));
        replicaAddresses.add(new AddressInformation(true, false, "https://cosmos3", Protocol.HTTPS));

        Mockito.doReturn(Mono.just(new PartitionAddressInformation(replicaAddresses)))
               .when(addressResolver).resolveAsync(Mockito.any(RxDocumentServiceRequest.class), ArgumentMatchers.eq(false));

        List<Uri> res = selector.resolveAllUriAsync(request, true, false).block();

        assertThat(res).isEqualTo(ImmutableList.of(Uri.create("rntbd://cosmos1/"), Uri.create("rntbd://cosmos2/")));
    }

}
