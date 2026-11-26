// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Schedules authorization dispatch without allowing cancellation to interrupt shared connection IO.
 */
final class AuthorizationScheduler {
    // Executor-backed cancellation suppresses queued work without interrupting a running task. The task owns its
    // scheduling handle until dispatch returns, and scheduling rejection propagates to the subscribing operator.
    static final Scheduler INSTANCE = Schedulers.fromExecutor(task -> {
        Disposable.Swap scheduled = Disposables.swap();
        scheduled.update(Schedulers.boundedElastic().schedule(() -> {
            try {
                task.run();
            } finally {
                scheduled.dispose();
            }
        }));
    });

    private AuthorizationScheduler() {
    }
}
