// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.UtilBridgeInternal;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ImplementationBridgeHelpersTest {

    private static final Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);

    @Test(groups = { "unit" })
    public void accessorInitialization() {

        String helperClassSuffix = "Helper";

        Class<?>[] declaredClasses = ImplementationBridgeHelpers.class.getDeclaredClasses();

        try {
            for (Class<?> declaredClass : declaredClasses) {

                if (declaredClass.getSimpleName().endsWith(helperClassSuffix)) {

                    Field[] fields = declaredClass.getDeclaredFields();
                    boolean isAccessorReset = false;
                    boolean isClassLoadedReset = false;

                    for (Field field : fields) {

                        if (field.getName().contains("accessor")) {
                            field.setAccessible(true);
                            AtomicReference<?> value = (AtomicReference<?>) FieldUtils.readStaticField(field);
                            value.set(null);
                            isAccessorReset = true;
                        }

                        if (field.getName().contains("ClassLoaded")) {
                            field.setAccessible(true);
                            AtomicBoolean value = (AtomicBoolean) FieldUtils.readStaticField(field);
                            value.set(false);
                            isClassLoadedReset = true;
                        }
                    }
                    assertThat(isAccessorReset).isTrue();
                    assertThat(isClassLoadedReset).isTrue();
                }
            }

            BridgeInternal.initializeAllAccessors();
            ModelBridgeInternal.initializeAllAccessors();
            UtilBridgeInternal.initializeAllAccessors();

            declaredClasses = ImplementationBridgeHelpers.class.getDeclaredClasses();

            for (Class<?> declaredClass : declaredClasses) {

                if (declaredClass.getSimpleName().endsWith(helperClassSuffix)) {

                    logger.info("Helper class name : {}", declaredClass.getSimpleName());

                    Field[] fields = declaredClass.getDeclaredFields();
                    boolean isAccessorSet = false;
                    boolean isClassLoaded = false;

                    for (Field field : fields) {

                        if (field.getName().contains("accessor")) {
                            field.setAccessible(true);
                            AtomicReference<?> value = (AtomicReference<?>) FieldUtils.readStaticField(field);
                            logger.info("Accessor name : {}", field.getName());
                            assertThat(value.get()).isNotNull();
                            isAccessorSet = true;
                        }

                        if (field.getName().contains("ClassLoaded")) {
                            field.setAccessible(true);
                            AtomicBoolean value = (AtomicBoolean) FieldUtils.readStaticField(field);
                            logger.info("ClassLoaded name : {}", field.getName());
                            assertThat(value.get()).isTrue();
                            isClassLoaded = true;
                        }
                    }
                    assertThat(isAccessorSet).isTrue();
                    assertThat(isClassLoaded).isTrue();
                }
            }
        } catch (IllegalAccessException e) {
            fail("Failed with IllegalAccessException : ", e.getMessage());
        }
    }

    @Test(groups = { "unit" })
    public void concurrentAccessorInitializationShouldNotDeadlock() throws Exception {
        // Regression test for https://github.com/Azure/azure-sdk-for-java/issues/48622
        // and https://github.com/Azure/azure-sdk-for-java/issues/48585
        //
        // Verifies that concurrently calling different getXxxAccessor() methods from
        // multiple threads completes without deadlock. Before the fix, each getter
        // called initializeAllAccessors() which eagerly loaded 40+ classes, creating
        // circular <clinit> dependencies that permanently deadlocked the JVM.

        // Reset all accessors to force re-initialization
        Class<?>[] declaredClasses = ImplementationBridgeHelpers.class.getDeclaredClasses();
        for (Class<?> declaredClass : declaredClasses) {
            if (declaredClass.getSimpleName().endsWith("Helper")) {
                for (Field field : declaredClass.getDeclaredFields()) {
                    if (field.getName().contains("accessor")) {
                        field.setAccessible(true);
                        AtomicReference<?> value = (AtomicReference<?>) FieldUtils.readStaticField(field);
                        value.set(null);
                    }
                    if (field.getName().contains("ClassLoaded")) {
                        field.setAccessible(true);
                        AtomicBoolean value = (AtomicBoolean) FieldUtils.readStaticField(field);
                        value.set(false);
                    }
                }
            }
        }

        try {
            final int threadCount = 6;
            final int timeoutSeconds = 30;
            final CyclicBarrier barrier = new CyclicBarrier(threadCount);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            List<Future<?>> futures = new ArrayList<>();

            // Each thread triggers a different accessor getter concurrently
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.CosmosAsyncClientHelper.getCosmosAsyncClientAccessor();
            }));
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.CosmosItemRequestOptionsHelper.getCosmosItemRequestOptionsAccessor();
            }));
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.FeedResponseHelper.getFeedResponseAccessor();
            }));
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.CosmosQueryRequestOptionsHelper.getCosmosQueryRequestOptionsAccessor();
            }));
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.CosmosAsyncContainerHelper.getCosmosAsyncContainerAccessor();
            }));
            futures.add(executor.submit(() -> {
                awaitBarrier(barrier);
                ImplementationBridgeHelpers.CosmosItemSerializerHelper.getCosmosItemSerializerAccessor();
            }));

            boolean deadlockDetected = false;
            for (int i = 0; i < futures.size(); i++) {
                try {
                    futures.get(i).get(timeoutSeconds, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    deadlockDetected = true;
                    logger.error("Thread {} did not complete within {} seconds - possible deadlock", i, timeoutSeconds);
                }
            }

            executor.shutdownNow();
            assertThat(deadlockDetected)
                .as("Concurrent accessor initialization should complete without deadlock")
                .isFalse();
        } finally {
            // Restore all accessors so subsequent tests in the same JVM are not affected
            BridgeInternal.initializeAllAccessors();
            ModelBridgeInternal.initializeAllAccessors();
            UtilBridgeInternal.initializeAllAccessors();
        }
    }

    private static void awaitBarrier(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
