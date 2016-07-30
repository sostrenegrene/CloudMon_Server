package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.ServerGlobalData;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.query.CMDQuery;
import dk.mudlogic.scripts.ScriptManager;
import dk.mudlogic.scripts.ScriptResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 20-07-2016.
 */
public class Process_CMDQuery {

    private LogTracer log = new LogFactory().tracer();
    private v2ProcessCommand pTable;
    private DB_ProcessReturnData returnData;

    private String result_str;
    private boolean failed = false;

    public Process_CMDQuery(DB_ProcessReturnData prd,v2ProcessCommand pTable) {
        //log.setTracerTitle(Process_CMDQuery.class);

        this.pTable = pTable;
        this.returnData = prd;

        connect();
        process();
    }

    private void connect() {
        String query = pTable.get_str("query_string");
        String hostname = pTable.get_str("host_name");

        result_str = new CMDQuery(hostname,query).result();

        //log.trace(query + " " + result_str);
    }

    private void process() {

        //Run any parser scripts for the process
        if (result_str != null) {
            //Run script manager with result string
            String path = ServerGlobalData.MAIN_CONFIG.group("server").get("install_path")+"parsers\\console\\"+pTable.get_str("parser_script");
            ScriptManager sm = new ScriptManager(path);
            ScriptResult result = sm.parse(result_str);

            finish(result);
        }

    }

    private void finish(ScriptResult result) {

        if (result.has_error) {
            this.failed = true;

            result_str = result.result;
        }
        else {
            this.failed = false;

            result_str = result.result;
        }

        console_output(this.pTable,this.failed);
        save(pTable,result.result,result.error_message);
        this.returnData.status( pTable.get_int("client_id"),pTable.get_int("id"), Boolean.toString( this.failed ) );
    }

    public boolean hasFailed() {
        return this.failed;
    }

    private void save(v2ProcessCommand pTable,String result,String errors) {

        int client_id   = pTable.get_int("client_id");
        int group_id    = pTable.get_int("command_group_id");
        int cmd_id      = pTable.get_int("id");

        this.returnData.store(client_id,group_id,cmd_id,result,errors);
    }

    private void console_output(v2ProcessCommand pTable, boolean status) {
        String status_str = "[ID:" + pTable.get_str("id")+"]";
        status_str += "[" + pTable.get_str("client_name")+"]";
        status_str += "[" + pTable.get_str("group_name")+"]";
        status_str += "[" + pTable.get_str("process_name")+"]";
        //status_str += "[Type: " + pTable.get_str("process_type")+"]";

        //String s = status_str + "- Fail: "+status;

        if (status) { log.warning(status_str); }
        else { log.trace(status_str); }
    }

}
