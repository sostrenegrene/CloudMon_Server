package dk.mudlogic.cloudmon.dbstore.store;

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

    private final int NO_NEW_STATUS = 0;
    private final int NEW_STATUS = 1;
    private final int NEW_DATA = 2;

    private LogTracer log = new LogFactory().tracer();

    private MSSql sql;
    private String change_reason = "";

    public ProcessStatusHandler(MSSql sql) {
        this.sql = sql;
    }

    private SQLResult get_last_change(int command_id) throws SQLException {
        SQLResult result;

        String query = "SELECT TOP 1 * FROM cloudmon_process_status_changelog WHERE command_id = '" + command_id + "' ORDER BY id DESC";
        result = sql.query(query);

        return result;
    }

    private int has_changed(int command_id,String current_status,int result_hash) {

        try {
            SQLResult result = get_last_change(command_id);

            //Get last changelog row
            Hashtable ht = (Hashtable) result.getRows().get(0);
            //Hash of last status from changelog
            int last_status_hash = ht.get("failed_status").hashCode();
            //Hash from last result data
            int last_result_hash = Integer.parseInt( (String) ht.get("result_hash") );
            //Hash from current status
            int current_status_hash = current_status.hashCode();

            //Status has changed if last and current is not equal
            if ( last_status_hash != current_status_hash ) {
                log.warning("Status changed [NEW STATUS]");

                change_reason = command_id+": New Status";
                return NEW_STATUS;
            }
            //Status has changed if result_hash is not 0 and not equal to last_result_hash
            else if ( ( Boolean.parseBoolean(current_status) == true ) && (result_hash != 0) && (result_hash != last_result_hash)) {
                log.warning("Status changed [NEW RESULT DATA]");

                change_reason = command_id+": Change in data";
                return NEW_DATA;
            }
            //Else status has not changed
            else {

                change_reason = "";
                return NO_NEW_STATUS;
            }

        } catch (SQLException e) {
            //e.printStackTrace();

            change_reason = command_id+": " + e.getSQLState();
            return NEW_STATUS;
        } catch (Exception e) {
            //2e.printStackTrace();

            change_reason = command_id+": " + e.getLocalizedMessage();
            return NEW_STATUS;
        }
    }

    private String getStatusString(String status) {
        if (status.equals("true")) { return "FAILED"; }
        else if (status.equals("false")) { return "OK"; }
        else { return null; }
    }

    public String getChange_reason() {
        return change_reason;
    }

    public boolean status(int client_id,int command_id,String status,int result_hash) {
        String query;

        //If status has changed, setup query to update status and add new return data id
        int changed = has_changed(command_id,status,result_hash);
        //if ( (changed == NEW_STATUS) || (changed == NEW_DATA) ) {
        if ( (changed == NEW_STATUS) ) {
            query = "UPDATE cloudmon_process_commands SET status = '"+getStatusString(status)+"' WHERE id = '"+command_id+"'";
            query += "INSERT INTO cloudmon_process_status_changelog (timestamp,client_id,command_id,status,failed_status,result_hash,start_return_data_id) " +
                     "VALUES (GETDATE()," +
                            "'"+client_id+"'," +
                            "'"+command_id+"'," +
                            "'"+getStatusString(status)+"'," +
                            "'"+status+"'," +
                            "'"+result_hash+"'," +
                            "(SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id))";
        }
        //Update the latest return data id to last status change
        else {
            query = "UPDATE cloudmon_process_status_changelog " +
                    "SET end_return_data_id = (SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id) " +
                    "WHERE id = (SELECT MAX(cl.id) FROM cloudmon_process_status_changelog AS cl WHERE cl.command_id = '"+command_id+"')";
        }

        //Test
        //Run query
        try {
            log.trace(query);
            sql.query(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return (changed == NEW_STATUS);
    }
}
