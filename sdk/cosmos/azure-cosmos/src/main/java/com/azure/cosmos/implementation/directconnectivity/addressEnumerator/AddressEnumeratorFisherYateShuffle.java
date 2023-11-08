// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.addressEnumerator;

import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.util.Collections.swap;

public class AddressEnumeratorFisherYateShuffle {
    public static List<Uri> getTransportAddressUris(List<Uri> addresses) {
        checkNotNull(addresses, "Argument 'addresses' should not be null");

        // Fisher Yates Shuffle algorithm: https://en.wikipedia.org/wiki/Fisher%E2%80%93Yates_shuffle
        List<Uri> addressesCopy = new ArrayList<>(addresses);

        for (int i = addressesCopy.size(); i > 0; i--) {
            int randomIndex = generateNextRandom(i);
            swap(addressesCopy, i - 1, randomIndex);
        }

        return addressesCopy;
    }

    private static int generateNextRandom(int maxValue) {
        // The benefit of using ThreadLocalRandom.current() over Random is
        // avoiding the synchronization contention due to multi-threading.
        return ThreadLocalRandom.current().nextInt(maxValue);
    }
}
