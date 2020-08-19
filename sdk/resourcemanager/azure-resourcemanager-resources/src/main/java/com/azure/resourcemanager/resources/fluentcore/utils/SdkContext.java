// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * The class to contain the common factory methods required for SDK framework.
 */
public class SdkContext {
    private ResourceNamerFactory resourceNamerFactory = new ResourceNamerFactory();
    private static DelayProvider delayProvider = new DelayProvider();
    private static FileProvider fileProvider = new FileProvider();
    private static Scheduler reactorScheduler = Schedulers.boundedElastic();

    /**
     * Default constructor for SdkContext.
     */
    public SdkContext() { }

    /**
     * Function to override the ResourceNamerFactory.
     *
     * @param resourceNamerFactory factory to override.
     */
    public void setResourceNamerFactory(ResourceNamerFactory resourceNamerFactory) {
        this.resourceNamerFactory = resourceNamerFactory;
    }

    /**
     * Gets the current factory for ResourceNamer.
     *
     * @return resourceNamer factory.
     */
    public ResourceNamerFactory getResourceNamerFactory() {
        return this.resourceNamerFactory;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the maximum length for the random generated name
     * @return the random name
     */
    public String randomResourceName(String prefix, int maxLen) {
        ResourceNamer resourceNamer = getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomName(prefix, maxLen);
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
        ResourceNamer resourceNamer = getResourceNamerFactory().createResourceNamer("");
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
        ResourceNamer resourceNamer = getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.randomUuid();
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
    public static void sleep(int milliseconds) {
        delayProvider.sleep(milliseconds);
    }

    /**
     * Wrapper for long-running operation retry timeout.
     *
     * @param lroRetryTimeout timeout value in seconds
     */
    public static void setLroRetryTimeOut(int lroRetryTimeout) {
        delayProvider.setLroRetryTimeout(lroRetryTimeout);
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
     * Get long-running operation retry timeout.
     *
     * @return the duration
     */
    public static Duration getLroRetryDuration() {
        return delayProvider.getLroRetryTimeout();
    }

    /**
     * @return the current date time.
     */
    public OffsetDateTime dateTimeNow() {
        ResourceNamer resourceNamer = getResourceNamerFactory().createResourceNamer("");
        return resourceNamer.dateTimeNow();
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

    /**
     * Sets the FileProvider for SDK framework, by default it does nothing.
     * @param fileProvider the FileProvider to override.
     */
    public static void setFileProvider(FileProvider fileProvider) {
        SdkContext.fileProvider = fileProvider;
    }

    /**
     * Prepares the location for file to be created.
     * @param files the files to be created.
     * @throws IOException thrown when failed on IO.
     */
    public static void prepareFileLocation(File... files) throws IOException {
        fileProvider.prepareFileLocation(files);
    }
}
