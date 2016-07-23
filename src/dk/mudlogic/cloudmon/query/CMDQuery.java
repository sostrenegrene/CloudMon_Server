package dk.mudlogic.cloudmon.query;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;

import java.util.ArrayList;

/**
 * Created by soren.pedersen on 06-07-2016.
 */
public class CMDQuery {

    private LogTracer log = new LogFactory().tracer();

    private String pHost;
    private String pOptions;

    private String result_str = null;


    public CMDQuery(String pHost,String pOptions) {
        this.pHost = pHost;
        this.pOptions = pOptions;

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
        try {
            String[] res = new Execute(this.pHost, this.pOptions).result();

            result_str = jsonString(res);

            end_res = "OK";
        }
        catch(Exception e) {
            result_str = null;
            err[0] = e.getMessage();

            end_res = "FAIL";
            e.printStackTrace();
        }
    }

    /** Returns the command result
     *
     * @return String
     */
    public String result() {
        String out = result_str;
        result_str = null;
        return out;
    }

}
