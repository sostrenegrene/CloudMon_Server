package dk.mudlogic;

import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.parse.Parser_Javascript;

/**
 * Created by soren.pedersen on 20-07-2016.
 */
public class ScriptManager {

    private LogTracer log = new LogFactory().tracer();

    private String script_file;
    private Parser_Javascript jsp;

    private boolean has_error = false;

    /** Setup javascript manager
     *
     *
     * @param script_file String
     */
    public ScriptManager(String script_file) {
        this.script_file = script_file;

        start();
    }

    private void start() {
        script_file = "C:\\Program Files\\CloudMon\\parsers\\database\\" + script_file;

        if (script_file != null) {
            jsp = new Parser_Javascript(script_file);
        }
    }

    public String parse(String method,String json_data) {

        log.trace(json_data);
        json_data = jsp.parse(method,json_data);

        String s = jsp.parse_jsd(method,json_data).err_message;
        log.trace(s);

        return json_data;
    }

}
