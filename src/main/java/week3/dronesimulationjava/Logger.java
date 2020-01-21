package week3.dronesimulationjava;

/**
 * Logger class - Useful for debugging purposes and pretty printing errors
 */
public class Logger {
    /**
     * Prints an info message
     * @param msg Message to print
     */
    static void info (String msg) {
        System.out.println(String.format("[INFO]: %s", msg));
    }

    /**
     * Prints a warning message - Used when something odd happens in the sim
     * @param msg Warning message to print
     */
    static void warn (String msg) {
        System.out.println(String.format("[WARN]: %s", msg));
    }

    /**
     * Prints an error message - Used when something went wrong
     * @param msg Error message to print
     */
    static void error (String msg) {
        System.out.println(String.format("[ERROR]: %s", msg));
    }
}
