package com.cashfree.esl

import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import static com.cashfree.esl.Terminal.printError

/**
 * This class wraps the linux shell commands and provides an easy mechanisms to execute it in ESL.
 * @author Pragalathan M <pragalathanm@gmail.com>
 */
class Command {
    def command
    def exitCode = Double.NaN
    def task
    def output
    ExecutorService executor = new ThreadPoolExecutor(0, 1,
            5L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>())

    private Command() {}

    private Command execute(String command, def wait, def emitOutput, def ignoreError, def envp = null, def pwd = null) {
        task = executor.submit({
            if (exitCode != 0 && !Double.isNaN(exitCode)) {
                printError("Previous command failed with exit code $exitCode. Hence will not run the command '${command}'")
                return -1
            }
            this.command = command
            def process = command.execute(envp, pwd)
            def output = new StringBuffer()

            process.waitForProcessOutput(output, System.err)

            exitCode = ignoreError ? 0 : process.exitValue()
            this.output = output.toString()

            if (emitOutput) println this.output
            return exitCode
        })

        if (wait) {
            task.get()
            task = null
        }
        return this
    }

    def join() {
        executor.shutdown()
        executor.awaitTermination(5, TimeUnit.MINUTES)
    }

    def then(String command, wait = false, output = false, ignoreError = false) {
        return execute(command, wait, output, ignoreError)
    }

    def then(Map<String, Object> command) {
        if (!command['command']) throw new IllegalArgumentException('command parameter is missing')
        return execute(command['command'],
                command.getOrDefault('wait', false),
                command.getOrDefault('output', false),
                command.getOrDefault('ignoreError', false),
                command.getOrDefault('envp', null),
                command.getOrDefault('pwd', null))
    }

    def then(Closure closure) {
        executor.submit(closure)
        return this
    }

    def onSuccess(Closure closure) {
        executor.submit({
            if (exitCode == 0) closure(exitCode, command)
            executor.shutdown()
        })
        return this
    }

    def onFailure(Closure closure) {
        executor.submit({
            if (exitCode != 0) closure(exitCode, command)
            executor.shutdown()
        })
        return this
    }

    def static run(Map<String, Object> command) {
        if (!command['command']) throw new IllegalArgumentException('command parameter is missing')
        return new Command().execute(command['command'],
                command.getOrDefault('wait', false),
                command.getOrDefault('ignoreError', false),
                command.getOrDefault('output', false))
    }

    def static run(String command, wait = false, output = false, ignoreError = false) {
        return new Command().execute(command, wait, output, ignoreError)
    }

    def static newCommand() {
        return new Command()
    }
}

