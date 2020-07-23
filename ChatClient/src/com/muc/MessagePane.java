package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

public class MessagePane extends JPanel implements MessageListener {


  private final ChatClient client;
  private final String login;

  private DefaultListModel<String> listModel = new DefaultListModel<>();

  private JList<String> messageList = new JList<>(listModel);

  private JTextField inputField = new JTextField();


  public MessagePane(ChatClient client, String login) {
    this.client = client;
    this.login = login;

    this.client.addMessageListener(this);

    setLayout(new BorderLayout());
    add(new JScrollPane(messageList), BorderLayout.CENTER);
    add(inputField, BorderLayout.SOUTH);

    inputField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String text = inputField.getText();
        client.msg(login,text);
        listModel.addElement("You:" + text);
        inputField.setText("");
      }
    });
  }

  @Override
  public void onMessage(String fromLogin, String msgBody) {
    if (login.equalsIgnoreCase(fromLogin)) {
      String line = fromLogin + ":" + msgBody;
      listModel.addElement(line);
    }
  }
}
