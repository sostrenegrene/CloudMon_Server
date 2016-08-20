package dk.mudlogic.cloudmon.client_v2;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 06-07-2016.
 *
 * Load config for all process commands from database
 * and generate and v2ProcessConfig[] array
 *
 */
public class Load_v2ProcessConfig {

    private LogTracer log = new LogFactory(Load_v2ProcessConfig.class.getName()).tracer();
    private SQLResult result;

    private v2ProcessConfig[] config;

    /** Loads settings for client processes
     *
     * @param sql MSSql
     */
    public Load_v2ProcessConfig(MSSql sql) {
        log.setTracerTitle(Load_v2ProcessConfig.class);

        try {
            //Run query and get result
            result = sql.query(query_commands());
        }
        catch(SQLException e) {
            log.strbuild_add(LogTracer.ERROR,e.getMessage());
        }

        //Process result and generate config for clients
        make_ClientConfig();

        //log.trace( result.toJSON() );
    }

    /** Query string
     *
     * @return String | Query
     */
    private String query_commands() {
        String s = "SELECT cpc.*,ccg.group_name AS group_name,ccg.id AS group_id,clients.client_name,clients.id AS client_id,clients.url AS client_url " +
                "FROM cloudmon_process_commands AS cpc " +
                "JOIN cloudmon_process_command_groups AS ccg ON ccg.id = cpc.command_group_id " +
                "JOIN cloudmon_process_clients AS clients ON ccg.client_id = clients.id " +
                "WHERE is_active = 1";

        return s;
    }

    /** Creates a command group list
     * Setup:
     * Hashtable<Command set name,ArrayList>
     *     ( ArrayList< Hashtable<Command set values> > )
     */
    private void make_ClientConfig() {
        log.strbuild_add("Generating command groups");

        //Get rows from sql result
        ArrayList rows = result.getRows();

        Hashtable<String,v2ProcessConfig> tmp = new Hashtable<>();

        //Get next row_table
        for (int i=0; i<rows.size(); i++) {
            Hashtable row_table = (Hashtable) rows.get(i);

            //Get name, group name and url of client process
            String client_name = (String) row_table.get("client_name");
            String group_name = (String) row_table.get("group_name");
            String url = (String) row_table.get("url");

            //Check if config group exist
            if (tmp.containsKey(group_name)) {

                //Fetch config group
                v2ProcessConfig clc = tmp.get(group_name);
                //Add row table to group
                clc.add_command(row_table);
                //Update group in tmp
                tmp.replace(group_name,clc);

            }
            //If group does not exist
            else {

                //Create new config group with client name, group name and client url
                v2ProcessConfig clc = new v2ProcessConfig(client_name,url,group_name);
                //Add row table to group
                clc.add_command(row_table);
                //Add group to tmp
                tmp.put(group_name,clc);

            }

        }

        config = tmp.values().toArray(new v2ProcessConfig[tmp.values().size()]);

        log.strbuild_add("Total " + config.length);
        log.trace( log.strbuild_build() );

    }

    public v2ProcessConfig[] getConfig() {
        return config;
    }

}
