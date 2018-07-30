#!/bin/bash
source scripts/uri_parser.sh

function parse_from_uri() {
	# Do the URL parsing
	uri_parser $1

	# Construct couchdb
    couchdb_username=${uri_user}
    couchdb_password=${uri_password}
	couchdb_host=${uri_host}
    couchdb_port=${uri_port}
	couchdb_proto=${uri_schema}

	JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.username=${couchdb_username}"
	JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.password=${couchdb_password}"
	JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.host=${couchdb_host}"
	JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.port=${couchdb_port}"
	JAVA_OPTS="${JAVA_OPTS} -Dspring.application.cloudant.protocol=${couchdb_proto}"
}

function parse_couchdb() {
	echo "Parsing couchdb info"

	# Protocol
	if [ -n "$COUCHDB_PROTOCOL" ]; then
		echo "Protocol defined. Using ${COUCHDB_PROTOCOL}"
		PROTOCOL=${COUCHDB_PROTOCOL}
	else
		echo "Protocol NOT defined. Using http"
		PROTOCOL=http
	fi

	# URI
	if [ -n "$COUCHDB_URI" ]; then
		echo "Getting elements from COUCHDB_URI"
		parse_from_uri $COUCHDB_URI

	elif [ -n "$couchdb" ]; then
		echo "Using old CouchDB Chart";
	    couchdb_uri=$(echo $couchdb | jq -r .uri)
	    parse_from_uri $couchdb_uri

    elif [ -n "$COUCHDB_PASSWORD" ]; then
	    echo "Using CouchDB Community Chart"
	    parse_from_uri "${PROTOCOL}://${COUCHDB_USER}:${COUCHDB_PASSWORD}@${COUCHDB_HOST}:${COUCHDB_PORT}/${COUCHDB_DATABASE}"

	else
	    echo "No Password was set. Probably using passwordless root"
	    parse_from_uri "${PROTOCOL}://${COUCHDB_USER}@${COUCHDB_HOST}:${COUCHDB_PORT}/${COUCHDB_DATABASE}"
	fi
}

