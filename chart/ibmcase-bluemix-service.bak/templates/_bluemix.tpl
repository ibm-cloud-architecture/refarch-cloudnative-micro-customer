{{- define "bluemixEndpoint" -}}
{{- if .Values.global.bluemix.target.endpoint -}}
  {{- .Values.global.bluemix.target.endpoint | quote -}}
{{- else -}}
  {{- .Values.target.endpoint | quote -}}
{{- end -}}
{{- end -}}

{{- define "bluemixOrg" -}}
{{- if .Values.global.bluemix.target.org -}}
  {{- .Values.global.bluemix.target.org | quote -}}
{{- else -}}
  {{- .Values.target.org | quote -}}
{{- end -}}
{{- end -}}

{{- define "bluemixSpace" -}}
{{- if .Values.global.bluemix.target.space -}}
  {{- .Values.global.bluemix.target.space | quote -}}
{{- else -}}
  {{- .Values.target.space | quote -}}
{{- end -}}
{{- end -}}

{{- define "bluemixClusterName" -}}
{{- if .Values.global.bluemix.clusterName -}}
  {{- .Values.global.bluemix.clusterName | quote -}}
{{- else -}}
  {{- .Values.clusterName | quote -}}
{{- end -}}
{{- end -}}

{{- define "bluemixApiKey" -}}
{{- if .Values.global.bluemix.apiKey -}}
  {{- .Values.global.bluemix.apiKey | quote -}}
{{- else -}}
  {{- .Values.target.apiKey | quote -}}
{{- end -}}
{{- end -}}
