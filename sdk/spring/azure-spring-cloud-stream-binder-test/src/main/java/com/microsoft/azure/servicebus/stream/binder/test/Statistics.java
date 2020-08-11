// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.stream.binder.test;

import com.google.common.math.Quantiles;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Statistics {
    private final List<Double> stats;
    private final String label;

    public Statistics(String label) {
        this.stats = new ArrayList<>();
        this.label = label;
    }

    public void record(double data) {
        stats.add(data);
    }

    public void printSummary() {
        Map<String, Double> statByColumn = new LinkedHashMap<>();

        DoubleSummaryStatistics summaryStatistics = stats.stream().collect(Collectors.summarizingDouble(i -> i));

        statByColumn.put("Average", summaryStatistics.getAverage());
        statByColumn.put("Min", summaryStatistics.getMin());
        statByColumn.put("Max", summaryStatistics.getMax());

        int[] perc = {50, 90, 95, 99};
        Map<Integer, Double> percentiles = Quantiles.percentiles().indexes(perc).compute(stats);

        percentiles.forEach((key, value) -> statByColumn.put(key.toString(), value));

        System.out.println(this.label);
        System.out.println("=====================");

        System.out.println(statByColumn.keySet().stream().collect(Collectors.joining("|", "|", "|")));
        System.out.println(IntStream.range(1, statByColumn.size() + 1).mapToObj(i -> " -- ")
                                    .collect(Collectors.joining("|", "|", "|")));
        System.out.println(statByColumn.values().stream().map(i -> String.format("%.4f", i))
                                       .collect(Collectors.joining("|", "|", "|")));
        System.out.println();
    }
}
