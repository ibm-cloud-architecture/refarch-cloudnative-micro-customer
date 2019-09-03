{{/* Customer Init Container Template */}}
{{- define "customer.labels" }}
{{- range $key, $value := .Values.labels }}
{{ $key }}: {{ $value | quote }}
{{- end }}
app.kubernetes.io/name: {{ .Release.Name }}-customer
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
heritage: {{ .Release.Service | quote }}
release: {{ .Release.Name | quote }}
chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end }}

{{/* Customer Resources */}}
{{- define "customer.resources" }}
limits:
  memory: {{ .Values.resources.limits.memory }}
requests:
  cpu: {{ .Values.resources.limits.cpu }}
  memory: {{ .Values.resources.requests.memory }}
{{- end }}