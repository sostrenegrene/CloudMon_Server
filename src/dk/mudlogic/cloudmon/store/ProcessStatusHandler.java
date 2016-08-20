package dk.mudlogic.cloudmon.store;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 13-07-2016.
 */
public class ProcessStatusHandler {

    private LogTracer log = new LogFactory().tracer();

    private MSSql sql;

    public ProcessStatusHandler(MSSql sql) {
        this.sql = sql;
    }

    private boolean has_changed(int command_id,String current_status,int result_hash) {
        String query = "SELECT TOP 1 * FROM cloudmon_process_status_changelog WHERE command_id = '" + command_id + "' ORDER BY id DESC";
        try {
            SQLResult result = sql.query(query);
            //log.trace(query);
            //log.trace(result.toJSON());

            Hashtable ht = (Hashtable) result.getRows().get(0);
            int last = ht.get("failed_status").hashCode();
            int last_hash = Integer.parseInt( (String) ht.get("result_hash") );
            int current = current_status.hashCode();

            if ( last != current ) {
                return true;
            }
            else if ((result_hash != 0) && (result_hash != last_hash)) {
                log.warning("Hash no match R:" + result_hash + " L:" + last_hash);
                return true;
            }
            else {
                return false;
            }

        } catch (SQLException e) {
            //e.printStackTrace();

            return true;
        } catch (Exception e) {
            //2e.printStackTrace();

            return true;
        }
    }

    private String getStatusString(String status) {
        if (status.equals("true")) { return "FAILED"; }
        else if (status.equals("false")) { return "OK"; }
        else { return null; }
    }

    public boolean status(int client_id,int command_id,String status,int result_hash) {
        String query;

        //If status has changed, setup query to update status and add new return data id
        boolean changed = has_changed(command_id,status,result_hash);
        if (changed) {
            query = "UPDATE cloudmon_process_commands SET status = '"+getStatusString(status)+"' WHERE id = '"+command_id+"';";
            query += "INSERT INTO cloudmon_process_status_changelog (timestamp,client_id,command_id,status,failed_status,result_hash,start_return_data_id) " +
                     "VALUES (GETDATE()," +
                            "'"+client_id+"'," +
                            "'"+command_id+"'," +
                            "'"+getStatusString(status)+"'," +
                            "'"+status+"'," +
                            "'"+result_hash+"'," +
                            "(SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id));";
        }
        //Update the latest return data id to last status change
        else {
            query = "UPDATE cloudmon_process_status_changelog " +
                    "SET end_return_data_id = (SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id) " +
                    "WHERE id = (SELECT MAX(cl.id) FROM cloudmon_process_status_changelog AS cl WHERE cl.command_id = '"+command_id+"')";
        }

        //Run query
        try {
            //log.trace(query);
            sql.query(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return changed;
    }
}
