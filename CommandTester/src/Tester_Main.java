import dk.mudlogic.query.CMDQuery;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import scripts.ScriptManager;
import scripts.ScriptResult;

/**
 * Created by soren.pedersen on 28-08-2016.
 */
public class Tester_Main {

    private static LogTracer log = new LogFactory().tracer();

    public static void main(String[] args) {
        String path = args[0];
        String file = args[1];
        String command = args[2];

        log.trace("Path: "+path + " File: " + file + " Cmd: " + command);
        String cmd_result = new CMDQuery(command).result();
        log.trace(cmd_result);

        ScriptManager sm = new ScriptManager(path,file);
        ScriptResult result = sm.parse(cmd_result);

        log.trace(result.toString());
    }

}
