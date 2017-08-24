{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "fullname" -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "couchdbDataVolume" -}}
        - name: couchdb-data
{{- if .Values.persistence.enabled }}
          persistentVolumeClaim:
    {{- if .Values.persistence.existingName }}
            claimName: {{ .Values.persistence.existingName }}
    {{- else }}
            claimName: {{ .Release.Name }}-{{ .Chart.Name }}-{{ .Values.nodename }}-data
    {{- end -}}
{{- else if .Values.global.persistence.enabled }}
          persistentVolumeClaim:
    {{- if .Values.persistence.existingName }}
            claimName: {{ .Values.persistence.existingName }}
    {{- else }}
            claimName: {{ .Release.Name }}-{{ .Chart.Name }}-{{ .Values.nodename }}-data
    {{- end -}}
{{- else }}
          hostPath:
            path: /var/lib/couchdb-{{ .Values.nodename }}
{{ end }}
{{- end -}}

{{- define "volumeSize" -}}
  {{- if .Values.persistence.volume.size -}}
    {{ .Values.persistence.volume.size }}
  {{- else if .Values.global.persistence.volume.size -}}
    {{ .Values.global.persistence.volume.size }}
  {{- else -}}
    {{- printf "20Gi" -}}
  {{- end -}}
{{- end -}}

{{- define "volumeStorageClass" -}}
  {{- if .Values.persistence.volume.storageClass -}}
    {{ .Values.persistence.volume.storageClass }}
  {{- else if .Values.global.persistence.volume.storageClass -}}
    {{ .Values.global.persistence.volume.storageClass }}
  {{- else -}}
    {{- printf "" -}}
  {{- end -}}
{{- end -}}
