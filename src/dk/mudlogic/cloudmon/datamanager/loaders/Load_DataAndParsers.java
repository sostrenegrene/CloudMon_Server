package dk.mudlogic.cloudmon.datamanager.loaders;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

import java.sql.SQLException;

/**
 * Created by soren.pedersen on 14-07-2016.
 */
public class Load_DataAndParsers {

    private LogTracer log = new LogFactory().tracer();

    private SQLResult data;
    private SQLResult parsers;

    private MSSql sql;

    public Load_DataAndParsers(MSSql sql) {
        this.sql = sql;

        try {
            data = sql.query(query_data());
            parsers = sql.query(query_parsers());

            log.trace(data.toJSON());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SQLResult getData() {
        return data;
    }

    public SQLResult getParsers() {
        return parsers;
    }

    /** Get all unparsed rows
     *
     * @return String
     */
    private String query_data() {
        String q = "SELECT * FROM cloudmon_process_return_data";

        return q;
    }

    /** Get parsers for unparsed rows
     *
     * @return String
     */
    private String query_parsers() {

        String q = "SELECT cpc.id,cpc.parser_script FROM cloudmon_process_commands AS cpc WHERE cpc.parser_script != 'NULL'";

        return q;
    }



}
