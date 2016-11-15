package dk.mudlogic.cloudmon.dbstore.store;


import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 05-07-2016.
 */
public class DB_ProcessReturnData {

    private LogTracer log = new LogFactory().tracer();

    private MSSql sql;
    private ProcessStatusHandler psh;

    public DB_ProcessReturnData(MSSql sql) {
        log.setTracerTitle(DB_ProcessReturnData.class);

        this.sql = sql;
        this.psh = new ProcessStatusHandler(sql);
    }

    public void store(int client_id,int group_id,int cmd_id,String data,String errors) {


        String query = "INSERT INTO cloudmon_process_return_data " +
                "(timestamp,client_id,group_id,command_id,result_data,result_errors) " +
                "VALUES (GETDATE(),'"+client_id+"','"+group_id+"','"+cmd_id+"','"+data+"','"+errors+"')";

        try {
            sql.query(query);

        }
        catch(Exception e) {
            log.error(e.getMessage());
            log.trace(query);
            //e.printStackTrace();
        }
    }

    public boolean status(int client_id,int command_id,String status,int result_hash) {
        return this.psh.status(client_id,command_id,status,result_hash);
    }

    public String changeReason() {
        return this.psh.getChange_reason();
    }

}
