import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer 
{
    private static final int PORT = 8080;
    private static HashSet<String> userNames = new HashSet<>();
    private static HashSet<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) 
    {
        System.out.println("The chat server is running...");
        ServerSocket listener = null;
        try 
        {
            listener = new ServerSocket(PORT);
            while (true) 
            {
                new Handler(listener.accept()).start();
            }
        } catch (IOException e) 
        {
            e.printStackTrace();
        } finally {
            if (listener != null) 
            {
                try 
                {
                    listener.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Handler extends Thread 
    {
        private String userName;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Handler(Socket socket) 
        {
            this.socket = socket;
        }

        public void run() 
        {
            try 
            {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) 
                {
                    out.println("SUBMITNAME");
                    userName = in.readLine();
                    if (userName == null) 
                    {
                        return;
                    }
                    synchronized (userNames) 
                    {
                        if (!userName.isEmpty() && !userNames.contains(userName)) 
                        {
                            userNames.add(userName);
                            break;
                        }
                    }
                }

                out.println("NAMEACCEPTED " + userName);
                writers.add(out);

                String message;
                while ((message = in.readLine()) != null) 
                {
                    System.out.println(message);
                    for (PrintWriter writer : writers) 
                    {
                        writer.println("MESSAGE " + userName + ": " + message);
                    }
                }
            } catch (IOException e) 
            {
                e.printStackTrace();
            } finally {
                if (userName != null) 
                {
                    userNames.remove(userName);
                }
                if (out != null) 
                {
                    writers.remove(out);
                }
                try 
                {
                    socket.close();
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
