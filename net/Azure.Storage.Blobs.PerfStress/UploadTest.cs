using Azure.Storage.Blobs.PerfStress.Core;
using CommandLine;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadTest : ParallelTransferTest<UploadTest.UploadOptions>
    {
        [Verb(nameof(UploadTest))]
        public class UploadOptions : ParallelTransferOptionsOptions { }

        public UploadTest(UploadOptions options) : base(options)
        {
        }

        public override void Run()
        {
            BlobClient.Upload(RandomStream, parallelTransferOptions: ParallelTransferOptions);
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
