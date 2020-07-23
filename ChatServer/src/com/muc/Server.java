package com.muc;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server extends Thread{

  private final int serverPort;


  private ArrayList<ServerWorker> workerList = new ArrayList<>();

  public Server(int serverPort) {
    this.serverPort = serverPort;
  }

  public List<ServerWorker> getWorkerList() {
    return workerList;
  }

  @Override
  public void run() {

    //Maintains the connection between client and server
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(serverPort);
    } catch (IOException e) {
      e.printStackTrace();
    }
    while(true) {

      Socket clientSocket = null;
      try {
        clientSocket = serverSocket.accept();
      } catch (IOException e) {
        e.printStackTrace();
      }
      ServerWorker worker = new ServerWorker(this,clientSocket);
      workerList.add(worker);
      worker.start();
    }
  }

  public void removeWorker(ServerWorker serverWorker) {
    workerList.remove(serverWorker);
  }
}
