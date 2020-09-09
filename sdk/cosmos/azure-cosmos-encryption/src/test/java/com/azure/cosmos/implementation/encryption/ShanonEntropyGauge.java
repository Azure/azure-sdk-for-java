// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class ShanonEntropyGauge {
    private final Map<BigInteger, Integer> freq;
    private int cnt = 0;

    public ShanonEntropyGauge() {
        this.freq = new HashMap<>();
    }

    public void add(byte[] input) {
        BigInteger number = new BigInteger(1, input);
        freq.compute(number, (bigInteger, cnt) -> {
            if (cnt == null) {
                return 1;
            } else {
                return cnt + 1;
            }
        });
        cnt++;
    }

    public double calculate() {
        // compute Shannon entropy
        double entropy = 0.0;
        for (Map.Entry<BigInteger, Integer> entry: freq.entrySet()) {
            double p = 1.0 * entry.getValue() / cnt;
            entropy -= p * Math.log(p) / Math.log(2);
        }

        return entropy;
    }
}
