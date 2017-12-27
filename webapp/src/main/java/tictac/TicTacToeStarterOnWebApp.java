package tictac;

import edu.lmu.cs.networking.TicTacToeServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class TicTacToeStarterOnWebApp implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {

        Thread serverThread = new Thread() {
            public void run() {
                TicTacToeServer server = new TicTacToeServer();
                try

                {
                    server.main(null);
                } catch (
                        Exception e
                        )

                {
                    e.printStackTrace();
                }
            }
        };
        serverThread.start();
    }


    public void contextDestroyed(ServletContextEvent sce) {

    }
}
