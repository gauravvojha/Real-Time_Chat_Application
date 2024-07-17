import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class ChatClient extends Application 
{
    private BufferedReader in;
    private PrintWriter out;
    private TextField textField = new TextField();
    private TextArea messageArea = new TextArea();

    public static void main(String[] args) 
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) 
    {
        primaryStage.setTitle("Chat Application");

        BorderPane root = new BorderPane();
        messageArea.setEditable(false);
        root.setCenter(new ScrollPane(messageArea));
        
        textField.setOnAction(event -> 
        {
            out.println(textField.getText());
            textField.setText("");
        });
        VBox bottomBox = new VBox();
        bottomBox.getChildren().addAll(new Label("Enter message:"), textField);
        root.setBottom(bottomBox);

        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();

        primaryStage.setOnCloseRequest(event -> 
        {
            out.println("LOGOUT");
            try 
            {
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException e) 
            {
                e.printStackTrace();
            }
        });

        connectToServer();
    }

    private void connectToServer() 
    {
        String serverAddress = "localhost";
        try 
        {
            try (Socket socket = new Socket(serverAddress, 8080)) 
            {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            }
            new Thread(() -> 
            {
                try 
                {
                    while (true) 
                    {
                        String line = in.readLine();
                        if (line.startsWith("SUBMITNAME")) 
                        {
                            out.println(getUserName());
                        } else if (line.startsWith("NAMEACCEPTED")) 
                        {
                            textField.setEditable(true);
                        } else if (line.startsWith("MESSAGE")) 
                        {
                            messageArea.appendText(line.substring(8) + "\n");
                        }
                    }
                } catch (IOException e) 
                {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) 
        {
            e.printStackTrace();
        }
    }

    private String getUserName() 
    {
        return "User" + (int)(Math.random() * 10000);
    }
}
