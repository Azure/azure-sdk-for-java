package com.azure.common.mgmt;

public abstract class BadErrorThrower {
    /**
     * I don't list my throw.
     */
    public void simpleThrow() {
        throw new IllegalArgumentException();
    }

    /**
     * @throws IllegalArgumentException I documented my throw!
     */
    protected void simpleThrowDocumented() {
        throw new IllegalArgumentException();
    }

    public void instantiatedThrow() {
        IllegalArgumentException e = new IllegalArgumentException();
        throw e;
    }

    /**
     * I don't list my throw.
     */
    public void catchAndThrow() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I listed that I throw but not why
     * @throws IllegalAccessError
     */
    protected void catchAndThrowSortOfDocumented() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I don't list my throws.
     */
    public void catchUnionAndThrow() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I don't document my throw.
     * @throws IllegalAccessError
     */
    public void statesThrows() throws IllegalAccessError {
        int i = 1;
    }

    /**
     * I am private I don't need to document my throw.
     */
    private void simpleThrowPrivate() {
        throw new IllegalArgumentException();
    }

    /**
     * I am private I don't need to document my throw.
     */
    private void catchAndThrowPrivate() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I am private I don't need to document my throws.
     */
    private void catchUnionAndThrowPrivate() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I am private I don't need to document my throw.
     * @throws IllegalAccessError
     */
    private void statesThrowsPrivate() throws IllegalAccessError {
        int i = 1;
    }

    public abstract void test();
}
