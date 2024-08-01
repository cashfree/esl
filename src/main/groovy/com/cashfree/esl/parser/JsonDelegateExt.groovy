package com.cashfree.esl.parser

/**
 * This class handles all the variables and functions in the ESL file. This class has access to {@class getEnvName()} and {@code getServiceName()} functions.
 * You can define any new function here and it will be available in the ESL file.
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class JsonDelegateExt {
    def substitutions = [
            "awsRegion"   : "N/A",
            "awsAccountId": "N/A",
            "env"         : "N/A"
    ]

    def awsRegions = [
            'prod' : "ap-south-1",
            'sbox' : "ap-south-1",
            'qa'   : "ap-south-1",
            'stage': "ap-south-1"
    ]

    def accountIds = [
            'prod' : 1784132035335,
            'sbox' : 1784132035335,
            'qa'   : 1784734040331,
            'stage': 1784734040331
    ]

    def activeMQServers = [
            'qa'   : 'ssl://c-hsdjda5-238b-68c0-b9e3-2ee00dc4c3db-1.mq.ap-south-1.amazonaws.com:61617',
            'stage': 'ssl://c-hsdjda5-238b-68c0-b9e3-2ee00dc4c3db-1.mq.ap-south-1.amazonaws.com:61617',
            'sbox' : 'ssl://c-hsdj033-23a0-614f-9900-2df1688be756-1.mq.ap-south-1.amazonaws.com:61617',
            'prod' : 'ssl://c-hsdj0ee-2308-6bbf-a90d-28930e9623fb-1.mq.ap-south-1.amazonaws.com:61617',
    ]

    def kafkaServers = [
            'qa'   : 'k8s-qa-kafka-bootstrap.kafka:9093',
            'stage': 'k8s-stage-kafka-bootstrap.kafka:9093',
            'sbox' : 'k8s-master-kafka-bootstrap.kafka:9093',
            'prod' : 'k8s-prod-kafka-bootstrap.kafka:9093',
    ]

    def elasticSearchHosts = [
            'qa'  : 'vpc-stage-crawler-ghdy675sp5y2874b1m7u22lyor4hq.ap-south-1.es.amazonaws.com',
            'sbox': 'vpc-gamma-ghdy6752qqoc87i61x6r22hwq2imi.ap-south-1.es.amazonaws.com',
            'prod': 'vpc-gamma-gmqjngghdy675dxam387641mns22u.ap-south-1.es.amazonaws.com',
    ]

    def mongoDBHosts = [
            'qa'   : 'staging-mongo.11blrdw.mongodb.net',
            'stage': 'staging-mongo.11blrdw.mongodb.net',
            'sbox' : 'gamma-cluster-1-pl-0.fmwn6.mongodb.net',
    ]

    def vmHosts = [
            'qa'   : 'http://victoria-metrics-cluster-vmselect.vm:8481/select/0/prometheus/api/v1/query',
            'stage': 'http://victoria-metrics-cluster-vmselect.vm:8481/select/0/prometheus/api/v1/query',
            'sbox' : 'http://vmcluster-victoria-metrics-cluster-vmselect.vm:8481/select/0/prometheus/api/v1/query',
            'prod' : 'http://vmcluster-victoria-metrics-cluster-vmselect.vm:8481/select/0/prometheus/api/v1/query'
    ]

    def ORG = 'cf'

    def keycloakUrl() {
        def env = getEnvName()
        env = (env == 'prod') ? '' : env + "."
        return "https://keycloak.${env}cashfree.com/keycloak-console"
    }

    def kafkaUrl() {
        def env = getEnvName()
        return kafkaServers[env]
    }

    def messageTopic(topic, prefix = '') {
        def env = getEnvName()
        if (env in ['prod'])
            return prefix + topic
        return "$prefix${ORG}-${env}-${topic}"
    }

    def activeMQUrl(port = 61617) {
        def env = getEnvName()
        return activeMQServers[env]
    }

    def s3Bucket(bucket) {
        def env = getEnvName()
        if (env == 'prod')
            return bucket
        return "${ORG}-${env}-${bucket}"
    }

    def s3Url(project) {
        def env = getEnvName()
        return "https://${ORG}-${env}-${project}.s3.${awsRegions[env]}.amazonaws.com"
    }

    def elasticSearchUrl() {
        def env = getEnvName()
        def service = getServiceName()
        if (env == 'prod')
            return "${ORG}-${service}.es.db"
        return "${ORG}-${env}-${service}.es.db"
    }

    def sqsUrl(def queueName = '') {
        def env = getEnvName()
        if (env == 'prod')
            return "https://sqs.${awsRegions[env]}.amazonaws.com/${accountIds[env]}" + (!queueName.trim().isEmpty() ? "/$queueName" : "")
        return "https://sqs.${awsRegions[env]}.amazonaws.com/${accountIds[env]}" + (!queueName.trim().isEmpty() ? "/${ORG}-${env}-${queueName}" : '')
    }

    def serviceUrl(Map args) {
        return serviceUrl_(args.get('protocol', 'http'), args.get('name'), args.get('namespace', args['name']), args.get('uri', ''))
    }

    private def serviceUrl_(def protocol, def name, def namespace, def uri) {
        def env = getEnvName()
        if (env == 'prod')
            return "$protocol://$name.$namespace$uri"
        if (env == 'sbox')
            return "$protocol://${name}-gamma.${namespace}-gamma${uri}"
        return "$protocol://$env-$name.$env-$namespace$uri"
    }

    def redshiftDBUrl(schema, port = 5439) {
        def env = getEnvName()
        def service = getServiceName()
        def params = ''
        if (schema.contains('?'))
            params = schema.substring(schema.indexOf('?'))
        if (env == 'prod')
            return "jdbc:redshift://$ORG-${service}.redshift.db:${port}/$schema"
        return "jdbc:redshift://$ORG-${env}-${service}.redshift.db:${port}/stage?$params"
    }

    def redshiftDBHost() {
        def env = getEnvName()
        def service = getServiceName()
        if (env == 'prod')
            return "$ORG-${service}.redshift.db"
        return "$ORG-${env}-${service}.redshift.db"
    }

    def mysqlDBUrl(schema, port = 3306, service = '') {
        def env = getEnvName()
        if (!service)
            service = getServiceName()
        def params = ''
        if (schema.contains('?'))
            params = schema.substring(schema.indexOf('?'))
        if (env.startsWith('prod'))
            return "jdbc:mysql://${ORG}-${service}.mysql.db:${port}/$schema"
        if (schema.startsWith("myexceptionaldb"))
            return "jdbc:mysql://$ORG-${env}-${service}.mysql.db:${port}/${ORG}_${env}_${schema}"
        return "jdbc:mysql://$ORG-${env}-${service}.mysql.db:${port}/${ORG}_${env}_${service}$params"
    }

    def mongoDBHost() {
        def env = getEnvName()
        return mongoDBHosts[env]
    }

    def victoriaMetricsUrl() {
        def env = getEnvName()
        return vmHosts[env]
    }

    def redisDBHost(service = '') {
        def env = getEnvName()
        if (!service)
            service = getServiceName()
        if (env == 'prod')
            return "${ORG}-${service}.redis.db"
        return "$ORG-${env}-${service}.redis.db"
    }

    def memcacheDBHost() {
        def env = getEnvName()
        def service = getServiceName()
        if (env == 'prod')
            return "${ORG}-${service}.memcache.db"
        return "$ORG-${env}-${service}.memcache.db"
    }

    @Deprecated
    def redisUrl() {
        return redisDBUrl()
    }

    def redisDBUrl(port = 6379, service = '') {
        def env = getEnvName()
        if (!service)
            service = getServiceName()
        if (env == 'prod')
            return "redis://${ORG}-${service}.redis.db:${port}"
        return "redis://${ORG}-${env}-${service}.redis.db:${port}"
    }

    def mysqlDBHost(service = '') {
        def env = getEnvName()
        if (!service)
            service = getServiceName()
        if (env.startsWith('prod'))
            return "${ORG}-${service}.mysql.db"
        return "$ORG-${env}-${service}.mysql.db"
    }

    def databaseSchema(schema) {
        def env = getEnvName()
        if (env in ['sbox', 'sboxdr', 'prod', 'proddr'])
            return schema
        return "${ORG}_${env}_${schema}"
    }

    def getProperty(String name) {
        def env = getEnvName()
        substitutions['awsAccountId'] = accountIds[env]
        substitutions['env'] = env
        substitutions['awsRegion'] = awsRegions[env]
        try {
            substitutions['service'] = getServiceName()
        } catch (MissingMethodException ex) {
            // ignore
        }
        return substitutions.get(name, "\${${name}}")
    }
}

