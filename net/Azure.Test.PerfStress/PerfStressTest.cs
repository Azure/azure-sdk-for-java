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

        public abstract void Run();

        public abstract Task RunAsync(CancellationToken cancellationToken);

        public virtual void Dispose()
        {
        }
    }
}
