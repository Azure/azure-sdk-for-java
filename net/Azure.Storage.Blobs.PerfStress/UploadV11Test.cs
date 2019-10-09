using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadV11Test : ContainerV11Test<SizeOptions>
    {
        private readonly CloudBlockBlob _cloudBlockBlob;

        public UploadV11Test(SizeOptions options) : base(options)
        {
            var blobName = "uploadv11test-" + Guid.NewGuid();
            _cloudBlockBlob = CloudBlobContainer.GetBlockBlobReference(blobName);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);

            // No need to delete file in Cleanup(), since ContainerTest.GlobalCleanup() deletes the whole container
            _cloudBlockBlob.UploadFromStream(stream);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);

            // No need to delete file in Cleanup(), since ContainerTest.GlobalCleanup() deletes the whole container
            await _cloudBlockBlob.UploadFromStreamAsync(stream, cancellationToken);
        }
    }
}
