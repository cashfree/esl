package com.cashfree.esl.parser

import groovy.json.DefaultJsonGenerator
import groovy.json.JsonDelegate
import org.apache.groovy.json.internal.CharBuf

/**
 * This class handles the override logic.
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class JsonGenerator extends DefaultJsonGenerator {

    static final char OPEN_BRACE = '{'
    static final char CLOSE_BRACE = '}'
    static final char COMMA = ','
    static final char[] EMPTY_MAP_CHARS = [OPEN_BRACE, CLOSE_BRACE]
    private String env

    JsonGenerator(Options options, String env) {
        super(options)
        this.env = env
    }

    /**
     * While generating the JSON, this method applies the environment specific overrides.
     *
     * @param map the configuration as map.
     * @param buffer the buffer to append the output to.
     */
    @Override
    protected void writeMap(Map<?, ?> map, CharBuf buffer) {
        if (map.isEmpty()) {
            buffer.addChars(EMPTY_MAP_CHARS)
            return
        }
        buffer.addChar(OPEN_BRACE)
        Map<String, Object> overrides = new HashMap<>()
        // since 'nonProd' is a keyword, check for it. any environment not starting with 'prod' is a nonProd environment
        boolean nonProd = !env.startsWith("prod")
        // see if this is a DR/BCP environment
        boolean dr = env.endsWith("dr")
        String envAlias = env.substring(0, env.length() - 2)
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON")
            }
            String key = entry.getKey().toString()
            Object value = entry.getValue()
            if (key.startsWith("@configmap/")) {
                // case where ESL file starts with 'configmaps', containing multiple 'configmap' elements within it.
                key = key.replace("@configmap/", '')
            }
            if (key.startsWith(env + ":")) { // overriden key
                overrides.put(key.substring(env.length() + 1), value)
            } else if (nonProd && key.startsWith("nonProd:")) {// overriden key for all nonProd envs
                overrides.put(key.substring("nonProd:".length()), value)
            } else if (dr && key.startsWith(envAlias + ":")) { // override applicable for DR too
                overrides.put(key.substring(envAlias.length() + 1), value)
            } else if (key.startsWith('~' + env + ":")) { // override not applicable DR
                overrides.put(key.substring(env.length() + 2), value)
            }
        }

        map.putAll((Map) overrides)
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() == null) {
                throw new IllegalArgumentException("Maps with null keys can\'t be converted to JSON")
            }
            String key = entry.getKey().toString()
            Object value = entry.getValue()
            if (key.startsWith("@configmap/")) {
                key = key.replace("@configmap/", '')
                if (overrides.containsKey(key)) {
                    value = overrides.get(key)
                }

                if (nonProd && (key.startsWith("nonProd:") || !key.contains(":")) || key.startsWith(env + ":")) {
                    def serviceName = key.startsWith(env + ":") ? key.replace(env + ":", '') : key
                    serviceName = key.startsWith("nonProd:") ? key.replace("nonProd:", '') : key
                    JsonDelegate.metaClass.getServiceName() {
                        return serviceName
                    }
                }
            }
            if (isExcludingValues(value) || isExcludingFieldsNamed(key)) {
                continue
            }
            if (key.matches("~?[a-zA-Z0-9]+:.*")) {
                // ignore all the overriden key as we have already collectd them in 'overrides' map and merged with 'map'
                continue
            }
            writeMapEntry(key, value, buffer)
            buffer.addChar(COMMA)
        }
        buffer.removeLastChar(COMMA) // dangling comma
        buffer.addChar(CLOSE_BRACE)
    }
}
