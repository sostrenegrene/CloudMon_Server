package dk.mudlogic.scripts;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.parse.Parser_Javascript;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 20-07-2016.
 */
public class ScriptManager {

    private LogTracer log = new LogFactory().tracer();

    private String script_path;
    private String script_file;
    private Parser_Javascript jsp;

    private boolean has_error = false;

    /** Setup javascript manager
     *
     *
     * @param script_file String
     */
    public ScriptManager(String script_path,String script_file) {
        log.setTracerTitle(ScriptManager.class);

        this.script_path = script_path;
        this.script_file = script_file;

        start();
    }

    private void start() {
        //script_file = "C:\\CloudMon\\parsers\\" + script_file;

        if ( !script_file.equals("") ) {
            //log.trace("Script File: " + script_file);

            //script_file = "C:/CloudMon/parsers/console/" + script_file;

            jsp = new Parser_Javascript(script_path + script_file);
        }
    }

    public ScriptResult parse(String json_data) {
        String err;
        boolean has_error;

        ScriptResult sr = null;

        try {
            //log.trace(json_data);

            //First eval js system files
            jsp.eval_file(script_path + "build_json.js");
            jsp.eval_file(script_path + "errors.js");

            Hashtable<String, Object> table = jsp.toTable("CloudMon_Javascript(" + json_data + ");");

            err = (String) table.get("error_message");
            has_error = (Boolean) table.get("has_error");
            json_data = (String) table.get("result");

            //err = err.replace("'","\"");
            //json_data = json_data.replace("'","\"");

            //log.trace(json_data);

            sr = new ScriptResult(json_data,err,has_error);
        }
        catch(Exception e) {
            //e.printStackTrace();
            String out = "Could not parse javascript! " + e.getMessage();
            sr = new ScriptResult("",out,true);
            log.error(out);
        }

        return sr;
    }

}
