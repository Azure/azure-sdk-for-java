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
}
