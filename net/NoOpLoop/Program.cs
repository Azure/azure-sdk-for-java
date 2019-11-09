using CommandLine;
using System;
using System.Diagnostics;
using System.Runtime;
using System.Threading;
using System.Threading.Tasks;

namespace NoOpLoop
{
    class Program
    {
        private static long _counter = 0;

        private static TimeSpan[] _lastCompletionTimes = new TimeSpan[1];
        private static TimeSpan _lastCompletionTime;

        public class Options
        {
            [Option('c', "count", Default = 1_000_000)]
            public long Count { get; set; }

            [Option('i', "interlockedIncrement", Default = false)]
            public bool InterlockedIncrement { get; set; }

            [Option('t', "cancellationToken", Default = false)]
            public bool CancellationToken { get; set; }

            [Option('l', "lastCompletionTimes", Default = false)]
            public bool LastCompletionTimes { get; set; }

            //[Option('p', "parallel", Default = 1)]
            //public int Parallel { get; set; }

            //[Option('s', "sync", Default = false)]
            //public bool Sync { get; set; }
        }

        static async Task Main(string[] args)
        {
            if (!GCSettings.IsServerGC)
            {
                throw new InvalidOperationException("Requires server GC");
            }

            await Parser.Default.ParseArguments<Options>(args).MapResult(
                async o => await Run(o),
                errors => Task.CompletedTask);
        }

        private static async Task Run(Options options)
        {
            bool interlockedIncrement = options.InterlockedIncrement;
;
            var cts = new CancellationTokenSource();
            var ct = cts.Token;

            var sw = Stopwatch.StartNew();

            if (options.CancellationToken)
            {
                for (long i = 0; i < options.Count && !ct.IsCancellationRequested; i++)
                {
                    NoOp();

                    long count = -1;
                    if (interlockedIncrement)
                    {
                        count = Interlocked.Increment(ref _counter);
                    }
                    if (options.LastCompletionTimes)
                    {
                        _lastCompletionTimes[count % _lastCompletionTimes.Length] = sw.Elapsed;
                        // _lastCompletionTime = sw.Elapsed;
                    }
                }
            }
            else
            {
                for (long i = 0; i < options.Count; i++)
                {
                    NoOp();
                    long count = -1;
                    if (interlockedIncrement)
                    {
                        count = Interlocked.Increment(ref _counter);
                    }
                    if (options.LastCompletionTimes)
                    {
                        _lastCompletionTimes[count % _lastCompletionTimes.Length] = sw.Elapsed;
                    }
                }
            }

            sw.Stop();

            var elapsed = sw.Elapsed.TotalSeconds;
            var opsPerSecond = options.Count / elapsed;

            Console.WriteLine($"Called {options.Count} functions in {elapsed:N2} seconds ({opsPerSecond:N2} ops/s)");
        }

        private static void NoOp()
        {
        }
    }
}
