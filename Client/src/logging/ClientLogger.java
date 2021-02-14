package logging;

import java.sql.Timestamp;

public class ClientLogger {

    //Text colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BRIGHT_WHITE = "\u001b[37;1m";

    public void printLog(String message, String name, boolean success, LogType logType){
        switch (logType){
            case Log:
                if (success){
                    System.out.printf( ANSI_BLUE + "(%s) " + ANSI_CYAN + "[%s] " + ANSI_GREEN + "%s%n" + ANSI_RESET,
                            new Timestamp(System.currentTimeMillis()), name, message);
                } else {
                    System.out.printf(ANSI_BLUE + "(%s) " + ANSI_CYAN + "[%s] " + ANSI_RED + "%s%n" + ANSI_RESET,
                            new Timestamp(System.currentTimeMillis()), name, message);
                }
                break;

            case Error:
                System.out.printf(ANSI_PURPLE + "(%s) [%s] %s%n"+ ANSI_RESET, new Timestamp(System.currentTimeMillis()), name, message);
                break;

            case Message:
                System.out.printf(ANSI_WHITE + "    (%s) [%s] %s%n" +ANSI_RESET, new Timestamp(System.currentTimeMillis()), name, message);
                break;

            default:
                System.err.println("NO VALID LOGTYPE GIVEN");;
        }
    }

    public void printLog(String message, boolean success, LogType logType){
        this.printLog(message, "server", success, logType);
    }
}
