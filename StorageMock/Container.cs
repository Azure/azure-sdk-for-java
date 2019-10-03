using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection.Metadata;
using System.Threading.Tasks;

namespace StorageMock
{
    public class Container
    {
        private readonly ConcurrentDictionary<string, Blob> _blobs = new ConcurrentDictionary<string, Blob>();

        public Blob Put(string blobName, Stream content)
        {
            return _blobs.AddOrUpdate(blobName,
                s => new Blob() { Content = content },
                (s, b) =>
                {
                    b.Content = content;
                    return b;
                });
        }

        public bool TryDelete(string blobName)
        {
            return _blobs.TryRemove(blobName, out _);
        }
    }
}
