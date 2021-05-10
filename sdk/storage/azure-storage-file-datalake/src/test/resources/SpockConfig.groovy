def isParallelEnabled = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_ENABLED", "true").toBoolean()
def factor = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_FACTOR", "1").toBigDecimal()

printf("Parallelization is enabled=%b, factor=%.2f, config=%s%n" , isParallelEnabled, factor, this.class.protectionDomain.codeSource.location)

runner {
    parallel {
        enabled isParallelEnabled
        dynamic(factor)
    }
}
