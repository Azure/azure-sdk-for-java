package com.azure.data.tables.models;

public interface TableBatchResult {
    final class Success implements TableBatchResult {
        private Success() { }

        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    final class Error implements TableBatchResult {
        private final Throwable throwable;

        private Error(Throwable throwable) {
            this.throwable = throwable;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    static TableBatchResult success() {
        return new Success();
    }

    static TableBatchResult error(Throwable throwable) {
        return new Error(throwable);
    }

    boolean isSuccess();
}
