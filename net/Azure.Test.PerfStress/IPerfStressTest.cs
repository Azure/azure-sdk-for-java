using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    internal interface IPerfStressTest
    {
        Task GlobalSetup();
        Task Setup();
        void Run(CancellationToken cancellationToken);
        Task RunAsync(CancellationToken cancellationToken);
        Task Cleanup();
        Task GlobalCleanup();
    }
}
