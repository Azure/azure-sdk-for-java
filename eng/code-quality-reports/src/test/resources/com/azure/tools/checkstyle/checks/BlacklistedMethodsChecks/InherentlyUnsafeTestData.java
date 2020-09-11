public class InherentlyUnsafeTestData {

    public void systemRunFinalizersOnExit() { // line 4, column 35
        System.runFinalizersOnExit(true);
        return;
    }

    public void systemRunFinalizersOnExitFullPath() { // line 9, column 45
        java.lang.System.runFinalizersOnExit(true);
        return;
    }

    public void runtimeRunFinalizersOnExit() { // line 14, column 36
        Runtime.runFinalizersOnExit(true);
        return;
    }

    public void runtimeRunFinalizersOnExitFullPath() { // line 19, column 46
        java.lang.Runtime.runFinalizersOnExit(true);
        return;
    }
}
