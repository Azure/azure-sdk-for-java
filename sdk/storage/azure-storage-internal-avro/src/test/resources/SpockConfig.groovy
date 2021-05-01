import org.spockframework.runtime.model.parallel.ExecutionMode;

runner {
    parallel {
        enabled true
        dynamic(10)
    }
}
