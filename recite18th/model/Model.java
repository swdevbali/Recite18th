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
public class Model {

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

    public String getFqn() {
        return fqn;
    }

    public String getPkFieldName() {
        return pkFieldName;
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

    
    protected Hashtable foreignFields = new Hashtable();
    public boolean isForeignField(String fieldName)
    {
        return foreignFields.get(fieldName)!=null;
    }
}
