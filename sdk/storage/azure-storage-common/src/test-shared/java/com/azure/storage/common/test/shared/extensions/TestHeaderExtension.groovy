// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions

import com.azure.storage.common.test.shared.TestNameProvider
import org.spockframework.runtime.extension.AbstractGlobalExtension
import org.spockframework.runtime.extension.IMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo


class TestHeaderExtension extends AbstractGlobalExtension {
    @Override
    void visitSpec(SpecInfo spec) {
        spec.allFeatures*.addIterationInterceptor(new FeatureInterceptor());
    }

    static class FeatureInterceptor implements IMethodInterceptor {
        @Override
        void intercept(IMethodInvocation invocation) {
            // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
            def testName = TestNameProvider.getTestName(invocation.getIteration());
            System.out.printf("========================= %s =========================%n", testName)
            invocation.proceed();
        }
    }
}
