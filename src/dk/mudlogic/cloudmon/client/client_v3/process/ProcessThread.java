package dk.mudlogic.cloudmon.client.client_v3.process;

import dk.mudlogic.cloudmon.client.client_v3.client.v3Command;
import dk.mudlogic.tools.callback.CallbackHandler;
import dk.mudlogic.tools.log.LogFactory;
import dk.mudlogic.tools.log.LogTracer;

/** Handles the run though of the command process as a thread
 *
 * Created by soren.pedersen on 13-09-2016.
 */
class ProcessThread implements Runnable {
    LogTracer log = new LogFactory().tracer();
    final v3Command_Process process;
    CallbackHandler group_callback;

    /** Constructor
     *
     * @param process v3Command_Process
     */
    public ProcessThread(v3Command_Process process, CallbackHandler group_callback) {
        log.setTracerTitle(ProcessThread.class);
        log.trace("Start: " + process.getCommand().get_str("process_name"));

        this.process = process;
        this.group_callback = group_callback;
    }

    @Override
    public void run() {

        //Exec process
        process.exec();
        //Get command
        v3Command command = process.getCommand();

        group_callback.callback("v3Command",command);

        //Finalize thread for cleanup
        try {
            log.trace("Finished: " + process.getCommand().get_str("process_name"));

            this.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
