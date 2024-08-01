package com.cashfree.esl.parser

import com.cashfree.esl.YamlBuilder
import groovy.json.JsonBuilder
import groovy.json.JsonDelegate
import groovy.json.JsonGenerator.Options
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

/**
 * This class extracts the service name from the ESL file. It also strips the quotes from the values in the output YAML.
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class DynamicYamlBuilder extends YamlBuilder {

    JsonBuilder builder

    DynamicYamlBuilder(String env) {
        this(new groovy.json.JsonBuilder(new JsonGenerator(new Options(), env)))
    }

    DynamicYamlBuilder(JsonBuilder builder) {
        super(builder)
        this.builder = builder
    }


    /**
     * Extracts the service name from the attribute of {@code configmap} element.
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        if (name == 'configmap') {
            if (args && args[0] instanceof Map && args[0].containsKey('serviceName')) {
                def serviceName = args[0].serviceName
                JsonDelegate.metaClass.getServiceName() {
                    return serviceName
                }
                args[0].clear()
            }
        }
        return super.invokeMethod(name, args)
    }

    /**
     * Overrides the {@link YamlBuilder#toString()} method to strip the quotes from the output YAML.
     * Unfortunately groovy.yaml.Builder doesnt have option to customize the quotes. Hence this method regenerates the yaml using direct snakeyaml parser and remove quotes in values
     */
    @Override
    String toString() {
        def result = super.toString()
        def dumperOptions = new DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        dumperOptions.indent = 2
        def yaml = new Yaml(dumperOptions)
        result = yaml.load(result)
        if (result.configmaps) {
            result['configmaps'] = result.configmaps.collect { it.value }
        }
        return yaml.dump(result)
    }
}