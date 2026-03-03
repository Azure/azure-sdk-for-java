#!/usr/bin/env python3
"""
generate-report.py — Generate a markdown benchmark report from result directories.

Usage:
    python3 generate-report.py --results-dir <path> [--output report.md] [--runs run1,run2,...]

Reads:
  - monitor.csv           (JVM metrics: threads, heap, FDs, GC)
  - metrics/*.csv         (Codahale: throughput, latency)
  - git-info.json         (branch, commit)

Generates:
  - Markdown report with comparison table, overlay time-series charts,
    throughput comparison, and per-run details.
"""

import argparse
import csv
import json
import os
import sys
from datetime import datetime
from pathlib import Path


# Colors for multi-run overlay charts
COLORS = ["#0066cc", "#cc3300", "#339933", "#9933cc", "#cc6600", "#006699"]


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


def generate_overlay_chart(all_series, key, label, width=700, height=180):
    """Generate an SVG chart overlaying the same metric from multiple runs.

    all_series: list of (run_label, color, rows)
    """
    # Compute global min/max across all runs
    all_values = []
    for _, _, rows in all_series:
        all_values.extend([r[key] for r in rows])
    if not all_values or max(all_values) == 0:
        return ""

    max_val = max(all_values)
    min_val = min(all_values)
    val_range = max_val - min_val if max_val != min_val else 1
    padding_left = 10
    padding_right = 10
    padding_top = 40
    padding_bottom = 30
    chart_w = width - padding_left - padding_right
    chart_h = height - padding_top - padding_bottom

    svg_parts = []
    svg_parts.append(f'<svg width="{width}" height="{height}" xmlns="http://www.w3.org/2000/svg">')
    svg_parts.append(f'  <style>text {{ font-family: Consolas, Monaco, monospace; }}</style>')

    # Title
    svg_parts.append(f'  <text x="{padding_left}" y="16" font-size="14" font-weight="bold" fill="#222">{label}</text>')

    # Background
    svg_parts.append(f'  <rect x="{padding_left}" y="{padding_top}" width="{chart_w}" height="{chart_h}" fill="#ffffff" stroke="#ccc"/>')

    # Grid lines (4 horizontal)
    for i in range(5):
        y = padding_top + (i / 4) * chart_h
        val = max_val - (i / 4) * val_range
        svg_parts.append(f'  <line x1="{padding_left}" y1="{y:.0f}" x2="{padding_left + chart_w}" y2="{y:.0f}" stroke="#eee" stroke-width="0.5"/>')
        svg_parts.append(f'  <text x="{padding_left + chart_w + 4}" y="{y + 4:.0f}" font-size="10" fill="#666">{val:,.0f}</text>')

    # Plot each series
    for run_label, color, rows in all_series:
        values = [r[key] for r in rows]
        n = len(values)
        if n < 2:
            continue
        points = []
        for i, v in enumerate(values):
            x = padding_left + (i / max(n - 1, 1)) * chart_w
            y = padding_top + chart_h - ((v - min_val) / val_range) * chart_h
            points.append(f"{x:.1f},{y:.1f}")
        polyline = " ".join(points)
        svg_parts.append(f'  <polyline points="{polyline}" fill="none" stroke="{color}" stroke-width="2" opacity="0.85"/>')

    # Legend
    legend_x = padding_left + 10
    legend_y = 24
    for i, (run_label, color, _) in enumerate(all_series):
        lx = legend_x + i * 280
        svg_parts.append(f'  <rect x="{lx}" y="{legend_y}" width="12" height="12" fill="{color}"/>')
        svg_parts.append(f'  <text x="{lx + 16}" y="{legend_y + 10}" font-size="11" fill="#444">{run_label}</text>')

    svg_parts.append('</svg>')
    return "\n".join(svg_parts)


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


def get_run_label(summary):
    """Short label for a run: use ref name from run-name, fallback to branch/commit."""
    name = summary["name"]
    # Strip date-scenario prefix: 20260302-SIMPLE-origin-main -> origin-main
    parts = name.split("-", 2)
    if len(parts) >= 3:
        return parts[2]
    git = summary["git"]
    branch = git.get("branch", "?")
    commit = git.get("commitId", git.get("commit", "?"))[:7]
    return f"{branch} ({commit})"


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
        lines.append(f"| Thread delta | — | — | {thread_delta} |")
        lines.append(f"| Heap ratio | — | — | {heap_ratio:.2f} |")
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
    """Format a unified comparison table with baseline/peak/final for all runs."""
    lines = []

    # Git info header per run
    lines.append("## Runs")
    lines.append("")
    for s in summaries:
        label = get_run_label(s)
        git = s["git"]
        branch = git.get("branch", git.get("branchName", "?"))
        commit = git.get("commitId", git.get("commit", "?"))
        lines.append(f"- **{label}**: branch=`{branch}`, commit=`{commit}`")
    lines.append("")

    # Unified JVM metrics table
    lines.append("## JVM Metrics")
    lines.append("")

    # Build header with sub-columns per run
    header = "| Metric |"
    separator = "|--------|"
    for s in summaries:
        label = get_run_label(s)
        header += f" {label} (baseline) | (peak) | (final) |"
        separator += "--------|--------|--------|"
    lines.append(header)
    lines.append(separator)

    def fmt_row(metric_label, key, transform=None):
        row = f"| {metric_label} |"
        for s in summaries:
            if s["monitor"]:
                b_val = s["monitor"]["baseline"][key]
                p_val = max(r[key] for r in s["monitor"]["rows"])
                f_val = s["monitor"]["final"][key]
                if transform:
                    row += f" {transform(b_val)} | {transform(p_val)} | {transform(f_val)} |"
                else:
                    row += f" {b_val} | {p_val} | {f_val} |"
            else:
                row += " — | — | — |"
        lines.append(row)

    fmt_row("Threads", "threads")
    fmt_row("Heap (MB)", "heap_used_kb", lambda v: v // 1024)
    fmt_row("RSS (MB)", "rss_kb", lambda v: v // 1024)
    fmt_row("FDs", "fds")
    fmt_row("CPU %", "cpu_pct", lambda v: f"{v:.1f}")

    # GC is cumulative, show baseline and final only
    row = "| GC count |"
    for s in summaries:
        if s["monitor"]:
            row += f" {s['monitor']['baseline']['gc_count']} | — | {s['monitor']['final']['gc_count']} |"
        else:
            row += " — | — | — |"
    lines.append(row)

    row = "| GC time (ms) |"
    for s in summaries:
        if s["monitor"]:
            row += f" {s['monitor']['baseline']['gc_time_ms']} | — | {s['monitor']['final']['gc_time_ms']} |"
        else:
            row += " — | — | — |"
    lines.append(row)

    # Derived metrics
    row = "| Thread delta |"
    for s in summaries:
        if s["monitor"]:
            delta = s["monitor"]["final"]["threads"] - s["monitor"]["baseline"]["threads"]
            row += f" — | — | {delta} |"
        else:
            row += " — | — | — |"
    lines.append(row)

    row = "| Heap ratio |"
    for s in summaries:
        if s["monitor"] and s["monitor"]["baseline"]["heap_used_kb"] > 0:
            ratio = s["monitor"]["final"]["heap_used_kb"] / s["monitor"]["baseline"]["heap_used_kb"]
            row += f" — | — | {ratio:.2f} |"
        else:
            row += " — | — | — |"
    lines.append(row)

    lines.append("")

    # Throughput comparison
    all_throughput_keys = set()
    for s in summaries:
        all_throughput_keys.update(s["throughput"].keys())

    if all_throughput_keys:
        lines.append("## Throughput")
        lines.append("")
        header = "| Metric |"
        separator = "|--------|"
        for s in summaries:
            label = get_run_label(s)
            header += f" {label} (count) | (mean ops/s) | (1m rate) |"
            separator += "--------|--------|--------|"
        lines.append(header)
        lines.append(separator)

        for key in sorted(all_throughput_keys):
            row = f"| {key} |"
            for s in summaries:
                if key in s["throughput"]:
                    d = s["throughput"][key]
                    row += f" {d['count']:,} | {d['mean_rate']:.1f} | {d['m1_rate']:.1f} |"
                else:
                    row += " — | — | — |"
            lines.append(row)
        lines.append("")

    return "\n".join(lines)


def format_overlay_charts(summaries):
    """Generate overlay charts section — one chart per metric with all runs overlaid."""
    lines = []
    lines.append("## Time Series (all runs overlaid)")
    lines.append("")

    # Build series list
    all_series = []
    for i, s in enumerate(summaries):
        if s["monitor"] and len(s["monitor"]["rows"]) > 2:
            label = get_run_label(s)
            color = COLORS[i % len(COLORS)]
            all_series.append((label, color, s["monitor"]["rows"]))

    if not all_series:
        return ""

    for key, label in [
        ("threads", "Threads"),
        ("heap_used_kb", "Heap Used (KB)"),
        ("fds", "File Descriptors"),
        ("rss_kb", "RSS (KB)"),
        ("gc_count", "GC Count"),
        ("cpu_pct", "CPU %"),
    ]:
        svg = generate_overlay_chart(all_series, key, label)
        if svg:
            lines.append(svg)
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

    # Unified comparison table (works for 1 or more runs)
    report.append(format_comparison(summaries))

    # Overlay charts
    report.append(format_overlay_charts(summaries))

    # Write output
    content = "\n".join(report)
    with open(output_path, "w") as f:
        f.write(content)

    print(f"✅ Report generated: {output_path}")
    print(f"   Runs analyzed: {len(summaries)}")
    for s in summaries:
        label = get_run_label(s)
        print(f"   📊 {s['name']} ({label})")


if __name__ == "__main__":
    main()
