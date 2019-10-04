using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
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
        public KeyValuePair<string, string[]>[] Headers { get; private set; }

        public RequestCacheKey(HttpRequest request)
        {
            Scheme = request.Scheme;
            Host = request.Host.Host;
            Port = request.Host.Port;
            Path = request.Path.Value;
            Query = request.QueryString.Value;

            // Header names and values must be sorted to ensure equivalence
            Headers = request.Headers
                .Where(h => h.Key != "Proxy-Connection")
                .OrderBy(h => h.Key)
                .Select(h => new KeyValuePair<string, string[]>(h.Key, h.Value.OrderBy(s => s).ToArray())).ToArray();
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
                foreach (var v in h.Value)
                {
                    hash.Add(v);
                }
            }
            return hash.ToHashCode();
        }

        public override bool Equals(object obj)
        {
            var other = obj as RequestCacheKey;
            return other != null &&
                Scheme == other.Scheme &&
                Host == other.Host &&
                Port == other.Port &&
                Path == other.Path &&
                Query == other.Query &&
                HeadersEqual(Headers, other.Headers);
        }

        private static bool HeadersEqual(KeyValuePair<string, string[]>[] headers1, KeyValuePair<string, string[]>[] headers2)
        {
            if (headers1.Length != headers2.Length)
            {
                return false;
            }

            for (var i=0; i < headers1.Length; i++)
            {
                if (headers1[i].Key != headers2[i].Key)
                {
                    return false;
                }

                if (headers1[i].Value.Length != headers2[i].Value.Length)
                {
                    return false;
                }

                for (var j =0; j < headers1[i].Value.Length; j++)
                {
                    if (headers1[i].Value[j] != headers2[i].Value[j])
                    {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
