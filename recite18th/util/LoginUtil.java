package recite18th.util;

import javax.servlet.http.HttpServletRequest;
import recite18th.model.Model;
import recite18th.model.Roleable;

public class LoginUtil 
{
    //todo : change it to Model for better casting
    public static Model getLogin(HttpServletRequest request)
    {
        return  (Model) request.getSession().getAttribute("user_credential");
    }
    
    public static boolean isLogin(HttpServletRequest request)
    {
        return getLogin(request)!=null;
    }

    public static String getLoginRole(HttpServletRequest request)
    {
        Model userCredential = getLogin(request);
        return ((Roleable) userCredential).getRole();
    }

    public static void setLogin(HttpServletRequest request, Object userCredential)
    {
        request.getSession().setAttribute("user_credential", userCredential);
    }
    
    public static void clearLogin(HttpServletRequest request)
    {
        request.getSession().removeAttribute("user_credential");
    }
}