using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    internal interface IPerfStressTest
    {
        Task GlobalSetupAsync();
        Task SetupAsync();
        void Run(CancellationToken cancellationToken);
        Task RunAsync(CancellationToken cancellationToken);
        Task CleanupAsync();
        Task GlobalCleanupAsync();
    }
}
