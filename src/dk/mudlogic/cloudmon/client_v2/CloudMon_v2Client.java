package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.cloudmon.query.DBQuery;
import dk.mudlogic.cloudmon.query.Process_DBQuery;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.tools.console.Execute;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.parse.ParseJSON;
import org.json.simple.JSONArray;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by soren.pedersen on 10-07-2016.
 */
public class CloudMon_v2Client extends CloudMon_v2Process {

    private LogTracer log = new LogFactory().tracer();

    private DB_ProcessReturnData returnData;

    public CloudMon_v2Client(DB_ProcessReturnData returnData,v2ProcessConfig config) {
        super(config);
        this.returnData = returnData;

        log.setTracerTitle(CloudMon_v2Client.class);

    }

    /** The active content of what is running
     * in the thread in the background
     * This will be call when the thread is activated
     *
     */
    @Override
    void processExecute(v2ProcessCommand process_table) {

        switch(process_table.get_str("process_type")) {
            case "query":
                execDBQuery(process_table);
                break;

            case "command":
                execCommandLine(process_table);
                break;

            default:
                break;
        }

    }

    /** Executes command line process
     *
     * @param pTable Hashtable
     */
    private void execCommandLine(v2ProcessCommand pTable) {
        //log.trace("Exec Command line");

        String result;
        String[] err = new String[1];
        String end_res;
        try {
            String[] res = new Execute(pTable.get_str("host_name"), pTable.get_str("query_string")).result();

            JSONArray a = new JSONArray();
            for (int i=0; i<res.length; i++) {
                String s = res[i];
                a.add(s);
            }

            result = a.toJSONString();
            end_res = "OK";
        }
        catch(Exception e) {
            result = null;
            err[0] = e.getMessage();

            end_res = "FAIL";
            e.printStackTrace();
        }

        save(pTable, result,err);

        console_output(pTable,end_res);
    }

    /** Executes SQL query
     *
     * @param pTable
     */
    private void execDBQuery(v2ProcessCommand pTable) {
        //log.trace("Exec Database query");

        boolean b = new Process_DBQuery(this.returnData,pTable).isOK();

        console_output(pTable,Boolean.toString(b));
    }

    private String jsonResult(String[] errors) {
        JSONArray a = new JSONArray();
        for (int i=0; i<errors.length; i++) {
            a.add( errors[i] );
        }

        return a.toJSONString();
    }

    private void save(v2ProcessCommand pTable,String result,String[] errors) {

        String err_str = jsonResult(errors);

        int client_id   = pTable.get_int("client_id");
        int group_id    = pTable.get_int("command_group_id");
        int cmd_id      = pTable.get_int("id");

        this.returnData.store(client_id,group_id,cmd_id,result,err_str);

    }

    private void console_output(v2ProcessCommand pTable, String status) {
        String status_str = "ID:" + pTable.get_str("id");
        status_str += " Client: " + pTable.get_str("client_name");
        status_str += " Process: " + pTable.get_str("process_name");
        status_str += " Type: " + pTable.get_str("process_type");

        log.trace(status_str + "-"+status);
    }

}
