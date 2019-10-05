using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Primitives;
using System;
using System.Collections.Generic;
using System.Linq;

namespace HttpMock
{
    public class RequestCacheKey
    {
        public string Scheme { get; private set; }
        public string Host { get; private set; }
        public int? Port { get; private set; }
        public string Path { get; private set; }
        public string Query { get; private set; }
        public KeyValuePair<string, StringValues>[] Headers { get; private set; }

        public RequestCacheKey(HttpRequest request, IEnumerable<string> headers)
        {
            Scheme = request.Scheme;
            Host = request.Host.Host;
            Port = request.Host.Port;
            Path = request.Path.Value;
            Query = request.QueryString.Value;
            Headers = request.Headers.Where(h => headers.Contains(h.Key)).ToArray();
        }

        public override int GetHashCode()
        {
            var hash = new HashCode();
            hash.Add(Scheme);
            hash.Add(Host);
            hash.Add(Port);
            hash.Add(Path);
            hash.Add(Query);
            foreach (var h in Headers)
            {
                hash.Add(h.Key);
                hash.Add(h.Value);
            }
            return hash.ToHashCode();
        }

        public override bool Equals(object obj)
        {
            return obj is RequestCacheKey other &&
                Scheme == other.Scheme &&
                Host == other.Host &&
                Port == other.Port &&
                Path == other.Path &&
                Query == other.Query &&
                Headers.SequenceEqual(other.Headers);
        }
    }
}
