/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.util.Arrays;
import java.util.List;

/**
 * A wrapper for the exceptions that can be generated during parallel
 * execution of the uploader.
 */
public class AggregateUploadException extends Exception {

    private final List<Exception> secondaryExceptions;

    public AggregateUploadException(String message, Exception primary, List<Exception> others) {
        super(message, primary);
        this.secondaryExceptions = others;
    }

    /***
     * Returns all of the exceptions (except the first one, which is used to construct this exception)
     * that are associated with this exception
     * @return an Array of {@link Throwable} objects that are associated with this exception
     */
    public Throwable[] getAllExceptions() {

        int start = 0;
        int size = secondaryExceptions.size();
        final Throwable primary = getCause();
        if (primary != null) {
            start = 1;
            size++;
        }

        Throwable[] all = new Exception[size];

        if (primary != null) {
            all[0] = primary;
        }

        Arrays.fill(all, start, all.length, secondaryExceptions);
        return all;
    }
}
