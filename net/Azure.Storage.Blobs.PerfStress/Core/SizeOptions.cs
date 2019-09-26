using Azure.Test.PerfStress;
using CommandLine;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public class SizeOptions : PerfStressOptions
    {
        [Option('s', "size", Default = 10 * 1024, HelpText = "Size of message (in bytes)")]
        public int Size { get; set; }
    }
}
