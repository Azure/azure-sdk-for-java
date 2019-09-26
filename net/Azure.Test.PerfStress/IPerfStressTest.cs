using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    internal interface IPerfStressTest : IDisposable
    {
        void Run();
        Task RunAsync(CancellationToken cancellationToken);
    }
}
