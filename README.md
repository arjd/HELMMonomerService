# HELMMonomerService

The HELMMonomerService provides monomer and rule data as a REST service.
The Service has two reference implementations:
1. Using a SQLite database as the backend for the REST service
2. Using a MongoDB database as the backend for the REST service

The two different backends can be configured by the file config.txt in  HELMMonomerService/src/test/resources/org/helm/monomerservice/resources/.

## SQLite backend
```
loader.rules=org.helm.monomerservice.RuleLibrarySQLite
loader.monomers=org.helm.monomerservice.MonomerLibrarySQLite
```

The file MonomerLib2.0.db contains an example SQLite database.

## MongoDB backend
```
loader.rules=org.helm.monomerservice.RuleLibraryMongoDB
loader.monomers=org.helm.monomerservice.MonomerLibraryMongoDB
loader.hostname=localhost
loader.port=27017
loader.database=local
```

The files Monomers.json and Rules.json contain example monomers and rules. Data can be imported using the monogoimport tool:
```
mongoimport --db local --collection monomers --file Monomers.json --jsonArray

mongoimport --db local --collection rules --file Rules.json --jsonArray
```

## Swagger API description
There is a Swagger API description available for the API methods:
http://localhost:8080/HELM2MonomerService/
Change hostname and port in accordance to your local setup.
