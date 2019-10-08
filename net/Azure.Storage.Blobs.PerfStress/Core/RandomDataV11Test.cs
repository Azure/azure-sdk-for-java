using System;
using System.IO;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class RandomDataV11Test<TOptions> : StorageV11Test<TOptions> where TOptions : SizeOptions
    {
        private readonly byte[] _randomData;

        public RandomDataV11Test(string id, TOptions options) : base(id, options)
        {
            _randomData = new byte[1024 * 1024];
            (new Random(0)).NextBytes(_randomData);
        }

        protected Stream RandomStream => new CircularStream(new MemoryStream(_randomData, writable: false), Options.Size);

        public override void Dispose()
        {
            RandomStream.Dispose();
            base.Dispose();
        }
    }
}
