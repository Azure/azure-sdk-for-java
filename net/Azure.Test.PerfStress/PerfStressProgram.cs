using CommandLine;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Reflection;
using System.Reflection.Emit;
using System.Runtime;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    public static class PerfStressProgram
    {
        private static int[] _completedOperations;
        private static TimeSpan[] _lastCompletionTimes;

        public static async Task Main(Assembly assembly, string[] args)
        {
            var testTypes = assembly.ExportedTypes
                .Concat(typeof(PerfStressProgram).Assembly.ExportedTypes)
                .Where(t => typeof(IPerfStressTest).IsAssignableFrom(t) && !t.IsAbstract);

            var optionTypes = GetOptionTypes(testTypes);

            await Parser.Default.ParseArguments(args, optionTypes).MapResult<PerfStressOptions, Task>(
                async o =>
                {
                    var verbName = o.GetType().GetCustomAttribute<VerbAttribute>().Name;
                    var testType = testTypes.Where(t => GetVerbName(t.Name) == verbName).Single();
                    await Run(testType, o);
                },
                errors => Task.CompletedTask
            );
        }

        private static async Task Run(Type testType, PerfStressOptions options)
        {
            if (!GCSettings.IsServerGC)
            {
                throw new InvalidOperationException("Requires server GC");
            }

            Console.WriteLine("=== Options ===");
            Console.WriteLine(JsonSerializer.Serialize(options, options.GetType(), new JsonSerializerOptions()
            {
                WriteIndented = true
            }));
            Console.WriteLine();

            using var setupStatusCts = new CancellationTokenSource();
            var setupStatusThread = PrintStatus("=== Setup ===", () => ".", newLine: false, setupStatusCts.Token);

            using var cleanupStatusCts = new CancellationTokenSource();
            Thread cleanupStatusThread = null;

            var tests = new IPerfStressTest[options.Parallel];
            for (var i = 0; i < options.Parallel; i++)
            {
                tests[i] = (IPerfStressTest)Activator.CreateInstance(testType, options);
            }

            try
            {
                await tests[0].GlobalSetupAsync();

                try
                {
                    await Task.WhenAll(tests.Select(t => t.SetupAsync()));
                    setupStatusCts.Cancel();
                    setupStatusThread.Join();

                    if (options.Warmup > 0)
                    {
                        await RunTestsAsync(tests, options.Sync, options.Parallel, options.Warmup, "Warmup");
                    }

                    for (var i=0; i< options.Iterations; i++)
                    {
                        var title = "Test";
                        if (options.Iterations > 1)
                        {
                            title += " " + (i + 1);
                        }
                        await RunTestsAsync(tests, options.Sync, options.Parallel, options.Duration, title);
                    }
                }
                finally
                {
                    if (!options.NoCleanup)
                    {
                        if (cleanupStatusThread == null)
                        {
                            cleanupStatusThread = PrintStatus("=== Cleanup ===", () => ".", newLine: false, cleanupStatusCts.Token);
                        }

                        await Task.WhenAll(tests.Select(t => t.CleanupAsync()));
                    }
                }
            }
            finally
            {
                if (!options.NoCleanup)
                {
                    if (cleanupStatusThread == null)
                    {
                        cleanupStatusThread = PrintStatus("=== Cleanup ===", () => ".", newLine: false, cleanupStatusCts.Token);
                    }

                    await tests[0].GlobalCleanupAsync();
                }
            }

            cleanupStatusCts.Cancel();
            if (cleanupStatusThread != null)
            {
                cleanupStatusThread.Join();
            }
        }

        private static async Task RunTestsAsync(IPerfStressTest[] tests, bool sync, int parallel, int durationSeconds, string title)
        {
            _completedOperations = new int[parallel];
            _lastCompletionTimes = new TimeSpan[parallel];

            var duration = TimeSpan.FromSeconds(durationSeconds);
            using var testCts = new CancellationTokenSource(duration);
            var cancellationToken = testCts.Token;

            var lastCompleted = 0;

            using var progressStatusCts = new CancellationTokenSource();
            var progressStatusThread = PrintStatus(
                $"=== {title} ===" + Environment.NewLine +
                "Current\t\tTotal",
                () =>
                {
                    var totalCompleted = _completedOperations.Sum();
                    var currentCompleted = totalCompleted - lastCompleted;
                    lastCompleted = totalCompleted;
                    return currentCompleted + "\t\t" + totalCompleted;
                },
                newLine: true,
                progressStatusCts.Token);

            if (sync)
            {
                var threads = new Thread[parallel];

                for (var i = 0; i < parallel; i++)
                {
                    var j = i;
                    threads[i] = new Thread(() => RunLoop(tests[j], j, cancellationToken));
                    threads[i].Start();
                }
                for (var i = 0; i < parallel; i++)
                {
                    threads[i].Join();
                }
            }
            else
            {
                var tasks = new Task[parallel];
                for (var i = 0; i < parallel; i++)
                {
                    var j = i;
                    // Call Task.Run() instead of directly calling RunLoopAsync(), to ensure the requested
                    // level of parallelism is achieved even if the test RunAsync() completes synchronously.
                    tasks[j] = Task.Run(() => RunLoopAsync(tests[j], j, cancellationToken));
                }
                await Task.WhenAll(tasks);
            }

            progressStatusCts.Cancel();
            progressStatusThread.Join();

            Console.WriteLine("=== Results ===");

            var totalOperations = _completedOperations.Sum();
            var operationsPerSecond = _completedOperations.Zip(_lastCompletionTimes, (operations, time) => (operations / time.TotalSeconds)).Sum();
            var secondsPerOperation = 1 / operationsPerSecond;
            var weightedAverageSeconds = totalOperations / operationsPerSecond;

            Console.WriteLine($"Completed {totalOperations} operations in a weighted-average of {weightedAverageSeconds:N2}s " +
                $"({operationsPerSecond:N2} ops/s, {secondsPerOperation:N3} s/op)");
            Console.WriteLine();
        }

        private static void RunLoop(IPerfStressTest test, int index, CancellationToken cancellationToken)
        {
            var sw = Stopwatch.StartNew();
            while (!cancellationToken.IsCancellationRequested)
            {
                try
                {
                    test.Run(cancellationToken);
                    _completedOperations[index]++;
                    _lastCompletionTimes[index] = sw.Elapsed;
                }
                catch (OperationCanceledException)
                {
                }
            }
        }

        private static async Task RunLoopAsync(IPerfStressTest test, int index, CancellationToken cancellationToken)
        {
            var sw = Stopwatch.StartNew();
            while (!cancellationToken.IsCancellationRequested)
            {
                try
                {
                    await test.RunAsync(cancellationToken);
                    _completedOperations[index]++;
                    _lastCompletionTimes[index] = sw.Elapsed;
                }
                catch (Exception e)
                {
                    // Ignore if any part of the exception chain is type OperationCanceledException
                    if (!ContainsOperationCanceledException(e))
                    {
                        throw;
                    }
                }
            }
        }

        // Run in dedicated thread instead of using async/await in ThreadPool, to ensure this thread has priority
        // and never fails to run to due ThreadPool starvation.
        private static Thread PrintStatus(string header, Func<object> status, bool newLine, CancellationToken token)
        {
            var thread = new Thread(() =>
            {
                Console.WriteLine(header);

                bool needsExtraNewline = false;

                while (!token.IsCancellationRequested)
                {
                    try
                    {
                        Sleep(TimeSpan.FromSeconds(1), token);
                    }
                    catch (OperationCanceledException)
                    {
                    }

                    var obj = status();

                    if (newLine)
                    {
                        Console.WriteLine(obj);
                    }
                    else
                    {
                        Console.Write(obj);
                        needsExtraNewline = true;
                    }
                }

                if (needsExtraNewline)
                {
                    Console.WriteLine();
                }

                Console.WriteLine();
            });
            
            thread.Start();
            
            return thread;
        }

        private static void Sleep(TimeSpan timeout, CancellationToken token)
        {
            var sw = Stopwatch.StartNew();
            while (sw.Elapsed < timeout)
            {
                if (token.IsCancellationRequested)
                {
                    // Simulate behavior of Task.Delay(TimeSpan, CancellationToken)
                    throw new OperationCanceledException();
                }

                Thread.Sleep(TimeSpan.FromMilliseconds(10));
            }
        }

        private static bool ContainsOperationCanceledException(Exception e)
        {
            if (e is OperationCanceledException)
            {
                return true;
            }
            else if (e.InnerException != null)
            {
                return ContainsOperationCanceledException(e.InnerException);
            }
            else
            {
                return false;
            }
        }

        // Dynamically create option types with a "Verb" attribute
        private static Type[] GetOptionTypes(IEnumerable<Type> testTypes)
        {
            var optionTypes = new List<Type>();

            var ab = AssemblyBuilder.DefineDynamicAssembly(new AssemblyName("Options"), AssemblyBuilderAccess.Run);
            var mb = ab.DefineDynamicModule("Options");

            foreach (var t in testTypes)
            {
                var baseOptionsType = t.GetConstructors().First().GetParameters()[0].ParameterType;
                var tb = mb.DefineType(t.Name + "Options", TypeAttributes.Public, baseOptionsType);

                var attrCtor = typeof(VerbAttribute).GetConstructor(new Type[] { typeof(string) });
                var verbName = GetVerbName(t.Name);
                tb.SetCustomAttribute(new CustomAttributeBuilder(attrCtor, new object[] { verbName }));

                optionTypes.Add(tb.CreateType());
            }

            return optionTypes.ToArray();
        }

        private static string GetVerbName(string testName)
        {
            var lower = testName.ToLowerInvariant();
            return lower.EndsWith("test") ? lower.Substring(0, lower.Length - 4) : lower;
        }
    }
}
