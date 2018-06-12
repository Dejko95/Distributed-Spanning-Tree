package bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BootstrapServerThread implements Runnable {
    private Socket socket;
    private BootstrapServer bootstrapServer;

    public BootstrapServerThread(BootstrapServer bootstrapServer, Socket socket) {
        this.bootstrapServer = bootstrapServer;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(),true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = in.readLine();     //expect "finished_setup"
            if (message.equals("finished_setup")) {
                bootstrapServer.getFinishedSetupConnections().incrementAndGet();
                System.out.println(socket.getPort() + " finished setup");
            } else {
                throw new IOException("error with setup connections");
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
