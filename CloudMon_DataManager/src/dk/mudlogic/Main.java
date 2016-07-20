package dk.mudlogic;

import dk.mudlogic.loaders.Load_DataAndParsers;
import dk.mudlogic.tools.config.GroupConfig;
import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;
import dk.mudlogic.tools.web.parse.Parser_Javascript;

import java.sql.SQLException;
import java.util.Hashtable;

/**
 * Created by soren.pedersen on 14-07-2016.
 */
public class Main {

    static LogTracer log = new LogFactory(Main.class.getName()).tracer();

    public static void main(String[] args) {

        log.setTracerTitle(Main.class);

        GlobalData.MAIN_CONFIG = new GroupConfig("Config_"+Main.class.getSimpleName());

        GlobalData.MAIN_CONFIG.group("database").put("url","localhost\\SQLEXPRESS");
        GlobalData.MAIN_CONFIG.group("database").put("database","cloudmon");
        GlobalData.MAIN_CONFIG.group("database").put("username","cloudmon");
        GlobalData.MAIN_CONFIG.group("database").put("password","Grenes1234");

        GlobalData.LOCAL_SQL   = new MSSql(GlobalData.MAIN_CONFIG.group("database").get("url").toString(),
                GlobalData.MAIN_CONFIG.group("database").get("username").toString(),
                GlobalData.MAIN_CONFIG.group("database").get("password").toString(),
                GlobalData.MAIN_CONFIG.group("database").get("database").toString());

        try {
            GlobalData.LOCAL_SQL.connect();

            Load_DataAndParsers db_loader = new Load_DataAndParsers(GlobalData.LOCAL_SQL);

            Hashtable[] parser_tables = (Hashtable[]) db_loader.getParsers().getRows().toArray(new Hashtable[db_loader.getParsers().getRows().size()]);
            for (int i=0; i<db_loader.getData().getRows().size(); i++) {
                Hashtable data_table = (Hashtable) db_loader.getData().getRows().get(i);

                data_table = parser(parser_tables,data_table);

            }

        } catch (SQLException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }


    }

    public static Hashtable parser(Hashtable[] parser_list,Hashtable h) {

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
