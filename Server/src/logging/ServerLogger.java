package logging;

import java.sql.Timestamp;

public class ServerLogger {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    public void printLog(String message, String name, LogType logType){
        switch (logType){
            case Log:
                System.out.printf(ANSI_CYAN + "%s %s%n"+ANSI_RESET, new Timestamp(System.currentTimeMillis()), message);
                break;

            case Error:
                System.out.printf(ANSI_RED + "%s %s%n"+ ANSI_RESET, new Timestamp(System.currentTimeMillis()), message);
                break;

            case Message:
                System.out.printf(ANSI_WHITE + "    %s %s %s%n" +ANSI_RESET, new Timestamp(System.currentTimeMillis()), name, message);
                break;

            default:
                System.err.println("NO VALID LOGTYPE GIVEN");;
        }
    }

    public void printLog(String message, LogType logType){
        this.printLog(message, "", logType);
    }
}
