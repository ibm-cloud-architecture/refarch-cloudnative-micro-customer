{{- define "customer.fullname" -}}
  {{- .Release.Name }}-{{ .Chart.Name -}}
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
  imagePullPolicy: {{ .Values.customercouchdb.imagePullPolicy }}
  command:
  - "/bin/bash"
  - "-c"
  - "until curl --max-time 1 ${COUCHDB_PROTOCOL}://${COUCHDB_USER}:${COUCHDB_PASSWORD}@${COUCHDB_HOST}:${COUCHDB_PORT}; do echo waiting for couchdb; /bin/sleep 1; done"
  env:
  {{- include "customer.couchdb.environmentvariables" . | indent 2 }}
{{- end }}

{{/* Customer CouchDB Environment Variables */}}
{{- define "customer.couchdb.environmentvariables" }}
{{- if and .Values.customercouchdb.enabled }}
- name: COUCHDB_HOST
  value: "{{ .Values.customercouchdb.fullnameOverride }}-svc-customercouchdb"
{{- else }}
- name: COUCHDB_HOST
  value: {{ .Values.customercouchdb.fullnameOverride | quote }}
{{- end }}
- name: COUCHDB_PROTOCOL
  value: {{ .Values.customercouchdb.protocol | quote }}
- name: COUCHDB_PORT
  value: {{ .Values.customercouchdb.service.externalPort | quote }}
- name: COUCHDB_USER
  valueFrom:
    secretKeyRef:
      name: {{ .Values.customercouchdb.fullnameOverride }}-customercouchdb
      key: adminUsername
- name: COUCHDB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.customercouchdb.fullnameOverride }}-customercouchdb
      key: adminPassword
{{- end }}

{{/* Customer HS256KEY Environment Variables */}}
{{- define "customer.hs256key.environmentvariables" }}
- name: HS256_KEY
  valueFrom:
    secretKeyRef:
        name: {{ template "customer.hs256SecretName" . }}
        key:  key
{{- end }}

{{/* Customer HS256KEY Secret Name */}}
{{- define "customer.hs256SecretName" -}}
  {{- if .Values.hs256key.secretName -}}
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