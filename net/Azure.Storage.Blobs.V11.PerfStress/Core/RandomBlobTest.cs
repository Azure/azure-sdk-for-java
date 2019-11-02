using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public abstract class RandomBlobTest<TOptions> : ContainerTest<TOptions> where TOptions: SizeOptions
    {
        protected CloudBlockBlob CloudBlockBlob { get; private set; }

        public RandomBlobTest(TOptions options) : base(options)
        {
            var blobName = "uploadv11test-" + Guid.NewGuid();
            CloudBlockBlob = CloudBlobContainer.GetBlockBlobReference(blobName);
        }
    }
}
