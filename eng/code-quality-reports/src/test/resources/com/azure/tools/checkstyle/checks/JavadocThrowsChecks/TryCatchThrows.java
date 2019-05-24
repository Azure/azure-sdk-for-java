public class TryCatchThrows {
    /**
     * I don't list my throw.
     */
    public void invalidCatchAndThrow() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) { // line 8, column 18
            throw e;
        }
    }

    /**
     * I don't list my throws.
     */
    public void invalidCatchAndThrowUnion() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) { //line 19, columns 18 and 45
            throw e;
        }
    }

    /**
     * I sort of documented my exceptions.
     * line 27
     * @throws IllegalAccessError
     */
    public void invalidCatchAndThrowUnion() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) { //line 32, columns 18
            throw e;
        }
    }

    /**
     * I list my throw.
     *
     * @throws IllegalAccessError My exception
     */
    public void validCatchAndThrow() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) {
            throw e;
        }
    }

    /**
     * I list my throws.
     *
     * @throws IllegalAccessError One of my exceptions
     * @throws IllegalArgumentException Another of my exceptions
     */
    public void validCatchAndThrowUnion() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e;
        }
    }
}
