package com.azure.core.util.polling;

import java.util.Objects;

public class OperationStatus {
    /**
     * An enum to represent all possible states that a long-running operation may find itself in.
     * The poll operation is considered complete when the status is one of {@code SUCCESSFULLY_COMPLETED}, {@code USER_CANCELLED} or {@code FAILED}.
     */
    public enum State {
        /**
         * Represents that polling has not yet started for this long-running operation.
         */
        NOT_STARTED,

        /**
         * Represents that this long-running operation is in progress and not yet complete.
         */
        IN_PROGRESS,

        /**
         * Represent that this long-running operation is completed successfully.
         */
        SUCCESSFULLY_COMPLETED,

        /**
         * Represents that this long-running operation has failed to successfully complete, however this is still
         * considered as complete long-running operation, meaning that the {@link Poller} instance will report that it is complete.
         */
        FAILED,

        /**
         * Represents that this long-running operation is cancelled by user, however this is still
         * considered as complete long-running operation.
         */
        USER_CANCELLED,

        /**
         * When long-running operation state could not be represented by any state in {@link State}, this state represents
         * a custom state Azure service could be in. This custom state is not considered as complete long-running operation.
         * It must have valid value for {@code otherStatus}.
         */
        OTHER
    }
    private State state;
    private String otherStatus;

    public OperationStatus(State state) {
        this.state = state;
    }

    public OperationStatus(State state, String otherStatus) {
        this(state);
        if (state == State.OTHER ) {
            if  ( Objects.isNull(otherStatus) || otherStatus.trim().length() == 0 ) {
                throw new IllegalArgumentException("otherStatus can not be empty or null for State.OTHER");
            }else {
                this.otherStatus = otherStatus;
            }
        }
    }

    public State getState(){
        return this.state;
    }

    public String getOtherStatus(){
        return this.otherStatus;
    }
}
