using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Test.PerfStress
{
    internal interface IPerfStressTest : IDisposable
    {
        void Run(CancellationToken cancellationToken);
        Task RunAsync(CancellationToken cancellationToken);
    }
}
