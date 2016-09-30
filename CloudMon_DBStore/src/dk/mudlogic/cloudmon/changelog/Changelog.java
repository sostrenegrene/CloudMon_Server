package dk.mudlogic.cloudmon.changelog;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 19-09-2016.
 */
public class Changelog {

    public static final int NO_NEW_STATUS = 0;
    public static final int NEW_STATUS = 1;
    public static final int NEW_DATA = 2;

    private LogTracer log = new LogFactory().tracer();

    private MSSql sql;
    private String change_reason = "";

    public Changelog(MSSql sql) {
        this.sql = sql;
    }

    private SQLResult get_last_change(int command_id) throws SQLException {
        SQLResult result;

        String query = "SELECT TOP 1 * FROM cloudmon_process_status_changelog WHERE command_id = '" + command_id + "' ORDER BY id DESC";
        result = sql.query(query);

        return result;
    }

    public int has_changed(int command_id,String current_status,int result_hash) {

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

                change_reason = ": New Status";
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

    private void insert_status(int client_id,int command_id,String status,int result_hash) {
        String query;

        query = "INSERT INTO cloudmon_process_status_changelog (timestamp,client_id,command_id,status,failed_status,result_hash,start_return_data_id) " +
                "VALUES (GETDATE()," +
                "'"+client_id+"'," +
                "'"+command_id+"'," +
                "'"+status+"'," +
                "'"+status+"'," +
                "'"+result_hash+"'," +
                "(SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id));";

        //Run query
        try {
            //log.trace(query);
            sql.query(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void update_status(int command_id) {

        String query = "UPDATE cloudmon_process_status_changelog " +
                        "SET end_return_data_id = (SELECT MAX(id) FROM cloudmon_process_return_data WHERE command_id = '"+command_id+"' GROUP BY command_id) " +
                        "WHERE id = (SELECT MAX(cl.id) FROM cloudmon_process_status_changelog AS cl WHERE cl.command_id = '"+command_id+"')";

        //Run query
        try {
            //log.trace(query);
            sql.query(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Updates the command's status if anything has changed
     *
     * @param client_id int
     * @param command_id int
     * @param status String
     * @param result_hash int
     * @return boolean
     */

    public boolean status(int client_id,int command_id,String status,int result_hash,int changelog_type) {
        boolean out = false;

        if ( changelog_type == Changelog.NEW_STATUS ) {
            insert_status(client_id,command_id,status,result_hash);

            out = true;
        }

        if ( changelog_type == Changelog.NEW_DATA ) {
            update_status(command_id);

            out = true;
        }

        return out;
    }


    /*
    public boolean status(int client_id,int command_id,String status,int result_hash) {
        boolean out = false;

        if ( has_changed(command_id,status,result_hash) == Changelog.NEW_STATUS ) {
            insert_status(client_id,command_id,status,result_hash);

            out = true;
        }

        if ( has_changed(command_id,status,result_hash) == Changelog.NEW_DATA ) {
            update_status(command_id);

            out = true;
        }

        return out;
    }
    */
}
