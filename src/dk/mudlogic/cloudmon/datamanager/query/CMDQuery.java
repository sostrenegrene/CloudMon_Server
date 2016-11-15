package dk.mudlogic.cloudmon.datamanager.query;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;

/**
 * Created by soren.pedersen on 06-07-2016.
 */
public class CMDQuery {

    private LogTracer log = new LogFactory().tracer();

    private String query;

    private String result_str = null;


    public CMDQuery(String query) {
        log.setTracerTitle(CMDQuery.class);

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
        String[] err = new String[1];
        String end_res;
        String[] res;
        try {

            res = new Execute(this.query).result();

            result_str = jsonString(res);
        }
        catch(Exception e) {
            result_str = null;
            err[0] = e.getMessage();
            res = new String[0];

            e.printStackTrace();
        }

        //log.trace(">>"+res.length+"-"+result_str);
    }

    /** Returns the command result
     *
     * @return String
     */
    public String result() {
        return result_str;
    }

}
