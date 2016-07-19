# Open mHealth Participant Endpoint 

This repository contains the Java reference implementation of an [Open mHealth](http://www.openmhealth.org/)
[Data Point API](docs/raml/API.yml) storage endpoint.

This code is a port of the OpenMHealth reference implementation that adds support for obtaining participant IDs from
the stored datapoints.


  
### Overview

A *data point* is a JSON document that represents a piece of data and conforms to
the [data-point](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_data-point) schema. The header of a data point conforms to
  the [header](http://www.openmhealth.org/documentation/#/schema-docs/schema-library/schemas/omh_header) schema, and the body can conform to any schema you like.
The header is designed to contain operational metadata, such as identifiers and provenance, whereas the body contains
the data being acquired or computed.

The *data point participant API* is a simple REST service that allows you to retrieve the participant IDs matching a given filter and does not currently require 
authorization. This url is exposed at: ``http://{{resourceServer.host}}:{{resourceServer.port}}/v1.0.M2/participants?dataPointFilter={{filter}}``



### Issuing requests with Postman

Your code interacts with the resource servers by sending them HTTP
requests. To make learning about those requests easier, we've created a [Postman](http://www.getpostman.com/) collection
that contains a predefined set of requests for different actions and outcomes. Postman is a Chrome
packaged application whose UI lets you craft and send HTTP requests.

> These instructions are written for Postman 1.0.1. If you're using a newer version and they don't work,
> [let us know](https://github.com/openmhealth/omh-dsu-ri/issues) and we'll fix them.

To set up the collection,

1. [Download Postman](https://chrome.google.com/webstore/detail/postman-rest-client-packa/fhbjgbiflinjbdggehcddcbncdddomop).
1. [Start it](http://www.getpostman.com/docs/launch).
1. Click the *Import* button, choose the *Download from link* tab and paste ``https://www.getpostman.com/collections/b9dca4016664b3a145d6)``

1. The collection should now be available. The folder names describe the requests, and the request names describe the expected outcome.
1. Create an [environment](https://www.getpostman.com/docs/environments). Environments provide values for the `{{...}}` placeholders in the collection.
   Add the following environment keys and values, possibly changing the values if you've customised the installation.
    * `resourceServer.host` -  IP address of your Docker host
    * `resourceServer.port` -  `8083`
    * `apiVersion` -  `v1.0.M2`

To send a request, pick the request and click its *Send* button. The different requests should be self-explanatory,
and correspond to the verbs and resources in the [data point API](docs/raml/API.yml).

The folders also have descriptions,
which you can currently only see by clicking the corresponding *Edit folder* button (but Postman are
[working on that](https://github.com/a85/POSTMan-Chrome-Extension/issues/816)). You can see the request descriptions by
selecting the request.


### Using the resource server

The data point API is documented in a [RAML file](docs/raml/API.yml) to avoid ambiguity.

A data point looks something like this

```json
{
    "header": {
        "id": "123e4567-e89b-12d3-a456-426655440000",
        "creation_date_time": "2013-02-05T07:25:00Z",
        "schema_id": {
            "namespace": "omh",
            "name": "physical-activity",
            "version": "1.0"
        },
        "acquisition_provenance": {
            "source_name": "RunKeeper",
            "modality": "sensed"
        },
        "user_id": "joe"
    },
    "body": {
        "activity_name": "walking",
        "distance": {
            "value": 1.5,
            "unit": "mi"
        },
        "reported_activity_intensity": "moderate",
        "effective_time_frame": {
            "time_interval": {
                "date": "2013-02-05",
                "part_of_day": "morning"
            }
        }
    }
}
```


### Fetching datapoint participants

The participant API can be located at the /v1.0.M2/dataPointsByParticipant location and accepts 1 parameters -- a filter that specifies a RSQL-formatted query (see below).

An example query would be:
``http://url:port/1.0.M2/participants?dataPointFilter=body.blood_glucose.value > 110 and header.creation_date_time >= 2014-11-01 and header.creation_date_time <= 2014-11-30``

which returns a list of participant IDs.

The filter syntax is described below.

### DataPoint query syntax

 The query syntax for datapoint filtering follows mostly the [RSQL syntax](https://github.com/jirutka/rsql-parser)
 
 Therefore the following rules apply:
 
 **Logical operators:**
 `AND:` `;`  or  `and`
 `OR:`  `,`  or `or`
 
 **Boolean operators:**
 Equal to : `==`
 Not equal to : `!=`
 Less than : `=lt=` or `<`
 Less than or equal to : `=le=` or `<=`
 Greater than operator : `=gt=` or `>`
 Greater than or equal to : `=ge=` or `>=`
 
 **Lists operators:**
 In : `=in=`
 Not in : `=out=`
 
 Exists: `=ex=`  for example ``header.schema_id.version =ex= true``
 
 **Arguments:**
 Argument can be a single value, or multiple values in parenthesis separated by comma. Value that doesn’t contain any reserved character or a white space can be unquoted, other arguments must be enclosed in single or double quotes.
 
 
 Query is rooted at DataPoint. Some examples:
 
 Find entries whose provenance matches "RunKeeper":
 `(header.acquisition_provenance.source_name=='RunKeeper')`
 
 AND examples (remember to escape spaces or use `;`. For example, the following two queries are equivalent)
 `((header.acquisition_provenance.source_name == 'RunKeeper') and (header.user_id == 'testUser'))`
 `((header.acquisition_provenance.source_name =eq= 'RunKeeper');(header.user_id =eq= 'testUser'))`
  
 `IN` operator:
 `(header.id=in=('foo1', 'foo3'))`
 
 An example of a complex query:
 `((header.schema_id.namespace=='omh') and (header.schema_id.name=='blood-glucose') and (header.schema_id.version.major==1) and (header.schema_id.version.minor==0) and (body.blood_glucose.value > 100)) or ((header.schema_id.namespace=='omh') and (header.schema_id.name=='physical-activity')) or ((header.schema_id.namespace=='omh') and (header.schema_id.name=='physical-activity'))`


### Roadmap

The following features are scheduled for future milestones

* improve test coverage
* Provide a way to provide an intersection between two different queries

If you have other feature requests, [create an issue for each](https://github.com/MD2Korg/openmhealth-ce-api-endpoint-mongo/issues)
and we'll figure out how to prioritise them.


### Contributing

If you'd like to contribute any code

1. [Open an issue](https://github.com/MD2Korg/openmhealth-ce-api-endpoint-mongo/issues) to let us know what you're going to work on.
  1. This lets us give you feedback early and lets us put you in touch with people who can help.
2. Fork this repository.
3. Create your feature branch `feature/do-x-y-z` from the `develop` branch.
4. Commit and push your changes to your fork.
5. Create a pull request.