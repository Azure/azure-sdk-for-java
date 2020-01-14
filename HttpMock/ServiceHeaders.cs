using System.Collections.Generic;
using System.Linq;

namespace HttpMock
{
    public static class ServiceHeaders
    {
        public static IEnumerable<string> Get(Service? service) =>
            service switch
            {
                Service.StorageBlob => new string[] { "x-ms-range" },
                _ => Enumerable.Empty<string>()
            };
    }
}
