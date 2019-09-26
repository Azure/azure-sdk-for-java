using Azure.Storage.Blobs.PerfStress.Core;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class DownloadTest : ParallelTransferTest<ParallelTransferOptionsOptions>
    {
        public DownloadTest(ParallelTransferOptionsOptions options) : base(options)
        {
        }

        public override void Run()
        {
            throw new System.NotImplementedException();
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            throw new System.NotImplementedException();
        }
    }
}
