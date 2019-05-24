public class SimpleValidThrow {
    /**
     * @throws IllegalArgumentException I documented my throw!
     */
    protected void simpleThrow() {
        throw new IllegalArgumentException(); // No message should be logged.
    }

    /**
     * @throws IllegalArgumentException I documented my throw!
     */
    public void instantiatedThrow() {
        IllegalArgumentException e = new IllegalArgumentException();
        throw e; // No message should be logged. // This is being thrown as an issue since I haven't implemented tracking instantiations.
    }
}
