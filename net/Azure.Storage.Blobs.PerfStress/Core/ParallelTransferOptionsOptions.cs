using Azure.Storage.Common;
using CommandLine;

namespace Azure.Storage.Blobs.PerfStress.Core
{
    public class ParallelTransferOptionsOptions : SizeOptions
    {
        private int? _maximumTransferLength;
        private int? _maximumThreadCount;

        [Option('l', "maximumTransferLength")]
        public int? MaximumTransferLength
        {
            get => _maximumTransferLength;
            set
            {
                _maximumTransferLength = value;
                UpdateParallelTransferOptions();
            }
        }

        [Option('t', "maximumThreadCount")]
        public int? MaximumThreadCount
        {
            get => _maximumThreadCount;
            set
            {
                _maximumThreadCount = value;
                UpdateParallelTransferOptions();
            }
        }

        public ParallelTransferOptions ParallelTransferOptions { get; private set; } = new ParallelTransferOptions();

        private void UpdateParallelTransferOptions()
        {
            ParallelTransferOptions = new ParallelTransferOptions()
            {
                MaximumThreadCount = MaximumThreadCount,
                MaximumTransferLength = MaximumTransferLength
            };
        }
    }
}
