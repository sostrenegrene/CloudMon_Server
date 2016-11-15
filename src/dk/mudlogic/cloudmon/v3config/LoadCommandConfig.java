package dk.mudlogic.cloudmon.v3config;

import dk.mudlogic.Main2;
import dk.mudlogic.cloudmon.client.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.client.client_v3.client.v3CommandGroup;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 07-10-2016.
 */
public class LoadCommandConfig {

    private LogTracer log = new LogFactory().tracer();

    private MSSql SQL;
    private SQLResult result;
    private ArrayList<v3Command> command_list;

    public LoadCommandConfig(MSSql sql)  {
        log.setTracerTitle(this.getClass());

        this.SQL = sql;
    }

    public void load() {
        try {
            //Run query and get result
            result = SQL.query(query_commands());

            command_list = make_Commands(result.getRows());
        }
        catch(SQLException e) {
            log.error(e.getMessage());
            //e.printStackTrace();
        }

    }

    public ArrayList getRows() {
        return result.getRows();
    }

    public ArrayList<v3Command> getList() {
        return command_list;
    }

    public v3Command[] toArray() {
        return command_list.toArray( new v3Command[ command_list.size() ] );
    }


    public v3CommandGroup add_Commands(v3CommandGroup group) {
        //Generate groups
        for (int i=0; i<getList().size(); i++) {
            v3Command command_table = getList().get(i);

            int groupID = command_table.getGroupID();
            if ( groupID == group.getID() ) {

                group.add_Command( command_table );
            }

        }

        return group;
    }

    private ArrayList<v3Command> make_Commands(ArrayList<Hashtable> rows) {
        ArrayList<v3Command> out = new ArrayList<>();

        //Generate groups
        for (int i=0; i<rows.size(); i++) {
            Hashtable command_table = rows.get(i);

            command_table.put("install_path", Main2.MAIN_CONFIG.group("server").get("install_path").getValue());
            out.add( new v3Command(command_table) );
        }

        return out;
    }

    /** Query string
     *
     * @return String | Query
     */
    private String query_commands() {
        String s = "SELECT cpc.*,ccg.group_name AS group_name,ccg.id AS group_id,clients.client_name,clients.client_description,clients.id AS client_id,clients.url AS client_url " +
                "FROM cloudmon_process_commands AS cpc " +
                "JOIN cloudmon_process_command_groups AS ccg ON ccg.id = cpc.command_group_id " +
                "JOIN cloudmon_process_clients AS clients ON ccg.client_id = clients.id " +
                "WHERE is_active = 1";

        return s;
    }

}
