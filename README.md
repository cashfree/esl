# ESL (Environment Specification Language)

## What is ESL?

ESL (**E**nvironment **S**pecification **L**anguage) is a language-cum-library to efficiently manage the configurations for a *Kubernetes* service across multiple environments.
 This library helps unify the multiple environment configurations of a service into a single file.

## Why is it important?

Given the era of microservices, the number of services an organization has, increases day by day and so is the agility index. 
If we plot these two in X and Y axis, what we get is the number of deployment of services. 
The expectation is that as X or Y increases deployment also increases. 
Though this is good for engineering and marketing teams, it is a nightmare for the infra/devops team which maintains the hardware/cloud infrastructure for you. 
Maintaining and keeping the configurations in sync across environments is a tedious process and the problem worsens in the following scenarios.
* When you want an additional permanent environment for dev/qa testing.
* When your QE team wants a new ephemeral environment for automation testing.
* When your organization wants to set up a BCP (Business Continuity Plan or Disaster Recovery) environment.

This is where ESL comes for rescue. It helps you maintain all the configurations of a service for all the environments in a single file.

## Assumptions
* In this entire document and in the library, it assumed that you have an existing `prod` environment, and you won't be able/don't want to change that environment configuration. You can extend this feature to any environment.
* All the database names should be as that of the service that owns it.
* All the URLs should reflect the service it uses.
* All the message topics/queues, DB names and other infrastructure resource to follow standard prefix such as <org>-<env>. Of course this is customizable.

## Features
The following variables are available if you want control over the config file customizations. 
However, you are encouraged to use the functions wherever possible to maximize the portability 
### Variables

    ${awsAccountId}
        e.g 1234567890

    ${awsRegion}
        e.g ap-south-1

    ${env}
        e.g qa

### Functions
#### 1. Message Brokers
##### 1.1 Kafka

`kafkaUrl()`

e.g.

    qa   : k8s-qa-kafka-bootstrap.kafka:9093

    stage: k8s-stage-kafka-bootstrap.kafka:9093

    prod : k8s-prod-kafka-bootstrap.kafka:9093


##### 1.2 ActiveMQ

`activeMQUrl()`

This will generate the activemq url based on the template configured. Refer to `JsonDelegateExt.activeMQServers`.

##### 1.3 SQS

`sqsUrl(queueName = '')`

**Parameters**

    queueName - [optional] the name of the queue

e.g

`sqsUrl()` =>

    any env: https://sqs.${awsRegion}.amazonaws.com/${accountId}

`sqsUrl('sampleQueue')` =>

    prod:       https://sqs.${awsRegion}.amazonaws.com/${accountId}/sampleQueue

    other envs: https://sqs.${awsRegion}.amazonaws.com/${accountId}/cf-${env}-sampleQueue

##### 1.4 Message Topic/Queue Name

`messageTopic(topic, prefix='')`

You can use this function to define,
1. kafka topic
2. activeMQ queue
3. SQS queue

**Parameters**

    topic - the name of the topic or queue. This is the default argument, hence you can write it as messageTopic('myQueue') if you don't have prefix.

    prefix - [optional] the prefix if any.

e.g.

`messageTopic('payout.myTopic')`  =>

    qa:    cf-qa-payout.myTopic

    stage: cf-stage-payout.myTopic

    prod:  payout.myTopic



`messageTopic(topic='payout.myTopic', prefix='maxwell.')`  =>

    qa:    maxwell.cf-qa-payout.myTopic

    stage: maxwell.cf-stage-payout.myTopic

    prod:  maxwell.payout.myTopic

#### 2. Database
##### 2.1 MySQL

`mysqlDBHost(service=' ')`

You can use this form to specify only the host name of the MySQL.

**Parameters**

    service - [optional] the name of the service whose database you want to use in your application. If you don’t specify this parameter, then the name of the service for which you are writing this function will be used.

e.g mysqlDBHost() =>

    prod    : cf-${service}.mysql.db

    others : cf-${env}-${service}.mysql.db

e.g mysqlDBHost('payoutsvc') =>

    prod    : cf-payoutsvc.mysql.db

    others : cf-${env}-payoutsvc.mysql.db



`mysqlDBUrl(schema, port=3306, service=' ')`

You can use this form to specify the complete URL along with the schema.

**Parameters**

    schema - the name of the schema. This is the default argument. You can also mention the query params that you want to pass to the DB.

    port - [optional] the port number; defaults to 3306

    service - [optional] the name of the service whose database you want to use in your application. If you don’t specify this parameter, then the name of the service for which you are writing this function will be used.

e.g `mysqlDBUrl('payoutdb?utf8=true')` =>

    prod    : jdbc:mysql://cf-${service}.mysql.db:3306/payoutdb?utf8=true

    others : jdbc:mysql://cf-${env}-${service}.mysql.db:3306/cf_${env}_${service}?utf8=true

e.g `mysqlDBUrl('myexceptionaldb?utf8=true')` =>

    prod    : jdbc:mysql://cf-${service}.mysql.db:3306/myexceptionaldb?utf8=true

    others : jdbc:mysql://cf-${env}-${service}.mysql.db:3306/cf_${env}_myexceptionaldb?utf8=true

myexceptionaldb schema is an exception as it differs from the standard naming inferred from the service name.

e.g `mysqlDBUrl('payoutdb', 3306, 'reconsvc')` => This is helpful incase one of your microservice wants to talk to another service's DB.
In this example the service (assumed as `payoutsvc`) is trying to connect to the database owned by the service `reconsvc`. 

    prod    : jdbc:mysql://cf-payoutbenesvc.mysql.db:3306/payoutdb?utf8=true

    others : jdbc:mysql://cf-${env}-payoutbenesvc.mysql.db:3306/cf_${env}_reconsvc?utf8=true

##### 2.2 MongoDB

`mongoDBHost()`

This will generate the mongodb url based on the template configured. Refer to `JsonDelegateExt.mongoDBHosts`.

##### 2.3 Redshift

`redshiftDBHost()`

You can use this form to specify only the host name of the redshift.

e.g `redshiftDBHost()` =>

    prod    : cf-${service}.redshift.db

    others : cf-${env}-${service}.redshift.db



`redshiftDBUrl(schema, port=5439)`

You can use this form to specify the complete URL along with the schema.

**Parameters**

    schema - the name of the schema. This is the default argument. You can also mention the query params that you want to pass to the DB.

    port - [optional] the port number; defaults to 5439

e.g `redshiftDBUrl(schema='mySchema?utf8=true')`

    prod:           jdbc:redshift://cf-${service}.redshift.db:5439/mySchema?utf8=true

    sbox:           jdbc:redshift://cf-${service}.redshift.db:5439/dev?utf8=true

    other envs:jdbc:redshift://cf-${service}.redshift.db:5439/stage?utf8=true

##### 2.4 Elasticsearch

`elasticSearchUrl()`

e.g ```cf-${env}-${service}.es.db```

##### 2.5 Database Schema

`databaseSchema(schema)`

Some applications mention the DB host and schema separately. In such cases, you can use this function to represent schema. This is applicable to all the database systems mentioned above.

e.g. `databaseSchema('mySchema')`

    prod & sbox: mySchema

    other envs:    cf_${env}_mySchema

#### 3. Cache
##### 3.1 Memcache

`memcacheDBHost()`

e.g

    prod    : cf-${service}.memcache.db

    others : cf-${env}-${service}.memcache.db

##### 3.2 Redis

`redisDBHost()`

You can use this form to specify only the host name of the redis.

e.g `redisDBHost()` =>

    prod    : cf-${service}.redis.db

    others : cf-${env}-${service}.redis.db



`redisDBUrl(port=6379)`

You can use this form to specify the complete URL.

**Parameters**

    port - [optional] the port number; defaults to 6379

e.g `redisDBUrl()`

    prod:           redis://cf-${service}.redis.db:6379

    other envs:redis://cf-${env}-${service}.redis.db:6379

#### 4. S3

`s3Url(project)`

e.g.

https://cf-${env}-${project}.s3.${awsRegion}.amazonaws.com


`s3Bucket(bucket)`

e.g `s3Bucket('mybucket')` =>

    qa:      cf-qa-mybucket

    stage: cf-stage-mybucket

    prod:   mybucket

#### 5. Keycloak

`keycloakUrl()`

e.g ```https://keycloak.${env}.cashfree.com/keycloak-console```
#### 6. Dependent Service

`serviceUrl(protocol, service, uri = '')`

**Parameters**
~~~
protocol - [optional] the protocol; defaults to http

name - the name of the service

namespace - [optional] the namespace of the service; defaults to the value of name

uri - [optional] the URI of the service\
~~~
e.g `serviceUrl(protocol:'http', name:'settlementsvc')`

    prod:   http://settlementsvc.settlementsvc

    others: http://${env}-settlementsvc.${env}-settlementsvc

e.g `serviceUrl(protocol:'http', name:'settlementsvc', uri: '/v1')`

    prod:   http://settlementsvc.settlementsvc/v1

    others: http://${env}-settlementsvc.${env}-settlementsvc/v1

e.g `serviceUrl(protocol:'http', name:'settlementsvc', namespace:'myspace'', uri: '/v1')`

    prod:   http://settlementsvc.myspace/v1

    others: http://${env}-settlementsvc.${env}-myspace/v1

### c) Array

ESL Syntax
~~~
key1 ([1,2,3])
key2 ([
  "abc",
  "xyz"
])
key3 ([
  {
   a 111
   c 444
  },
 { b 222 }
])
~~~

YAML Output
~~~
key1:
  - 1
  - 2
  - 3
key2:
  - "abc"
  - "xyz"
key3:
  - a: 111
    c: 444
  - b: 222
~~~

### D) Overrides

When you can't represent property in a neutral way using variables and functions, you can use override option, as explained below.

prod.yaml
~~~
spring:
  profile: prod
  debug: false
~~~
sbox.yaml
~~~
spring:
  profile: gamma
  debug: true
~~~
qa.yaml
~~~
spring:
  profile: master
  debug: true
~~~
stage.yaml
~~~
spring:
  profile: master
  debug: true
~~~


If you have the above YAML files, then the ESL file will look like the following.

environments.esl
~~~
spring {
    profile 'master'
    'sbox:profile' 'gamma'
    'prod:profile' 'prod'
    debug false
    prod:debug' true
}
~~~


### E) Special Case Strings

| ESL                                                                           | Output                                                                |
|:------------------------------------------------------------------------------|:----------------------------------------------------------------------|
| ‘${awsRegion}’                                                                | ${awsRegion}                                                          |
| “${awsRegion}”                                                                | ap-south-1                                                            |
| ‘''this won’t interpolate but escapes single quote'''                         | this won’t interpolate but escapes single quote                       |
| “““This “special” case escapes double quote”””                                | This “special” case escapes double quote                              |
| '''This carries<br>new line characters'''                                     | This carries <br>new line characters                                  |
| “““This carries<br>new line characters<br>with interpolation: ${awsRegion}””” | This carries<br>new line characters<br>with interpolation: ap-south-1 |

### F) Special Environments
Any key without override prefix, will act as the base configuration for all the envs. 
If a key is overriden for a given env, then that will be picked up as explained in section D. However for DR/BCP env it is different.

environment.esl

~~~
configmap(serviceName='mysvc') {
  key1 123
  'sbox:key1' 222
  key2 888
  '~sbox:key2' 999
}
~~~

In this case, the qa.yaml will look like,
~~~
key1: 123
~~~
sbox.yaml will be
~~~
key1: 222
~~~
but the sboxdr (as it is DR env) it should be similar to sbox and NOT like the default env.
~~~
key1: 222
~~~
Note that the value of `key1` is `222` and not `123`. 
This behaviour is already taken care by ESL as long you name your DR env in the format < env ><'dr'>. 
Please note that you also don't have to override anything for your DR env.

However, if you want the override only for sbox and not for sboxdr, then prefix it with `~` as it is done for key2.
In that case sbox.yaml will have `key2: 999` but sboxdr.yaml will have `key2 888`.

## FAQ
### How can I add a new function to the ESL?
Add your functions to `JsonDelegateExt.groovy` class

### Can I add a new variable?
Yes, add it to `JsonDelegateExt.substitutions` variable and return it in `JsonDelegateExt.getProperty()`

### Where can I look to understand the override logic?
Please look into `JsonGenerator.groovy`

### Given that \<env>.yaml files are generated, I planned to not check in those files but only the environments.esl file. In that case, how will I verify the change after modifying the ESL file?
Yes, that is the recommended approach. To verify the change, you can generate the YAML file locally using `generate-yaml-dev.sh` file.

### What is the use of `generate-yaml-local.sh` file?
If you are a developer enchancing the ESL, then you can run this file to verify language changes. You can also use your IDE directly.

### I have \<env>.yaml files already. Can I generate ESL file out of it?
Yes, you can check `EslGenerator.groovy` and the `generate-esl-and-yaml.sh` files. Also look at `esl-substitutions.yml` file converting your yaml to ESL.
