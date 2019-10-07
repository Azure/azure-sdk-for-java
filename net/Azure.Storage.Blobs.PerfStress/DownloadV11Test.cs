using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class DownloadV11Test : RandomDataV11Test<SizeOptions>
    {
        public DownloadV11Test(SizeOptions options) : base(options)
        {
            try
            {
                CloudBlockBlob.Delete();
            }
            catch (StorageException)
            {
            }

            CloudBlockBlob.UploadFromStream(RandomStream);
        }
        
        public override void Run(CancellationToken cancellationToken)
        {
            CloudBlockBlob.DownloadToStream(Stream.Null);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
             await CloudBlockBlob.DownloadToStreamAsync(Stream.Null, cancellationToken);
        }

        public override void Dispose()
        {
            try
            {
                CloudBlockBlob.Delete();
            }
            catch (StorageException)
            {
            }

            base.Dispose();
        }
    }
}
