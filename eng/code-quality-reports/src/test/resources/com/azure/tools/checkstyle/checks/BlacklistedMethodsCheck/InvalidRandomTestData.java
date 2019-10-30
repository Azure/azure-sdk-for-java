public class InvalidRandomTestData {
    /**
     * random method in Math class
     */
    public void mathClassRandom() { // line 6, column 20
        Math.random();
        return;
    }

    /**
     * random method in Math class with full class path
     */
    public void mathFullClassRandom() { // line 14, column 30
        java.lang.Math.random();
        return;
    }
}
