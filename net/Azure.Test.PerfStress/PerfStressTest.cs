using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    public abstract class PerfStressTest<TOptions> : IPerfStressTest where TOptions : PerfStressOptions
    {
        protected TOptions Options { get; private set; }

        public PerfStressTest(TOptions options)
        {
            Options = options;
        }

        public virtual Task GlobalSetup()
        {
            return Task.CompletedTask;
        }

        public virtual Task Setup()
        {
            return Task.CompletedTask;
        }

        public abstract void Run(CancellationToken cancellationToken);

        public abstract Task RunAsync(CancellationToken cancellationToken);

        public virtual Task Cleanup()
        {
            return Task.CompletedTask;
        }

        public virtual Task GlobalCleanup()
        {
            return Task.CompletedTask;
        }
    }
}
