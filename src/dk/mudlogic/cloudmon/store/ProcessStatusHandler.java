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

    private int command_id = 0;
    private MSSql sql;

    public ProcessStatusHandler(MSSql sql) {
        this.sql = sql;
    }

    private boolean has_changed(String current_status) {
        String query = "SELECT TOP 1 * FROM cloudmon_process_status_changelog WHERE command_id = '" + command_id + "' ORDER BY id DESC";
        try {
            SQLResult result = sql.query(query);
            //log.trace(query);
            //log.trace(result.toJSON());

            Hashtable ht = (Hashtable) result.getRows().get(0);
            int last = ht.get("status").hashCode();
            int current = current_status.hashCode();

            if ( last != current ) {
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

    public void status(int client_id,int command_id,String status) {
        this.command_id = command_id;
        String query;

        //If status has changed, setup query to update status and add new return data id
        if (has_changed(status)) {
            query = "UPDATE cloudmon_process_commands SET status = '"+status+"' WHERE id = '"+command_id+"';";
            query += "INSERT INTO cloudmon_process_status_changelog (timestamp,client_id,command_id,status,start_return_data_id) " +
                     "VALUES (GETDATE(),'"+client_id+"','"+command_id+"','"+status+"', (SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id) );";
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

    }
}
