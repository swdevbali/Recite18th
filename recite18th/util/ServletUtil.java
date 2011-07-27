package recite18th.util;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletResponse;

public class ServletUtil {

    // constructors /////////////////////////////////////////////////////////////
    // constants ////////////////////////////////////////////////////////////////
    // classes //////////////////////////////////////////////////////////////////
    // methods //////////////////////////////////////////////////////////////////
    /**
     * NOT UNIT TESTED Returns the URL (including query parameters) minus the scheme, host, and
     * context path.  This method probably be moved to a more general purpose
     * class.
     */
    public static String getRelativeUrl(
            HttpServletRequest request) {

        String baseUrl = null;

        if ((request.getServerPort() == 80)
                || (request.getServerPort() == 443)) {
            baseUrl =
                    request.getScheme() + "://"
                    + request.getServerName()
                    + request.getContextPath();
        } else {
            baseUrl =
                    request.getScheme() + "://"
                    + request.getServerName() + ":" + request.getServerPort()
                    + request.getContextPath();
        }

        StringBuffer buf = request.getRequestURL();

        if (request.getQueryString() != null) {
            buf.append("?");
            buf.append(request.getQueryString());
        }

        return buf.substring(baseUrl.length());
    }

    /**
     * NOT UNIT TESTED Returns the base url (e.g, <tt>http://myhost:8080/myapp</tt>) suitable for
     * using in a base tag or building reliable urls.
     */
    /*public static String getBaseUrl(HttpServletRequest request) {
    if ((request.getServerPort() == 80)
    || (request.getServerPort() == 443)) {
    return request.getScheme() + "://"
    + request.getServerName()
    + request.getContextPath() + "/";
    } else {
    return request.getScheme() + "://"
    + request.getServerName() + ":" + request.getServerPort()
    + request.getContextPath() + "/";
    }
    }*/
    /**
     * Returns the file specified by <tt>path</tt> as returned by
     * <tt>ServletContext.getRealPath()</tt>.
     */
    public static File getRealFile(
            HttpServletRequest request,
            String path) {

        return new File(request.getSession().getServletContext().getRealPath(path));
    }

    public static void dispatch(String viewPage, HttpServletRequest request, HttpServletResponse response) {
        RequestDispatcher rd = request.getRequestDispatcher(viewPage);
        try {
            if (rd != null) {
                Logger.getLogger(ServletUtil.class.getName()).log(Level.INFO, "Dispatch ke " + viewPage);
                rd.forward(request, response);
            } else {
                PrintWriter out = response.getWriter();
                out.print("can not dispatch to " + viewPage);
            }
        } catch (Exception ex) {
            Logger.getLogger(ServletUtil.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public static void redirect(String page, HttpServletRequest request, HttpServletResponse response)  {
        try
        {
            response.sendRedirect(page);
        }catch(IOException ex)
        {
            Logger.getLogger(ServletUtil.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);         
        }
    }
    // properties ///////////////////////////////////////////////////////////////
    // attributes ///////////////////////////////////////////////////////////////
}
