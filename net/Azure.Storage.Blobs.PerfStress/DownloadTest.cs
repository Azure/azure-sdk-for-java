using Azure.Storage.Blobs.PerfStress.Core;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class DownloadTest : ContainerTest<StorageTransferOptionsOptions>
    {
        private readonly BlobClient _blobClient;

        public DownloadTest(StorageTransferOptionsOptions options) : base(options)
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
            _blobClient.DownloadTo(Stream.Null, transferOptions: Options.StorageTransferOptions, cancellationToken: cancellationToken);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            await _blobClient.DownloadToAsync(Stream.Null, transferOptions: Options.StorageTransferOptions, cancellationToken: cancellationToken);
        }
    }
}
