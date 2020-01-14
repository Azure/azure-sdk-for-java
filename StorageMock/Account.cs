using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

namespace StorageMock
{
    public class Account
    {
        private readonly ConcurrentDictionary<string, Container> _containers = new ConcurrentDictionary<string, Container>();

        public bool TryPut(string containerName, string blobName, Stream content, out Blob blob)
        {
            if (_containers.TryGetValue(containerName, out var container))
            {
                blob = container.Put(blobName, content);
                return true;
            }
            else
            {
                blob = null;
                return false;
            }
        }

        public bool TryDelete(string containerName, string blobName)
        {
            if (_containers.TryGetValue(containerName, out var container))
            {
                return container.TryDelete(blobName);
            }
            else
            {
                return false;
            }
        }
    }
}
