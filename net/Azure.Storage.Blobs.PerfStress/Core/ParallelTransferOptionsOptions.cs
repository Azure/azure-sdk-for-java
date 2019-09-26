using CommandLine;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public class ParallelTransferOptionsOptions : SizeOptions
    {
        [Option('l', "maximumTransferLength")]
        public int? MaximumTransferLength { get; set; }

        [Option('t', "maximumThreadCount")]
        public int? MaximumThreadCount { get; set; }
    }
}
