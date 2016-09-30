package dk.mudlogic.cloudmon.client_v3.query;

import dk.mudlogic.cloudmon.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.client_v3.client.v3CommandResult;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.json.ToJSON;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 11-09-2016.
 */
public class v3Query_Database {

    private LogTracer log = new LogFactory().tracer();

    private v3Command command;

    private SQLResult result;
    private MSSql sql;

    public v3Query_Database(v3Command command) {
        this.command = command;

        connect();
    }

    private void connect() {
        String query    = command.get_str("query_string");
        String hostname = command.get_str("host_name");
        String username = command.get_str("username");
        String password = command.get_str("password");
        String database = command.get_str("database_name");

        this.sql = new MSSql(hostname,username,password,database);

        try {
            this.sql.connect();
            result = this.sql.query( query );

            String jstring = getJson(result);
            log.trace("DB RESULT: " + jstring );

            this.command.setResult( new v3CommandResult( jstring ) );
        } catch (SQLException e) {
            e.printStackTrace();

            result = null;
        }


    }

    /** Converts the SQLResult data to JSON object string
     *
     * @param res
     * @return
     */
    private String getJson(SQLResult res) {

        Hashtable[] htables = (Hashtable[]) res.getRows().toArray(new Hashtable[res.getRows().size()]);
        return new ToJSON().json_string(htables);

    }

    public v3Command getCommand() {
        return command;
    }

}
