def isParallelEnabled = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_ENABLED", "true").toBoolean()
def factor = System.getenv().getOrDefault("AZURE_STORAGE_TESTS_PARALLEL_FACTOR", "5").toBigDecimal()
def numberOfProcessors = Runtime.getRuntime().availableProcessors()

printf("Parallelization is enabled=%b, factor=%.2f, processors=%d config=%s%n",
    isParallelEnabled, factor, numberOfProcessors, this.class.protectionDomain.codeSource.location)

def isRunningOnAgent = System.getenv().getOrDefault("TF_BUILD", "false").toBoolean()
if (isRunningOnAgent) {
    printf("%s=%s%n", "AGENT_JOBNAME", System.getenv("AGENT_JOBNAME"))
    printf("%s=%s%n", "BUILD_BUILDID", System.getenv("BUILD_BUILDID"))
    printf("%s=%s%n", "BUILD_DEFINITIONNAME", System.getenv("BUILD_DEFINITIONNAME"))
    printf("%s=%s%n", "BUILD_REASON", System.getenv("BUILD_REASON"))
    printf("%s=%s%n", "SYSTEM_TEAMPROJECT", System.getenv("SYSTEM_TEAMPROJECT"))
    printf("%s=%s%n", "SETDEVVERSION", System.getenv("SETDEVVERSION"))
}

def parallelism = factor.multiply(numberOfProcessors).intValue()
def minimumRunnable = parallelism * 2
def maxPoolSize = 256 + parallelism
def corePoolSize = parallelism * 2
def keepAliveSeconds = 30

runner {
    parallel {
        enabled isParallelEnabled
        custom(parallelism, minimumRunnable, maxPoolSize, corePoolSize, keepAliveSeconds)
    }
}
