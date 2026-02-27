import csv, re, os, sys
from datetime import datetime, timezone
import plotly.graph_objects as go
from plotly.subplots import make_subplots

metrics_dir = sys.argv[1]
log_file = sys.argv[2]
output_html = sys.argv[3]
# Optional: external monitor CSV
monitor_csv = sys.argv[4] if len(sys.argv) > 4 else None

# Parse lifecycle events
lifecycle = []
with open(log_file, 'r', encoding='utf-8', errors='ignore') as f:
    for line in f:
        m = re.search(r'\[LIFECYCLE\]\s+(\S+).*timestamp=(\S+)', line)
        if m:
            event = m.group(1)
            ts_str = m.group(2)
            try:
                dt = datetime.fromisoformat(ts_str.replace('Z', '+00:00'))
                lifecycle.append((dt.isoformat(), event))
            except:
                pass

def read_metric(filename, value_col=1):
    filepath = os.path.join(metrics_dir, filename)
    if not os.path.exists(filepath):
        return [], []
    times, vals = [], []
    seen = set()
    with open(filepath, 'r') as f:
        reader = csv.reader(f)
        next(reader)
        for row in reader:
            t = int(row[0])
            if t in seen:
                continue
            seen.add(t)
            dt = datetime.fromtimestamp(t, tz=timezone.utc)
            try:
                v = float(row[value_col])
            except:
                continue
            times.append(dt.isoformat())
            vals.append(v)
    return times, vals

def read_monitor_csv(filepath):
    if not filepath or not os.path.exists(filepath):
        return {}
    cols = {}
    with open(filepath, 'r') as f:
        reader = csv.DictReader(f)
        for row in reader:
            for key in row:
                if key not in cols:
                    cols[key] = []
                cols[key].append(row[key])
    # Convert timestamps
    if 'timestamp' in cols:
        cols['time'] = cols['timestamp']
    elif 'epoch' in cols:
        cols['time'] = [datetime.fromtimestamp(int(e), tz=timezone.utc).isoformat() for e in cols['epoch']]
    # Convert numeric cols
    for key in ['cpu_pct', 'rss_mb', 'vsz_mb', 'threads', 'fd_count', 'tcp_established', 'tcp_time_wait', 'tcp_close_wait']:
        if key in cols:
            cols[key] = [float(v) for v in cols[key]]
    return cols

# Load in-process metrics
heap_t, heap_v = read_metric('memory.heap.used.csv')
heap_v_mb = [v / (1024*1024) for v in heap_v]
thread_t, thread_v = read_metric('threads.count.csv')
success_t, success_v = read_metric('#Successful Operations.csv', value_col=1)
gc_t, gc_v = read_metric('gc.G1-Young-Generation.count.csv')

# Load external monitor
mon = read_monitor_csv(monitor_csv)
has_monitor = 'time' in mon and len(mon['time']) > 0

# Determine layout
if has_monitor:
    rows = 7
    titles = ('Heap Memory (MB) [JVM]', 'RSS Memory (MB) [OS]', 'CPU % [OS]',
              'Thread Count [JVM]', 'File Descriptors [OS]', 'TCP Connections [OS]',
              'Successful Ops (cumulative)')
    heights = [0.16, 0.14, 0.14, 0.14, 0.14, 0.14, 0.14]
else:
    rows = 4
    titles = ('Heap Memory (MB)', 'Thread Count', 'Successful Ops (cumulative)', 'GC Count (G1 Young)')
    heights = [0.3, 0.25, 0.25, 0.2]

fig = make_subplots(rows=rows, cols=1, shared_xaxes=True, vertical_spacing=0.04,
    subplot_titles=titles, row_heights=heights)

if has_monitor:
    # Row 1: Heap (JVM)
    fig.add_trace(go.Scatter(x=heap_t, y=heap_v_mb, mode='lines', name='Heap Used',
        line=dict(color='#2196F3', width=1), fill='tozeroy', fillcolor='rgba(33,150,243,0.1)'), row=1, col=1)
    # Row 2: RSS (OS)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('rss_mb', []), mode='lines', name='RSS',
        line=dict(color='#E91E63', width=2)), row=2, col=1)
    # Row 3: CPU (OS)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('cpu_pct', []), mode='lines', name='CPU %',
        line=dict(color='#F44336', width=1.5), fill='tozeroy', fillcolor='rgba(244,67,54,0.1)'), row=3, col=1)
    # Row 4: Threads (JVM)
    fig.add_trace(go.Scatter(x=thread_t, y=thread_v, mode='lines', name='Threads (JVM)',
        line=dict(color='#FF9800', width=2)), row=4, col=1)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('threads', []), mode='lines', name='Threads (OS)',
        line=dict(color='#FF9800', width=1, dash='dot')), row=4, col=1)
    # Row 5: FDs (OS)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('fd_count', []), mode='lines', name='File Descriptors',
        line=dict(color='#795548', width=2)), row=5, col=1)
    # Row 6: TCP (OS)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('tcp_established', []), mode='lines', name='TCP ESTAB',
        line=dict(color='#009688', width=2)), row=6, col=1)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('tcp_time_wait', []), mode='lines', name='TCP TIME_WAIT',
        line=dict(color='#CDDC39', width=1)), row=6, col=1)
    fig.add_trace(go.Scatter(x=mon['time'], y=mon.get('tcp_close_wait', []), mode='lines', name='TCP CLOSE_WAIT',
        line=dict(color='#FF5722', width=1)), row=6, col=1)
    # Row 7: Success ops
    fig.add_trace(go.Scatter(x=success_t, y=success_v, mode='lines', name='Success Ops',
        line=dict(color='#4CAF50', width=2)), row=7, col=1)
else:
    fig.add_trace(go.Scatter(x=heap_t, y=heap_v_mb, mode='lines', name='Heap Used MB',
        line=dict(color='#2196F3', width=1), fill='tozeroy', fillcolor='rgba(33,150,243,0.1)'), row=1, col=1)
    fig.add_trace(go.Scatter(x=thread_t, y=thread_v, mode='lines', name='Threads',
        line=dict(color='#FF9800', width=2)), row=2, col=1)
    fig.add_trace(go.Scatter(x=success_t, y=success_v, mode='lines', name='Success Ops',
        line=dict(color='#4CAF50', width=2)), row=3, col=1)
    fig.add_trace(go.Scatter(x=gc_t, y=gc_v, mode='lines', name='GC Count',
        line=dict(color='#9C27B0', width=1.5)), row=4, col=1)

# Lifecycle vertical lines
event_colors = {'CYCLE_START': 'green', 'POST_CREATE': 'blue', 'POST_WORKLOAD': 'orange',
    'POST_CLOSE': 'red', 'POST_SETTLE': 'gray', 'COMPLETE': 'black', 'PRE_CREATE': 'lightgray'}
shapes = []
annotations = []
for dt_str, event in lifecycle:
    color = event_colors.get(event, 'gray')
    shapes.append(dict(type='line', x0=dt_str, x1=dt_str, y0=0, y1=1, yref='paper',
        line=dict(color=color, width=1, dash='dot')))
    short = event.replace('POST_', '').replace('CYCLE_', '')
    annotations.append(dict(x=dt_str, y=1.02, yref='paper', text=short,
        showarrow=False, font=dict(size=7, color=color), textangle=-45))

monitor_label = ' + OS Monitor' if has_monitor else ''
fig.update_layout(
    shapes=shapes, annotations=annotations,
    title=f'CHURN Benchmark Dashboard{monitor_label}',
    height=250 * rows, showlegend=True, hovermode='x unified', template='plotly_white',
    legend=dict(orientation='h', y=-0.03)
)

fig.write_html(output_html, include_plotlyjs=True)
print(f'Dashboard ({rows} panels, monitor={"yes" if has_monitor else "no"}): {output_html}')
