package dk.mudlogic;

import dk.mudlogic.loaders.Load_DataAndParsers;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.parse.Parser_Javascript;

import java.util.Hashtable;

/**
 * Created by soren.pedersen on 20-07-2016.
 */
public class ScriptManager {

    private LogTracer log = new LogFactory().tracer();

    private String script_file;

    /** Setup javascript manager
     *
     *
     * @param script_file String
     */
    public ScriptManager(String script_file) {
        this.script_file = script_file;
    }

    private Hashtable parser(Hashtable[] parser_list,Hashtable h) {

        for (int i=0; i<parser_list.length; i++) {
            int hID = Integer.parseInt( (String) h.get("command_id") );
            int pID = Integer.parseInt( (String) parser_list[i].get("id") );
            String script = (String) parser_list[i].get("parser_script");
            script = "C:\\Program Files\\CloudMon\\parsers\\database\\" + script;

            String data = (String) h.get("result_data");
            //data = data.replace("\"","\\\"");

            if (hID == pID) {
                log.trace(h.get("id")+": " + h.get("result_data"));

                Parser_Javascript pj = new Parser_Javascript(script);
                String res = pj.parse("toJAVA",data);
                //parser_list[i].replace("result",res);

                log.trace(pj.parse("getSomething") + "\n" + res);

            }

        }

        return h;
    }

}
