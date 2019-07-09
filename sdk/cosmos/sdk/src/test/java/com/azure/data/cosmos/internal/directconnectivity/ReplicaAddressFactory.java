/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
