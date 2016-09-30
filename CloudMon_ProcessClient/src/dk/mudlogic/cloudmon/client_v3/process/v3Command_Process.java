package dk.mudlogic.cloudmon.client_v3.process;

import dk.mudlogic.cloudmon.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.client_v3.client.v3CommandResult;
import dk.mudlogic.cloudmon.client_v3.query.v3Query_Console;
import dk.mudlogic.cloudmon.client_v3.query.v3Query_Database;
import dk.mudlogic.scripts.ScriptManager;
import dk.mudlogic.scripts.ScriptResult;
import dk.mudlogic.tools.callback.CallbackHandler;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3Command_Process {

    private LogTracer log = new LogFactory().tracer();

    private final int PTYPE_CONSOLE = "console".trim().hashCode();
    private final int PTYPE_QUERY = "database".trim().hashCode();

    private TimeHandler time = new TimeHandler();

    private v3Command command;

    public v3Command_Process(v3Command command, CallbackHandler group_callback) {
        log.setTracerTitle(v3Command_Process.class);

        //Set client and command data
        this.command = command;

        //Start new thread that uses an instance of this class
        //In the internal class ProcessThread
        new Thread( new ProcessThread(this,group_callback) ).start();
    }


    /** Get command
     *
     * @return
     */
    public v3Command getCommand() { return command; }


    /** Select the process type of the conmmand
     *
     */
    private void select_ProcessType() {
        int type = command.get_str("process_type").trim().hashCode();

        //If process type is console
        if (PTYPE_CONSOLE == type) {
            log.trace( "Console: " + command.get_str("process_name") );

            command = new v3Query_Console(command).getCommand();
        }

        //If process type is database
        if (PTYPE_QUERY == type) {

            //TODO do SQL query
            command = new v3Query_Database(command).getCommand();
           // log.trace("Do database query");
            log.trace( "Database: " + command.get_str("process_name") );

        }

        //Run javascript parser
        js_parse();

        //Update last changed on command
        command.last_changed( time.unixTime() );
    }

    private void js_parse() {

        try {
            //Run script manager with result string
            //String path = ServerGlobalData.MAIN_CONFIG.group("server").get("install_path")+"parsers\\";
            String path = command.get_str("install_path") + "parsers\\";
            String file = command.get_str("process_type") + "\\" + command.get_str("parser_script");
            ScriptManager sm = new ScriptManager(path, file);

            if (command.getResult().hasResult()) {
                ScriptResult sc_result = sm.parse(command.getResult().getResult());

                log.trace("Script: " + sc_result.error_message + " " + sc_result.result);
                command.getResult().setParsedResult(sc_result.result);

                String err = sc_result.error_message + " " + command.getResult().getErrorMessages();
                command.getResult().setErrorMessages(err);
            }
        }
        catch(Exception e) {}
    }

    /** Execute the command
     * and returns result object
     *
     * @return v3CommandResult
     */
    public void exec() {
        //log.trace("exec() " + command.get_str("process_name"));

        select_ProcessType();
    }

}
