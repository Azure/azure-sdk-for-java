import org.spockframework.runtime.model.parallel.ExecutionMode;

runner {
    parallel {
        enabled false
        dynamic(5)
    }
}
