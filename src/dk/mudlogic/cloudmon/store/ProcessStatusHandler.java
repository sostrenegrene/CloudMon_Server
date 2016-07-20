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
            e.printStackTrace();

            return true;
        } catch (Exception e) {
            e.printStackTrace();

            return true;
        }
    }

    public void status(int client_id,int command_id,String status) {
        this.command_id = command_id;

        if (has_changed(status)) {
            String query = "INSERT INTO cloudmon_process_status_changelog (timestamp,client_id,command_id,status) VALUES (GETDATE(),'"+client_id+"','"+command_id+"','"+status+"');" +
                    "UPDATE cloudmon_process_commands SET status = '"+status+"' WHERE id = '"+command_id+"'";
            try {
                sql.query(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
