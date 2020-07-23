package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {

  private final String serverName;
  private final int serverPort;
  private Socket socket;
  private InputStream serverIn;
  private OutputStream serverOut;
  private BufferedReader bufferedIn;

  private PrintWriter printWriter;
  private List<UserStatusListener> userStatusListenerList = new ArrayList<>();

  private List<MessageListener> messageListenerList = new ArrayList<>();
  public ChatClient(String serverName, int serverPort) {
    this.serverPort = serverPort;
    this.serverName = serverName;
  }

  public static void main(String[] args) throws IOException, InterruptedException {
    ChatClient client = new ChatClient("127.0.0.1",8818);

    client.addUserStatusListener(new UserStatusListener() {
      @Override
      public void online(String login) {
        System.out.println("ONLINE : " + login);
      }

      @Override
      public void offline(String login) {
        System.out.println("OFFLINE : " + login);
      }
    });

    client.addMessageListener(new MessageListener() {
      @Override
      public void onMessage(String fromLogin, String msgBody) {
        System.out.println("You got a message from " + fromLogin + ": " + msgBody);
      }
    });

    if (!client.connect()) {
      System.err.println("Connection failed");
    }
    else {
      System.out.println("Connection successful");
      if (client.login("guest","guest")) {
        System.out.println("Login successful");
        client.msg("pavan","Hello Pavan");
      }
      else {
        System.err.println("Login failed");
      }
//      client.logoff();
    }
  }

  public boolean connect() {
    try {

      this.socket = new Socket(serverName, serverPort);
      System.out.println("The client port is " + socket.getLocalPort());
      this.serverOut = socket.getOutputStream();
      this.printWriter = new PrintWriter(serverOut);
      this.serverIn = socket.getInputStream();
      this.bufferedIn = new BufferedReader(new InputStreamReader(this.serverIn));
      return true;
    }
    catch(IOException e) {
      e.printStackTrace();
      return false;
    }
  }


  public boolean login(String login, String password) throws IOException, InterruptedException {
    String cmd = "login " + login + " " + password;

    printWriter.println(cmd);
    printWriter.flush();

    String line = bufferedIn.readLine();
    if(StringUtils.equals(line,"ok login")) {
      startMessageReader();
      return true;
    }
    return false;
  }

  private void startMessageReader() {
    Thread t = new Thread() {
      @Override
      public void run() {
        readMessageLoop();
      }
    };
    t.start();
  }

  private void readMessageLoop() {
    String line;
    try {
      while ((line = bufferedIn.readLine()) != null) {
          String[] tokens = StringUtils.split(line);
          if (tokens != null && tokens.length > 0) {
            String cmd = tokens[0];
            if ("online".equalsIgnoreCase(cmd)) {
              handleOnline(tokens);
            }
            else if ("offline".equalsIgnoreCase(cmd)) {
              handleOffline(tokens);
            } else if ("msg".equalsIgnoreCase(cmd)) {
              String[] tokensMsg = StringUtils.split(line,null,3);
              handleMessage(tokensMsg);
            }
          }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      try {
        socket.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }
  public void handleOnline(String[] tokens) {
    String login = tokens[1];
    for (UserStatusListener listener: userStatusListenerList) {
      listener.online(login);
    }
  }
  public void handleOffline(String[] tokens) {
    String login = tokens[1];
    for (UserStatusListener listener: userStatusListenerList) {
      listener.offline(login);
    }
  }
  public void msg(String sendTo, String msgBody) {
    String cmd = "msg " + sendTo + " " + msgBody;
    printWriter.println(cmd);
    printWriter.flush();
  }

  public void logoff() {
    String cmd = "logoff";

    printWriter.println(cmd);
    printWriter.flush();

  }
  public void handleMessage(String[] tokens) {
    String login = tokens[1];
    String msgBody = tokens[2];
    for (MessageListener listener:messageListenerList) {
      listener.onMessage(login,msgBody);
    }
  }





  public void removeUserStatusListener(UserStatusListener userStatusListener) {
    userStatusListenerList.remove(userStatusListener);
  }
  public void addUserStatusListener(UserStatusListener userStatusListener) {
    userStatusListenerList.add(userStatusListener);
  }

  public void addMessageListener(MessageListener messageListener) {
    messageListenerList.add(messageListener);
  }
  public void removeMessageListener(MessageListener messageListener) {
    messageListenerList.remove(messageListener);
  }

}
