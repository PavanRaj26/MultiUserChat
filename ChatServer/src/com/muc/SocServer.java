package com.muc;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocServer {

  public static void main(String[] args) throws IOException {

    System.out.println("Server started");

    ServerSocket ss = new ServerSocket(8084);

    System.out.println("Server is waiting for client request");

    Socket s = ss.accept();

    System.out.println("Client connected");

    BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

    String str = br.readLine();

    System.out.println("Client data: " + str);

  }
}
