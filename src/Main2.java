import dk.mudlogic.tools.database.MSSql;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 01-08-2016.
 */
public class Main2 {

    LogTracer log = new LogFactory().tracer();

    public static void main(String[] args) {

        MSSql sql = new MSSql("sgsql01.grenes.local","svc_cloudmon","SGrenes1234","sgidrift2013r2");


        try {
            sql.connect();
            //sql.query("SELECT * FROM (SELECT sjh.[Job ID], Description, CONVERT(datetime,([Last Date Checked] + [Last Time Checked])) as [Last Check], CONVERT(datetime,([Next Check Date] + [Next Check Time])) as [Next Check], [Error Occurred], [Last Message Text] FROM [SGSQL01].[GRENEDK].[dbo].[SG Handelskompagnie$Scheduler Job Header] sjh) t where [Error Occurred] = 1 or [Next Check] < getdate()");
            sql.query("SELECT [Entry No_], Status, No_, [Error Message] FROM [SGIDrift2013R2].[dbo].[SGI$HLT Job Order] WHERE Status = 2");
        }
        catch(Exception e) {
            e.printStackTrace();
        }


    }

}
