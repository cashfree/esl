package com.cashfree.esl


import com.cashfree.esl.parser.EslProcessor

import static com.cashfree.esl.Terminal.printError

/**
 *
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class Main {
    static void main(String[] args) {
        if (args.length < 1) {
            printError("Invalid number of arguments. run it with \n <env1> [<env2>...<envN>]")
            System.exit(1)
        }
        println "Groovy Version : " + GroovySystem.version
        println "Java Version : " + System.getProperty("java.version")
        EslProcessor.main(args);
    }
}

