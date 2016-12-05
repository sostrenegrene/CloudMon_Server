package dk.mudlogic.cloudmon.client.client_v3.client;

/** The standard identifier, can be extended
 * to implement standard id's types for v3 clients, groups and commands
 *
 * Created by soren.pedersen on 08-10-2016.
 */
class v3Identifier {

    private String NAME = "";
    private int CLIENT_ID = 0;
    private int GROUP_ID = 0;
    private int COMMAND_ID = 0;
    private int HASHCODE = 0;

    public void set_Name(String name) { NAME = name; }

    public void set_ClientID(int id) { CLIENT_ID = id; }

    public void set_GroupID(int id) { GROUP_ID = id; }

    public void set_CommandID(int id) { COMMAND_ID = id; }

    public void set_Hashcode(int hashcode) {
        HASHCODE = hashcode;
    }

    public String get_Name() { return NAME; }

    public int get_ClientID() { return CLIENT_ID; }

    public int get_GroupID() { return GROUP_ID; }

    public int get_CommandID() { return COMMAND_ID; }

    public int get_Hashcode() {
        return HASHCODE;
    }

}
