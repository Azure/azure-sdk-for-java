using System;
using System.IO;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public abstract class RandomDataTest<TOptions> : StorageTest<TOptions> where TOptions : SizeOptions
    {
        private readonly byte[] _randomData;

        public RandomDataTest(TOptions options) : base(options)
        {
            _randomData = new byte[options.Size];
            (new Random(0)).NextBytes(_randomData);
        }

        protected Stream RandomStream => new MemoryStream(_randomData, writable: false);
    }
}
