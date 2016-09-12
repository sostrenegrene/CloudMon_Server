package dk.mudlogic.cloudmon.client_v3.process;

import dk.mudlogic.cloudmon.client_v3.client.v3Client;
import dk.mudlogic.cloudmon.client_v3.client.v3Command;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/**
 * Created by soren.pedersen on 08-09-2016.
 */
public class v3Command_Process {

    private LogTracer log = new LogFactory().tracer();

    private final int PTYPE_CONSOLE = "command".trim().hashCode();
    private final int PTYPE_QUERY = "query".trim().hashCode();

    private v3Client client;
    private v3Command command;

    public v3Command_Process(v3Client client, v3Command command) {
        log.setTracerTitle(v3Command_Process.class);

        this.client = client;
        this.command = command;

        new Thread( new ProcessThread(this) ).start();
    }

    private void select_ProcessType() {
        int type = command.get_str("process_type").trim().hashCode();

        if (PTYPE_CONSOLE == type) {
            //TODO do console command
            log.trace("Do console command");
            log.trace( command.get_str("process_name") );
        }

        if (PTYPE_QUERY == type) {
            //TODO do SQL query
            log.trace("Do database query");
            log.trace( command.get_str("process_name") );
        }

    }

    public void exec() {
        log.trace("exec()");

        select_ProcessType();
    }


    class ProcessThread implements Runnable {
        LogTracer log = new LogFactory().tracer();
        final v3Command_Process process;

        public ProcessThread(v3Command_Process process) {
            log.setTracerTitle(ProcessThread.class);
            this.process = process;
        }

        @Override
        public void run() {
            log.trace("run()");
            process.exec();
        }
    }


}
