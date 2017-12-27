package edu.lmu.cs.networking;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;


public class TicTacToeClient {

    private JFrame frame = new JFrame("Minotaur");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;
    private ImageIcon white = createImageIcon("smallwhite.png", "White image");
    private ImageIcon smoke = createImageIcon("smallsmoke.png", "Smoke image");
    private ImageIcon barrier = createImageIcon("smallbarrier.png", "Barrier image");
    private ImageIcon granite = createImageIcon("smallgranite.png", "Granite image");
    private ImageIcon destroyed = createImageIcon("smalldestroyed.png", "Destroyed image");

    private ImageIcon destroyedIcon;
    private ImageIcon destroyedOpponentIcon;

    private final int SIZE = 81;
    private final int columnLength = (int) Math.sqrt(SIZE);
    private Square[] board = new Square[SIZE];
    private Square[] opponentBoard = new Square[SIZE];
    private int currentLocation;
    private int opponentCurrentLocation;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private boolean error = false;

    private boolean wasChange = false;

    /**
     * Constructs the client by connecting to a server, laying out the
     * GUI and registering GUI listeners.
     */

    private TicTacToeClient(String serverAddress) throws Exception {

        // Setup networking
        int PORT = 8102;
        try {
            socket = new Socket(serverAddress, PORT);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.out.println("CLIENT");
                    out.println("EXIT");
                }
            });

            // Layout GUI
            messageLabel.setBackground(Color.lightGray);
            frame.getContentPane().add(messageLabel, "South");

            frame.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    int key = e.getKeyCode();
                    if ((key == KeyEvent.VK_LEFT)) {
                        out.println("MOVE LEFT");
                    }
                    if ((key == KeyEvent.VK_RIGHT)) {
                        out.println("MOVE RIGHT");
                    }
                    if ((key == KeyEvent.VK_UP)) {
                        out.println("MOVE UP");
                    }
                    if ((key == KeyEvent.VK_DOWN)) {
                        out.println("MOVE DOWN");
                    }
                }
            });

            frame.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON2) {
                        if (!wasChange) {
                            out.println("CHANGE");
                            wasChange = true;
                            messageLabel.setText("Change turn");
                        }
                    }
                }
            });

            JPanel boardPanel = new JPanel();
            boardPanel.setBackground(Color.PINK);
            JPanel opponentBoardPanel = new JPanel();
            opponentBoardPanel.setBackground(SystemColor.red);
            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));

            ImageIcon icon1 = createImageIcon("minotauro.jpg","des");
            JLabel label = new JLabel(icon1);
//            JLabel label2 = new JLabel("Правая кнопка мыши - завершение хода");
//            JLabel label3 = new JLabel("Стрелки - для движения");
//
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
//            label2.setAlignmentX(Component.CENTER_ALIGNMENT);
//            label3.setAlignmentX(Component.CENTER_ALIGNMENT);
//
            labelPanel.add(label);
//            labelPanel.add(label2);
//            labelPanel.add(label3);

            boardPanel.setLayout(new GridLayout(columnLength, columnLength, columnLength - 1, columnLength - 1));
            opponentBoardPanel.setLayout(new GridLayout(columnLength, columnLength, columnLength - 1, columnLength - 1));

            for (int i = 0; i < board.length; i++) {
                final int j = i;
                board[i] = new Square();
                board[i].setIcon(smoke);
                board[i].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (j == currentLocation - 1)
                                out.println("BOMB LEFT");
                            else if (j == currentLocation + 1)
                                out.println("BOMB RIGHT");
                            else if (j == currentLocation - columnLength)
                                out.println("BOMB UP");
                            else if (j == currentLocation + columnLength)
                                out.println("BOMB DOWN");
                        }

                        if (e.getButton() == MouseEvent.BUTTON3) {
                            if (j == currentLocation - 1)
                                out.println("SHOW LEFT");
                            else if (j == currentLocation + 1)
                                out.println("SHOW RIGHT");
                            else if (j == currentLocation - columnLength)
                                out.println("SHOW UP");
                            else if (j == currentLocation + columnLength)
                                out.println("SHOW DOWN");
                        }
                    }
                });
                boardPanel.add(board[i]);
            }
            frame.getContentPane().add(boardPanel, "West");

            frame.getContentPane().add(labelPanel, "Center");

            for (int i = 0; i < opponentBoard.length; i++) {
                opponentBoard[i] = new Square();
                opponentBoard[i].setIcon(smoke);
                opponentBoardPanel.add(opponentBoard[i]);
            }
            frame.getContentPane().add(opponentBoardPanel, "East");

        } catch (ConnectException e) {
            errorMessage();
        }
    }

    public void errorMessage() {
        JOptionPane.showConfirmDialog(frame,
                "Please,run the server",
                "Error",
                JOptionPane.CLOSED_OPTION);
        error = true;
    }

    public boolean error() {
        return error;
    }

    /**
     * The main thread of the client will listen for messages
     * from the server.  The first message will be a "WELCOME"
     * message in which we receive our mark.  Then we go into a
     * loop listening for "VALID_MOVE", "OPPONENT_MOVED", "VICTORY",
     * "DEFEAT", "TIE", "OPPONENT_QUIT or "MESSAGE" messages,
     * and handling each message appropriately.  The "VICTORY",
     * "DEFEAT" and "TIE" ask the user whether or not to play
     * another game.  If the answer is no, the loop is exited and
     * the server is sent a "QUIT" message.  If an OPPONENT_QUIT
     * message is recevied then the loop will exit and the server
     * will be sent a "QUIT" message also.
     */

    private void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icon = mark == 'X' ? createImageIcon("shahid1.jpg", "shahid1") : createImageIcon("shahid2.jpg", "shahid2");
                opponentIcon = mark == 'X' ? createImageIcon("shahid2.jpg", "shahid2") : createImageIcon("shahid1.jpg", "shahid1");

                destroyedIcon = mark == 'X' ? createImageIcon("shahid1.jpg", "shahid1") : createImageIcon("shahid2.jpg", "shahid2");
                destroyedOpponentIcon = mark == 'X' ? createImageIcon("shahid2.jpg", "shahid2") : createImageIcon("shahid1.jpg", "shahid1");
                frame.setTitle("Minotaur - Player " + mark);
            }
            while (true) {
                response = in.readLine();

                if (response.startsWith("YOUR_MOVE")) {
                    wasChange = false;
                    messageLabel.setText("Your turn");
                }

                else if (response.startsWith("START")) {
                    startLocation(board, icon);

                } else if (response.startsWith("OPPONENT_START")) {
                    startLocation(opponentBoard, opponentIcon);

                } else if (response.startsWith("OPEN")) {
                    openLocation(response, board, 5, true);

                } else if (response.startsWith("OPPONENT_OPEN")) {
                    openLocation(response, opponentBoard, 14, false);

                } else if (response.startsWith("VALID_MOVE")) {
                    moveLocation(response, board, 11, icon, true);

                } else if (response.startsWith("OPPONENT_MOVED")) {
                    moveLocation(response, opponentBoard, 15, opponentIcon, false);

                } else if (response.startsWith("DESTROYED")) {
                    destroyedLocation(response, board, 10, true);

                } else if (response.startsWith("OPPONENT_DESTROYED")) {
                    destroyedLocation(response, opponentBoard, 19, false);

                } else if (response.startsWith("NOT_DESTROYED")) {
                    notDestroyedLocation(response, board, 14, true);

                } else if (response.startsWith("OPPONENT_NOT_DESTROYED")) {
                    notDestroyedLocation(response, opponentBoard, 23, false);

                } else if (response.startsWith("THROW_INTO_THE_VOID")) {
                    throwIntoTheVoid(response, board, 20, icon, true);

                } else if (response.startsWith("OPPONENT_THROW_INTO_THE_VOID")) {
                    throwIntoTheVoid(response, opponentBoard, 29, opponentIcon, false);

                }  else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("You win");
                    break;

                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("You lose");
                    break;

                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
        } catch (SocketException e) {
            socket.close();
            errorMessage();
            System.exit(0);
        } finally {
            try {
                socket.close();
            }
            catch (NullPointerException e) {
                //NOTHING
            }
        }
    }
    private int direction(String subCommand, boolean current) { //направление хода
        int location;
        if (current)
            location = currentLocation;
        else
            location = opponentCurrentLocation;

        if (subCommand.startsWith("LEFT"))
            return location - 1;
        else if (subCommand.startsWith("RIGHT"))
            return location + 1;
        else if (subCommand.startsWith("UP"))
            return location - columnLength;
        else if (subCommand.startsWith("DOWN"))
            return location + columnLength;
        else
            return -1;
    }

    private void startLocation(Square[] board, ImageIcon icon) {
        int startLocation = (SIZE - 1) / 2;
        if (icon.equals(this.icon)) {
            currentLocation = startLocation;
        } else {
            opponentCurrentLocation = startLocation;
        }
        board[startLocation].setIcon(icon);
        board[startLocation].repaint();
    }

    private void openLocation(String response, Square[] board, int n, boolean current) {
        String codestr = (response.substring(n, n + 1));
        if (codestr.equals("-")){
            codestr = response.substring(n, n + 2);
        }
        int code = Integer.parseInt(codestr);
        String turn = response.substring(n + 2);
        int playerLocation;

        if (current)
            playerLocation = currentLocation;
        else
            playerLocation = opponentCurrentLocation;

        if (playerLocation % columnLength == 0 && turn.equals("LEFT")) {
            //NOTHING

        } else if ((playerLocation + 1) % columnLength == 0 && turn.equals("RIGHT")) {
            //NOTHING

        } else if (playerLocation <= columnLength && turn.equals("UP")) {
            //NOTHING

        } else if (SIZE - playerLocation <= columnLength && turn.equals("DOWN")) {
            //NOTHING

        } else {

            int location = direction(turn, current);//определяем координату по направлению

            if (code == 0 && board[location].getIcon() == destroyed) {
                board[location].setIcon(destroyed);
            }

            if (code == 0 && board[location].getIcon() != destroyed) {
                board[location].setIcon(white);
            }

            if (code == 1 && board[location].getIcon() != granite) {
                board[location].setIcon(barrier);
            }
            board[location].repaint();

            if (current)
                messageLabel.setText("Open " + turn);
            else
                messageLabel.setText("Opponent open " + turn);
        }
    }

    private void moveLocation(String response, Square[] board, int n, ImageIcon icon, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current); //определяем координату по направлению
        if (response.startsWith("VALID_MOVE_") || response.startsWith("OPPONENT_MOVED_")) { //если команда с обломками
            if (icon.equals(this.icon)) { //игрок на руинах
                board[location].setIcon(destroyedIcon);
            } else {
                board[location].setIcon(destroyedOpponentIcon);
            }
        }
        else {
            board[location].setIcon(icon);
        }
        /*
        if (board[location].getIcon().equals(destroyed)) {
            if (icon.equals(this.icon)) {
                board[location].setIcon(destroyedIcon);
            }
            else {
                board[location].setIcon(destroyedOpponentIcon);
            }
        }
        */
        //else {
        //board[location].setIcon(icon);
        //}
        board[location].repaint();

        if (icon.equals(this.icon)) { //старая локация игрока
            if (board[currentLocation].getIcon().equals(destroyedIcon)) {
                board[currentLocation].setIcon(destroyed);
            }
            else {
                board[currentLocation].setIcon(white);
            }
            board[currentLocation].repaint();
            currentLocation = location;

        } else {
            if (board[opponentCurrentLocation].getIcon().equals((destroyedOpponentIcon))) {
                board[opponentCurrentLocation].setIcon(destroyed);
            }
            else {
                board[opponentCurrentLocation].setIcon(white);
            }
            board[opponentCurrentLocation].repaint();
            opponentCurrentLocation = location;
        }
    }

    private void destroyedLocation(String response, Square[] board, int n, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current); //определяем координату по направлению
        board[location].setIcon(destroyed);
       board[location].repaint();

        if (current)
            messageLabel.setText("Wall destroyed " + turn);
        else
            messageLabel.setText("Opponent destroyed wall " + turn);
    }

    private void notDestroyedLocation(String response, Square[] board, int n, boolean current) {
        String turn = response.substring(n);
        int location = direction(turn, current); //определяем координату по направлению
        board[location].setIcon(granite);
        board[location].repaint();

        if (current)
            messageLabel.setText("Wall not destroyed " + turn);
        else
            messageLabel.setText("Opponent not destroyed wall " + turn);

    }

    private void throwIntoTheVoid(String response, Square[] board, int n, ImageIcon icon, boolean current) {
        String turn = response.substring(n);
        if (response.startsWith("THROW_INTO_THE_VOID_") || response.startsWith("OPPONENT_THROW_INTO_THE_VOID_")) {
            int location = direction(turn, current); //определяем координату по направлению
            if (icon.equals(this.icon)) {
                board[location].setIcon(destroyed);
            }
            else {
                board[location].setIcon(destroyed);
            }
            board[location].repaint();
        }
        else {

            if (current)
                messageLabel.setText("Throw into the void " + turn);
            else
                messageLabel.setText("Opponent throw into the void " + turn);
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
                "Play again?",
                "It was cool, agree!",
                JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * Graphical square in the client window.  Each square is
     * a white panel containing.  A client calls setIcon() to fill
     * it with an Icon, presumably an X or O.
     */

    static class Square extends JPanel {
        JLabel label = new JLabel((Icon) null);

        Square() {
            setBackground(Color.white);
            add(label);
        }

        void setIcon(Icon icon) {
            label.setIcon(icon);
        }

        Icon getIcon() {
            return label.getIcon();
        }
    }

    /**
     * Runs the client as an application.
     */
    public static void main(String[] args) throws Exception {
        String serverAddress = null;
        boolean askAddress = true;
        while (true) {
            if (askAddress) {
                IP ip = new IP();
                serverAddress = ip.setIP();
            }
            TicTacToeClient client = new TicTacToeClient(serverAddress);
            if (!client.error()) {
                client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                client.frame.setSize(860, 350);
                client.frame.setLocation(500, 500);
                client.frame.setVisible(true);
                client.frame.setResizable(false);
                client.play();

                if (!client.wantsToPlayAgain()) {
                    break;
                }
                else {
                    askAddress = false;
                }
            } else {
                System.exit(0);
            }
        }
    }

    private static class IP {

        String setIP() {
            int response = JOptionPane.showConfirmDialog(null,
                    "Server?",
                    "Input IP",
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                return "localhost";
            }
            else if (response == JOptionPane.NO_OPTION) {
                return JOptionPane.showInputDialog(null, "Input IP:", "Input", JOptionPane.QUESTION_MESSAGE);
            }
            else {
                System.exit(0);
            }
            return null;
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    private ImageIcon createImageIcon(String path,
                                      String description) {

        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        System.out.println(imgURL);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);

        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}