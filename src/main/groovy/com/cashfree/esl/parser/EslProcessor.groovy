package com.cashfree.esl.parser

import com.cashfree.esl.Command
import groovy.json.JsonDelegate

import java.nio.file.Paths

import static com.cashfree.esl.Terminal.*

/**
 * This is the main class for generating the environment specific YAML files.
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class EslProcessor {
    def ROOT_DIR
    def PROJECTS_DIR

    EslProcessor() {
        JsonDelegate.mixin JsonDelegateExt
        ROOT_DIR = Paths.get(".").toAbsolutePath().normalize()
        PROJECTS_DIR = ROOT_DIR.resolve("temp")
    }

    /**
     * Generates YAML file for the {@code env}
     * @param templatePath the path of the ESL file
     * @param env the environment for which the YAML to be generated
     */
    def generateYaml(templatePath, env) {
        JsonDelegate.metaClass.getEnvName() { return env }
        def eslFileContent = new File(templatePath).text.trim()
        def builder = Eval.me("""
            def builder = new com.cashfree.esl.parser.DynamicYamlBuilder('$env')
            builder.${eslFileContent}
            builder
        """)

        def output = builder.toString()
        println '[INFO] -------- Generating configmap yaml from ESL file --------'
        println output
        println '[INFO] --------------------------------------------------------'
        def outputDir = new File(templatePath).parent
        Paths.get(outputDir, "${env}.yaml").toFile().write(output);
        printSuccess "Generated new ${env}.yaml file successfully!"
    }

    /**
     * Generates YAML files for the given {@code envs} of the project located at {@code projectPath}.
     * To be precise, this method looks for a file named {@code environments.esl} in all the (sub)folders inside {@code projectPath}.
     * The ESL file name can be customized by setting the environment variable {@code ESL_FILE}. In the absence of that variable,
     * it will fall back to {@code environments.esl}
     * @param yamlGenerator the instance of {@link EslProcessor}
     * @param envs the environments for which YAMLS to be generated
     * @param projectPath the path where the {@code environments.esl} resides
     */
    def static generateYaml(def yamlGenerator, def envs, String projectPath) {
        def ESL_FILE = System.getenv('ESL_FILE')
        if (!ESL_FILE) {
            ESL_FILE = 'environments.esl'
        }
        def allConfigmapValuesFolders = Command.run("find $projectPath -name $ESL_FILE", true).output.trim().split('\n')
        printInfo "Config folders: " + allConfigmapValuesFolders
        allConfigmapValuesFolders.each { eslFile ->
            printInfo "Processing $projectPath: ${new File(eslFile).parent}"
            envs.each { env -> yamlGenerator.generateYaml(eslFile.trim(), env) }
        }
    }

    /**
     * Main entry for generating YAML file from ESL file. There are two ways in which this methods tries to locate the project folder.
     * <ol>
     *     <li> Environment variable: in this mode this method infers the project folder via the env variable {@code SERVICE}.
     *     <li> repos file: in this mode, this methods infers each line in a file called {@code repos} as a project folder.
     *     </ol>
     *     If you want to bulk generate the YAML files for multiple projects, then it is recommended to use {@code repos} file.
     * @param args the list of environments for which the YAML files to be generated.
     */
    static void main(String[] envs) {
        def service = System.getenv('SERVICE')
        def yamlGenerator = new EslProcessor()
        if (service) {
            generateYaml(yamlGenerator, envs, service)
        } else {
            def repos = new File('repos').readLines().collect { it.trim() }.findAll { !it.startsWith("#") && !it.isEmpty() }
            repos.each { repo ->
                try {
                    generateYaml(yamlGenerator, envs, "${PROJECTS_DIR}/$repo")
                } catch (Exception e) {
                    printError("Error while processing repo: $repo")
                    throw e
                }
            }
        }
    }
}