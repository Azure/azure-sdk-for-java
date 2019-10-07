using Azure.Storage.Blobs.PerfStress.Core;
using Microsoft.Azure.Storage;
using System.IO;
using System.Threading;
using System.Threading.Tasks;

namespace Azure.Storage.Blobs.PerfStress
{
    public class UploadV11Test : RandomDataV11Test<SizeOptions>
    {
        public UploadV11Test(SizeOptions options) : base(options)
        {
            try
            {
                CloudBlockBlob.Delete();
            }
            catch (StorageException)
            {
            }
        }

        public override void Run(CancellationToken cancellationToken)
        {
            CloudBlockBlob.UploadFromStream(RandomStream);
        }

        public override async Task RunAsync(CancellationToken cancellationToken)
        {
            await CloudBlockBlob.UploadFromStreamAsync(RandomStream, cancellationToken);
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
