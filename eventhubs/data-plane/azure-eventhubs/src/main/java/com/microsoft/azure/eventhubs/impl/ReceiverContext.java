// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import com.microsoft.azure.eventhubs.ErrorContext;

import java.util.Locale;

public class ReceiverContext extends ErrorContext {
    static final boolean EPOCH_RECEIVER_TYPE = true;
    static final boolean NON_EPOCH_RECEIVER_TYPE = !ReceiverContext.EPOCH_RECEIVER_TYPE;
    private static final long serialVersionUID = 2581371351997722504L;

    final String receivePath;
    final String referenceId;
    final Integer prefetchCount;
    final Integer currentLinkCredit;
    final Integer prefetchQueueLength;

    ReceiverContext(
            final String namespaceName,
            final String receivePath,
            final String referenceId,
            final Integer prefetchCount,
            final Integer currentLinkCredit,
            final Integer prefetchQueueLength) {
        super(namespaceName);
        this.receivePath = receivePath;
        this.referenceId = referenceId;
        this.prefetchCount = prefetchCount;
        this.currentLinkCredit = currentLinkCredit;
        this.prefetchQueueLength = prefetchQueueLength;
    }

    @Override
    public String toString() {
        final String superString = super.toString();
        StringBuilder toString = new StringBuilder();

        if (!StringUtil.isNullOrEmpty(superString)) {
            toString.append(superString);
            toString.append(", ");
        }

        if (this.receivePath != null) {
            toString.append(String.format(Locale.US, "PATH: %s", this.receivePath));
            toString.append(", ");
        }

        if (this.referenceId != null) {
            toString.append(String.format(Locale.US, "REFERENCE_ID: %s", this.referenceId));
            toString.append(", ");
        }

        if (this.prefetchCount != null) {
            toString.append(String.format(Locale.US, "PREFETCH_COUNT: %s", this.prefetchCount));
            toString.append(", ");
        }

        if (this.currentLinkCredit != null) {
            toString.append(String.format(Locale.US, "LINK_CREDIT: %s", this.currentLinkCredit));
            toString.append(", ");
        }

        if (this.prefetchQueueLength != null) {
            toString.append(String.format(Locale.US, "PREFETCH_Q_LEN: %s", this.prefetchQueueLength));
            toString.append(", ");
        }

        if (toString.length() > 2) {
            toString.setLength(toString.length() - 2);
        }

        return toString.toString();
    }
}
