using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    public abstract class PerfStressTest<TOptions> : IPerfStressTest where TOptions : PerfStressOptions
    {
        protected string Id { get; private set; }

        protected TOptions Options { get; private set; }

        public PerfStressTest(string id, TOptions options)
        {
            Id = id;
            Options = options;
        }

        public abstract void Run(CancellationToken cancellationToken);

        public abstract Task RunAsync(CancellationToken cancellationToken);

        public virtual void Dispose()
        {
        }
    }
}
