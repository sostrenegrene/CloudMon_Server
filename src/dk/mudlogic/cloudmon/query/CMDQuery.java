package dk.mudlogic.cloudmon.query;

import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;

import java.util.ArrayList;

/**
 * Created by soren.pedersen on 06-07-2016.
 */
public class CMDQuery implements Query_Process {

    private LogTracer log = new LogFactory().tracer();

    private String pHost;
    private String pOptions;

    private String result_str = null;


    public CMDQuery(String pHost,String pOptions) {
        this.pHost = pHost;
        this.pOptions = pOptions;
    }

    /** Creates a JS array with each line from
     * return output as rows
     *
     * @param list String[]
     * @return String | JS array
     */
    private String toJSON(String[] list) {

        JSONArray a = new JSONArray();
        for(int i=0; i<list.length; i++) {
            String s = "\\\"" + list[i] + "\\\"";
            a.add( s );
        }

        return a.toJSONString();
    }

    /** Executes the command
     *
     */
    @Override
    public void start() {
        String out = "";
        ArrayList<String> result_list = new ArrayList<>();

        try {
            String[] s = new Execute(this.pHost, this.pOptions).result();
            result_str = toJSON(s);//out;
        }
        catch(Exception e) {
            log.error("Command line failed");
        }
    }

    /** Returns the command result
     *
     * @return
     */
    @Override
    public String result() {
        String out = result_str;
        result_str = null;
        return out;
    }

}
