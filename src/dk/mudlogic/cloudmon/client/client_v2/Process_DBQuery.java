package dk.mudlogic.cloudmon.client.client_v2;

import dk.mudlogic.cloudmon.dbstore.store.DB_ProcessReturnData;
import dk.mudlogic.cloudmon.dbstore.store.SendMail;
import dk.mudlogic.cloudmon.datamanager.mail.MailMan;
import dk.mudlogic.cloudmon.datamanager.query.DBQuery;
import dk.mudlogic.cloudmon.datamanager.scripts.ScriptManager;
import dk.mudlogic.cloudmon.datamanager.scripts.ScriptResult;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.time.TimeHandler;
import org.json.simple.JSONArray;

import java.sql.SQLException;
import java.util.ArrayList;

/** @deprecated
 * Created by soren.pedersen on 19-07-2016.
 */
public class Process_DBQuery {

    private LogTracer log = new LogFactory().tracer();
    private MSSql sql;
    private v2ProcessCommand pTable;
    private DB_ProcessReturnData returnData;
    private GroupConfig MAIN_CONFIG;
    private MailMan mailman;
    private ArrayList<String> errors = new ArrayList<>();

    private boolean failed;
    private String result;

    public Process_DBQuery(GroupConfig main_config, DB_ProcessReturnData prd, v2ProcessCommand pTable, MailMan mailman) {
        //log.setTracerTitle(Process_DBQuery.class);

        this.returnData = prd;
        this.MAIN_CONFIG = main_config;
        this.pTable = pTable;
        this.failed = false;
        this.mailman = mailman;

        connect();
        process();
        finish();
    }

    public v2ProcessCommand getpTable() {
        return pTable;
    }

    public int result_hash() {
        try { return result.hashCode(); }
        catch (Exception e) { return 0; }
    }

    private boolean hasFailed() {
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

                    String path = MAIN_CONFIG.group("server").get("install_path")+"parsers\\";
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
            e.printStackTrace();

        } catch(NullPointerException ne) {
            this.failed = true;
            errors.add(ne.getMessage());
            log.error(ne.getMessage());

            ne.printStackTrace();
        }

    }

    /** Get result of process and update status and save if needed
     *
     */
    private void finish() {
        String[] err_list = errors.toArray(new String[errors.size()]);

        //log.trace(result);

        //Save log entry
        if ( ( result != null ) || (err_list.length >= 1) ) {
            save(result, err_list);
        }

        console_output(this.failed );

        //Update status
        //status() returns true if changed
        if ( this.returnData.status( pTable.get_int("client_id"),pTable.get_int("id"), Boolean.toString(this.failed),result_hash() ) ) {

            //Make time handler
            TimeHandler t = new TimeHandler();
            //Get time diff in seconds from last notify to now
            int time_diff = t.unixTimeDiff( pTable.lastNotify() );

            if (pTable.lastNotify() != 0) {
                //if time diff is >= to mail interval
                if (pTable.get_int("mail_interval") <= time_diff ) {

                    //Send mail
                   // new SendMail(this.MAIN_CONFIG, "CloudMon-NAV", pTable, this.failed, this.returnData.changeReason());

                    //Send mail
                    SendMail mail = new SendMail(this.MAIN_CONFIG,this.mailman);
                    mail.send(pTable.get_str("client_name"),pTable.get_str("process_name"),pTable.get_str("process_type"),this.failed,this.returnData.changeReason());

                    //Update last notification time
                    pTable.lastNotify(t.unixTime());
                }
            }
            else {
                //Update last notification time
                pTable.lastNotify(t.unixTime());
            }

            //log.trace(pTable.get_str("process_name") + " mail interval " + pTable.get_int("mail_interval") + "s current time " + time_diff);
        }
    }

    /** Saves new process data
     *
     * @param result String
     * @param errors String[]
     */
    private void save(String result,String[] errors) {

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

    private void console_output( boolean status) {
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
