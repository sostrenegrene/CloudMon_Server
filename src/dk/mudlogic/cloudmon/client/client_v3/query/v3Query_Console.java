package dk.mudlogic.cloudmon.client.client_v3.query;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.client.client_v3.client.v3CommandResult;
import dk.mudlogic.tools.Objects.strings.SearchAndReplace;
import dk.mudlogic.tools.console.v2Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import org.json.simple.JSONArray;

/** Performs a console command
 * Returns command result or any error messages from the command
 *
 * Created by soren.pedersen on 11-09-2016.
 */
public class v3Query_Console {

    private LogTracer log = new LogFactory().tracer();

    private v3Command command;

    private v3CommandResult result;

    private String result_str;
    private String error_str;

    public v3Query_Console(v3Command command) {
        log.setTracerTitle(v3Query_Console.class);
        this.command = command;

        result_str = "";
        error_str = "";

        connect();
    }

    public v3Command getCommand() {
        return command;
    }

    private void connect() {
        String query = command.get_str("query_string");
        String hostname = command.get_str("host_name");
        String username = command.get_str("username");
        String password = command.get_str("password");

        //First setup query string
        //Replace and tags in query with new value
        SearchAndReplace sar = new SearchAndReplace(query);
        sar.replace("{host_name}",hostname);
        sar.replace("{username}",username);
        sar.replace("{password}",password);
        query = sar.getResult();

        //Run the query
        start_query( query );

        result = new v3CommandResult( result_str );

        result.setErrorMessages( error_str );

        log.trace(result.getResult() +" " + result.getErrorMessages());

        command.setResult( result );
    }

    private String jsonString(String[] res) {
        JSONArray a = new JSONArray();

        if (res.length > 0) {
            for (int i = 0; i < res.length; i++) {
                //String s = StringEscapeUtils.escapeEcmaScript( res[i] );
                String s = res[i];
                a.add(s);
            }

            //return StringEscapeUtils.escapeJson( a.toJSONString() );
            return a.toJSONString().replace("\'","\"");

        }
        else {
            return "";
        }

    }

    /** Executes the command
     *
     */
    public void start_query(String query) {
        try {

            //res = new Execute(query).result();
           v2Execute exec = new v2Execute( query );

            result_str = jsonString( exec.result() );
            error_str = jsonString( exec.error() );
        }
        catch(Exception e) {
            result_str = "";
            error_str = e.getMessage();

            e.printStackTrace();
        }

    }

}
