using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    // Measures the overhead of creating, throwing, and catching an exception (compared to NoOpTest)
    public class ExceptionTest : PerfStressTest<PerfStressOptions>
    {
        public ExceptionTest(PerfStressOptions options) : base(options) { }

        public override void Run(CancellationToken cancellationToken)
        {
            try
            {
                throw new InvalidOperationException();
            }
            catch
            {
            }
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            try
            {
                throw new InvalidOperationException();
            }
            catch
            {
            }

            return Task.CompletedTask;
        }
    }
}
