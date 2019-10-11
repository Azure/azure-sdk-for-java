using Azure.Storage.Blobs.PerfStress.Core;
using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class GetBlobsTest : ContainerTest<CountOptions>
    {
        public GetBlobsTest(CountOptions options) : base(options)
        {
        }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            var uploadTasks = new Task[Options.Count];
            for (var i = 0; i < uploadTasks.Length; i++)
            {
                var blobName = "getblobstest-" + Guid.NewGuid();
                uploadTasks[i] = BlobContainerClient.UploadBlobAsync(blobName, Stream.Null);
            }
            await Task.WhenAll(uploadTasks);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            // Enumerate collection to ensure all BlobItems are downloaded
            foreach (var _ in BlobContainerClient.GetBlobs(cancellationToken: cancellationToken));
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            // Enumerate collection to ensure all BlobItems are downloaded
            await foreach (var _ in BlobContainerClient.GetBlobsAsync(cancellationToken: cancellationToken)) { }
        }
    }
}
