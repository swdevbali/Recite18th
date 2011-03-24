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

/* HISTORY
   Mar 22, 2011 = first created
 */

package recite18th.util;
import java.io.*;
import recite18th.library.Db;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.*;
import java.lang.reflect.*;
import recite18th.model.Model;
import application.models.*;

public class GenerateForm 
{
    public static void main(String arg[])
    {
        System.out.println("Synchronizing forms with database...");
        Db.init();

        try{
            DatabaseMetaData meta = Db.getCon().getMetaData();
            String[] types = {"TABLE"};
            ResultSet rs = meta.getTables(null,null,"%",types);
           
            //prepare directory
            File fDir = new File("../../web/WEB-INF/views/crud_form");
            if(!fDir.exists()) fDir.mkdir();
            while (rs.next()){
                //proper file name generation
                String className = "";
                String tableName = rs.getString("TABLE_NAME");
                className = StringUtil.toProperClassName(tableName);//space allowed...
                
                //open table
                String sql = "select * from " + tableName;
                PreparedStatement pstmt = Db.getCon().prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = pstmt.executeQuery();
                ResultSetMetaData metaColumn = resultSet.getMetaData();
                int nColoumn = metaColumn.getColumnCount();
                
                //create crud file
                System.out.println("Creating crud form " + tableName +" from table + " + application.config.Database.DB + "." + tableName);
                File f = new File("../../web/WEB-INF/views/crud_form/" + tableName + ".jsp");
                Writer out = new FileWriter(f);

                //iterate all columns
                resultSet.beforeFirst();
                resultSet.next();
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i);
                    String dataType = metaColumn.getColumnClassName(i);       
                    
                    out.write("<input name=\""+columnName+"\" type=\"text\" id=\""+columnName+"\" value=\"${model."+columnName+"}\">\")\n");

                }
                out.flush();
                out.close();                           

                //create viewPage
                System.out.println("Creating view page " + tableName);
                fDir = new File("../../web/WEB-INF/views/" + tableName);
                if(!fDir.exists()) fDir.mkdir();
                File fView = new File("../../web/WEB-INF/views/" + tableName + "/view_"+ tableName + ".jsp");
                Writer outView = new FileWriter(fView);                
                outView.write("<a href=\"<%=Config.base_url%>index/"+ className + "/input/-1\">Tambah Data</a>\n");
                outView.write("<table width=\"100%\" id=\"rounded-corner\">\n");
                outView.write("<thead>\n");
                //iterate all columns : table header
                outView.write("  <tr>\n");
                outView.write("  <th scope=\"col\" class=\"rounded-company\">No.</th>\n");
                resultSet.beforeFirst();
                resultSet.next();
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i);
                    String dataType = metaColumn.getColumnClassName(i);       
                    String thClass = "rounded-q1";
                    String thTitle = StringUtil.toProperFieldTitle(columnName);
                    outView.write("  <th scope=\"col\" class=\"" + thClass + "\">"+thTitle+"</th>\n");
                }
                outView.write("  <th scope=\"col\" class=\"rounded-q4\">Aksi</th>\n");
                outView.write("  </tr>\n");
                outView.write("</thead>\n");
                outView.write("<tfoot>\n");
                outView.write("  <tr>\n");
                outView.write("    <td colspan=\"2\" class=\"rounded-foot-left\"><%=Pagination.createLinks(pagenum)%></td>\n");
                outView.write("    <td class=\"rounded-foot-right\">&nbsp;</td>\n");
                outView.write("  </tr>\n");
                outView.write("</tfoot>\n");

                outView.write("<tbody>\n");
                outView.write("  <c:forEach items=\"${row}\" var=\"item\" varStatus=\"status\" >\n");
                outView.write("    <tr>\n");
                outView.write("      <td>${status.count}</td>\n");
                
                //iterate all columns : table data
                
                String pkFieldName="";
                try {
                    Class cl = Class.forName("application.models." + className + "Model");
                    Class params[] = null;
                    Method objMethod = cl.getMethod("getPkFieldName", params);
                    Model model = (Model) cl.newInstance();
                    pkFieldName = "" + objMethod.invoke(model);
                } catch (Exception ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }
                resultSet.beforeFirst();
                resultSet.next();
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i);
                    outView.write("      <td>${item."+columnName + "}</td>\n");
                }
                
                outView.write("      <td>\n");
                outView.write("         <a href=\"<%=Config.base_url%>index/"+ className + "/input/${item." + pkFieldName + "}\">Ubah</a>\n");
                outView.write("         <a href=\"<%=Config.base_url%>index/" + className + "/delete/${item."+ pkFieldName +"}\" onClick=\"return confirm('Apakah Anda yakin?');\">Hapus</a>\n");
                outView.write("      </td>\n");

                outView.write("    </tr>\n");
                outView.write("  </c:forEach>\n");
                outView.write("</tbody>\n");
                outView.write("</table>\n");

                outView.flush();
                outView.close();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}