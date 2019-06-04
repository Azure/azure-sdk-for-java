// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventprocessorhost;

class Closable {
    private final Object syncClose;
    private final Closable parent; // null for top-level
    private boolean isClosing;
    private boolean isClosed;

    // null parent means top-level
    Closable(Closable parent) {
        this.syncClose = new Object();
        this.parent = parent;
        this.isClosing = false;
        this.isClosed = false;
    }
    
    protected final boolean getIsClosed() {
        final boolean isParentClosed = this.parent != null && this.parent.getIsClosed();
        synchronized (this.syncClose) {
            return isParentClosed || this.isClosed;
        }
    }

    // returns true even if the Parent is (being) Closed
    protected final boolean getIsClosingOrClosed() {
        final boolean isParentClosingOrClosed = this.parent != null && this.parent.getIsClosingOrClosed();
        synchronized (this.syncClose) {
            return isParentClosingOrClosed || this.isClosing || this.isClosed;
        }
    }

    protected final void setClosing() {
        synchronized (this.syncClose) {
            this.isClosing = true;
        }
    }
    
    protected final void setClosed() {
        synchronized (this.syncClose) {
            this.isClosing = false;
            this.isClosed = true;
        }
    }
    
    protected final void throwIfClosingOrClosed(String message) {
        if (getIsClosingOrClosed()) {
            throw new ClosingException(message);
        }
    }
    
    static class ClosingException extends RuntimeException {
        private static final long serialVersionUID = 1138985585921317036L;

        ClosingException(String message) {
            super(message);
        }
    }
}
