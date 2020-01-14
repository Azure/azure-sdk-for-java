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
        self._latest_operation_durations = []
        # These (durations and status) are different despite tracking the runners _technically_ it's not wise to use structures like this in a non-thread-safe manner, 
        # so I'm only abusing this for ease of status updates where error doesn't really matter, and durations is still aggregated in a safe way.
        self._status = {} 
        

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

            try:
                module = loader.find_module(name).load_module(name)
            except Exception as e:
                self.logger.warn("Unable to load module {}: {}".format(name, e))
                continue
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
        self._latest_operation_durations = []
        self._status = {}

        status_thread = RepeatedTimer(1, self._PrintStatus, title)

        if self._test_class_to_run.Arguments.sync:
            threads = []
            for id, test in enumerate(tests):
                thread = threading.Thread(target=lambda: self.RunLoop(test, duration, id))
                threads.append(thread)
                thread.start()
            for thread in threads:
                thread.join()
        else:
            await asyncio.gather(*[self.RunLoopAsync(test, duration, id) for id, test in enumerate(tests)])

        status_thread.stop()

        self.logger.info("=== Results ===")
        try:
            count_per_second = (self._operation_count / (sum(self._latest_operation_durations) / len(self._latest_operation_durations)))
            count_per_second_per_thread = count_per_second / self._test_class_to_run.Arguments.parallel
            seconds_per_operation = 1/count_per_second_per_thread
        except ZeroDivisionError as e:
            self.logger.warn("Attempted to divide by zero: {}".format(e))
            count_per_second = 0
            count_per_second_per_thread = 0
            seconds_per_operation = 'N/A'
        self.logger.info("\tCompleted {} operations\n\tAverage {} operations per thread per second\n\tAverage {} seconds per operation".format(self._operation_count, count_per_second_per_thread, seconds_per_operation))


    def RunLoop(self, test, duration, id):
        start = time.time()
        runtime = 0
        count = 0
        while runtime < duration:
            test.Run()
            runtime = time.time() - start
            count += 1
            self._status[id] = count
        self._IncrementOperationCountAndTime(count, runtime)


    async def RunLoopAsync(self, test, duration, id):
        start = time.time()
        runtime = 0
        count = 0
        while runtime < duration:
            await test.RunAsync()
            runtime = time.time() - start
            count += 1
            self._status[id] = count
        self._IncrementOperationCountAndTime(count, runtime) 


    def _IncrementOperationCountAndTime(self, count, runtime):
        with self._operation_count_lock: # Be aware that while this can be used to update more often than "once at the end" it'll thrash the lock in the parallel case and ruin perf.
            self._operation_count += count
            self._latest_operation_durations.append(runtime)


    def _PrintStatus(self, title):
        if self._last_completed == -1:
            self._last_completed = 0
            self.logger.info("=== {} ===\nCurrent\t\tTotal".format(title))
        operation_count = sum(self._status.values())
        self.logger.info("{}\t\t{}".format(operation_count - self._last_completed, operation_count))
        self._last_completed = operation_count