// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.management.provider.DelayProvider;
import com.azure.core.management.provider.IdentifierProvider;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.function.Function;

/**
 * The class to contain the common factory methods required for SDK framework.
 */
public class SdkContext {
    private Function<String, IdentifierProvider> identifierFunction = ResourceNamer::new;
    private static DelayProvider delayProvider = new ResourceDelayProvider();
    private static Scheduler reactorScheduler = Schedulers.parallel();

    /**
     * Default constructor for SdkContext.
     */
    public SdkContext() { }

    /**
     * Sets the resource namer
     *
     * @param identifierFunction the function.
     */
    public void setIdentifierFunction(Function<String, IdentifierProvider> identifierFunction) {
        this.identifierFunction = identifierFunction;
    }

    /**
     * Creates a resource namer
     *
     * @param name the name value.
     * @return the new resource namer
     */
    public IdentifierProvider createIdentifierProvider(String name) {
        return identifierFunction.apply(name);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public String randomResourceName(String prefix, int maxLen) {
        return identifierFunction.apply("").randomName(prefix, maxLen);
    }

    /**
     * Generates the specified number of random resource names with the same prefix.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @param count the number of names to generate
     * @return random names
     */
    public String[] randomResourceNames(String prefix, int maxLen, int count) {
        String[] names = new String[count];
        IdentifierProvider resourceNamer = identifierFunction.apply("");
        for (int i = 0; i < count; i++) {
            names[i] = resourceNamer.randomName(prefix, maxLen);
        }
        return names;
    }

    /**
     * Gets a random UUID.
     *
     * @return the random UUID.
     */
    public String randomUuid() {
        return identifierFunction.apply("").randomUuid();
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
     * Wrapper for sleep, based on delayProvider.
     *
     * @param milliseconds number of millisecond for which thread should put on sleep.
     */
    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(delayProvider.getDelayDuration(Duration.ofMillis(milliseconds)).toMillis());
        } catch (InterruptedException e) {
        }
    }

    /**
     * Wrapper for the duration for delay, based on delayProvider.
     *
     * @param delay the duration of proposed delay.
     * @return the duration of delay.
     */
    public static Duration getDelayDuration(Duration delay) {
        return delayProvider.getDelayDuration(delay);
    }

    /**
     * Gets the current Rx Scheduler for the SDK framework.
     *
     * @return current rx scheduler.
     */
    public static Scheduler getReactorScheduler() {
        return reactorScheduler;
    }

    /**
     * Sets the Rx Scheduler for SDK framework, by default is Scheduler.io().
     *
     * @param reactorScheduler current Rx Scheduler to be used in SDK framework.
     */
    public static void setReactorScheduler(Scheduler reactorScheduler) {
        SdkContext.reactorScheduler = reactorScheduler;
    }
}
