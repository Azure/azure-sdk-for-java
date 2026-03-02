#!/usr/bin/env python3
"""
generate-report.py — Generate a markdown benchmark report from result directories.

Usage:
    python3 generate-report.py --results-dir <path> [--output report.md] [--runs run1,run2,...]

Reads:
  - monitor.csv           (JVM metrics: threads, heap, FDs, GC)
  - metrics/*.csv         (Codahale: throughput, latency)
  - git-info.json         (branch, commit)
  - gc.log                (GC pause summary)

Generates:
  - Markdown report with per-run summaries, time-series tables, inline SVG charts,
    and a comparison table if multiple runs are present.
"""

import argparse
import csv
import json
import os
import sys
from datetime import datetime
from pathlib import Path


def read_csv(path):
    """Read a CSV file and return list of dicts."""
    if not os.path.isfile(path):
        return []
    with open(path, newline="") as f:
        reader = csv.DictReader(f)
        return [row for row in reader]


def read_json(path):
    """Read a JSON file."""
    if not os.path.isfile(path):
        return {}
    with open(path) as f:
        return json.load(f)


def safe_int(val, default=0):
    try:
        return int(float(val))
    except (ValueError, TypeError):
        return default


def safe_float(val, default=0.0):
    try:
        return float(val)
    except (ValueError, TypeError):
        return default


def parse_monitor(rows):
    """Extract baseline, peak, and final snapshots from monitor.csv rows."""
    if not rows:
        return None
    numeric_rows = []
    for r in rows:
        numeric_rows.append({
            "timestamp": r.get("timestamp", ""),
            "threads": safe_int(r.get("threads")),
            "fds": safe_int(r.get("fds")),
            "rss_kb": safe_int(r.get("rss_kb")),
            "cpu_pct": safe_float(r.get("cpu_pct")),
            "heap_used_kb": safe_int(r.get("heap_used_kb")),
            "heap_max_kb": safe_int(r.get("heap_max_kb")),
            "gc_count": safe_int(r.get("gc_count")),
            "gc_time_ms": safe_int(r.get("gc_time_ms")),
        })

    baseline = numeric_rows[0]
    peak = max(numeric_rows, key=lambda r: r["heap_used_kb"])
    final = numeric_rows[-1]

    return {
        "baseline": baseline,
        "peak": peak,
        "final": final,
        "rows": numeric_rows,
        "count": len(numeric_rows),
    }


def parse_throughput(metrics_dir):
    """Read Codahale CSV metrics for throughput."""
    result = {}
    if not os.path.isdir(metrics_dir):
        return result
    for fname in os.listdir(metrics_dir):
        if not fname.endswith(".csv"):
            continue
        path = os.path.join(metrics_dir, fname)
        rows = read_csv(path)
        if not rows:
            continue
        label = fname.replace(".csv", "")
        last = rows[-1]
        result[label] = {
            "count": safe_int(last.get("count", 0)),
            "mean_rate": safe_float(last.get("mean_rate", 0)),
            "m1_rate": safe_float(last.get("m1_rate", 0)),
            "m5_rate": safe_float(last.get("m5_rate", 0)),
            "rows": rows,
        }
    return result


def generate_svg_chart(rows, key, label, width=600, height=150):
    """Generate an inline SVG sparkline chart for a metric over time."""
    values = [r[key] for r in rows]
    if not values or max(values) == 0:
        return ""

    n = len(values)
    max_val = max(values)
    min_val = min(values)
    val_range = max_val - min_val if max_val != min_val else 1
    padding = 10

    points = []
    for i, v in enumerate(values):
        x = padding + (i / max(n - 1, 1)) * (width - 2 * padding)
        y = height - padding - ((v - min_val) / val_range) * (height - 2 * padding)
        points.append(f"{x:.1f},{y:.1f}")

    polyline = " ".join(points)

    svg = f"""<svg width="{width}" height="{height + 30}" xmlns="http://www.w3.org/2000/svg">
  <text x="{padding}" y="12" font-size="12" fill="#333">{label} (min={min_val:,.0f}, max={max_val:,.0f})</text>
  <rect x="{padding}" y="18" width="{width - 2 * padding}" height="{height}" fill="#f8f9fa" stroke="#ddd"/>
  <polyline points="{polyline}" fill="none" stroke="#0066cc" stroke-width="1.5"/>
</svg>"""
    return svg


def run_summary(run_dir):
    """Generate summary data for a single run."""
    git_info = read_json(os.path.join(run_dir, "git-info.json"))
    monitor_rows = read_csv(os.path.join(run_dir, "monitor.csv"))
    monitor = parse_monitor(monitor_rows)
    throughput = parse_throughput(os.path.join(run_dir, "metrics"))
    run_name = os.path.basename(run_dir)

    return {
        "name": run_name,
        "dir": run_dir,
        "git": git_info,
        "monitor": monitor,
        "throughput": throughput,
    }


def format_run_section(summary):
    """Format a markdown section for one run."""
    lines = []
    name = summary["name"]
    git = summary["git"]
    monitor = summary["monitor"]
    throughput = summary["throughput"]

    branch = git.get("branch", git.get("branchName", "?"))
    commit = git.get("commitId", git.get("commit", "?"))

    lines.append(f"### {name}")
    lines.append(f"")
    lines.append(f"**Branch:** `{branch}` | **Commit:** `{commit}`")
    lines.append("")

    if monitor:
        b, p, f_ = monitor["baseline"], monitor["peak"], monitor["final"]
        thread_delta = f_["threads"] - b["threads"]
        heap_ratio = f_["heap_used_kb"] / b["heap_used_kb"] if b["heap_used_kb"] > 0 else 0
        thread_status = "✅" if thread_delta <= 2 else "🔴"
        heap_status = "✅" if heap_ratio <= 1.1 else "🔴"
        overall = "✅ PASSED" if (thread_delta <= 2 and heap_ratio <= 1.1) else "🔴 FAILED"

        lines.append("#### JVM Metrics")
        lines.append("")
        lines.append("| Metric | Baseline | Peak | Final |")
        lines.append("|--------|----------|------|-------|")
        lines.append(f"| Threads | {b['threads']} | {p['threads']} | {f_['threads']} |")
        lines.append(f"| Heap (MB) | {b['heap_used_kb']//1024} | {p['heap_used_kb']//1024} | {f_['heap_used_kb']//1024} |")
        lines.append(f"| RSS (MB) | {b['rss_kb']//1024} | {p['rss_kb']//1024} | {f_['rss_kb']//1024} |")
        lines.append(f"| FDs | {b['fds']} | {p['fds']} | {f_['fds']} |")
        lines.append(f"| GC count | {b['gc_count']} | — | {f_['gc_count']} |")
        lines.append(f"| GC time (ms) | {b['gc_time_ms']} | — | {f_['gc_time_ms']} |")
        lines.append("")
        lines.append(f"- {thread_status} Thread leak: delta={thread_delta} (threshold: ≤2)")
        lines.append(f"- {heap_status} Memory leak: ratio={heap_ratio:.2f} (threshold: ≤1.1)")
        lines.append(f"- **Overall: {overall}**")
        lines.append("")

        # Time-series charts
        rows = monitor["rows"]
        if len(rows) > 2:
            lines.append("#### Time Series")
            lines.append("")
            for key, label in [
                ("threads", "Threads"),
                ("heap_used_kb", "Heap Used (KB)"),
                ("fds", "File Descriptors"),
                ("rss_kb", "RSS (KB)"),
                ("gc_count", "GC Count"),
                ("cpu_pct", "CPU %"),
            ]:
                svg = generate_svg_chart(rows, key, label)
                if svg:
                    lines.append(svg)
                    lines.append("")

    if throughput:
        lines.append("#### Throughput Metrics")
        lines.append("")
        lines.append("| Metric | Count | Mean Rate (ops/s) | 1m Rate | 5m Rate |")
        lines.append("|--------|-------|-------------------|---------|---------|")
        for label, data in sorted(throughput.items()):
            lines.append(
                f"| {label} | {data['count']:,} | {data['mean_rate']:.1f} | "
                f"{data['m1_rate']:.1f} | {data['m5_rate']:.1f} |"
            )
        lines.append("")

    return "\n".join(lines)


def format_comparison(summaries):
    """Format a comparison table across multiple runs."""
    lines = []
    lines.append("## Comparison")
    lines.append("")

    # Header
    header = "| Metric |"
    separator = "|--------|"
    for s in summaries:
        branch = s["git"].get("branch", s["git"].get("branchName", "?"))
        commit = s["git"].get("commitId", s["git"].get("commit", "?"))[:7]
        header += f" {branch} ({commit}) |"
        separator += "--------|"
    lines.append(header)
    lines.append(separator)

    # Rows
    metrics = [
        ("Threads (final)", lambda s: s["monitor"]["final"]["threads"] if s["monitor"] else "—"),
        ("Heap final (MB)", lambda s: s["monitor"]["final"]["heap_used_kb"] // 1024 if s["monitor"] else "—"),
        ("Heap ratio", lambda s: f"{s['monitor']['final']['heap_used_kb'] / s['monitor']['baseline']['heap_used_kb']:.2f}" if s["monitor"] and s["monitor"]["baseline"]["heap_used_kb"] > 0 else "—"),
        ("Thread delta", lambda s: s["monitor"]["final"]["threads"] - s["monitor"]["baseline"]["threads"] if s["monitor"] else "—"),
        ("Peak FDs", lambda s: s["monitor"]["peak"]["fds"] if s["monitor"] else "—"),
        ("GC count", lambda s: s["monitor"]["final"]["gc_count"] if s["monitor"] else "—"),
        ("GC time (ms)", lambda s: s["monitor"]["final"]["gc_time_ms"] if s["monitor"] else "—"),
        ("RSS peak (MB)", lambda s: s["monitor"]["peak"]["rss_kb"] // 1024 if s["monitor"] else "—"),
    ]

    for label, extractor in metrics:
        row = f"| {label} |"
        for s in summaries:
            val = extractor(s)
            row += f" {val} |"
        lines.append(row)

    # Throughput comparison
    all_throughput_keys = set()
    for s in summaries:
        all_throughput_keys.update(s["throughput"].keys())

    if all_throughput_keys:
        lines.append("")
        lines.append("### Throughput Comparison")
        lines.append("")
        header = "| Metric |"
        separator = "|--------|"
        for s in summaries:
            branch = s["git"].get("branch", s["git"].get("branchName", "?"))[:20]
            header += f" {branch} |"
            separator += "--------|"
        lines.append(header)
        lines.append(separator)

        for key in sorted(all_throughput_keys):
            row = f"| {key} (ops/s) |"
            for s in summaries:
                if key in s["throughput"]:
                    row += f" {s['throughput'][key]['mean_rate']:.1f} |"
                else:
                    row += " — |"
            lines.append(row)

    lines.append("")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(description="Generate benchmark report")
    parser.add_argument("--results-dir", required=True, help="Directory containing run subdirectories")
    parser.add_argument("--output", default=None, help="Output markdown file (default: <results-dir>/report.md)")
    parser.add_argument("--runs", default=None, help="Comma-separated run names (default: all in results-dir)")
    args = parser.parse_args()

    results_dir = args.results_dir
    if not os.path.isdir(results_dir):
        print(f"ERROR: {results_dir} not found", file=sys.stderr)
        sys.exit(1)

    # Find runs
    if args.runs:
        run_names = [r.strip() for r in args.runs.split(",")]
    else:
        run_names = sorted([
            d for d in os.listdir(results_dir)
            if os.path.isdir(os.path.join(results_dir, d))
        ])

    if not run_names:
        print("No runs found", file=sys.stderr)
        sys.exit(1)

    output_path = args.output or os.path.join(results_dir, "report.md")

    # Generate report
    summaries = []
    for name in run_names:
        run_dir = os.path.join(results_dir, name)
        if not os.path.isdir(run_dir):
            print(f"WARNING: {run_dir} not found, skipping", file=sys.stderr)
            continue
        summaries.append(run_summary(run_dir))

    report = []
    report.append(f"# Benchmark Report")
    report.append(f"")
    report.append(f"Generated: {datetime.utcnow().strftime('%Y-%m-%d %H:%M:%S UTC')}")
    report.append(f"")
    report.append(f"Runs: {len(summaries)}")
    report.append(f"")

    # Comparison table (if multiple runs)
    if len(summaries) > 1:
        report.append(format_comparison(summaries))

    # Per-run details
    report.append("## Run Details")
    report.append("")
    for s in summaries:
        report.append(format_run_section(s))
        report.append("---")
        report.append("")

    # Write output
    content = "\n".join(report)
    with open(output_path, "w") as f:
        f.write(content)

    print(f"✅ Report generated: {output_path}")
    print(f"   Runs analyzed: {len(summaries)}")
    for s in summaries:
        branch = s["git"].get("branch", s["git"].get("branchName", "?"))
        overall = "?"
        if s["monitor"]:
            td = s["monitor"]["final"]["threads"] - s["monitor"]["baseline"]["threads"]
            hr = s["monitor"]["final"]["heap_used_kb"] / s["monitor"]["baseline"]["heap_used_kb"] if s["monitor"]["baseline"]["heap_used_kb"] > 0 else 0
            overall = "✅" if (td <= 2 and hr <= 1.1) else "🔴"
        print(f"   {overall} {s['name']} ({branch})")


if __name__ == "__main__":
    main()
