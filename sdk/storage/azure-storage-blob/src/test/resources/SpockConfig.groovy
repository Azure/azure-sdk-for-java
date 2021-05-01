import org.spockframework.runtime.model.parallel.ExecutionMode;

runner {
    parallel {
        enabled "LIVE" == System.getProperty("AZURE_TEST_MODE")
        dynamic(10)
    }
}
