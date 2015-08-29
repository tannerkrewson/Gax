package GaxServer;

import java.net.Socket;
import org.json.JSONObject;

public class ServerThreadRunner implements Runnable {

    private Socket socket;

    public ServerThreadRunner(Socket s) {
        socket = s;
    }

    @Override
    public void run() {
        ServerThread commander = new ServerThread(socket);
        JSONObject received = commander.getCommandFromClient();
        commander.execJSONCommand(received);
    }
}