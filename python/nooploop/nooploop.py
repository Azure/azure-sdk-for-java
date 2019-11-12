import argparse
import timeit

def noop():
    return None

parser = argparse.ArgumentParser()
parser.add_argument("-c", "--count", default=1000000)
args = parser.parse_args()

start = timeit.default_timer()

for x in range(int(args.count)):
    noop()

elapsed = timeit.default_timer() - start
ops_per_second = int(args.count) / elapsed

print(f"Called {args.count} functions in {elapsed:.2f} seconds ({ops_per_second:.2f} ops/s)")

