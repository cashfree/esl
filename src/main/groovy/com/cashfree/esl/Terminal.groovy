package com.cashfree.esl

/**
 * Small ANSI coloring utility.
 *
 * @see http://www.bluesock.org/~willg/dev/ansi.html
 * @see https://gist.github.com/dainkaplan/4651352
 */
class Terminal {

    static final String NORMAL = "\u001B[0m"

    static final String BOLD = "\u001B[1m"
    static final String ITALIC = "\u001B[3m"
    static final String UNDERLINE = "\u001B[4m"
    static final String BLINK = "\u001B[5m"
    static final String RAPID_BLINK = "\u001B[6m"
    static final String REVERSE_VIDEO = "\u001B[7m"
    static final String INVISIBLE_TEXT = "\u001B[8m"

    static final String BLACK = "\u001B[30m"
    static final String RED = "\u001B[31m"
    static final String GREEN = "\u001B[32m"
    static final String YELLOW = "\u001B[33m"
    static final String BLUE = "\u001B[34m"
    static final String MAGENTA = "\u001B[35m"
    static final String CYAN = "\u001B[36m"
    static final String WHITE = "\u001B[37m"

    static final String DARK_GRAY = "\u001B[1;30m"
    static final String LIGHT_RED = "\u001B[1;31m"
    static final String LIGHT_GREEN = "\u001B[1;32m"
    static final String LIGHT_YELLOW = "\u001B[1;33m"
    static final String LIGHT_BLUE = "\u001B[1;34m"
    static final String LIGHT_PURPLE = "\u001B[1;35m"
    static final String LIGHT_CYAN = "\u001B[1;36m"

    static String color(String text, String ansiValue) {
        ansiValue + text + NORMAL
    }

    static void printError(String text) {
        println RED + "[ERROR] " + text + NORMAL
    }

    static void printInfo(String text) {
        println BLUE + "[INFO] " + text + NORMAL
    }

    static void printWarning(String text) {
        println YELLOW + "[WARN] " + text + NORMAL
    }

    static void printSuccess(String text) {
        println GREEN + "[SUCCESS] " + text + NORMAL
    }
}