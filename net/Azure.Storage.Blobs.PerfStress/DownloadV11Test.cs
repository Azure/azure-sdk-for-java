using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage.Blob;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class DownloadV11Test : ContainerV11Test<SizeOptions>
    {
        private readonly CloudBlockBlob _cloudBlockBlob;

        public DownloadV11Test(SizeOptions options) : base(options)
        {
            _cloudBlockBlob = CloudBlobContainer.GetBlockBlobReference("downloadv11test");
        }

        public override async Task GlobalSetup()
        {
            await base.GlobalSetup();

            using var stream = RandomStream.Create(Options.Size);

            // No need to delete file in GlobalCleanup(), since ContainerV11Test.GlobalCleanup() deletes the whole container
            await _cloudBlockBlob.UploadFromStreamAsync(stream);
        }

        public override void Run(CancellationToken cancellationToken)
        {
            _cloudBlockBlob.DownloadToStream(Stream.Null);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
             await _cloudBlockBlob.DownloadToStreamAsync(Stream.Null, cancellationToken);
        }
    }
}
