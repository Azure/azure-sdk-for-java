using Azure.Storage.Blobs.PerfStress.Core;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadTest : ParallelTransferTest<ParallelTransferOptionsOptions>
    {
        public UploadTest(ParallelTransferOptionsOptions options) : base(options)
        {
        }

        public override void Run(CancellationToken cancellationToken)
        {
            BlobClient.Upload(RandomStream, parallelTransferOptions: ParallelTransferOptions, cancellationToken: cancellationToken);
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            return BlobClient.UploadAsync(RandomStream, parallelTransferOptions: ParallelTransferOptions, cancellationToken: cancellationToken);
        }

        public override void Dispose()
        {
            BlobClient.Delete();
            base.Dispose();
        }
    }
}
