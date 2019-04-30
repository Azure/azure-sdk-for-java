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

package com.microsoft.azure.cosmosdb.rx.internal;

import org.apache.commons.lang3.Range;
import rx.Completable;
import rx.functions.Action1;

import java.util.ArrayList;
import java.util.List;

public class ParallelAsync {

    static Completable forEachAsync(Range<Integer> range, int partition, Action1<Integer> func) {

        int partitionSize = (range.getMaximum() - range.getMinimum()) / partition;
        List<Completable> task = new ArrayList<>();
        int startRange = range.getMinimum();
        for (int i = 0; i < partition; i++) {
            Range<Integer> integerRange = Range.between(startRange, startRange + partitionSize);
            task.add(Completable.defer(() -> {
                for(int j = integerRange.getMinimum(); j < integerRange.getMaximum();j++) {
                    func.call(j);
                }
                return Completable.complete();
            }));
            startRange = startRange + partitionSize ;
        }
        return Completable.mergeDelayError(task);
    }
}
