package dk.mudlogic.cloudmon.client.client_v3.query;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Command;
import dk.mudlogic.cloudmon.client.client_v3.client.v3CommandResult;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.json.ToJSON;
import org.apache.commons.lang3.StringEscapeUtils;

import java.sql.SQLException;
import java.util.Hashtable;

import static org.apache.commons.lang3.StringEscapeUtils.*;

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

            //log.warning(">>>" + jstring);

            this.command.setResult( new v3CommandResult( jstring ) );
        } catch (SQLException e) {
            //e.printStackTrace();
            log.error( e.getMessage() );

            result = null;
        }

    }

    /** Converts the SQLResult data to JSON object string
     *
     * @param res
     * @return
     */
    private String getJson(SQLResult res) {
        //AUT-STMTPOST - 5 af 6 opgørelser bogført. The Bogføringsopsætning does not exist. \r\rIdentification fields and values:\r\rVirksomhedsbogføringsgruppe='INTERCOMP' Produktbogføringsgruppe='VG ITEMS'\r\r\r\\Programmet returnerede en fejl

        String out = "";

        Hashtable[] htables = (Hashtable[]) res.getRows().toArray(new Hashtable[res.getRows().size()]);

        for (int i=0; i<htables.length; i++) {
            String msg = (String) htables[i].get("Error Message");

            //Remove escapes slashes(\) from string first
            msg = unescapeJava( msg );

            //Replace all ' with "
            msg = msg.replaceAll("\'","\"");

            htables[i].replace("Error Message",msg);

            //log.warning("With Escape: " + msg);
        }


        if (htables.length > 0) { out = new ToJSON().json_string(htables); }
        //else { out = "No return data from query"; }

        return out;

    }

    public v3Command getCommand() {
        return command;
    }

}
