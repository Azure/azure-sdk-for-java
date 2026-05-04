{{- define "cosmos-soak.labels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{- define "cosmos-soak.selectorLabels" -}}
app.kubernetes.io/name: {{ .Chart.Name }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{- define "cosmos-soak.cosmosEnv" -}}
- name: COSMOS_ENDPOINT
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: endpoint
- name: COSMOS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Name }}-secrets
      key: cosmos-key
- name: COSMOS_DATABASE
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: database
- name: COSMOS_FEED_CONTAINER
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: feedContainer
- name: COSMOS_LEASE_CONTAINER
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: leaseContainer
- name: COSMOS_PREFERRED_REGION
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: preferredRegion
- name: OPS_PER_SEC
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: opsPerSec
- name: DOC_SIZE_BYTES
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: docSizeBytes
- name: LOGICAL_PARTITION_COUNT
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: logicalPartitionCount
- name: DURATION_SECONDS
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: durationSeconds
- name: WORKER_COUNT
  valueFrom:
    configMapKeyRef:
      name: {{ .Release.Name }}-config
      key: workerCount
{{- end }}
