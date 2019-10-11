using Azure.Storage.Blobs.PerfStress.Core;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class DownloadTest : ContainerTest<ParallelTransferOptionsOptions>
    {
        private readonly BlobClient _blobClient;

        public DownloadTest(ParallelTransferOptionsOptions options) : base(options)
        {
            _blobClient = BlobContainerClient.GetBlobClient("downloadtest");
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            using var stream = RandomStream.Create(Options.Size);
            
            // No need to delete file in GlobalCleanup(), since ContainerTest.GlobalCleanup() deletes the whole container
            await _blobClient.UploadAsync(stream);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            _blobClient.Download(Stream.Null, parallelTransferOptions: Options.ParallelTransferOptions, cancellationToken: cancellationToken);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            await _blobClient.DownloadAsync(Stream.Null, parallelTransferOptions: Options.ParallelTransferOptions, cancellationToken: cancellationToken);
        }
    }
}
