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
   Apr 20, 2011 = About to implement form generator
   Jul 21, 2011 = About to implement PK entry field (previously, it was hidden and assumed to be of autoincrement value)
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
import application.config.TableCustomization;
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
                //proper file name generationm
                String className = "";
                String tableName = rs.getString("TABLE_NAME");
                className = StringUtil.toProperClassName(tableName);//space allowed...
                //tableName = tableName.toUpperCase(); //If Oracle that need uppercase tablename. In MySQL in Mac OS X (and probably Linux), it mustbe case sensitive
                //open table
                String sql = "select * from " + tableName;
                PreparedStatement pstmt = Db.getCon().prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = pstmt.executeQuery();
                ResultSetMetaData metaColumn = resultSet.getMetaData();
                int nColoumn = metaColumn.getColumnCount();
                
                //get foreign keys,and stored it in hashtable
                ResultSet rsFk = meta.getImportedKeys(Db.getCon().getCatalog(), null, tableName);
                Hashtable hashFk = new Hashtable();
                System.out.println("FK Infos for table " + tableName);
                while (rsFk.next()) {
                    String pkTableName = rsFk.getString("PKTABLE_NAME");
                    String pkColumnName = rsFk.getString("PKCOLUMN_NAME");
                    String fkColumnName = rsFk.getString("FKCOLUMN_NAME");

                    int fkSequence = rsFk.getInt("KEY_SEQ");
                    System.out.println(tableName+"."+fkColumnName + " => " + pkTableName + "." + pkColumnName);
                    hashFk.put(fkColumnName, pkColumnName);
                    hashFk.put(fkColumnName + "_table", pkTableName);
                }
                rsFk.close();

                //create form page
                System.out.println("Creating form page for " + tableName +" from table + " + application.config.Database.DB + "." + tableName);
                fDir = new File("../../web/WEB-INF/views/" + tableName);
                if(!fDir.exists()) fDir.mkdir();
                File f = new File("../../web/WEB-INF/views/" + tableName + "/form_" + tableName + ".jsp");
                Writer out = new FileWriter(f);
                out.write("<%@ page contentType=\"text/html; charset=UTF-8\" language=\"java\" import=\"java.sql.*,recite18th.library.Db,application.config.Config,recite18th.library.Pagination\" %>");
                out.write("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\" %>\n");
                out.write("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/functions\" prefix=\"fn\" %>\n");


                //create model for this class, use in detecting its PK Field
                Class cl = Class.forName("application.models." + className + "Model");
                Model model = (Model) cl.newInstance();
                
                //iterate all columns
                resultSet.beforeFirst();
                resultSet.next();
                out.write("<table border=\"1\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" bordercolor=\"#E8EDFF\">\n");
                out.write("<tr>\n");
                out.write("<td>\n");
                out.write("<form action=\"<%=Config.base_url%>index/" + className + "/save\">\n");
                out.write("<table id=\"hor-zebra\" summary=\"Form "+ className + "\">\n");
                out.write("<thead>\n");
                out.write("<tr>\n");
                out.write("<th colspan=\"2\" class=\"odd\" scope=\"col\">Form " + className + " </th>\n");
                out.write("</tr>\n");
                out.write("</thead>\n");
                out.write("<tbody>\n");
                                
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i);
                    String dataType = metaColumn.getColumnClassName(i);
                    out.write("<tr>\n");
    
                    //if(!columnName.equals(model.getPkFieldName())) // implementing the case of PK not AutoIncrement
                    //if(!metaColumn.isAutoIncrement(i))
                    //{
                        // varying field input for type
                        
                        //foreign field, as chooser page view
                        if(hashFk.get(columnName)!=null)
                        {
                            String fkTableName = hashFk.get(columnName+"_table") + "";
                            String fkColumnName = hashFk.get(columnName) + "";
                            String fkController = StringUtil.toProperClassName(fkTableName);

                            out.write("<td>" + columnName + "</td>\n");
                            out.write("<td>\n");
                            out.write("<input name=\""+columnName+"\" type=\"hidden\" id=\""+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                            out.write("<input name=\"label_"+columnName+"\" readonly=\"true\" type=\"text\" id=\"label_"+columnName+"\" value=\"${model."+columnName+"}\"/>\n");//TODO : translate I
                            out.write("<a href=\"<%=Config.base_url%>index/"+fkController+"/chooseView?height=220&width=700\" class=\"thickbox\">Pilih</a>");
                            out.write("</td>\n");                            
                        }
                        else 
                        {
                            
                            out.write("<td>" + columnName + "</td>\n");
                            out.write("<td>\n");

                            
                            // regular field input, not foreign key case
                            if(!columnName.equals(model.getPkFieldName()))
                            {
                                
                                // ENUM Column, displayed as HTML SELECT. May will only work for mysql only...
                                if(metaColumn.getColumnType(i)==1)
                                {
                                    String enum_content[][] = Db.getDataSet("SELECT SUBSTRING(COLUMN_TYPE,6,length(SUBSTRING(COLUMN_TYPE,6))-1) as enum_content " +
                                                                            " FROM information_schema.COLUMNS " +
                                                                            " WHERE TABLE_NAME='" + tableName + "' " +
                                                                            " AND COLUMN_NAME='" + columnName + "'");
                                    if(enum_content.length>0)
                                    {
                                        //Logger.getLogger(Model.class.getName()).log(Level.INFO, "Enum Content = " + enum_content[0][0]);
                                        String enum_list[] = enum_content[0][0].split(",");
                                        out.write("<select name=\"" + columnName + "\" id=\"" + columnName + "\">\n");
                                        for(int ienum_list=0; ienum_list < enum_list.length; ienum_list++)
                                            out.write("\t<option <c:if test=\"${model."+ columnName +"=='" + enum_list[ienum_list].substring(1,enum_list[ienum_list].length()-1) + "'}\"> selected=\"selected\" </c:if> value=\"" + enum_list[ienum_list].substring(1,enum_list[ienum_list].length()-1) + "\">" + enum_list[ienum_list].substring(1,enum_list[ienum_list].length()-1) + "</option>\n");
                                        out.write("</select>\n\n");
                                        
                                    }else{
                                        // no enum content detected.. :)
                                        out.write("<input name=\""+columnName+"\" type=\"text\" id=\""+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                                    }
                                }else{
                                    out.write("<input name=\""+columnName+"\" type=\"text\" id=\""+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                                }
                            } else { // PK case
                                if(metaColumn.isAutoIncrement(i))
                                {
                                    out.write("<input name=\"hidden_"+columnName+"\" type=\"hidden\" id=\"hidden_"+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                                }else{
                                    out.write("<input name=\""+columnName+"\" type=\"text\" id=\""+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                                    out.write("<input name=\"hidden_"+columnName+"\" type=\"hidden\" id=\"hidden_"+columnName+"\" value=\"${model."+columnName+"}\"/>\n");
                                }
                            }
                            out.write("</td>\n");
                        }
                        //}else
                        //{
                        //hide PK if it's autoincrement.

                        //}
                out.write("</tr>\n");
            }
            out.write("<tr class=\"odd\">\n");
            out.write("<td>&nbsp;</td>\n");
            out.write("<td><input type=\"submit\" name=\"Submit\" value=\"Simpan\">");
            out.write("<input name=\"Button\" type=\"button\" id=\"Submit\" value=\"Batal\" onClick=\"javascript:history.back(-1);\"></td>\n");
            out.write("</tr>\n");
            out.write("</tbody>\n");
            out.write("</table>\n");
            out.write("</form></td>\n");
            out.write("</tr>\n");
            out.write("</table>\n");
                out.flush();
                out.close();                           

                //create viewPage
                System.out.println("Creating view page " + tableName);
                fDir = new File("../../web/WEB-INF/views/" + tableName);
                if(!fDir.exists()) fDir.mkdir();
                File fView = new File("../../web/WEB-INF/views/" + tableName + "/view_"+ tableName + ".jsp");
                Writer outView = new FileWriter(fView);                
                outView.write("<%@ page contentType=\"text/html; charset=UTF-8\" language=\"java\" import=\"java.sql.*,recite18th.library.Db,application.config.Config,recite18th.library.Pagination\" %>");
                outView.write("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\" %>\n");
                outView.write("<%@ taglib uri=\"http://java.sun.com/jsp/jstl/functions\" prefix=\"fn\" %>\n");
                outView.write("<% int pagenum = 0; %>\n");
                //outView.write("<%@ include file=\"/WEB-INF/views/header.jsp\" %>");
                outView.write("<a href=\"<%=Config.base_url%>index/"+ className + "/input/-1\">Tambah Data</a>\n");
                outView.write("<table width=\"100%\" id=\"rounded-corner\">\n");
                outView.write("<thead>\n");
                //iterate all columns : table header
                outView.write("  <tr>\n");
                outView.write("  <th scope=\"col\" class=\"rounded-company\">No.</th>\n");
                resultSet.beforeFirst();
                resultSet.next();

                //get Primary Key Field Name : often use
                String pkFieldName="";
                try {
                    Class params[] = null;
                    Method objMethod = cl.getMethod("getPkFieldName", params);
                    pkFieldName = "" + objMethod.invoke(model);
                } catch (Exception ex) {
                    Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
                }

                //ALL Lower Case
                pkFieldName = pkFieldName.toLowerCase();

                //customize column view page
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i).toLowerCase(); //Caution : ALL LowerCase
                    String dataType = metaColumn.getColumnClassName(i);       
                    String thClass = "rounded-q1";
                    String thTitle = StringUtil.toProperFieldTitle(columnName);

                    if(TableCustomization.getTable(tableName)!=null) // there is customization for this table
                    {
                        if(TableCustomization.getTable(tableName).get(columnName)!=null)
                        {
                            thTitle = TableCustomization.getTable(tableName).get(columnName) + "";
                            outView.write("  <th scope=\"col\" class=\"" + thClass + "\">"+thTitle+"</th>\n");
                        }
                    }else{ // standard view for this table : hide PK, because mostly is auto increment
                        if(!metaColumn.isAutoIncrement(i))
                            outView.write("  <th scope=\"col\" class=\"" + thClass + "\">"+thTitle+"</th>\n");
                    }
                }
                outView.write("  <th scope=\"col\" class=\"rounded-q4\">Aksi</th>\n");
                outView.write("  </tr>\n");
                outView.write("</thead>\n");
                outView.write("<tfoot>\n");
                outView.write("  <tr>\n");
                outView.write("    <td colspan=\"" + (nColoumn + 1) +"\" class=\"rounded-foot-left\"><%=Pagination.createLinks(pagenum)%></td>\n");
                outView.write("    <td class=\"rounded-foot-right\">&nbsp;</td>\n");
                outView.write("  </tr>\n");
                outView.write("</tfoot>\n");

                outView.write("<tbody>\n");
                outView.write("  <c:forEach items=\"${row}\" var=\"item\" varStatus=\"status\" >\n");
                outView.write("    <tr>\n");
                outView.write("      <td>${status.count}</td>\n");
                
                //iterate all columns : table data
                resultSet.beforeFirst();
                resultSet.next();
                for (int i = 1; i <= nColoumn; i++) {
                    String columnName = metaColumn.getColumnName(i);
                    //if(!columnName.equals(pkFieldName)) //TOFIX : currently, PK Field is not shown
                    if(TableCustomization.getTable(tableName)!=null)
                    {
                        if(TableCustomization.getTable(tableName).get(columnName)!=null)
                        {                        
                            outView.write("      <td>${item."+columnName + "}</td>\n");
                        }
                    }else{
                        if(!metaColumn.isAutoIncrement(i))
                            outView.write("      <td>${item."+columnName + "}</td>\n");
                    }
                      
                    
                }
                
                outView.write("      <td>\n");
                outView.write("         <a href=\"<%=Config.base_url%>index/"+ className + "/input/${item." + pkFieldName + "}\">Ubah</a>\n");
                outView.write("         <a href=\"<%=Config.base_url%>index/" + className + "/delete/${item."+ pkFieldName +"}\" onClick=\"return confirm('Apakah Anda yakin?');\">Hapus</a>\n");
                outView.write("      </td>\n");

                outView.write("    </tr>\n");
                outView.write("  </c:forEach>\n");
                outView.write("</tbody>\n");
                outView.write("</table>\n");
                //outView.write("<%@ include file=\"/WEB-INF/views/footer.jsp\" %>");
                outView.flush();
                outView.close();
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    //create form page (this should be easy)
}