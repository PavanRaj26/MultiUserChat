package com.muc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocClient {

  public static void main(String[] args) throws IOException {

    String ip = "localhost";
    int port = 8084;
    Socket s = new Socket(ip,port);

    String str = "Pavan Raj";

    OutputStreamWriter os = new OutputStreamWriter(s.getOutputStream());

    PrintWriter out = new PrintWriter(os);

    os.write(str);

    os.close();
  }
}
