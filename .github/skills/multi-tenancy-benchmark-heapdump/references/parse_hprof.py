#!/usr/bin/env python3
"""
Lightweight .hprof histogram parser for multi-tenancy benchmark heap dump analysis.
Does NOT load the entire heap into memory -- streams through the binary format.

Usage:
  python3 parse_hprof.py <file.hprof> [--top N]
  python3 parse_hprof.py --diff <pre.hprof> <post.hprof> [--top N]
"""

import struct
import sys
import argparse
from collections import defaultdict

# HPROF binary format constants
HPROF_UTF8          = 0x01
HPROF_LOAD_CLASS    = 0x02
HPROF_HEAP_DUMP     = 0x0C
HPROF_HEAP_DUMP_SEG = 0x1C
HPROF_HEAP_DUMP_END = 0x2C

# Heap dump sub-tags
HPROF_GC_CLASS_DUMP        = 0x20
HPROF_GC_INSTANCE_DUMP     = 0x21
HPROF_GC_OBJ_ARRAY_DUMP    = 0x22
HPROF_GC_PRIM_ARRAY_DUMP   = 0x23

TYPE_SIZES = {2: 4, 4: 1, 5: 2, 6: 4, 7: 8, 8: 1, 9: 2, 10: 4, 11: 8}


def read_id(f, id_size):
    data = f.read(id_size)
    if len(data) < id_size:
        return None
    if id_size == 4:
        return struct.unpack('>I', data)[0]
    return struct.unpack('>Q', data)[0]


def parse_histogram(filepath):
    """Parse an hprof file and return a dict of {class_name: (instance_count, total_bytes)}."""
    strings = {}       # id -> utf8 string
    class_names = {}   # class_serial -> string_id
    class_id_to_name = {}  # class_obj_id -> class_name
    class_instance_sizes = {}  # class_obj_id -> instance_size
    histogram = defaultdict(lambda: [0, 0])  # class_name -> [count, bytes]

    with open(filepath, 'rb') as f:
        # Read header
        header = b''
        while True:
            c = f.read(1)
            if c == b'\x00':
                break
            header += c

        id_size = struct.unpack('>I', f.read(4))[0]
        f.read(8)  # timestamp

        while True:
            tag_data = f.read(1)
            if not tag_data:
                break
            tag = tag_data[0]
            f.read(4)  # timestamp
            length = struct.unpack('>I', f.read(4))[0]

            if tag == HPROF_UTF8:
                str_id = read_id(f, id_size)
                name = f.read(length - id_size).decode('utf-8', errors='replace')
                strings[str_id] = name

            elif tag == HPROF_LOAD_CLASS:
                serial = struct.unpack('>I', f.read(4))[0]
                class_obj_id = read_id(f, id_size)
                f.read(4)  # stack trace serial
                name_id = read_id(f, id_size)
                if name_id in strings:
                    class_id_to_name[class_obj_id] = strings[name_id]

            elif tag in (HPROF_HEAP_DUMP, HPROF_HEAP_DUMP_SEG):
                end_pos = f.tell() + length
                while f.tell() < end_pos:
                    sub_tag_data = f.read(1)
                    if not sub_tag_data:
                        break
                    sub_tag = sub_tag_data[0]

                    if sub_tag == HPROF_GC_CLASS_DUMP:
                        class_obj_id = read_id(f, id_size)
                        f.read(4)  # stack serial
                        read_id(f, id_size)  # super
                        read_id(f, id_size)  # classloader
                        read_id(f, id_size)  # signer
                        read_id(f, id_size)  # protection domain
                        read_id(f, id_size)  # reserved1
                        read_id(f, id_size)  # reserved2
                        inst_size = struct.unpack('>I', f.read(4))[0]
                        class_instance_sizes[class_obj_id] = inst_size

                        # constant pool
                        cp_count = struct.unpack('>H', f.read(2))[0]
                        for _ in range(cp_count):
                            f.read(2)  # index
                            tp = f.read(1)[0]
                            f.read(TYPE_SIZES.get(tp, id_size))

                        # static fields
                        sf_count = struct.unpack('>H', f.read(2))[0]
                        for _ in range(sf_count):
                            read_id(f, id_size)  # name
                            tp = f.read(1)[0]
                            f.read(TYPE_SIZES.get(tp, id_size))

                        # instance fields
                        if_count = struct.unpack('>H', f.read(2))[0]
                        for _ in range(if_count):
                            read_id(f, id_size)  # name
                            f.read(1)  # type

                    elif sub_tag == HPROF_GC_INSTANCE_DUMP:
                        read_id(f, id_size)  # obj id
                        f.read(4)  # stack serial
                        class_id = read_id(f, id_size)
                        data_len = struct.unpack('>I', f.read(4))[0]
                        f.read(data_len)

                        name = class_id_to_name.get(class_id, f'unknown_0x{class_id:x}')
                        inst_size = class_instance_sizes.get(class_id, data_len)
                        histogram[name][0] += 1
                        histogram[name][1] += inst_size + data_len

                    elif sub_tag == HPROF_GC_OBJ_ARRAY_DUMP:
                        read_id(f, id_size)  # obj id
                        f.read(4)  # stack serial
                        num_elements = struct.unpack('>I', f.read(4))[0]
                        array_class_id = read_id(f, id_size)
                        f.read(num_elements * id_size)

                        name = class_id_to_name.get(array_class_id, 'Object[]')
                        histogram[name + '[]'][0] += 1
                        histogram[name + '[]'][1] += num_elements * id_size

                    elif sub_tag == HPROF_GC_PRIM_ARRAY_DUMP:
                        read_id(f, id_size)  # obj id
                        f.read(4)  # stack serial
                        num_elements = struct.unpack('>I', f.read(4))[0]
                        elem_type = f.read(1)[0]
                        elem_size = TYPE_SIZES.get(elem_type, 1)
                        f.read(num_elements * elem_size)

                        type_names = {4: 'boolean', 5: 'char', 6: 'float', 7: 'double',
                                      8: 'byte', 9: 'short', 10: 'int', 11: 'long'}
                        name = type_names.get(elem_type, 'unknown') + '[]'
                        histogram[name][0] += 1
                        histogram[name][1] += num_elements * elem_size

                    elif sub_tag == 0xFF:
                        read_id(f, id_size)  # obj id
                    elif sub_tag in (0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08):
                        read_id(f, id_size)  # obj id
                    else:
                        # Unknown sub-tag, try to skip to end
                        remaining = end_pos - f.tell()
                        if remaining > 0:
                            f.seek(end_pos)
                        break

            else:
                f.read(length)

    return dict(histogram)


def print_histogram(hist, top_n=30):
    sorted_items = sorted(hist.items(), key=lambda x: x[1][1], reverse=True)
    print(f"{'Class':<60} {'Count':>10} {'Bytes':>12}")
    print('-' * 84)
    for name, (count, bytes_) in sorted_items[:top_n]:
        print(f"{name:<60} {count:>10} {bytes_:>12}")
    total_count = sum(v[0] for v in hist.values())
    total_bytes = sum(v[1] for v in hist.values())
    print('-' * 84)
    print(f"{'TOTAL':<60} {total_count:>10} {total_bytes:>12}")


def print_diff(pre_hist, post_hist, top_n=20):
    all_classes = set(pre_hist.keys()) | set(post_hist.keys())
    diffs = []
    for cls in all_classes:
        pre_count, pre_bytes = pre_hist.get(cls, (0, 0))
        post_count, post_bytes = post_hist.get(cls, (0, 0))
        d_count = post_count - pre_count
        d_bytes = post_bytes - pre_bytes
        if d_bytes != 0:
            diffs.append((cls, pre_count, post_count, d_count, pre_bytes, post_bytes, d_bytes))

    # Sort by absolute byte delta descending
    diffs.sort(key=lambda x: abs(x[6]), reverse=True)

    print(f"{'Class':<55} {'PRE cnt':>8} {'POST cnt':>9} {'D cnt':>7} {'D bytes':>12}")
    print('-' * 93)
    for cls, pre_c, post_c, d_c, pre_b, post_b, d_b in diffs[:top_n]:
        sign = '+' if d_b > 0 else ''
        print(f"{cls:<55} {pre_c:>8} {post_c:>9} {d_c:>+7} {sign}{d_b:>11}")
    print('-' * 93)

    total_pre = sum(v[1] for v in pre_hist.values())
    total_post = sum(v[1] for v in post_hist.values())
    print(f"Total: PRE={total_pre/1024/1024:.1f}MB  POST={total_post/1024/1024:.1f}MB  Delta={sign}{(total_post-total_pre)/1024/1024:.1f}MB")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Analyze .hprof heap dump files')
    parser.add_argument('files', nargs='+', help='.hprof file(s)')
    parser.add_argument('--top', type=int, default=30, help='Top N classes to show')
    parser.add_argument('--diff', action='store_true', help='Compare two hprof files')
    args = parser.parse_args()

    if args.diff and len(args.files) == 2:
        print(f"Parsing {args.files[0]}...")
        pre = parse_histogram(args.files[0])
        print(f"Parsing {args.files[1]}...")
        post = parse_histogram(args.files[1])
        print()
        print_diff(pre, post, args.top)
    elif len(args.files) == 1:
        print(f"Parsing {args.files[0]}...")
        hist = parse_histogram(args.files[0])
        print()
        print_histogram(hist, args.top)
    else:
        parser.print_help()
