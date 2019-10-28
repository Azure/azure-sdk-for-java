import argparse
import asyncio
import time
import inspect
import json
import logging
import os
import pkgutil
import sys
import threading

from .PerfStressTest import PerfStressTest
from .Helpers import RepeatedTimer

_DEFAULT_TEST_LOCATION = os.path.join(os.path.dirname(__file__), '../tests')

class PerfStressRunner:
    def __init__(self, test_folder_path=_DEFAULT_TEST_LOCATION):
        self.logger = logging.getLogger(__name__)
        self.logger.setLevel(level=logging.INFO)
        handler = logging.StreamHandler()
        handler.setLevel(level=logging.INFO)
        self.logger.addHandler(handler)

        #NOTE: If you need to support registering multiple test locations, move this into Initialize, call lazily on Run, expose RegisterTestLocation function.
        self._DiscoverTests(test_folder_path)
        self._ParseArgs()


        self._operation_count_lock = threading.Lock()
        self._operation_count = 0
        self._last_completed = -1
        #NOTE: despite this being "for each thread" the semantics don't actually care about each thread; just the N latest durations where N arbitrarily == # of parallel threads.
        self._latest_operation_durations = [0 for _ in range(0, self._test_class_to_run.Arguments.parallel)] 
        

    def _ParseArgs(self):
        # First, detect which test we're running.
        arg_parser = argparse.ArgumentParser(
            description='Python Perf Test Runner',
            usage='{} <TEST> [<args>]'.format(__file__))

        # NOTE: remove this and add another help string to query for available tests
        # if/when # of classes become enough that this isn't practical.
        arg_parser.add_argument('test', help='Which test to run.  Supported tests: {}'.format(" ".join(sorted(self._test_classes.keys()))))

        args = arg_parser.parse_args(sys.argv[1:2])
        try:
            self._test_class_to_run = self._test_classes[args.test]
        except KeyError as e:
            self.logger.error("Invalid test: {}\n    Test must be one of: {}\n".format(args.test, " ".join(sorted(self._test_classes.keys()))))
            raise

        # Next, parse args for that test.  We also do global args here too so as not to confuse the initial test parse.
        per_test_arg_parser = argparse.ArgumentParser(
            description=self._test_class_to_run.__doc__ or args.test,
            usage='{} {} [<args>]'.format(__file__, args.test))

        # Global args
        per_test_arg_parser.add_argument('-p', '--parallel', nargs='?', type=int, help='Degree of parallelism to run with.  Default is 1.', default=1)
        per_test_arg_parser.add_argument('-d', '--duration', nargs='?', type=int, help='Duration of the test in seconds.  Default is 10.', default=10)
        per_test_arg_parser.add_argument('-i', '--iterations', nargs='?', type=int, help='Number of iterations in the main test loop.  Default is 1.', default=1)
        per_test_arg_parser.add_argument('-w', '--warmup', nargs='?', type=int, help='Duration of warmup in seconds.  Default is 5.', default=5)
        per_test_arg_parser.add_argument('--no-cleanup', action='store_true', help='Do not run cleanup logic.  Default is false.', default=False)
        per_test_arg_parser.add_argument('--sync', action='store_true', help='Run tests in sync mode.  Default is False.', default=False)

        # Per-test args
        self._test_class_to_run.AddArguments(per_test_arg_parser)
        per_test_args = per_test_arg_parser.parse_args(sys.argv[2:])

        self._test_class_to_run.Arguments = per_test_args

        self.logger.info("=== Options ===")
        self.logger.info(args)
        self.logger.info(per_test_args)


    def _DiscoverTests(self, test_folder_path=_DEFAULT_TEST_LOCATION):
        self._test_classes = {}

        # Dynamically enumerate all python modules under the tests path for classes that implement PerfStressTest       
        for loader, name, _ in pkgutil.walk_packages([test_folder_path]):

            module = loader.find_module(name).load_module(name)
            for name, value in inspect.getmembers(module):

                if name.startswith('_'):
                    continue
                if inspect.isclass(value) and issubclass(value, PerfStressTest) and value != PerfStressTest:
                    self.logger.info("Loaded test class: {}".format(name))
                    self._test_classes[name] = value


    async def RunAsync(self):      
        self.logger.info("=== Setup ===")
       
        tests = []
        for _ in range(0, self._test_class_to_run.Arguments.parallel):
            tests.append(self._test_class_to_run())

        try:
            await tests[0].GlobalSetupAsync()
            try:
                await asyncio.gather(*[test.SetupAsync() for test in tests])

                if self._test_class_to_run.Arguments.warmup > 0:
                    await self._RunTestsAsync(tests, self._test_class_to_run.Arguments.warmup, "Warmup")

                self.logger.info("=== Running ===")

                for i in range(0, self._test_class_to_run.Arguments.iterations):
                    await self._RunTestsAsync(tests, self._test_class_to_run.Arguments.duration, "Test {}".format(i))

            finally:
                if not self._test_class_to_run.Arguments.no_cleanup:
                    self.logger.info("=== Cleanup ===")
                    await asyncio.gather(*[test.CleanupAsync() for test in tests])
        finally:
            if not self._test_class_to_run.Arguments.no_cleanup:
                await tests[0].GlobalCleanupAsync()


    async def _RunTestsAsync(self, tests, duration, title):
        self._operation_count = 0
        self._last_completed = -1
        self._latest_operation_durations = [0 for _ in range(0, self._test_class_to_run.Arguments.parallel)] 

        status_thread = RepeatedTimer(1, self._PrintStatus, title)

        if self._test_class_to_run.Arguments.sync:
            threads = []
            for test in tests:
                thread = threading.Thread(target=lambda: self.RunLoop(test, duration))
                threads.append(thread)
                thread.start()
            for thread in threads:
                thread.join()
        else:
            #await asyncio.gather(*[self.RunLoopAsync(test, duration) for test in tests])
            await self.RunLoopAsync(tests[0], duration)

        status_thread.stop()

        self.logger.info("=== Results ===")
        count_per_second = self._operation_count / (sum(self._latest_operation_durations) / len(self._latest_operation_durations))
        count_per_second_per_thread = count_per_second / self._test_class_to_run.Arguments.parallel
        seconds_per_operation = 1/count_per_second_per_thread
        self.logger.info("\tCompleted {} operations\n\tAverage {} operations per thread per second\n\tAverage {} seconds per operation".format(self._operation_count, count_per_second_per_thread, seconds_per_operation))


    def RunLoop(self, test, duration):
        start = time.time()
        runtime = 0
        while runtime < duration:
            test.Run()
            runtime = time.time() - start
            self._IncrementOperationCountAndTime(runtime)


    async def RunLoopAsync(self, test, duration):
        start = time.time()
        runtime = 0
        while runtime < duration:
            await test.RunAsync()
            runtime = time.time() - start
            self._IncrementOperationCountAndTime(runtime) #NOTE: to exactly match the other algo, change this to not count the last iteration which exceeds duration. (since the cancellation token there will actually cancel it.)


    def _IncrementOperationCountAndTime(self, runtime):
        with self._operation_count_lock: #NOTE: If you want to improve perf, calling this at the end of every thread instead of every iteration would be a great attempt.
            self._operation_count += 1
            self._latest_operation_durations[self._operation_count % len(self._latest_operation_durations)] = runtime


    def _PrintStatus(self, title):
        if self._last_completed == -1:
            self._last_completed = 0
            self.logger.info("=== {} ===\nCurrent\t\tTotal".format(title))
        self.logger.info("{}\t\t{}".format(self._operation_count - self._last_completed, self._operation_count))
        self._last_completed = self._operation_count