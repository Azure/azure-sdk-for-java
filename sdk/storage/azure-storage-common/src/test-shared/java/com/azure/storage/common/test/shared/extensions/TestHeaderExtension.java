// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.storage.common.test.shared.TestNameProvider;
import org.spockframework.runtime.extension.AbstractGlobalExtension;
import org.spockframework.runtime.extension.IMethodInterceptor;
import org.spockframework.runtime.extension.IMethodInvocation;
import org.spockframework.runtime.model.SpecInfo;

public final class TestHeaderExtension extends AbstractGlobalExtension {

    @Override
    public void visitSpec(SpecInfo specInfo) {
        specInfo.getAllFeatures().forEach(feature ->
            feature.addIterationInterceptor(new TestHeaderIterationInterceptor()));
    }

    private static class TestHeaderIterationInterceptor implements IMethodInterceptor {
        @Override
        public void intercept(IMethodInvocation invocation) throws Throwable {
            // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
            String testName = TestNameProvider.getTestName(invocation.getIteration());
            System.out.printf("%s is starting", testName);
            long startTimestamp = System.currentTimeMillis();

            try {
                invocation.proceed();
            } finally {
                long duration = System.currentTimeMillis() - startTimestamp;
                System.out.printf("%s finished and took %d ms", testName, duration);
            }
        }
    }
}
