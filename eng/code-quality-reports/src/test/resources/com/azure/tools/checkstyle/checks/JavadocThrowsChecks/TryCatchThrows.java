public class TryCatchThrows {
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
}
