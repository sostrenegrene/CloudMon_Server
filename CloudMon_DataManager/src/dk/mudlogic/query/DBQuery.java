package dk.mudlogic.query;

import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.database.SQLResult;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 05-07-2016.
 */
public class DBQuery {

    private LogTracer log = new LogFactory().tracer();

    private SQLResult res;
    private MSSql sql;
    private String query;

    private String result = null;

    public DBQuery(MSSql sql, String query) {
        this.sql = sql;
        this.query = query;
    }

    private String json(  ) {

        String out = null;

        if ( (res != null) && (res.getRows().size() > 0) ) {
            ArrayList rows = res.getRows();
            Hashtable[] tables = (Hashtable[]) rows.toArray( new Hashtable[rows.size()] );

            JSONArray jarr = new JSONArray();

            for (int i=0; i<tables.length; i++) {
                Hashtable ht = tables[i];
                String[] keys = (String[]) ht.keySet().toArray(new String[ht.keySet().size()]);

                JSONObject jobj = new JSONObject();

                for (int j=0; j<keys.length; j++) {
                    jobj.put(keys[j],ht.get(keys[j]));
                }

                jarr.add(jobj);
            }

            out = jarr.toJSONString();
            res.close();
        }

        return out;
    }

    public void start() throws SQLException {
        //log.warning(query);

        res = sql.query(query);
        result = json();
    }

    public String result() throws SQLException {
        start();

        String out = result;
        result = null;
        return out;
    }
}
