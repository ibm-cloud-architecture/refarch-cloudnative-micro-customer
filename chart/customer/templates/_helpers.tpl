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

{{- define "couchdbBinding" -}}
  {{- if ((index .Values "bluemix-cloudantdb").enabled) -}}
    binding-{{ index .Values "bluemix-cloudantdb" "service" "name" | lower | replace " " "-" }}
  {{- else if (.Values.tags.bluemix) -}}
    binding-{{ index .Values "bluemix-cloudantdb" "service" "name" | lower | replace " " "-" }}
  {{- else -}}
    {{ index .Values "ibmcase-couchdb" "binding" "name" }}
  {{- end -}}
{{- end -}}

{{- define "hs256SecretName" -}}
  {{- if .Values.global.hs256key.secretName -}}
    {{- .Release.Name }}-{{ .Values.global.hs256key.secretName -}}
  {{- else -}}
    {{- .Release.Name }}-{{ .Chart.Name }}-{{ .Values.hs256key.secretName -}}
  {{- end }}
{{- end -}}
