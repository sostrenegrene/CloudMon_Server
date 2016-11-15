package dk.mudlogic.cloudmon.datamanager.query;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;

/**
 * Created by soren.pedersen on 06-07-2016.
 */
public class ConsoleQuery {

    private LogTracer log = new LogFactory().tracer();

    private String query;

    private String result_str = "";
    private String error_str = "";

    public ConsoleQuery(String query) {
        log.setTracerTitle(ConsoleQuery.class);

        this.query = query;

        start();
    }

    private String jsonString(String[] res) {
        JSONArray a = new JSONArray();

        for (int i=0; i<res.length; i++) {
            String s = res[i];
            a.add(s);
        }

        return a.toJSONString();
    }

    /** Executes the command
     *
     */
    public void start() {
        String[] res;
        try {

            res = new Execute(this.query).result();

            result_str = jsonString(res);
            error_str = "";
        }
        catch(Exception e) {
            result_str = "";
            error_str = e.getMessage();

            e.printStackTrace();
        }

    }

    /** Returns the command result
     *
     * @return String
     */
    public String result() {
        return result_str;
    }

    public String errors() {
        return error_str;
    }
}
