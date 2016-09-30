package dk.mudlogic.cloudmon.setvice;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 09-08-2016.
 */
public class ServiceMain {

    public static LogTracer log = new LogFactory().tracer();

    public static void main(String[] args) {
        log.setTracerTitle(ServiceMain.class);

        if ("start".equals(args[0])) {
            start(args);
        } else if ("stop".equals(args[0])) {
            stop(args);
        }
    }

    public static void start(String[] args) {

        try {
            new Execute("c:\\CloudMon\\run_cloudmon.bat");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void stop(String[] args) {
        try {
            new Execute("c:\\CloudMon\\stop_cloudmon.bat");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
