using Azure.Storage.Blobs.PerfStress.Core;
using Azure.Storage.Blobs.Specialized;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadBlockBlobTest : RandomBlobTest<SizeOptions>
    {
        public UploadBlockBlobTest(SizeOptions options) : base(options)
        {
        }

        public override void Run(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);
            BlockBlobClient.Upload(stream, cancellationToken: cancellationToken);
        }

        public override Task RunAsync(CancellationToken cancellationToken)
        {
            using var stream = RandomStream.Create(Options.Size);
            return BlockBlobClient.UploadAsync(stream, cancellationToken: cancellationToken);
        }
    }
}
