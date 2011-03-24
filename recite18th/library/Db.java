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

/*
  HISTORY
  1) forgot when first create this file
  2) Mar 23, 2011 = about to create a SQL-less model/controller
 */

package recite18th.library;

import application.config.Database;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;

public class Db {

    private static Connection con;

    public static Connection getCon() {
        try {
            if (con == null) {
                init();
            } else if (con.isClosed()) {
                openConnection();
            }
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        return con;
    }

    public static int getLastInsertId() {
        String[][] data = getDataSet("select LAST_INSERT_ID()");
        if(data.length>0){
            return Integer.parseInt(data[0][0]);
        }else{
            return -1;
        }
    }

    public static void init() {
        Logger.getLogger(Db.class.getName()).log(Level.INFO, "Database Initialization");
        if (con == null & !Database.DB.equals("")) {
            try {
                if (Database.DB_TYPE.equals("mysql")) {
                    Class.forName("com.mysql.jdbc.Driver");
                } else if (Database.DB_TYPE.equals("mssql")) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                } else {
                    Class.forName("sun.jdbc.odbc.JdbcOdbc");
                }

            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);

            }
            openConnection();
        }
    }

    private static void openConnection() {
        String urldb = "";
        try {
            urldb = getUrl();
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "openConnectin : {0}", urldb);
            if (Database.DB_TYPE.equals("mysql")) {
                con = DriverManager.getConnection(urldb, Database.USER_NAME, Database.PASSWORD);
            } else if (Database.DB_TYPE.equals("mssql")) {
                con = DriverManager.getConnection(urldb, Database.USER_NAME, Database.PASSWORD);
            } else {
                con = DriverManager.getConnection(urldb); //
            }
        } catch (Exception ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int findValueAsInt(String table, String value, String where) {
        String val = findValue(table, value, where);
        if (val.equals("")) {
            return 0;
        }
        return Integer.parseInt(val);
    }

    public static double findValueAsDouble(String table, String value, String where) {
        String val = findValue(table, value, where);
        if (val.equals("")) {
            return 0;
        }
        return Double.parseDouble(val);
    }

    public static String findValue(String table, String value, String where) {
        String sql = "select " + value + " from " + table + " where " + where;
        String[][] data = getDataSet(sql);
        if (data.length == 0) {
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "findValue: empty.");
            return "";
        } else {
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "findValue: {0}.", data[0][0]);
            return data[0][0];
        }
    }

    public static void closeConnection() {
        try {
            getCon().close();
            con = null;
        } catch (Exception ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int selectLastInsertedId() {
        String sql = "select last_insert_id()";
        String data[][] = getDataSet(sql);
        if (data.length > 0) {
            return Integer.parseInt(data[0][0]);
        }
        return -1;
    }

    public static boolean executeQuery(String sql) {
        try {
            Statement statement = getCon().createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, "executeQuery : {0}", sql);
            statement.execute(sql);

            return true;
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public static ResultSet selectQuery(String sql) {
        try {
            sql = sql.toUpperCase();
            Statement stmt = getCon().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = stmt.executeQuery(sql);
            return resultSet;
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static void update(String table, String value, String where) {
        if (!"".equals(where)) {
            executeQuery("update " + table + " set " + value + " where " + where);
        } else {
            executeQuery("update " + table + " set " + value);
        }
    }

    public static String[][] processDataSetResultSet(ResultSet resultSet) {
        try {
            ResultSetMetaData metaData;
            int indexOfRow = 0; // index dimulai dari nol.
            int nColoumn;
            int nRow;
            String[][] result;


            metaData = resultSet.getMetaData();
            nColoumn = metaData.getColumnCount();

            resultSet.last(); // menuju paling baris terakhir
            nRow = resultSet.getRow();
            result = new String[nRow][];
            resultSet.beforeFirst();
            while (resultSet.next()) {
                // disini skalian langsung ke baris berikutnya.
                result[indexOfRow] = new String[nColoumn];
                for (int i = 0; i < nColoumn; i++) {
                    ////LoggingWindow.addToLog(nColoumn);
                    result[indexOfRow][i] = resultSet.getString(i + 1);

                }
                indexOfRow++;
            }
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String[][] getCallableDataSet(String sql) {
        try {
            CallableStatement cstmt = getCon().prepareCall(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            return processDataSetResultSet(cstmt.executeQuery());
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static String[][] getDataSet(String sqlStatement) {
        try {
            Statement stmt = getCon().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "getDataSet:" + sqlStatement);
            return processDataSetResultSet(stmt.executeQuery(sqlStatement));

        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

    }

    public static String getUrl() {
        String url;
        if (Database.DB_TYPE.equals("mysql")) {
            url = "jdbc:mysql://" + Database.HOST + ":" + Database.PORT + "/" + Database.DB;
        } else if (Database.DB_TYPE.equals("mssql")) {//jdbc:sqlserver://localhost:1433;database=smk2depo_smk2depok";
            url = "jdbc:sqlserver://" + Database.HOST + ":" + Database.PORT + ";database=" + Database.DB;
        } else {
            url = "jdbc:odbc:" + Database.DB;
        }
        return url;
    }

    public static void shutdownDatabase() {
        con = null;
    }

    public static List get(String sqlSelect, String fqnModel) {
        List list = new ArrayList();
        try {
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "get : {0}", sqlSelect);
            PreparedStatement pstmt = getCon().prepareStatement(sqlSelect, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            list = getDataSetAsArrayList(pstmt, fqnModel);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    private static ArrayList getDataSetAsArrayList(PreparedStatement prepStmtFillControl, String fqnModel) {
        try {
            return processDataSetResultSetAsArrayList(prepStmtFillControl.executeQuery(), fqnModel);
        } catch (SQLException ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static ArrayList processDataSetResultSetAsArrayList(ResultSet resultSet, String fqnModel) {
        ArrayList result = new ArrayList();

        try {
            ResultSetMetaData metaData;
            int nColoumn;
            String columnName;
            String fieldValue;
            Field field;
            Object modelInstance;

            metaData = resultSet.getMetaData();
            nColoumn = metaData.getColumnCount();
            resultSet.beforeFirst();
            Class modelClass = Class.forName(fqnModel);
            
            while (resultSet.next()) {
                modelInstance = modelClass.newInstance();
                for (int i = 1; i <= nColoumn; i++) {
                    columnName = metaData.getColumnName(i);
                    fieldValue = resultSet.getString(i);
                    PropertyUtils.setSimpleProperty(modelInstance, columnName,fieldValue);
                    //the good ol'ways.. don't use BeanUtils... The problem is, how can it able to get the 
                    //field from super class??
                    //field = modelInstance.getClass().getDeclaredField(columnName);                    
                    //field.set(modelInstance, fieldValue);
                    
                }
                result.add(modelInstance);
            }
        } catch (Exception ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    /*
     * Eventually, this will be the core method to get object from database
     */
    public static Object getBySql(String sql, String fqn)
    {
        Object modelInstance = null;
        try {
            Class modelClass = Class.forName(fqn);
            ResultSetMetaData metaData;
            String columnName;
            String fieldValue;
            Field field;

            PreparedStatement pstmt = getCon().prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Logger.getLogger(Db.class.getName()).log(Level.INFO, sql);
            ResultSet resultSet = pstmt.executeQuery();
            metaData = resultSet.getMetaData();
            int nColoumn = metaData.getColumnCount();
            resultSet.beforeFirst();
            Logger.getLogger(Db.class.getName()).log(Level.INFO, "About to process data");
            if(resultSet.next())
            {
                Logger.getLogger(Db.class.getName()).log(Level.INFO, "Data exist");
                modelInstance = modelClass.newInstance();
                for (int i = 1; i <= nColoumn; i++) {
                    //the good ol'ways.. don't use BeanUtils... The problem is, how can it able to get the 
                    //field from super class??
                    //field = modelInstance.getClass().getDeclaredField(columnName);
                    //field.set(modelInstance, fieldValue);
                    columnName = metaData.getColumnName(i);                
                    fieldValue = resultSet.getString(i);                
                    PropertyUtils.setSimpleProperty(modelInstance, columnName,fieldValue);
                }                
            } else{ 
                Logger.getLogger(Db.class.getName()).log(Level.INFO, "Data !exist");
            }
        } catch (Exception ex) {
            Logger.getLogger(Db.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return modelInstance;
    }

    
    public static Object getById(String tableName, String pkFieldName, String fqn, String pkFieldValue) {        
        String sql = "select * from " + tableName + " where " + pkFieldName + "='" + pkFieldValue + "'";
        return getBySql(sql,fqn);
    }
}
