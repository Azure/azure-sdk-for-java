public class TryCatchThrows {
    /**
     * I don't list my throw.
     */
    public void invalidCatchAndThrow() {
        try {
            int i = 1;
        } catch (IllegalAccessError e) {
            throw e; // line 9, column 19
        }
    }

    /**
     * I don't list my throws.
     */
    public void invalidCatchAndThrowUnion() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e; //line 20, columns 19
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
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e; //line 33, columns 19
        }
    }

    /**
     * I sort of documented my exceptions.
     *
     * @throws IllegalAccessError One of my throws
     */
    public void anotherInvalidCatchAndThrowUnion() {
        try {
            int i = 1;
        } catch (IllegalArgumentException | IllegalAccessError e) {
            throw e; //line 46, columns 19
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
