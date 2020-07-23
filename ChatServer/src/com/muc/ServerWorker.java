package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class ServerWorker extends Thread {

  private final Socket clientSocket;
  private final Server server;

  private String login = null;
  private OutputStream outputStream;

  private PrintWriter printWriter;

  private OutputStreamWriter os;
  private HashSet<String> topicSet  = new HashSet<>();

  public ServerWorker(Server server, Socket clientSocket) {
    this.server = server;
    this.clientSocket = clientSocket;

  }


  @Override
  public void run() {
    try {
      handleClientSocket();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public String getLogin() {
    return login;
  }

  private void handleClientSocket() throws IOException, InterruptedException {

    //For bidirectional communication
    InputStream inputStream = clientSocket.getInputStream();
    this.outputStream = clientSocket.getOutputStream();
    this.printWriter = new PrintWriter(outputStream);
    this.os = new OutputStreamWriter(outputStream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    String line;
    //Here is the chat panel where messages are printed on the console and read
    //by the inputStream of the Server
    while ((line = reader.readLine()) != null) {
      String[] tokens = StringUtils.split(line);
      if (tokens != null && tokens.length > 0) {
        String cmd = tokens[0];
        if ("logoff".equalsIgnoreCase(cmd) || "quit".equalsIgnoreCase(cmd)) {
          handleLogoff();
          break;
        } else if ("login".equalsIgnoreCase(cmd)) {
          handleLogin(tokens);
        }
        else if ("msg".equalsIgnoreCase(cmd)) {
          String[] tokensMsg = StringUtils.split(line,null,3);
          handleMessage(tokensMsg);
        }else if ("join".equalsIgnoreCase(cmd)) {
          handleJoin(tokens);
        }else if ("leave".equalsIgnoreCase(cmd)) {
          handleLeave(tokens);
        }
        else {
          String msg = "Unknown " + cmd + "\n";
          printWriter.write(msg);
          printWriter.flush();
        }
      }
    }
    clientSocket.close();
  }

  private void handleLeave(String[] tokens) {
    if (tokens.length > 1) {
      String topic = tokens[1];
      topicSet.remove(topic);
    }
  }

  public Boolean isMemberTopic(String topic) {
    return topicSet.contains(topic);
  }
  private void handleJoin(String[] tokens) {
    if (tokens.length > 1) {
      String topic = tokens[1];
      topicSet.add(topic);
    }
  }

  ///format msg : login message
  // format topic : msg "#topic" message
  private void handleMessage(String[] tokens) throws IOException {
    String sendTo = tokens[1];
    String body = tokens[2];

    boolean isTopic = sendTo.charAt(0) == '#';
    boolean isMember = isMemberTopic(sendTo);
    List<ServerWorker> workerList = server.getWorkerList();
    for (ServerWorker worker : workerList) {
      if (isTopic) {
          if (worker.isMemberTopic(sendTo)) {
            String outMsg = "msg " + sendTo + ":" +  login + " " + body + "\n";
            worker.send(outMsg);
          }
      }
      else {
        if (sendTo.equalsIgnoreCase(worker.getLogin())) {
          String outMsg = "msg " + login + " " + body + "\n";
          worker.send(outMsg);
        }
      }
    }
  }

  private void handleLogoff() throws IOException {

    server.removeWorker(this);
    List<ServerWorker> workerList = server.getWorkerList();

    //Sends to all other workers that the current user has logged off
    String offlineMsg = "offline " + login + "\n";

    for (ServerWorker worker : workerList) {
      if (!login.equals(worker.getLogin())) {
        worker.send(offlineMsg);
      }
    }

    clientSocket.close();
  }

  private void handleLogin(String[] tokens) throws IOException {

    if (tokens.length == 3) {
      String login = tokens[1];
      String password = tokens[2];

      if ((login.equalsIgnoreCase("guest") && password.equalsIgnoreCase("guest"))
              || (login.equalsIgnoreCase("pavan") && password.equalsIgnoreCase("123"))) {
        String msg = "ok login\n";
        printWriter.write(msg);
        printWriter.flush();

        this.login = login;

        System.out.println("User has logged in : " + login);

        List<ServerWorker> workerList = server.getWorkerList();
        String onlineMsg = "online " + login + "\n";

        //Prints message on current user browser all the existing online workers info
        for (ServerWorker worker : workerList) {

            if (worker.getLogin() != null) {
                String msg1 = "online " + worker.getLogin() + "\n";
                send(msg1);
            }
        }

        //Sends to all other workers that the current user has logged in
        for (ServerWorker worker : workerList) {
          if (!login.equals(worker.getLogin())) {
            worker.send(onlineMsg);
          }
        }
      } else {
        String msg = "error login\n";
        printWriter.write(msg);
        printWriter.flush();
        System.out.println("Login failed for " + login);
      }
    }
  }

  private void send(String msg) throws IOException {
    if (login!= null) {
      printWriter.write(msg);
      printWriter.flush();
    }
  }
}
