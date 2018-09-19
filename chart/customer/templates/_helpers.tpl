{{- define "customer.fullname" -}}
  {{- if .Values.fullnameOverride -}}
    {{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
  {{- else -}}
    {{- printf "%s-%s" .Release.Name .Chart.Name -}}
  {{- end -}}
{{- end -}}

{{/* Customer Labels Template */}}
{{- define "customer.labels" }}
app: bluecompute
micro: customer
tier: backend
heritage: {{ .Release.Service | quote }}
release: {{ .Release.Name | quote }}
chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
{{- end }}

{{/* Customer Init Container Template */}}
{{- define "customer.initcontainer" }}
- name: test-customer
  image: {{ .Values.bash.image.repository }}:{{ .Values.bash.image.tag }}
  imagePullPolicy: {{ .Values.bash.image.pullPolicy }}
  command:
  - "/bin/bash"
  - "-c"
  - "until curl --max-time 1 http://{{- template "customer.fullname" . }}:{{ .Values.service.externalPort }}; do echo waiting for customer-service; sleep 1; done"
  env:
  {{- include "customer.couchdb.environmentvariables" . | indent 2 }}
{{- end }}

{{/* CouchDB Init Container Template */}}
{{- define "customer.couchdb.initcontainer" }}
- name: test-couchdb
  image: {{ .Values.bash.image.repository }}:{{ .Values.bash.image.tag }}
  imagePullPolicy: {{ .Values.couchdb.imagePullPolicy }}
  command:
  - "/bin/bash"
  - "-c"
  - "until curl --max-time 1 ${COUCHDB_PROTOCOL}://${COUCHDB_USER}:${COUCHDB_PASSWORD}@${COUCHDB_HOST}:${COUCHDB_PORT}; do echo waiting for couchdb; /bin/sleep 1; done"
  env:
  {{- include "customer.couchdb.environmentvariables" . | indent 2 }}
{{- end }}

{{/* Customer CouchDB Environment Variables */}}
{{- define "customer.couchdb.environmentvariables" }}
- name: COUCHDB_HOST
  value: {{ template "customer.couchdb.host" . }}
- name: COUCHDB_PROTOCOL
  value: {{ .Values.couchdb.protocol | quote }}
- name: COUCHDB_PORT
  value: {{ .Values.couchdb.service.externalPort | quote }}
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

{{/* Customer CouchDB Host */}}
{{- define "customer.couchdb.host" }}
  {{- if .Values.couchdb.enabled }}
    {{ .Values.couchdb.fullnameOverride }}-svc-couchdb
  {{- else -}}
    {{ .Values.couchdb.fullnameOverride }}
  {{- end }}
{{- end }}

{{/* Customer CouchDB Secret Name */}}
{{- define "customer.couchdb.secretName" }}
  {{- if .Values.couchdb.enabled }}
    {{- printf "%s-couchdb" .Values.couchdb.fullnameOverride -}}
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