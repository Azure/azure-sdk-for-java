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

        assertSame(Context.none(), contexts.getContext());
    }

    @Test
    public void createFromExisting() {
        Context context = Context.none().addData("foo", "bar");
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
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(bytesTransferred -> {
        });

        contexts.setHttpRequestProgressReporter(progressReporter);

        assertSame(progressReporter, contexts.getHttpRequestProgressReporter());

        Context newContext = contexts.getContext();
        assertSame(progressReporter, Contexts.with(newContext).getHttpRequestProgressReporter());
    }

    @Test
    public void canUnsetProgressReporter() {
        Contexts contexts = Contexts.empty();
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(bytesTransferred -> {
        });
        Context newContext = contexts.setHttpRequestProgressReporter(progressReporter).getContext();

        Context newNewContext = Contexts.with(newContext).setHttpRequestProgressReporter(null).getContext();

        assertNull(Contexts.with(newNewContext).getHttpRequestProgressReporter());
    }
}
