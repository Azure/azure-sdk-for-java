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
        private static int _completedOperations;
        private static TimeSpan[] _lastCompletionTimes;

        public static async Task Main(Assembly assembly, string[] args)
        {
            var testTypes = assembly.ExportedTypes
                .Where(t => typeof(IPerfStressTest).IsAssignableFrom(t) && !t.IsAbstract)
                .Append(typeof(NoOpTest));

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
            var setupStatusTask = PrintStatusAsync("=== Setup ===", () => ".", newLine: false, setupStatusCts.Token);

            using var cleanupStatusCts = new CancellationTokenSource();
            Task cleanupStatusTask = null;

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
                    await setupStatusTask;

                    if (options.Warmup > 0)
                    {
                        await RunTestsAsync(tests, options.Sync, options.Parallel, options.Warmup, "Warmup");
                    }

                    await RunTestsAsync(tests, options.Sync, options.Parallel, options.Duration, "Test");
                }
                finally
                {
                    if (!options.NoCleanup)
                    {
                        if (cleanupStatusTask == null)
                        {
                            cleanupStatusTask = PrintStatusAsync("=== Cleanup ===", () => ".", newLine: false, cleanupStatusCts.Token);
                        }

                        await Task.WhenAll(tests.Select(t => t.CleanupAsync()));
                    }
                }
            }
            finally
            {
                if (!options.NoCleanup)
                {
                    if (cleanupStatusTask == null)
                    {
                        cleanupStatusTask = PrintStatusAsync("=== Cleanup ===", () => ".", newLine: false, cleanupStatusCts.Token);
                    }

                    await tests[0].GlobalCleanupAsync();
                }
            }

            cleanupStatusCts.Cancel();
            if (cleanupStatusTask != null)
            {
                await cleanupStatusTask;
            }

            Console.WriteLine("=== Results ===");

            var averageElapsedSeconds = _lastCompletionTimes.Select(t => t.TotalSeconds).Average();
            var operationsPerSecond = _completedOperations / averageElapsedSeconds;
            var secondsPerOperation = 1 / operationsPerSecond;

            Console.WriteLine($"Completed {_completedOperations} operations in an average of {averageElapsedSeconds:N2}s " +
                $"({operationsPerSecond:N2} ops/s, {secondsPerOperation:N3} s/op)");
            Console.WriteLine();
        }

        private static async Task RunTestsAsync(IPerfStressTest[] tests, bool sync, int parallel, int durationSeconds, string title)
        {
            _completedOperations = 0;
            _lastCompletionTimes = new TimeSpan[parallel];

            var duration = TimeSpan.FromSeconds(durationSeconds);
            using var cts = new CancellationTokenSource(duration);
            var cancellationToken = cts.Token;

            var lastCompleted = 0;
            var progressStatusTask = PrintStatusAsync(
                $"=== {title} ===" + Environment.NewLine +
                "Current\t\tTotal",
                () =>
                {
                    var totalCompleted = _completedOperations;
                    var currentCompleted = totalCompleted - lastCompleted;
                    lastCompleted = totalCompleted;
                    return currentCompleted + "\t\t" + totalCompleted;
                },
                newLine: true,
                cancellationToken);

            if (sync)
            {
                var threads = new Thread[parallel];

                for (var i = 0; i < parallel; i++)
                {
                    var j = i;
                    threads[i] = new Thread(() => RunLoop(tests[j], cancellationToken));
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
                    tasks[i] = RunLoopAsync(tests[i], cancellationToken);
                }
                await Task.WhenAll(tasks);
            }

            await progressStatusTask;
        }

        private static void RunLoop(IPerfStressTest test, CancellationToken cancellationToken)
        {
            var sw = Stopwatch.StartNew();
            while (!cancellationToken.IsCancellationRequested)
            {
                try
                {
                    test.Run(cancellationToken);
                    var count = Interlocked.Increment(ref _completedOperations);
                    _lastCompletionTimes[count % _lastCompletionTimes.Length] = sw.Elapsed;
                }
                catch (OperationCanceledException)
                {
                }
            }
        }

        private static async Task RunLoopAsync(IPerfStressTest test, CancellationToken cancellationToken)
        {
            var sw = Stopwatch.StartNew();
            while (!cancellationToken.IsCancellationRequested)
            {
                try
                {
                    await test.RunAsync(cancellationToken);
                    var count = Interlocked.Increment(ref _completedOperations);
                    _lastCompletionTimes[count % _lastCompletionTimes.Length] = sw.Elapsed;
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

        private static async Task PrintStatusAsync(string header, Func<object> status, bool newLine, CancellationToken token)
        {
            Console.WriteLine(header);

            bool needsExtraNewline = false;

            while (!token.IsCancellationRequested)
            {
                try
                {
                    await Task.Delay(TimeSpan.FromSeconds(1), token);

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
                catch (OperationCanceledException)
                {
                }
            }

            if (needsExtraNewline)
            {
                Console.WriteLine();
            }

            Console.WriteLine();
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
