package week3.dronesimulationjava;

public class Logger {
    static void info (String msg) {
        System.out.println(String.format("[INFO]: %s", msg));
    }

    static void warn (String msg) {
        System.out.println(String.format("[WARN]: %s", msg));
    }

    static void error (String msg) {
        System.out.println(String.format("[ERROR]: %s", msg));
    }
}
