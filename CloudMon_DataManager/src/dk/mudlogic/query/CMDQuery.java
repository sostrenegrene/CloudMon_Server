package dk.mudlogic.query;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;

/**
 * Created by soren.pedersen on 06-07-2016.
 */
public class CMDQuery {

    private LogTracer log = new LogFactory().tracer();

    private String pHost;
    private String pOptions;

    private String result_str = null;


    public CMDQuery(String pHost,String pOptions) {
        log.setTracerTitle(CMDQuery.class);
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
        String[] res;
        try {
            res = new Execute(this.pHost, this.pOptions).result();

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
