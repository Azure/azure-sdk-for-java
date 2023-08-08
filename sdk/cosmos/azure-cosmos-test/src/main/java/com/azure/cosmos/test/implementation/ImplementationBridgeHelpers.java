// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation;

import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.implementation.faultinjection.IFaultInjectionRuleInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ImplementationBridgeHelpers {
    private static final Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);

    public static final class FaultInjectionRuleHelper {
        private static final AtomicBoolean faultInjectionRuleClassLoaded = new AtomicBoolean(false);
        private static final AtomicReference<FaultInjectionRuleAccessor> accessor = new AtomicReference<>();

        private FaultInjectionRuleHelper() {
        }

        public static FaultInjectionRuleAccessor getFaultInjectionRuleAccessor() {
            if (!faultInjectionRuleClassLoaded.get()) {
                logger.debug("Initializing FaultInjectionRuleAccessor...");
            }

            FaultInjectionRuleAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("FaultInjectionRuleAccessor is not initialized yet!");
                System.exit(8700); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setFaultInjectionRuleAccessor(
            final FaultInjectionRuleAccessor newAccessor) {

            assert (newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("FaultInjectionRuleAccessor already initialized!");
            } else {
                logger.debug("Setting FaultInjectionRuleAccessor...");
                faultInjectionRuleClassLoaded.set(true);
            }
        }

        public interface FaultInjectionRuleAccessor {
            void setEffectiveFaultInjectionRule(FaultInjectionRule rule, IFaultInjectionRuleInternal ruleInternal);
        }
    }

    public static final class FaultInjectionConditionHelper {
        private static final AtomicBoolean faultInjectionConditionClassLoaded = new AtomicBoolean(false);
        private static final AtomicReference<FaultInjectionConditionAccessor> accessor = new AtomicReference<>();

        private FaultInjectionConditionHelper() {
        }

        public static FaultInjectionConditionAccessor getFaultInjectionConditionAccessor() {
            if (!faultInjectionConditionClassLoaded.get()) {
                logger.debug("Initializing FaultInjectionConditionAccessor...");
            }

            FaultInjectionConditionAccessor snapshot = accessor.get();
            if (snapshot == null) {
                logger.error("FaultInjectionConditionAccessor is not initialized yet!");
                System.exit(8701); // Using a unique status code here to help debug the issue.
            }

            return snapshot;
        }

        public static void setFaultInjectionConditionAccessor(final FaultInjectionConditionAccessor newAccessor) {

            assert (newAccessor != null);

            if (!accessor.compareAndSet(null, newAccessor)) {
                logger.debug("FaultInjectionConditionAccessor already initialized!");
            } else {
                logger.debug("Setting FaultInjectionConditionAccessor...");
                faultInjectionConditionClassLoaded.set(true);
            }
        }

        public interface FaultInjectionConditionAccessor {
            boolean isMetadataOperationType(FaultInjectionCondition faultInjectionCondition);
        }
    }
}
