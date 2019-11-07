from core.PerfStressTest import PerfStressTest

class NoOpTest(PerfStressTest):
    def Run(self):
        pass

    async def RunAsync(self):
        pass
    