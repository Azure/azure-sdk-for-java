using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadTest : RandomBlobTest<SizeOptions>
    {
        public UploadTest(SizeOptions options) : base(options)
        { 
        }

        public override void Run(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);
            CloudBlockBlob.UploadFromStream(stream);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);
            await CloudBlockBlob.UploadFromStreamAsync(stream, cancellationToken);
        }
    }
}
