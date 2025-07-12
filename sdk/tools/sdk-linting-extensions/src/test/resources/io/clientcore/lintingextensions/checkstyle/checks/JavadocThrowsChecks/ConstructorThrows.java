public class ConstructorThrows {
    /**
     * Invalid checked exception documentation
     */
    public ConstructorThrows() throws IllegalArgumentException { // line 5, column 39
    }

    /**
     * Invalid unchecked exception documentation
     * @param i My parameter
     */
    public ConstructorThrows(int i) {
        throw new IllegalArgumentException("Invalid documentation"); // line 13, column 9
    }

    /**
     * Valid checked exception documentation
     * @param s My parameter
     * @throws IllegalArgumentException When I get bad input
     */
    public ConstructorThrows(String s) throws IllegalArgumentException {
    }

    /**
     * Valid unchecked exception documentation
     * @param b My parameter
     * @throws IllegalArgumentException When I get bad input
     */
    public ConstructorThrows(boolean b) {
        throw new IllegalArgumentException("Valid documentation");
    }
}
