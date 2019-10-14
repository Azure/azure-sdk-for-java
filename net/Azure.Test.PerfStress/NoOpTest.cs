using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    public class NoOpTest : PerfStressTest<PerfStressOptions>
    {
        public NoOpTest(PerfStressOptions options) : base(options) { }

        public override void Run(CancellationToken cancellationToken)
        {
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }
    }
}
