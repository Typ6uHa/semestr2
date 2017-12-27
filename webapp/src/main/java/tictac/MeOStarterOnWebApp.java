package tictac;

import com.meo.MeOServer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


public class MeOStarterOnWebApp implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {

        Thread serverThread = new Thread() {
            public void run() {
                MeOServer server = new MeOServer();
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

