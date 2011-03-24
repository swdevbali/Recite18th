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
  1) Forgot when I first create this
  2) Mar 23, 2011 = reshape to a better CI activeRecordset things
 */

package recite18th.model;

import recite18th.library.Db;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Model resemble all fields in a table. This is call primary field
 * Whereas if a foreign key is needed to be translated into its foreign value,
 * we must also add that foreign value in the coresponding model.
 * 
 * @author Eko SW
 */
public class Model
{
    String criteria="";
    protected boolean isExist = false;
    public Model createNewModel() {
        Model model = null;
        try {
            Class cl = Class.forName(fqn);
            model = (Model) cl.newInstance();
        } catch (Exception ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        }
        return model;
    }

    public String getTableName() {
        return tableName;
    }
    protected String tableName;
    protected String pkFieldName, pkFieldValue;

    public Model() {
    }

    public String getPkFieldValue() {
        return pkFieldValue;
    }

    public void setPkFieldValue(String pkFieldValue) {
        this.pkFieldValue = pkFieldValue;
    }
    protected String fqn;
    protected String plainClassName;

    public String getPlainClassName() {
        return plainClassName;
    }

    public void setPlainClassName(String plainClassName) {
        this.plainClassName = plainClassName;
    }

    public String getFqn() {
        return fqn;
    }

    public String getPkFieldName() {
        return pkFieldName;
    }

    public void setPkFieldName(String pkFieldName) {
        this.pkFieldName = pkFieldName;
    }

    public Model(String table, String pkFieldName) {
        this.fqn = this.tableName = table;
        this.pkFieldName = pkFieldName;
    }

    /**
     * here we go. 1st challenge : how to enforce data type restriction
     * in insert/update query? How do we know that certain field require
     * certain data type??? ^_^
     * @param params
     */
    public void update(Hashtable params) {
        String sql = "update `" + tableName + "` set ";
        Enumeration e;
        String key, value;

        e = params.keys();
        while (e.hasMoreElements()) {
            key = e.nextElement() + "";
            value = params.get(key) + "";
            sql = sql + key + "='" + value.trim() + "'";
            if (e.hasMoreElements()) {
                sql = sql + ",";
            }
        }

        sql = sql + " where " + pkFieldName + "=" + pkFieldValue.trim();
        System.out.println(sql);
        Db.executeQuery(sql);
    }

    public void insert(Hashtable params) {
        String sql = "insert into `" + tableName + "` (";
        Enumeration e;
        String key, value;

        e = params.keys();
        while (e.hasMoreElements()) {
            key = e.nextElement() + "";
            sql = sql + key;
            if (e.hasMoreElements()) {
                sql = sql + ",";
            }
        }
        sql = sql + ") values (";

        e = params.keys();
        while (e.hasMoreElements()) {
            key = e.nextElement() + "";
            value = params.get(key) + "";
            sql = sql + "'" + value.trim() + "'";
            if (e.hasMoreElements()) {
                sql = sql + ",";
            }
        }
        sql = sql + ")";
        System.out.println(sql);

        Db.executeQuery(sql);

    }

    public void delete(String condition) {
        String sql;
        sql = "delete from `" + tableName + "` where " + pkFieldName + "='" + condition +"'";
        System.out.println(sql);
        Db.executeQuery(sql);
    }

    public void save(Hashtable params) {
        //null == "" == -1 for database application ;)
        if (pkFieldValue==null || "".equals(pkFieldValue)||"-1".equals(pkFieldValue)) {
            insert(params);
        } else {
            update(params);

        }
    }

    public List getDataPerPage(String sql) {
        return Db.get(sql, fqn);
    }
    public List getAllData(){
        return Db.get("select * from " + tableName, fqn);
    }

    public Model getModelById(String pkFieldValue) {
        return  (Model) Db.getById(tableName,pkFieldName,fqn, pkFieldValue);
    }

// foreign field is useless, because we separate between user modifed model and system imported model    
//    protected Hashtable foreignFields = new Hashtable();
/*    public boolean isForeignField(String fieldName)
    {
        return foreignFields.get(fieldName)!=null;
        }*/
    public void addCriteria(String fieldName, String fieldValue)
    {
        
        //listCriteria.put(fieldName, fieldValue);
        if(!criteria.equals(""))
        {
            criteria =  criteria + " and " + fieldName + " = '" + fieldValue + "'";                 
        }else
        {
            criteria =  fieldName + " = '" + fieldValue + "'";
        }

    }

    // Get data for this model, can be single row, or multiple row
    public Object getModel()
    {
        String sql="select * from " + tableName;
        if(!criteria.equals("")) sql =  sql + " where " + criteria;
        Object result = Db.getBySql(sql, fqn);
        isExist = result!=null;
        return result;

    }

    public boolean exist()
    {
        if(!isExist) Logger.getLogger(Model.class.getName()).log(Level.INFO, "Data don't exist, don't forget to call get() first!");
        return isExist;
    }
}
