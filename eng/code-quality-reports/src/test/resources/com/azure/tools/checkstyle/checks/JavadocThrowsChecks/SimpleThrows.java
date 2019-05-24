public class SimpleThrows {
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
        //throw e; // No message should be logged. // This is being thrown as an issue since I haven't implemented tracking instantiations.
    }

    /**
     * I state that I throw but I don't say why.
     * line 32, column 16
     * @throws IllegalArgumentException
     */
    public void noMessageThrow() {
        throw new IllegalArgumentException();
    }
}
