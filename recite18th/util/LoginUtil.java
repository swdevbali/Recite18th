package recite18th.util;

import javax.servlet.http.HttpServletRequest;

public class LoginUtil 
{
    //todo : change it to Model for better casting
    public static Object getLogin(HttpServletRequest request)
    {
        return  request.getSession().getAttribute("user_credential");
    }
}