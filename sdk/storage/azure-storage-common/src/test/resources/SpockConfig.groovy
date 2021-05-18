def isParallelEnabled = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_ENABLED", "true").toBoolean()
def factor = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_FACTOR", "5").toBigDecimal()
def numberOfProcessors = Runtime.getRuntime().availableProcessors()

printf("Parallelization is enabled=%b, factor=%.2f, processors=%d config=%s%n",
    isParallelEnabled, factor, numberOfProcessors, this.class.protectionDomain.codeSource.location)

runner {
    parallel {
        enabled isParallelEnabled
        dynamic(factor)
    }
}
