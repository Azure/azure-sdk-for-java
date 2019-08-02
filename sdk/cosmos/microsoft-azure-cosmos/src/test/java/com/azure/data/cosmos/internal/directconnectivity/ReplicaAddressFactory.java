// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.directconnectivity.AddressInformation;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import com.azure.data.cosmos.internal.directconnectivity.AddressInformation;
import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReplicaAddressFactory {
    private static String TEMPLATE = "https://by4prdddc03-docdb-1.documents.azure.com:9056" +
            "/apps/%s/services/e7c8d429-c379-40c9-9486-65b89b70be2f" +
            "/partitions/%s/replicas/%s/";

    public static String createPartitionPhysicalURI(String partitionId, boolean isPrimary) {
        return String.format(TEMPLATE, UUID.randomUUID(), partitionId, RandomStringUtils.randomNumeric(18) + (isPrimary ? "p" : "s"));
    }

    public static String createPrimaryPhysicalURI(String partitionId) {
        return createPartitionPhysicalURI(partitionId, true);
    }

    public static String createSecondaryPhysicalURI(String partitionId) {
        return createPartitionPhysicalURI(partitionId, false);
    }

    public static AddressInformation createAddressInformation(String partitionId, boolean isPrimary, Protocol protocol) {
        String loc = createPartitionPhysicalURI(partitionId, isPrimary);
        return new AddressInformation(true, isPrimary, loc, protocol);
    }

    public static List<AddressInformation> createPartitionAddressInformation(String partitionId,
                                                                             boolean includePrimary,
                                                                             int numberOfAllReplicas,
                                                                             Protocol protocol) {
        List<AddressInformation> addressInformationList = new ArrayList<>();
        for (boolean isPrimary = includePrimary; numberOfAllReplicas > 0; numberOfAllReplicas--) {
            addressInformationList.add(createAddressInformation(partitionId, isPrimary, protocol));
            isPrimary = false;
        }

        return addressInformationList;
    }
}
