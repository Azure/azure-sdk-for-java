// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test;

import com.typespec.core.test.annotation.SyncAsyncTest;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 * A test template extension that helps to branch out a single test into sync and async invocation.
 */
public final class SyncAsyncExtension implements TestTemplateInvocationContextProvider {

    private static final ThreadLocal<Boolean> IS_SYNC_THREAD_LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> WAS_EXTENSION_USED_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * Creates a new instance of {@link SyncAsyncExtension}.
     */
    public SyncAsyncExtension() {
    }

    /**
     * Executes sync or async branch depending on the context.
     * @param sync sync callable.
     * @param async async callable. It should block at some point to return result.
     * @param <T> type of result of either sync or async invocation.
     * @return result of either sync or async invocation.
     * @throws IllegalStateException if extension doesn't work as expected.
     * @throws RuntimeException a runtime exception wrapping error from callable.
     */
    public static <T> T execute(Callable<T> sync, Callable<Mono<T>> async) {
        Boolean isSync = IS_SYNC_THREAD_LOCAL.get();
        WAS_EXTENSION_USED_THREAD_LOCAL.set(true);
        if (isSync == null) {
            throw new IllegalStateException("The IS_SYNC_THREAD_LOCAL is undefined. Make sure you're using"
                + "@SyncAsyncTest with SyncAsyncExtension.execute()");
        } else {
            try {
                if (isSync) {
                    return sync.call();
                } else {
                    return async.call().block();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Executes sync or async branch depending on the context.
     * @param sync sync callable.
     * @param async async callable. It should block at some point to return result.
     * @throws IllegalStateException if extension doesn't work as expected.
     * @throws RuntimeException a runtime exception wrapping error from callable.
     */
    public static void execute(Runnable sync, Callable<Mono<Void>> async) {
        Boolean isSync = IS_SYNC_THREAD_LOCAL.get();
        WAS_EXTENSION_USED_THREAD_LOCAL.set(true);
        if (isSync == null) {
            throw new IllegalStateException("The IS_SYNC_THREAD_LOCAL is undefined. Make sure you're using"
                + "@SyncAsyncTest with SyncAsyncExtension.execute()");
        } else {
            try {
                if (isSync) {
                    sync.run();
                } else {
                    async.call().block();
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext extensionContext) {
        return extensionContext.getTestMethod()
            .map(method -> method.getAnnotation(SyncAsyncTest.class) != null)
            .orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
        ExtensionContext extensionContext) {
        return Stream.of(
            new SyncAsyncTestTemplateInvocationContext(true),
            new SyncAsyncTestTemplateInvocationContext(false)
        );
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

    private static final class SyncAsyncTestInterceptor
        implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
        private final boolean isSync;

        private SyncAsyncTestInterceptor(boolean isSync) {
            this.isSync = isSync;
        }

        @Override
        public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
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
        public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
            IS_SYNC_THREAD_LOCAL.remove();
            if (!WAS_EXTENSION_USED_THREAD_LOCAL.get()) {
                throw new IllegalStateException(
                    "You should use SyncAsyncExtension.execute() in test annotated with @SyncAsyncTest");
            }
            WAS_EXTENSION_USED_THREAD_LOCAL.remove();
        }
    }
}
