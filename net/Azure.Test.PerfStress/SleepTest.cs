using System;
using System.Numerics;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    // Used for verifying the perf framework correctly computes average throughput across parallel tests of different speed
    public class SleepTest : PerfStressTest<PerfStressOptions>
    {
        private static int _instanceCount = 0;
        private readonly int _secondsPerOperation;

        public SleepTest(PerfStressOptions options) : base(options) {
            // Each instance of this test completes operations at a different rate, to allow for testing scenarios where
            // some instances are still waiting when time expires.  The first instance completes in 2 seconds per operation,
            // the second instance in 4 seconds, the third instance in 8 seconds, and so on. 

            var instanceCount = Interlocked.Increment(ref _instanceCount);
            _secondsPerOperation = Pow(2, instanceCount);
        }

        private static int Pow(int value, int exponent)
        {
            return (int)BigInteger.Pow(new BigInteger(value), exponent);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            Thread.Sleep(TimeSpan.FromSeconds(_secondsPerOperation));
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            return Task.Delay(TimeSpan.FromSeconds(_secondsPerOperation), cancellationToken);
        }
    }
}
