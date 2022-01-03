import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * ChatClient.java
 * @version 1.0
 * @author Trotyl
 * @since 2020 Dec.3
 * The client side of a chat program
 */

class ChatClient {
    private JFrame connectWindow;//first window
    private JTextField typeField;//type field
    private JTextArea msgArea;//message area
    private Socket mySocket; //socket for connection
    private BufferedReader input; //reader for network stream
    private PrintWriter output;  //printWriter for network output
    private boolean running = true; //thread status via boolean
    private JLabel connectionError;//label
    private boolean isConnected = false;
    private JComboBox userList;//list of all online users
    private String userName;//current user name
    private JTextArea statusArea;//area of all the users online
    private JLabel label;
    private String selected;//selected user

    public static void main(String[] args) {
        new ChatClient().getPort();
    }

    /**
     * window for entering the ip and port
     */
    public void getPort() {
        //setting up the connecting window
        connectWindow = new JFrame("connect");
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(400, 250));
        JLabel name = new JLabel("Connect");
        name.setBounds(170, 20, 70, 20);
        panel.setLayout(null);
        JLabel hostL = new JLabel("ip   ");
        JTextField hostT = new JTextField(10);
        hostL.setBounds(110, 70, 70, 20);
        hostT.setText("127.0.0.1");
        hostT.setBounds(170, 70, 150, 20);
        JLabel portL = new JLabel("port   ");
        portL.setBounds(110, 150, 70, 20);
        JTextField portT = new JTextField(10);
        portT.setText("5000");
        portT.setBounds(170, 150, 150, 20);
        JButton connectButton = new JButton("connect");
        connectButton.setBounds(80, 200, 90, 30);
        connectionError = new JLabel("");
        connectionError.setForeground(Color.red);
        connectionError.setBounds(200, 200, 200, 30);
        //adding all the components
        panel.add(name);
        panel.add(hostL);
        panel.add(hostT);
        panel.add(portL);
        panel.add(portT);
        panel.add(connectionError);
        panel.add(connectButton);
        //exit on close
        connectWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectWindow.add(panel);
        connectWindow.pack();
        //press the connect button when press enter
        connectWindow.getRootPane().setDefaultButton(connectButton);
        //open in the middle of the screen
        connectWindow.setLocationRelativeTo(null);
        connectWindow.setVisible(true);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String host = hostT.getText();
                String port = portT.getText();
                //connect
                connect(host, port);
                hostT.setText("");
                portT.setText("");
                if (isConnected) {
                    //open login page
                    logIn();
                }
            }
        });
    }

    public Socket connect(String ip, String port) {
        System.out.println("Attempting to make a connection..");
        //default ip and port are 127.0.0.1 and 5000
        try {
            int port1 = Integer.parseInt(port);
            mySocket = new Socket(ip, port1); //attempt socket connection (local address). This will wait until a connection is made
            InputStreamReader stream1 = new InputStreamReader(mySocket.getInputStream()); //Stream for network input
            input = new BufferedReader(stream1);
            output = new PrintWriter(mySocket.getOutputStream()); //assign printWriter to network stream
            connectWindow.dispose();
            isConnected = true;
        } catch (Exception e) {  //connection error occurred
            System.out.println("Connection to Server Failed");
            connectionError.setText("Wrong ip or port");
        }
        if (isConnected == true) {
            //this is to get rid of the first and second welcome message that disturbs the login
            try {
                if (input.ready()) {
                    input.readLine();
                }
            } catch (IOException ex) {
                System.out.println("Failed to receive msg from the server");
            }
            try {
                if (input.ready()) {
                    input.readLine();
                }
            } catch (IOException ex) {
                System.out.println("Failed to receive msg from the server");
            }
        }
        System.out.println("Connection made.");
        return mySocket;
    }

    /**
     * window for login
     */
    public void logIn() {
        //setting up the log in page
        JFrame logInWindow = new JFrame("LogIn");
        JPanel panel = new JPanel();
        panel.setLayout((new GridLayout(4, 2)));
        JLabel passwordL = new JLabel("password");
        JTextField passwordT = new JTextField(10);
        JLabel userNameL = new JLabel("username");
        JTextField userNameT = new JTextField(10);
        JButton logInButton = new JButton("log in");
        JButton registerButton = new JButton("register");
        JLabel logInError = new JLabel("");
        JLabel blank = new JLabel("");
        logInError.setForeground(Color.red);
        //adding all the components
        panel.add(userNameT);
        panel.add(userNameL);
        panel.add(passwordT);
        panel.add(passwordL);
        panel.add(logInButton);
        panel.add(registerButton);
        panel.add(logInError);
        panel.add(blank);
        logInWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        logInWindow.add(BorderLayout.CENTER, panel);
        logInWindow.setSize(500, 200);
        logInWindow.setLocationRelativeTo(null);
        logInWindow.setVisible(true);
        logInWindow.getRootPane().setDefaultButton(logInButton);
        //when the log in button is pressed
        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userName = userNameT.getText();
                String password = passwordT.getText();
                //check if username and password is blank
                if (userName.equals("") || password.equals("")) {
                    logInError.setText("username and password can't be blank");
                } else {
                    //pass in the password and username
                    output.println(userName + ":" + password);
                    output.flush();
                    //read message
                    boolean loop = true;
                    while (loop) {
                        try {
                            if (input.ready()) {
                                //check for an incoming message
                                String msg;
                                msg = input.readLine(); //read the message
                                if (msg.equals("Username is currently online! Please use another account! \"username:password\"")) {
                                    logInError.setText("user online, try another account");
                                    loop = false;
                                } else if (msg.equals("Username or password incorrect! Please try again! \"username:password\"")) {
                                    logInError.setText("username of password incorrect");
                                    loop = false;
                                } else if (msg.startsWith("Welcome")) {//if success
                                    //close the log in window and open the message window
                                    logInWindow.dispose();
                                    loop = false;
                                    startMessaging();
                                }
                            }
                        } catch (IOException ex) {
                            System.out.println("Failed to receive msg from the server");
                        }
                    }
                }
            }
        });

        //when the register button is pressed
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //close the login widow and open a register window
                logInWindow.dispose();
                register();
            }
        });
    }

    /**
     * window for registering an account
     */
    public void register() {
        //components
        JFrame registrationWindow = new JFrame("Registration");
        JPanel panel = new JPanel();
        panel.setLayout((new GridLayout(3, 3)));
        JLabel userNameL = new JLabel("user name   ");
        JTextField userNameT = new JTextField(10);
        JLabel passwordL = new JLabel("password   ");
        JTextField passwordT = new JTextField(10);
        JButton registerButton = new JButton("register");
        JButton logInButton = new JButton("log in");
        JLabel text = new JLabel("");
        text.setForeground(Color.red);
        //add the components to panel
        panel.add(userNameL);
        panel.add(userNameT);
        panel.add(registerButton);
        panel.add(passwordL);
        panel.add(passwordT);
        panel.add(logInButton);
        panel.add(text);
        //exit on close
        registrationWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        registrationWindow.add(BorderLayout.CENTER, panel);
        registrationWindow.setSize(550, 170);
        registrationWindow.setLocationRelativeTo(null);
        registrationWindow.setVisible(true);
        //when log in button is pressed
        logInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //close the register window and open the log in window
                registrationWindow.dispose();
                logIn();
            }
        });

        //when register button is pressed
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get the username and password
                String userName = userNameT.getText();
                String password = passwordT.getText();
                //check if they are blank
                if (userName.equals("") || password.equals("")) {
                    text.setText("name and pwd can't be blank");
                } else {
                    //pass in the username and password to the server
                    output.println("register:" + userName + ":" + password);
                    output.flush();
                    boolean loop = true;
                    while (loop) {
                        try {
                            if (input.ready()) {
                                //check for an incoming message
                                String msg;
                                msg = input.readLine(); //read the message
                                if (msg.equals("Username already exists, please use another one")) {
                                    text.setText("username already exists");
                                    loop = false;
                                } else if (msg.startsWith("Successfully")) {
                                    registrationWindow.dispose();
                                    loop = false;
                                    //start login window
                                    logIn();
                                }
                            }
                        } catch (IOException ex) {
                            System.out.println("Failed to receive msg from the server");
                        }
                    }
                }
            }
        });
        //exit on close
        registrationWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * message window
     */
    public void startMessaging() {
        //send message to all users as default
        selected = "All";
        //start two new threads
        Thread t = new Thread(new CheckMessage());
        Thread t2 = new Thread(new checkUsers());
        t.start();
        t2.start();
        //frame and components
        JFrame window = new JFrame("Chat Client");
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new GridLayout(2, 2));
        label = new JLabel("press [Enter] to send");
        //area that contains status of online users
        statusArea = new JTextArea(18, 14);
        statusArea.setEditable(false);
        JButton sendButton = new JButton("SEND");
        sendButton.addActionListener(new SendButtonListener());
        typeField = new JTextField(10);
        //place where selecting which person to chat with
        userList = new JComboBox();
        userList.addItem("All");
        msgArea = new JTextArea();
        msgArea.setEditable(false);
        southPanel.add(userList);
        southPanel.add(label);
        southPanel.add(typeField);
        southPanel.add(sendButton);
        //add a scroll pane to the two text areas
        JScrollPane jsp = new JScrollPane(msgArea);
        JScrollPane jsp2 = new JScrollPane(statusArea);
        window.add(BorderLayout.SOUTH, southPanel);
        window.add(jsp);
        window.add(BorderLayout.WEST, jsp2);
        //exit on close
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(400, 400);
        window.setVisible(true);
        //getting all the users' information just after log in
        output.println("-list");
        output.flush();
        //open at middle of the screen
        window.setLocationRelativeTo(null);
        window.getRootPane().setDefaultButton(sendButton);
        window.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
                //tell the server to close it's socket when closing
                output.println("$closed$");
                output.flush();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                running = false;
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });
    }

    //inner class that implements Runnable
    class CheckMessage implements Runnable {
        //convert second to hours or minutes or still seconds
        String convertTime(String sec) {
            int second = Integer.parseInt(sec);
            if (second > 86400) {
                return 86400 / 24 + " days";
            } else if (second > 3600) {
                return second / 60 + " hours";
            } else if (second > 60) {
                return second / 60 + " mins";
            } else {
                return second + " secs";
            }
        }

        @Override
        public void run() {
            while (running) {  // loop unit a message is received
                try {
                    if (input.ready()) {//check for an incoming message
                        String msg;
                        msg = input.readLine(); //read the message
                        if (msg.equals("use -pm:username:message to send a pm") || msg.equals("Online Users: ") || msg.contains(", seen")) {
                            if (msg.equals("Online Users: ")) {
                                statusArea.setText("Online Users:\n");
                                selected = userList.getSelectedItem() + "";
                                userList.removeAllItems();
                                userList.addItem("All");
                                if (!selected.equals("All")) {
                                    userList.addItem(selected);
                                }
                                userList.setSelectedItem(selected);
                            }
                            int i = msg.indexOf(", seen");
                            if (i != -1) {
                                String onlineUser = msg.substring(0, i);
                                String userStatus = msg.substring(0, i) + "\nlast activity [" + convertTime(msg.substring(i + 7, msg.indexOf("."))) + "] ago";
                                statusArea.append(userStatus + "\n");
                                if (!onlineUser.equals(userName) && !onlineUser.equals(selected)) {
                                    userList.addItem(onlineUser);
                                }
                            }
                        } else if (msg.equals("Unknown user!")) {
                            label.setForeground(Color.red);
                            label.setText("[" + selected + "] is offline");
                            userList.setSelectedItem("All");
                        } else if (!msg.startsWith("use -")) {
                            //if it's not a server instruction
                            msgArea.append(msg + "\n");
                            //getting who is the one sending the message
                            int i = msg.indexOf("->");
                            if (i != -1) {
                                label.setForeground(new Color(0, 102, 0));
                                if (msg.substring(0, i).equals(userName)) {
                                    label.setText("You are talking only to [" + msg.substring(i + 2, msg.indexOf(":")) + "]");
                                } else {
                                    label.setText("[" + msg.substring(0, i) + "] is talking only to you");
                                }
                            } else if (msg.contains(":")) {
                                String name = msg.substring(0, msg.indexOf(":"));
                                label.setForeground(Color.blue);
                                if (!name.equals(userName)) {
                                    label.setText("[" + msg.substring(0, msg.indexOf(":")) + "] is talking to everyone");
                                } else {
                                    label.setText("You are talking to everyone");
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Failed to receive msg from the server");
                }
            }
            try {  //after leaving the main loop we need to close all the sockets
                input.close();
                output.close();
                mySocket.close();
                System.exit(0);
            } catch (Exception e) {
                System.out.println("Failed to close socket");
            }
        }
    }

    //another inner class that implements Runnable
    class checkUsers implements Runnable {
        @Override
        //check the status of all the users every second
        public void run() {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                output.println("-list");
                output.flush();
            }
        }
    }

    //****** Inner Classes for Action Listeners ****
    // send - send msg to server (also flush), then clear the JTextField
    class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            //Send a message to the client
            msgArea.setCaretPosition(msgArea.getText().length());
            String userSelected = userList.getSelectedItem() + "";
            if (!userSelected.equals("All")) {
                //sending a private massage
                /* added a space in the end because it's going to have an
                error when blank massage is sent privately*/
                output.println("-pm:" + userSelected + ":" + typeField.getText() + " ");
            } else {
                output.println(typeField.getText());
            }
            output.flush();
            typeField.setText("");
        }
    }
}//end ChatClient class