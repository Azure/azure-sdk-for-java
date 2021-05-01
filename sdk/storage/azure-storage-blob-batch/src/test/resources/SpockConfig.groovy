runner {
    parallel {
        enabled "true" == System.getProperty("junit.jupiter.execution.parallel.enabled")
        dynamic(10)
    }
}
