public class CheckedThrows {
    /**
     * I don't document my throw.
     */
    public void invalidCheckedThrow() throws IllegalAccessError { // line 5, column 46
        return;
    }

    /**
     * I don't document either of my throws.
     */
    public void invalidCheckedThrows() throws IllegalAccessError, IllegalArgumentException { // line 12, columns 47 and 67
        return;
    }

    /**
     * @throws IllegalAccessError Documented throw.
     */
    public void validCheckedThrow() throws IllegalAccessError {
        return;
    }
}
