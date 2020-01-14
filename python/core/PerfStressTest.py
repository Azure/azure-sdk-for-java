class PerfStressTest:
    '''Base class for implementing a python perf test.  
    
- Run and RunAsync must be implemented.
- GlobalSetup and GlobalCleanup are optional and run once, ever, regardless of parallelism.
- Setup and Cleanup are run once per test instance (where each instance runs in its own thread/process), regardless of #iterations.
- Run/RunAsync are run once per iteration.'''
    async def GlobalSetupAsync(self):
        return

    async def GlobalCleanupAsync(self):
        return

    async def SetupAsync(self):
        return

    async def CleanupAsync(self):
        return

    def __enter__(self):
        return

    def __exit__(self, exc_type, exc_value, traceback):
        return

    def Run(self):
        raise Exception('Run must be implemented for {}'.format(self.__class__.__name__))

    async def RunAsync(self):
        raise Exception('RunAsync must be implemented for {}'.format(self.__class__.__name__))

    Arguments = {}
    # Override this method to add test-specific argparser args to the class.
    # These are accessable in the Arguments class property.
    @staticmethod
    def AddArguments(parser):
        return