public class SimpleInvalidThrow {
    /**
     * Method that doesn't list its throw.
     */
    public void simpleThrow() {
        throw new IllegalArgumentException(); // line 6, column 9
    }

    public void instantiatedThrow() {
        IllegalArgumentException e = new IllegalArgumentException();
        throw e; // line 11, column 9
    }
}
