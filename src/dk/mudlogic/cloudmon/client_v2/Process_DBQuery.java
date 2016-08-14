package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.ServerGlobalData;
import dk.mudlogic.cloudmon.store.SendMail;
import dk.mudlogic.query.DBQuery;
import dk.mudlogic.scripts.ScriptManager;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
import dk.mudlogic.scripts.ScriptResult;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by soren.pedersen on 19-07-2016.
 */
public class Process_DBQuery {

    private LogTracer log = new LogFactory().tracer();
    private MSSql sql;
    private v2ProcessCommand pTable;
    private DB_ProcessReturnData returnData;
    private GroupConfig MAIN_CONFIG;
    private ArrayList<String> errors = new ArrayList<>();

    private boolean failed;
    private String result;

    public Process_DBQuery(GroupConfig main_config,DB_ProcessReturnData prd,v2ProcessCommand pTable) {
        //log.setTracerTitle(Process_DBQuery.class);

        this.returnData = prd;
        this.MAIN_CONFIG = main_config;
        this.pTable = pTable;
        this.failed = false;

        connect();
        process();
        finish();
    }

    public boolean hasFailed() {
        return failed;
    }

    public v2ProcessCommand getCommand() {
        return this.pTable;
    }

    private void connect() {
        try {
            this.failed = false;

            String host = pTable.get_str("host_name");
            String username = pTable.get_str("username");
            String password = pTable.get_str("password");
            String db_name = pTable.get_str("database_name");
            this.sql = new MSSql(host,username,password,db_name);

            log.trace(host +"-"+username+"-"+password+"-"+db_name);

            sql.connect();
        } catch (Exception e) {
            this.failed = true;
            errors.add( e.getMessage() );
            //e.printStackTrace();
        }

    }

    private void process() {

        try {
            result = new DBQuery(sql,pTable.get_str("query_string")).result();

            //Run any parser scripts for the process
            if (result != null) {
                this.failed = true;

                if (!pTable.get_str("parser_script").equals(null)) {
                    //Run script manager with result string

                    String path = ServerGlobalData.MAIN_CONFIG.group("server").get("install_path")+"parsers\\";
                    String file = "database\\"+pTable.get_str("parser_script");
                    ScriptManager sm = new ScriptManager(path,file);
                    ScriptResult res = sm.parse(result);

                    if (res.has_error) {
                        errors.add(res.error_message);
                    }
                    else {
                        result = res.result;
                    }

                }

            }

        } catch (SQLException e) {
            this.failed = true;
            errors.add( e.getMessage() );
           log.error(e.getMessage());
            //e.printStackTrace();

        } catch(NullPointerException ne) {
            this.failed = true;
            errors.add(ne.getMessage());
            log.error(ne.getMessage());
        }

    }

    /** Get result of process and update status and save if needed
     *
     */
    private void finish() {
        String[] err_list = errors.toArray(new String[errors.size()]);

        //Save log entry
        if ( ( result != null ) || (err_list.length >= 1) ) {
            save(pTable, result, err_list);
        }

        console_output(pTable, this.failed );

        //Update status
        //status() returns true if changed
        if ( this.returnData.status( pTable.get_int("client_id"),pTable.get_int("id"), Boolean.toString(this.failed) ) ) {
            new SendMail(this.MAIN_CONFIG,"CloudMon-NAV",pTable,this.failed);
        }
    }

    /** Saves new process data
     *
     * @param pTable v2ProcessCommand
     * @param result String
     * @param errors String[]
     */
    private void save(v2ProcessCommand pTable,String result,String[] errors) {

        String err_str = jsonArray(errors);
        result = addslashes(result);
        int client_id   = pTable.get_int("client_id");
        int group_id    = pTable.get_int("command_group_id");
        int cmd_id      = pTable.get_int("id");

        this.returnData.store(client_id,group_id,cmd_id,result,err_str);
    }

    private String addslashes(String s) {
        if (s != null) { s = s.replace("\'","\""); }

        return s;
    }

    private String jsonArray(String[] errors) {
        JSONArray a = new JSONArray();
        for (int i=0; i<errors.length; i++) {
            a.add( addslashes( errors[i] ) );

            //log.error(errors[i]);
        }

        return a.toJSONString();
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
