using Microsoft.Extensions.Primitives;
using System.Collections.Generic;

namespace HttpMock
{
    public class UpstreamResponse
    {
        public int StatusCode { get; set; }
        public KeyValuePair<string, StringValues>[] Headers { get; set; }
        public byte[] Content { get; set; }
    }
}
