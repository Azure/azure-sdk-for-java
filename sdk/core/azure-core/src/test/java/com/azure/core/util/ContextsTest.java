// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContextsTest {

    @Test
    public void createFromEmpty() {
        Contexts contexts = Contexts.empty();

        assertSame(Context.NONE, contexts.context());
    }

    @Test
    public void createFromExisting() {
        Context context = Context.NONE.addData("foo", "bar");
        Contexts contexts = Contexts.with(context);

        assertSame(context, contexts.context());
    }

    @Test
    public void createWithNull() {
        assertThrows(NullPointerException.class, () -> Contexts.with(null));
    }

    @Test
    public void canAddProgressReporter() {
        Contexts contexts = Contexts.empty();
        ProgressReporter progressReporter = ProgressReporter.withProgressReceiver(
            bytesTransferred -> { });

        contexts.setProgressReporter(progressReporter);

        assertSame(progressReporter, contexts.getProgressReporter());

        Context newContext = contexts.context();
        assertSame(progressReporter, Contexts.with(newContext).getProgressReporter());
    }

    @Test
    public void canUnsetProgressReporter() {
        Contexts contexts = Contexts.empty();
        ProgressReporter progressReporter = ProgressReporter.withProgressReceiver(
            bytesTransferred -> { });
        Context newContext = contexts.setProgressReporter(progressReporter).context();

        Context newNewContext = Contexts.with(newContext).setProgressReporter(null).context();

        assertNull(Contexts.with(newNewContext).getProgressReporter());
    }
}
