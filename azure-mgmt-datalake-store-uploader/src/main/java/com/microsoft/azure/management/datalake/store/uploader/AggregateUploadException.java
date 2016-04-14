/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import java.util.Arrays;
import java.util.List;

/***
 *
 */
public class AggregateUploadException extends Exception {

    private final Exception[] secondaryExceptions;

    public AggregateUploadException(String message, Exception primary, List<Exception> others) {
        super(message, primary);
        this.secondaryExceptions = others == null ? new Exception[0] : (Exception[]) others.toArray();
    }

    /***
     * 
     * @return
     */
    public Throwable[] getAllExceptions() {

        int start = 0;
        int size = secondaryExceptions.length;
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
