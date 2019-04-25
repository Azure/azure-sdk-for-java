// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import com.azure.common.test.TestMode;
import com.azure.common.test.models.RecordedData;
import com.azure.common.test.namer.ResourceNamer;
import com.azure.common.test.namer.ResourceNamerFactory;
import com.azure.common.test.namer.TestResourceNamer;
import com.azure.common.test.provider.DelayProvider;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import java.util.Objects;

/**
 * The class to contain the common test methods for testing SDK.
 */
public class SdkContext {
    private static ResourceNamerFactory resourceNamerFactory = new ResourceNamerFactory();
    private final ResourceNamer nameGenerator;
    private static DelayProvider delayProvider = new DelayProvider();
    private static Scheduler rxScheduler = Schedulers.io();


    /**
     * Creates an SDK context that keeps track of variable names for the recorded data.
     *
     * <ul>
     *     <li>If the {@code testMode} is {@link TestMode#PLAYBACK}, this will generate random variables by reading from
     *     {@code recordedData}.</li>
     *     <li>If the {@code testMode} is {@link TestMode#RECORD}, this will generate random variables and write them to
     *     {@code recordedData}.</li>
     * </ul>
     *
     * @param testMode The test for this context.
     * @param recordedData The data to persist or read any variables names to or from.
     */
    public SdkContext(TestMode testMode, RecordedData recordedData) {
        Objects.requireNonNull(recordedData);
        nameGenerator =  new TestResourceNamer("", testMode, recordedData);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public String randomResourceName(String prefix, int maxLen) {
        return nameGenerator.randomName(prefix, maxLen);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public static String getRandomResourceName(String prefix, int maxLen) {
        ResourceNamer resourceNamer = SdkContext.getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random resource names with the same prefix.
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return random names
     */
    public static String[] randomResourceNames(String prefix, int maxLen, int count) {
        String[] names = new String[count];
        ResourceNamer resourceNamer = SdkContext.getResourceNamerFactory().createResourceNamer("");
        for (int i = 0; i < count; i++) {
            names[i] = resourceNamer.randomName(prefix, maxLen);
        }
        return names;
    }

    /**
     * Wrapper for sleep, based on delayProvider.
     * @param milliseconds number of millisecond for which thread should put on sleep.
     */
    public static void sleep(int milliseconds) {
        delayProvider.sleep(milliseconds);
    }


    /**
     * Gets the current namer for ResourceNamer.
     * @return resourceNamer namer.
     */
    public static ResourceNamerFactory getResourceNamerFactory() {
        return SdkContext.resourceNamerFactory;
    }

    /**
     * Function to override the ResourceNamerFactory.
     *
     * @param resourceNamerFactory namer to override.
     */
    public static void setResourceNamerFactory(ResourceNamerFactory resourceNamerFactory) {
        SdkContext.resourceNamerFactory = resourceNamerFactory;
    }


    /**
     * Function to override the DelayProvider.
     *
     * @param delayProvider delayProvider to override.
     */
    public static void setDelayProvider(DelayProvider delayProvider) {
        SdkContext.delayProvider = delayProvider;
    }

    /**
     * Gets the Rx Scheduler for SDK framework, by default is Scheduler.io().
     *
     * @return The Rx Scheduler used in SDK framework.
     */
    public static Scheduler getRxScheduler() {
        return SdkContext.rxScheduler;
    }

    /**
     * Sets the Rx Scheduler for SDK framework, by default is Scheduler.io().
     * @param rxScheduler current Rx Scheduler to be used in SDK framework.
     */
    public static void setRxScheduler(Scheduler rxScheduler) {
        SdkContext.rxScheduler = rxScheduler;
    }
}
