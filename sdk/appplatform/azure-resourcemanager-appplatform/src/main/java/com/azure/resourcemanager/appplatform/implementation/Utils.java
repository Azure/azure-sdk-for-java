// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** (Internal Use Only) */
class Utils {
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
        CpuMatcher matcher = CpuMatcher.create(cpu);
        if (matcher.noMatch()) {
            throw new IllegalArgumentException(String.format("Illegal cpu format : %s", cpu));
        }
        if (matcher.matchFraction()) {
            return BigDecimal.valueOf(Long.parseLong(matcher.getFraction()))
                .divide(D_1000, 1, RoundingMode.CEILING)
                .doubleValue();
        } else {
            return Double.valueOf(cpu);
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
        MemoryMatcher matcher = MemoryMatcher.create(memory);
        if (matcher.noMatch()) {
            throw new IllegalArgumentException(String.format("Illegal memory format : %s", memory));
        }
        if (matcher.matchGB()) {
            return Double.valueOf(matcher.getGBString());
        } else { // Mb
            return BigDecimal.valueOf(Long.parseLong(matcher.getMBString()))
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

    private static class CpuMatcher {
        static final Pattern CPU_INTEGER = Pattern.compile("^[0-9]+$");     // 1, 2, 3
        static final Pattern CPU_FRACTION = Pattern.compile("^([0-9]+)m$"); // 500m, 1000m
        Matcher integerMatcher;
        Matcher fractionMatcher;

        CpuMatcher(Matcher integerMatcher, Matcher fractionMatcher) {
            this.integerMatcher = integerMatcher;
            this.fractionMatcher = fractionMatcher;
        }

        static CpuMatcher create(String cpu) {
            Matcher integerMatcher = CPU_INTEGER.matcher(cpu);
            Matcher fractionMatcher = CPU_FRACTION.matcher(cpu);
            return new CpuMatcher(integerMatcher.matches() ? integerMatcher : null,
                fractionMatcher.matches() ? fractionMatcher : null);
        }

        boolean noMatch() {
            return integerMatcher == null && fractionMatcher == null;
        }

        boolean matchFraction() {
            return this.fractionMatcher != null;
        }

        String getFraction() {
            return this.fractionMatcher.group(1);
        }
    }

    private static class MemoryMatcher {
        static final Pattern MEMORY_GB = Pattern.compile("^([0-9]+)Gi"); // 1Gi, 2Gi
        static final Pattern MEMORY_MB = Pattern.compile("^([0-9]+)Mi"); // 512Mi, 1024Mi
        Matcher gbMatcher;
        Matcher mbMatcher;

        MemoryMatcher(Matcher gbMatcher, Matcher mbMatcher) {
            this.gbMatcher = gbMatcher;
            this.mbMatcher = mbMatcher;
        }

        static MemoryMatcher create(String memory) {
            Matcher gbMatcher = MEMORY_GB.matcher(memory);
            Matcher mbMatcher = MEMORY_MB.matcher(memory);
            return new MemoryMatcher(gbMatcher.matches() ? gbMatcher : null, mbMatcher.matches() ? mbMatcher : null);
        }

        boolean noMatch() {
            return gbMatcher == null && mbMatcher == null;
        }

        boolean matchGB() {
            return this.gbMatcher != null;
        }

        String getGBString() {
            return gbMatcher.group(1);
        }

        String getMBString() {
            return mbMatcher.group(1);
        }
    }
}
