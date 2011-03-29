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
package recite18th;

import application.config.Config;
import recite18th.library.Db;
import recite18th.controller.Controller;
import recite18th.util.ServletUtil;
import recite18th.util.StringUtil;
import java.io.IOException;
import java.lang.String;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Eko SW
 */
public class Index extends HttpServlet {

    protected HttpServletRequest request;
    protected HttpServletResponse response;

    @Override
    public void init() throws ServletException {
        super.init();
        Logger.getLogger(Index.class.getName()).log(Level.INFO, "Servlet Initialization");
        Db.init();
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //response.setContentType("text/html;charset=UTF-8");
        String[] path = request.getPathInfo() != null ? request.getPathInfo().split("/") : null;
        if (path == null) { //after index, no controller asked
            ServletUtil.dispatch("index/" + Config.firstController, request, response);
        } else {
            try {
                String controller =  path[1];//path[0] is index --this controller, path[1] is the asked controller
                String method = path.length == 2 ? "index" : path[2];//path[2] the default method, if not asked is index
                String className = "application.controllers." + StringUtil.firstCap(controller);//controller with one phrase
                Class cl = Class.forName(className);
                Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Menjalankan " + className + ",method " + method));
                Constructor constructor = cl.getConstructor();
                Controller objController = (Controller) constructor.newInstance();
                int paramCount = path.length - 3;
                Class params[] = null;
                if (paramCount > 0) {
                    params = new Class[paramCount];
                    for (int i = 0; i < paramCount; i++) {
                        params[i] = String.class;
                    }
                }
                Method objMethod = cl.getMethod(method, params);//if there's none, page404 is shown :)
                objController.setRequest(request);
                objController.setResponse(response);

                switch (paramCount) {
                    case -1:
                    case 0:
                        objMethod.invoke(objController);
                        Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Tanpa parameter"));
                        break;
                    case 1:
                        objMethod.invoke(objController, path[3]);
                        Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Dengan " + paramCount + " parameter : " + path[3]));
                        break;
                    case 2:
                        objMethod.invoke(objController, path[3], path[4]);
                        Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Dengan " + paramCount + " parameter : " + path[3] + "," + path[4]));
                        break;
                    case 3:
                        objMethod.invoke(objController, path[3], path[4], path[5]);
                        Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Dengan " + paramCount + " parameter : " + path[3] + "," + path[4] + "," + path[5]));
                        break;
                    case 4:
                        objMethod.invoke(objController, path[3], path[4], path[5], path[6]); //ini berarti index/class/method/1/2/3/4... sptnya dah cukup sekali... :)
                        Logger.getLogger(Index.class.getName()).log(Level.INFO, ("Dengan " + paramCount + " parameter : " + path[3] + "," + path[4] + "," + path[5] + "," + path[5]));
                        break;
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
                ServletUtil.dispatch("/" + Config.page404, request, response);
            } catch (SecurityException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Index.class.getName()).log(Level.SEVERE, null, ex);
                ServletUtil.dispatch("/" + Config.page404, request, response);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
