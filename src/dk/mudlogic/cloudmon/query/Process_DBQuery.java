package dk.mudlogic.cloudmon.query;

import com.microsoft.sqlserver.jdbc.SQLServerException;
import dk.mudlogic.cloudmon.client_v2.v2ProcessCommand;
import dk.mudlogic.cloudmon.store.DB_ProcessReturnData;
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
    private ArrayList<String> errors = new ArrayList<>();

    private boolean status_ok;
    private String result;

    public Process_DBQuery(DB_ProcessReturnData prd,v2ProcessCommand pTable) {
        log.setTracerTitle(Process_DBQuery.class);

        this.returnData = prd;
        this.pTable = pTable;
        this.status_ok = true;

        connect();
        process();
        finish();
    }

    public boolean isOK() {
        return status_ok;
    }

    public v2ProcessCommand getCommand() {
        return this.pTable;
    }

    private void connect() {
        try {
            this.status_ok = true;

            String host = pTable.get_str("client_url") + "\\" + pTable.get_str("host_name");
            this.sql = new MSSql(host,pTable.get_str("username"),pTable.get_str("password"),pTable.get_str("database_name"));

            sql.connect();
        } catch (SQLException e) {
            this.status_ok = false;

            errors.add( e.getMessage() );
            //e.printStackTrace();
        }

    }

    private void process() {

        try {
            result = new DBQuery(sql,pTable.get_str("query_string")).result();

            this.status_ok = true;
        } catch (SQLException e) {
            this.status_ok = false;

            errors.add( e.getMessage() );
           // log.error(e.getMessage());
            //e.printStackTrace();
        }

    }

    private void finish() {
        String[] err_list = errors.toArray(new String[errors.size()]);

        //Save log entry
        if ( ( result != null ) || (err_list.length >= 1) ) {
            save(pTable, result, err_list);
            this.status_ok = false;

            //Update status to error
            this.returnData.status( pTable.get_int("client_id"),pTable.get_int("id"), "FAIL" );
        }
        //If no result and no errors
        //Update status to ok
        else {
            this.status_ok = true;
            this.returnData.status( pTable.get_int("client_id"),pTable.get_int("id"), "OK" );
        }
    }

    private void save(v2ProcessCommand pTable,String result,String[] errors) {

        String err_str = jsonArray(errors);

        int client_id   = pTable.get_int("client_id");
        int group_id    = pTable.get_int("command_group_id");
        int cmd_id      = pTable.get_int("id");

        this.returnData.store(client_id,group_id,cmd_id,result,err_str);
    }

    private String addslashes(String s) {
        //s = s.replace("\"","\\\"");
        s = s.replace("\'","\"");

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

}
