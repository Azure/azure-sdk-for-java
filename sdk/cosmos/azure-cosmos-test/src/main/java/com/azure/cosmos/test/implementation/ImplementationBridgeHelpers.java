// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.test.implementation;

import com.azure.cosmos.test.implementation.faultinjection.IFaultInjectionRuleInternal;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ImplementationBridgeHelpers {
    private static final Logger logger = LoggerFactory.getLogger(ImplementationBridgeHelpers.class);

    public static final class FaultInjectionRuleHelper {
        private final static AtomicBoolean faultInjectionRuleClassLoaded = new AtomicBoolean(false);
        private final static AtomicReference<FaultInjectionRuleAccessor> accessor = new AtomicReference<>();

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

            assert(newAccessor != null);

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
}
