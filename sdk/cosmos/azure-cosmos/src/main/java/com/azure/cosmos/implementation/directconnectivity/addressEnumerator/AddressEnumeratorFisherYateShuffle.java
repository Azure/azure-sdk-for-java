// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.addressEnumerator;

import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.util.Collections.swap;

public class AddressEnumeratorFisherYateShuffle {
    private static Random random = new Random();

    public static List<Uri> getTransportAddressUris(List<Uri> addresses) {
        checkNotNull(addresses, "Argument 'addresses' should not be null");

        // Fisher Yates Shuffle algorithm: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
        List<Uri> addressesCopy = new ArrayList<>(addresses);

        for (int i = addressesCopy.size(); i > 0; i--) {
            int randomIndex = random.nextInt(i);
            swap(addressesCopy, i - 1, randomIndex);
        }

        return addressesCopy;
    }
}
