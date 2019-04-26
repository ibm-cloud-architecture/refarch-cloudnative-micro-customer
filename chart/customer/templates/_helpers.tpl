{{- define "customer.fullname" -}}
  {{- if .Values.fullnameOverride -}}
    {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
  {{- else -}}
    {{- printf "%s-%s" .Release.Name .Chart.Name -}}
  {{- end -}}
{{- end -}}

{{/* Customer Labels Template */}}
{{- define "customer.labels" }}
{{- range $key, $value := .Values.labels }}
{{ $key }}: {{ $value | quote }}
{{- end }}
heritage: {{ .Release.Service | quote }}
release: {{ .Release.Name | quote }}
chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end }}

{{/* Customer Environment Variables */}}
{{- define "customer.environmentvariables" }}
- name: SERVICE_PORT
  value: {{ .Values.service.internalPort | quote }}
- name: JAVA_TMP_DIR
  value: /spring-tmp
{{- end }}

{{/* Customer Init Container Template */}}
{{- define "customer.initcontainer" }}
{{- if not (or .Values.global.istio.enabled .Values.istio.enabled) }}
- name: test-customer
  image: {{ .Values.bash.image.repository }}:{{ .Values.bash.image.tag }}
  imagePullPolicy: {{ .Values.bash.image.pullPolicy }}
  command:
  - "/bin/bash"
  - "-c"
  - "until curl --max-time 1 http://{{- template "customer.fullname" . }}:{{ .Values.service.externalPort }}; do echo waiting for customer-service; sleep 1; done"
  resources:
  {{- include "customer.resources" . | indent 4 }}
  securityContext:
  {{- include "customer.securityContext" . | indent 4 }}
  env:
  {{- include "customer.couchdb.environmentvariables" . | indent 2 }}
{{- end }}
{{- end }}

{{/* CouchDB Init Container Template */}}
{{- define "customer.couchdb.initcontainer" }}
{{- if not (or .Values.global.istio.enabled .Values.istio.enabled) }}
- name: test-couchdb
  image: {{ .Values.bash.image.repository }}:{{ .Values.bash.image.tag }}
  imagePullPolicy: {{ .Values.couchdb.imagePullPolicy }}
  command:
  - "/bin/bash"
  - "-c"
  - "until curl --max-time 1 ${COUCHDB_PROTOCOL}://${COUCHDB_USER}:${COUCHDB_PASSWORD}@${COUCHDB_HOST}:${COUCHDB_PORT}; do echo waiting for couchdb; /bin/sleep 1; done"
  resources:
  {{- include "customer.resources" . | indent 4 }}
  securityContext:
  {{- include "customer.securityContext" . | indent 4 }}
  env:
  {{- include "customer.couchdb.environmentvariables" . | indent 2 }}
{{- end }}
{{- end }}

{{/* Customer CouchDB Environment Variables */}}
{{- define "customer.couchdb.environmentvariables" }}
- name: COUCHDB_HOST
  value: {{ .Values.couchdb.host | quote }}
- name: COUCHDB_PROTOCOL
  value: {{ .Values.couchdb.protocol | quote }}
- name: COUCHDB_PORT
  value: {{ .Values.couchdb.port | quote }}
- name: COUCHDB_DATABASE
  value: {{ .Values.couchdb.database | quote }}
- name: COUCHDB_USER
  valueFrom:
    secretKeyRef:
      name: {{ template "customer.couchdb.secretName" . }}
      key: adminUsername
- name: COUCHDB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ template "customer.couchdb.secretName" . }}
      key: adminPassword
{{- end }}

{{/* Customer CouchDB Secret Name */}}
{{- define "customer.couchdb.secretName" }}
  {{- if .Values.couchdb.existingSecret }}
    {{- .Values.couchdb.existingSecret }}
  {{- else -}}
    {{ template "customer.fullname" . }}-couchdb-secret
  {{- end }}
{{- end }}

{{/* Customer HS256KEY Environment Variables */}}
{{- define "customer.hs256key.environmentvariables" }}
- name: HS256_KEY
  valueFrom:
    secretKeyRef:
      name: {{ template "customer.hs256key.secretName" . }}
      key:  key
{{- end }}

{{/* Customer HS256KEY Secret Name */}}
{{- define "customer.hs256key.secretName" -}}
  {{- if .Values.global.hs256key.secretName -}}
    {{ .Values.global.hs256key.secretName -}}
  {{- else if .Values.hs256key.secretName -}}
    {{ .Values.hs256key.secretName -}}
  {{- else -}}
    {{- .Release.Name }}-{{ .Chart.Name }}-hs256key
  {{- end }}
{{- end -}}

{{/* Customer Test User Environment Variables */}}
{{- define "customer.testuser.environmentvariables" }}
- name: TEST_USER
  value: {{ .Values.testUser.username | quote }}
- name: TEST_PASSWORD
  value: {{ .Values.testUser.password | quote }}
{{- end }}

{{/* Customer Resources */}}
{{- define "customer.resources" }}
limits:
  memory: {{ .Values.resources.limits.memory }}
requests:
  memory: {{ .Values.resources.requests.memory }}
{{- end }}

{{/* Customer Security Context */}}
{{- define "customer.securityContext" }}
{{- range $key, $value := .Values.securityContext }}
{{ $key }}: {{ $value }}
{{- end }}
{{- end }}

{{/* Istio Gateway */}}
{{- define "customer.istio.gateway" }}
  {{- if or .Values.global.istio.gateway.name .Values.istio.gateway.enabled .Values.istio.gateway.name }}
  gateways:
  {{ if .Values.global.istio.gateway.name -}}
  - {{ .Values.global.istio.gateway.name }}
  {{- else if .Values.istio.gateway.enabled }}
  - {{ template "customers.fullname" . }}-gateway
  {{ else if .Values.istio.gateway.name -}}
  - {{ .Values.istio.gateway.name }}
  {{ end }}
  {{- end }}
{{- end }}