package dk.mudlogic.cloudmon.returndata;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 19-09-2016.
 */
public class Returndata {

    private LogTracer log = new LogFactory().tracer();

    private MSSql sql;

    public Returndata(MSSql sql) {
        log.setTracerTitle(Returndata.class);

        this.sql = sql;
    }

    public void save(int client_id,int group_id,int cmd_id,String data,String errors) {

        String query = "INSERT INTO cloudmon_process_return_data " +
                        "(timestamp,client_id,group_id,command_id,result_data,result_errors) " +
                        "VALUES (GETDATE(),'"+client_id+"','"+group_id+"','"+cmd_id+"','"+data+"','"+errors+"')";

        try {
            sql.query(query);

            //log.trace("Saved " + client_id+":"+group_id+":"+cmd_id);
        }
        catch(Exception e) {
            log.error(e.getMessage());

            //log.trace(query);
            e.printStackTrace();
        }
    }

}
