// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Objects;

/**
 * A utility type that can be used to add and retrieve instances commonly used in {@link Context}.
 */
public final class Contexts {

    private static final String PROGRESS_REPORTER_CONTEXT_KEY = "com.azure.core.util.ProgressReporter";

    private Context context;

    private Contexts(Context context) {
        this.context = Objects.requireNonNull(context, "'context' must not be null");
    }

    /**
     * Creates {@link Contexts} from empty {@link Context}.
     * @return The {@link Contexts} instance.
     */
    public static Contexts empty() {
        return with(Context.NONE);
    }

    /**
     * Creates {@link Contexts} from supplied {@link Context}.
     * @param context Existing {@link Context}. Must not be null.
     * @return The {@link Contexts} instance.
     * @throws NullPointerException If {@code context} is null.
     */
    public static Contexts with(Context context) {
        return new Contexts(context);
    }

    /**
     * Adds {@link ProgressReporter} instance to the {@link Context}.
     * @param progressReporter The {@link ProgressReporter} instance.
     * @return Itself.
     */
    public Contexts setProgressReporter(ProgressReporter progressReporter) {
        context = context.addData(PROGRESS_REPORTER_CONTEXT_KEY, progressReporter);
        return this;
    }

    /**
     * Retrieves {@link ProgressReporter} from the {@link Context}.
     * @return The {@link ProgressReporter}.
     */
    public ProgressReporter getProgressReporter() {
        return (ProgressReporter) context.getData(PROGRESS_REPORTER_CONTEXT_KEY).orElse(null);
    }

    /**
     * Returns a version of the {@link Context} reflecting mutations.
     * @return The version of the {@link Context} reflecting mutations.
     */
    public Context context() {
        return context;
    }
}
