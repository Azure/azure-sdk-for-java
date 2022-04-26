// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** (Internal Use Only) */
class Utils {
    private static final Pattern CPU_INTEGER = Pattern.compile("^[0-9]+$");     // 1, 2, 3
    private static final Pattern CPU_DOUBLE = Pattern.compile("^([0-9]+)m$");   // 500m, 1000m
    private static final Pattern MEMORY_GB = Pattern.compile("^([0-9]+)Gi");    // 1Gi, 2Gi
    private static final Pattern MEMORY_MB = Pattern.compile("^([0-9]+)Mi");    // 512Mi, 1024Mi
    private static final BigDecimal D_1000 = BigDecimal.valueOf(1000);
    private static final BigDecimal D_1024 = BigDecimal.valueOf(1024);

    /**
     * Converts cpu from String to Double
     * @param cpu cpu String
     * @return cpu count
     */
    public static Double fromCpuString(String cpu) {
        if (cpu == null) {
            return null;
        }
        CpuMatcher matcher = checkCpu(cpu);
        if (matcher.noMatch()) {
            throw new IllegalArgumentException(String.format("Illegal cpu format : %s", cpu));
        }
        if (matcher.integerMatcher != null) {
            return Double.valueOf(cpu);
        } else {
            return BigDecimal.valueOf(Long.parseLong(matcher.doubleMatcher.group(1)))
                .divide(D_1000, 1, RoundingMode.CEILING)
                .doubleValue();
        }
    }

    /**
     * Converts memory from String to Double, in GB
     * @param memory memory String
     * @return memory in GB
     */
    public static Double fromMemoryString(String memory) {
        if (memory == null) {
            return null;
        }
        MemoryMatcher matcher = checkMemory(memory);
        if (matcher.noMatch()) {
            throw new IllegalArgumentException(String.format("Illegal memory format : %s", memory));
        }
        if (matcher.gbMatcher != null) {
            return Double.valueOf(matcher.gbMatcher.group(1));
        } else { // Mb
            return BigDecimal.valueOf(Long.parseLong(matcher.mbMatcher.group(1)))
                .divide(D_1024, 1, RoundingMode.CEILING)
                .doubleValue();
        }
    }

    /**
     * Converts cpu count from double to String
     * @param cpuCount cpu count, 1 core can be represented by 1 or 1000m
     * @return cpu String
     */
    public static String toCpuString(double cpuCount) {
        BigDecimal cpuDecimal = BigDecimal.valueOf(cpuCount);
        if (isInteger(cpuCount)) {
            return String.valueOf(cpuDecimal.intValue());
        } else {
            return String.format("%dm", cpuDecimal.multiply(D_1000).intValue());
        }
    }

    /**
     * Converts memory count from double to String
     * @param sizeInGB memory in GB
     * @return memory String
     */
    public static String toMemoryString(double sizeInGB) {
        BigDecimal memoryDecimal = BigDecimal.valueOf(sizeInGB);
        if (isInteger(sizeInGB)) {
            return String.format("%dGi", memoryDecimal.intValue());
        } else {
            return String.format("%dMi", memoryDecimal.multiply(D_1024).intValue());
        }
    }

    private static boolean isInteger(double sizeInGB) {
        return (sizeInGB % 1) == 0;
    }

    private static CpuMatcher checkCpu(String cpu) {
        Matcher integerMatcher = CPU_INTEGER.matcher(cpu);
        Matcher doubleMatcher = CPU_DOUBLE.matcher(cpu);
        return new CpuMatcher(
            integerMatcher.matches() ? integerMatcher : null,
            doubleMatcher.matches() ? doubleMatcher : null
        );
    }

    private static MemoryMatcher checkMemory(String memory) {
        Matcher gbMatcher = MEMORY_GB.matcher(memory);
        Matcher mbMatcher = MEMORY_MB.matcher(memory);
        return new MemoryMatcher(
            gbMatcher.matches() ? gbMatcher : null,
            mbMatcher.matches() ? mbMatcher : null
        );
    }

    private static class CpuMatcher {
        Matcher integerMatcher;
        Matcher doubleMatcher;

        CpuMatcher(Matcher integerMatcher, Matcher doubleMatcher) {
            this.integerMatcher = integerMatcher;
            this.doubleMatcher = doubleMatcher;
        }

        boolean noMatch() {
            return integerMatcher == null && doubleMatcher == null;
        }
    }

    private static class MemoryMatcher {
        Matcher gbMatcher;
        Matcher mbMatcher;

        MemoryMatcher(Matcher gbMatcher, Matcher mbMatcher) {
            this.gbMatcher = gbMatcher;
            this.mbMatcher = mbMatcher;
        }

        boolean noMatch() {
            return gbMatcher == null && mbMatcher == null;
        }
    }
}
