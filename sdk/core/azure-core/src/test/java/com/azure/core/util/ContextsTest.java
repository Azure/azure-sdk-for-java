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

        assertSame(Context.NONE, contexts.getContext());
    }

    @Test
    public void createFromExisting() {
        Context context = Context.NONE.addData("foo", "bar");
        Contexts contexts = Contexts.with(context);

        assertSame(context, contexts.getContext());
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

        contexts.setRequestProgressReporter(progressReporter);

        assertSame(progressReporter, contexts.getRequestProgressReporter());

        Context newContext = contexts.getContext();
        assertSame(progressReporter, Contexts.with(newContext).getRequestProgressReporter());
    }

    @Test
    public void canUnsetProgressReporter() {
        Contexts contexts = Contexts.empty();
        ProgressReporter progressReporter = ProgressReporter.withProgressReceiver(
            bytesTransferred -> { });
        Context newContext = contexts.setRequestProgressReporter(progressReporter).getContext();

        Context newNewContext = Contexts.with(newContext).setRequestProgressReporter(null).getContext();

        assertNull(Contexts.with(newNewContext).getRequestProgressReporter());
    }
}
