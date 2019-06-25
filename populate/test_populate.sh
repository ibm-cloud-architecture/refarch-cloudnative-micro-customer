curl -X POST \
  http://localhost:5984/customers/_find \
  -H 'Accept: */*' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:5985' \
  -H 'Postman-Token: 9d2011df-3455-45c6-bd9c-f5437408fbf5,5237dc70-1f10-4923-9431-c19f432c60e9' \
  -H 'User-Agent: PostmanRuntime/7.13.0' \
  -H 'accept-encoding: gzip, deflate' \
  -H 'cache-control: no-cache' \
  -H 'content-length: 143' \
  -d '{
    "selector": {
        "username": "foo"
    },
    "fields": ["username", "email", "firstName", "lastName", "imageUrl"],
    "limit": 1
}'