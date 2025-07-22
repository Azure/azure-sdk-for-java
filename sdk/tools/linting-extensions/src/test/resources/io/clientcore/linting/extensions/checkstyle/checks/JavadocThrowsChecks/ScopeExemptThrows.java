public class ScopeExemptThrows {
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
     */
    private void statesThrowsPrivate() throws IllegalAccessError {
        int i = 1;
    }
}
