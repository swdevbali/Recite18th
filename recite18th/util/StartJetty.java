/*
    Recite18th is a simple, easy to use and straightforward Java Database 
    Web Application Framework. See http://code.google.com/p/recite18th
    Copyright (C) 2011  Eko Suprapto Wibowo (swdev.bali@gmail.com)

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see http://www.gnu.org/licenses/.
*/

// this file source was from http://www.draconianoverlord.com/ Many thanks!

package recite18th.util;
import application.config.Config;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.jetty.webapp.WebInfConfiguration;
import org.mortbay.jetty.webapp.WebXmlConfiguration;

public class StartJetty {
    private static final Server SERVER = new Server();
    public static void main(String[] args) 
    {
        String webapp = Config.base_path;
        if (args.length > 0) 
        {
            webapp = args[0];
        }
 
        WebAppContext app = new WebAppContext();
        app.setContextPath(Config.application_context);
        app.setWar(webapp);
        // Avoid the taglib configuration because its a PITA if you don't have a net connection
        app.setConfigurationClasses(new String[] { WebInfConfiguration.class.getName(), WebXmlConfiguration.class.getName() });
        app.setParentLoaderPriority(true);
       // We explicitly use the SocketConnector because the SelectChannelConnector locks files
        Connector connector = new SocketConnector();
        connector.setPort(Integer.parseInt(System.getProperty("jetty.port", "8080")));
        connector.setMaxIdleTime(60000);
        StartJetty.SERVER.setConnectors(new Connector[] { connector });
        StartJetty.SERVER.setHandlers(new Handler[] { app });
        StartJetty.SERVER.setAttribute("org.mortbay.jetty.Request.maxFormContentSize", 0);
        StartJetty.SERVER.setStopAtShutdown(true);
        try
        {
            StartJetty.SERVER.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
