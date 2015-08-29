package GaxServer;

public class ServerRunner {

    public static void main(String[] args) {
        Server gaxServer = new Server(42924);
        gaxServer.startup();
        gaxServer.startConsoleThread();
        gaxServer.connectToDatabase();
        gaxServer.loadMemory();
        gaxServer.waitForClients();
    }
}
