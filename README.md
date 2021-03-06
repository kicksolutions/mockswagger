[![Build Status](https://travis-ci.org/kicksolutions/mockswagger.svg?branch=master)](https://travis-ci.org/kicksolutions/mockswagger)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.swagger/swagger-codegen-project/badge.svg?style=plastic)](https://oss.sonatype.org/#nexus-search;gav~io.github.kicksolutions~mock-swagger-server~~~)
[![codecov](https://codecov.io/gh/kicksolutions/swagger2puml/branch/master/graph/badge.svg)](https://codecov.io/gh/kicksolutions/mockswagger)

# Mock Swagger

This Creates a mock service for given swagger definitions.

This Project is based on Maven and plan to support Gradle also in future.
Following are modules we currently have 

- mock-swagger-core
- mock-swagger-server

Following are the tools which this project internally uses:

- [Swagger Parser]

# How does it work

- Mock Swagger Server internally reads the swagger definitions using [Swagger Parser] which constructs a Map of URI's, Methods and Example of Responses. 
- If the System Variable mockSucessResponses is set to true then application will return only mock responses from Response Codes <=400
- Else it will return randomly any response code.

### Usage:

```
java -jar mock-swagger-server-<version>-war-exec.jar [options]

-DswaggerLocation {Path/Folder of Swagger Definitions}
-DmockSucessResponses {If to Provide only sucess Responses}
```

License
----

Apacahe 2.0

[Swagger]: <https://swagger.io/>
[Swagger Parser]: <https://github.com/swagger-api/swagger-parser>
