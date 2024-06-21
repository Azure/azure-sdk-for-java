// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * An extension that helps to branch out a single test into sync and async invocation.
 *
 * Using azure-core copy of the com.azure.core.test.SyncAsyncExtension class
 * since azure-core cannot take dependency on azure-core-test package.
 */
public class SyncAsyncExtension implements TestTemplateInvocationContextProvider {

    private static final ThreadLocal<Boolean> IS_SYNC_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> WAS_EXTENSION_USED_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Executes sync or async branch depending on the context.
     * @param sync sync callable.
     * @param async async callable. It should block at some point to return result.
     * @param <T> type of result of either sync or async invocation.
     * @return result of either sync or async invocation.
     * @throws Exception exception propagated from the callables.
     * @throws IllegalStateException if extension doesn't work as expected.
     */
    public static <T> T execute(Callable<T> sync, Callable<T> async) throws Exception {
        Boolean isSync = IS_SYNC_THREAD_LOCAL.get();
        WAS_EXTENSION_USED_THREAD_LOCAL.set(true);
        if (isSync == null) {
            throw new IllegalStateException("The IS_SYNC_THREAD_LOCAL is undefined. Make sure you're using"
                + "@SyncAsyncTest with SyncAsyncExtension.execute()");
        } else if (isSync) {
            return sync.call();
        } else {
            return async.call();
        }
    }

    /**
     * Executes sync or async branch depending on the context.
     * @param sync sync runnable.
     * @param async async runnable. It should block at some point.
     * @throws IllegalStateException if extension doesn't work as expected.
     */
    public static void execute(Runnable sync, Runnable async) {
        Boolean isSync = IS_SYNC_THREAD_LOCAL.get();
        WAS_EXTENSION_USED_THREAD_LOCAL.set(true);
        if (isSync == null) {
            throw new IllegalStateException("The IS_SYNC_THREAD_LOCAL is undefined. Make sure you're using"
                + "@SyncAsyncTest with SyncAsyncExtension.execute()");
        } else if (isSync) {
            sync.run();
        } else {
            async.run();
        }
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
            .map(method -> method.getAnnotation(SyncAsyncTest.class) != null)
            .orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext>
        provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {
        return Stream.of(new SyncAsyncTestTemplateInvocationContext(true),
            new SyncAsyncTestTemplateInvocationContext(false));
    }

    private static final class SyncAsyncTestTemplateInvocationContext implements TestTemplateInvocationContext {
        private final boolean isSync;

        private SyncAsyncTestTemplateInvocationContext(boolean isSync) {
            this.isSync = isSync;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return isSync ? "sync" : "async";
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.singletonList(new SyncAsyncTestInterceptor(isSync));
        }
    }

    static final class SyncAsyncTestInterceptor implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
        private final boolean isSync;

        private SyncAsyncTestInterceptor(boolean isSync) {
            this.isSync = isSync;
        }

        @Override
        public void beforeTestExecution(ExtensionContext extensionContext) {
            if (IS_SYNC_THREAD_LOCAL.get() != null) {
                throw new IllegalStateException("The IS_SYNC_THREAD_LOCAL shouldn't be set at this point");
            }
            if (WAS_EXTENSION_USED_THREAD_LOCAL.get() != null) {
                throw new IllegalStateException("The WAS_EXTENSION_USED_THREAD_LOCAL shouldn't be set at this point");
            }
            IS_SYNC_THREAD_LOCAL.set(isSync);
            WAS_EXTENSION_USED_THREAD_LOCAL.set(false);
        }

        @Override
        public void afterTestExecution(ExtensionContext extensionContext) {
            IS_SYNC_THREAD_LOCAL.remove();
            if (!WAS_EXTENSION_USED_THREAD_LOCAL.get()) {
                throw new IllegalStateException(
                    "You should use SyncAsyncExtension.execute() in test annotated with @SyncAsyncTest");
            }
            WAS_EXTENSION_USED_THREAD_LOCAL.remove();
        }
    }
}
