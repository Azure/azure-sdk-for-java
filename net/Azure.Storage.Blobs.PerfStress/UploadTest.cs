using Azure.Storage.Blobs.PerfStress.Core;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadTest : ContainerTest<StorageTransferOptionsOptions>
    {
        private readonly BlobClient _blobClient;

        public UploadTest(StorageTransferOptionsOptions options) : base(options)
        {
            var blobName = "uploadtest-" + Guid.NewGuid();
            _blobClient = BlobContainerClient.GetBlobClient(blobName);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);

            // No need to delete file in Cleanup(), since ContainerTest.GlobalCleanup() deletes the whole container
            _blobClient.Upload(stream, transferOptions: Options.StorageTransferOptions, cancellationToken: cancellationToken);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);

            // No need to delete file in Cleanup(), since ContainerTest.GlobalCleanup() deletes the whole container
            await _blobClient.UploadAsync(stream, transferOptions: Options.StorageTransferOptions,  cancellationToken: cancellationToken);
        }
    }
}
