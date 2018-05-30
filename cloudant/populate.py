from cloudant.client import Cloudant
from cloudant.error import CloudantException
from cloudant.result import Result, ResultByKey

client = Cloudant("admin", "pass", url = "http://cloudant-service:80", connect=True)
client.connect()
databaseName = "customers"

database = client.create_database(databaseName)

# Create a JSON document that represents
# all the data in the row.
jsonDocument = {
  "username": "foo",
  "password": "bar",
  "email": "foo@address.com",
  "firstName": "foo",
  "lastName": "fooLast",
  "imageUrl": "image"
}

jsonDocument2 = {
  "username": "user",
  "password": "pass",
  "email": "user@address.com",
  "firstName": "user",
  "lastName": "userLast",
  "imageUrl": "image"
}

designDocument = {
  "_id": "_design/username_searchIndex",
  "key": "_design/username_searchIndex",
  "_rev": "3-4db1e691296d562be509c3344d74a76f",
  "indexes": {
    "usernames": {
        "index": "function(doc){index(\"usernames\", doc.username); }"
        }
    }
}

session = client.session()

database.create_document(jsonDocument)
database.create_document(jsonDocument2)
database.create_document(designDocument)
client.disconnect()
