// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import org.apache.commons.lang3.Range;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ParallelAsync {

    static Mono<Void> forEachAsync(Range<Integer> range, int partition, Consumer<Integer> func) {

        int partitionSize = (range.getMaximum() - range.getMinimum()) / partition;
        List<Mono<Void>> task = new ArrayList<>();
        int startRange = range.getMinimum();
        for (int i = 0; i < partition; i++) {
            Range<Integer> integerRange = Range.between(startRange, startRange + partitionSize);
            task.add(Mono.defer(() -> {
                for(int j = integerRange.getMinimum(); j < integerRange.getMaximum();j++) {
                    func.accept(j);
                }
                return Mono.empty();
            }));
            startRange = startRange + partitionSize ;
        }
        return Mono.whenDelayError(task);
    }
}
