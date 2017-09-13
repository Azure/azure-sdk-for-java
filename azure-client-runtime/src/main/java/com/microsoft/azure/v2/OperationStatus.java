package com.microsoft.azure.v2;

public class OperationStatus<T> {
    private final boolean isDone;
    private final T result;

    private OperationStatus(boolean isDone, T result) {
        this.isDone = isDone;
        this.result = result;
    }

    public boolean isDone() {
        return isDone;
    }

    public T result() {
        return result;
    }

    public static <T> OperationStatus<T> inProgress() {
        return new OperationStatus<>(false, null);
    }

    public static <T> OperationStatus<T> completed(T result) {
        return new OperationStatus<>(true, result);
    }
}
