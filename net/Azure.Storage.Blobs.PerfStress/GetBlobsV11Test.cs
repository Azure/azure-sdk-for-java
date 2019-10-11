using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class GetBlobsV11Test : ContainerV11Test<CountOptions>
    {
        public GetBlobsV11Test(CountOptions options) : base(options) { }

        public override async Task GlobalSetupAsync()
        {
            await base.GlobalSetupAsync();

            var uploadTasks = new Task[Options.Count];
            for (var i = 0; i < uploadTasks.Length; i++)
            {
                var blobName = "getblobstest-" + Guid.NewGuid();
                uploadTasks[i] = CloudBlobContainer.GetBlockBlobReference(blobName).UploadFromStreamAsync(Stream.Null);
            }
            await Task.WhenAll(uploadTasks);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            // Enumerate collection to ensure all IListBlobItems are downloaded
            foreach (var _ in CloudBlobContainer.ListBlobs()) { }
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            BlobContinuationToken continuationToken = null;
            do
            {
                var result = await CloudBlobContainer.ListBlobsSegmentedAsync(continuationToken, cancellationToken);
                continuationToken = result.ContinuationToken;

                // Enumerate collection to ensure all IListBlobItems are downloaded
                foreach (var _ in result.Results) { }
            }
            while (continuationToken != null);
        }

    }
}
