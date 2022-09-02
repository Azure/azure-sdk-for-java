// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.addressEnumerator;

import com.azure.cosmos.implementation.directconnectivity.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class AddressEnumeratorUsingPermutations {

    private static AtomicReference<List<List<List<Integer>>>> allPermutations = new AtomicReference<>(null);

    static {
        if (allPermutations.compareAndSet(null, new ArrayList<>())) {
            for (int i = 0; i <= 6; i++) {
                List<List<Integer>> permutations = new ArrayList<>();
                permuteIndexPositions(IntStream.range(0, i).toArray(), 0, i, permutations);
                allPermutations.get().add(permutations);
            }
        }
    }

    public static List<Uri> getTransportAddressUris(List<Uri> addresses)
    {
        checkNotNull(addresses, "Argument 'addresses' should not be null");

        List<List<Integer>> allPermutationsForSpecificSize = allPermutations.get().get(addresses.size());
        int permutation = generateNextRandom(allPermutationsForSpecificSize.size());

        List<Uri> addressList = new ArrayList<>();
        for (int index : allPermutationsForSpecificSize.get(permutation)) {
            addressList.add(addresses.get(index));
        }

        return addressList;
    }

    public static boolean isSizeInPermutationLimits(int size) {
        return size < allPermutations.get().size();
    }

    private static void permuteIndexPositions(int[] array, int start, int length, List<List<Integer>> output)
    {
        if (start == length)
        {
            output.add(Arrays.stream(array).boxed().collect(Collectors.toList()));
        }
        else
        {
            for (int j = start; j < length; j++)
            {
                swap(array, start, j); // pick the item to be put at the start
                permuteIndexPositions(array, start + 1, length, output);
                swap(array, start, j); // switch back the change made in previous swap
            }
        }
    }

    private static void swap(int[] array, int leftIndex, int rightIndex)
    {
        int tmp = array[leftIndex];

        array[leftIndex] = array[rightIndex];
        array[rightIndex] = tmp;
    }

    private static int generateNextRandom(int maxValue) {
        // The benefit of using ThreadLocalRandom.current() over Random is
        // avoiding the synchronization contention due to multi-threading.
        return ThreadLocalRandom.current().nextInt(maxValue);
    }
}
