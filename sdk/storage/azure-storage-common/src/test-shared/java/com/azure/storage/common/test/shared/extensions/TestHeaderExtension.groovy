package com.azure.storage.common.test.shared.extensions

import org.spockframework.runtime.extension.AbstractGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.extension.MethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.IterationInfo
import org.spockframework.runtime.model.SpecInfo

class TestHeaderExtension extends AbstractGlobalExtension {
    @Override
    void visitSpec(SpecInfo spec) {
        spec.allFeatures*.addIterationInterceptor(new FeatureInterceptor());
        spec.addInitializerInterceptor(new FeatureInterceptor());
    }

    static class FeatureInterceptor implements IMethodInterceptor {
        @Override
        void intercept(IMethodInvocation inv) {
            // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
            System.out.printf("========================= %s =========================%n", getTestName(inv.getIteration()))
            inv.proceed();
            FieldInfo field = new FieldInfo();
            inv
        }

        static String getTestName(IterationInfo iterationInfo) {
            def featureInfo = iterationInfo.getParent()
            def specInfo = featureInfo.getParent()
            def fullName = specInfo.getName() + featureInfo.getName().split(" ").collect { it.capitalize() }.join("")

            if (iterationInfo.getDataValues().length == 0) {
                return fullName
            }
            def prefix = fullName
            def suffix = "[" + iterationInfo.getIterationIndex() + "]"

            return prefix + suffix
        }
    }
}
