package recite18th.controller;

import application.config.Config;
import recite18th.model.Model;
import recite18th.util.ServletUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//multi-part data
import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.servlet.*;
import org.apache.commons.fileupload.disk.*;
import java.io.*;
import java.util.*;
import org.apache.commons.beanutils.PropertyUtils;

public class Controller extends HttpServlet {

    protected Model modelForm;
    protected Model modelRow;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String viewPage;
    protected String formPage;
    private Hashtable params; //parameter for sql INSERT/UPDATE manipulation
    private HashMap formParams = new HashMap(); //parameter populate by form. I make it private, I want to make sure all field data get by method getFormFieldValue
    List row = new ArrayList();
    protected String controllerName;
    protected String sqlViewDataPerPage;
    boolean isMultipart;

    public void doSave() {
        modelForm.save(fillParams());
    }

    public void goAfterSave() {
        try {
            ServletUtil.redirect(Config.base_url + "index/" + controllerName, request, response);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isNewData() {
        //get field value of primary key
        modelForm.setPkFieldValue(getFormFieldValue(modelForm.getPkFieldName()));
        boolean bIsNewData = "".equals(modelForm.getPkFieldValue());
        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "isNewData = " + bIsNewData);
        return bIsNewData;
    }

    public void save() {
        processFormData();
        if (validationRun()) {
            doSave();
            goAfterSave();
        } else {
            //masukkan nilai yg tadi dimasukkan.. hadeuh...            
            input("-2");//MAGIC!
        }


    }

    /**
     * Open main view for this controller
     */
    public void index() {
        Model model;
        if (modelRow == null) {
            model = modelForm;
        } else {
            model = modelRow;
        }
        if (model != null) {
            if (sqlViewDataPerPage == null) {
                sqlViewDataPerPage = "select * from " + model.getTableName();
            }
            row = model.getDataPerPage(sqlViewDataPerPage);
            request.setAttribute("row", row);
        }
        try {
            ServletUtil.dispatch("/WEB-INF/views/" + viewPage, request, response);
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            ServletUtil.dispatch("/WEB-INF/views/" + viewPage, request, response);
        }
    }

    /**
     * open another view for this controller
     * @param page
     */
    public void index(String page) {
        ServletUtil.dispatch("/WEB-INF/views/" + page, request, response);
    }

    public void input(String pkFieldValue) {
        try {
            if (pkFieldValue.equals("-2")) {
                //Drawbacks : semua field harus didefinisikan jenis validasinya. dan itu ga baik. TODO : ubah ke formParams
                // dan sesuaikan dengan ada/tidaknya fieldnya dari model. Jika ada, baru diset. Jika tidak, berarti kontrol lain,e.g, Submit
                Enumeration e = validation.keys();
                modelForm = modelForm.createNewModel();
                while (e.hasMoreElements()) {
                    String ruleName = (String) e.nextElement();
                    String value = getFormFieldValue(ruleName);
                    PropertyUtils.setSimpleProperty(modelForm, ruleName, value);
                    Logger.getLogger(Controller.class.getName()).log(Level.INFO, "validasi error, mengisi kembali " + ruleName + ", dengan value = " + value);
                }
            } else if (pkFieldValue == null || pkFieldValue.equals("") || pkFieldValue.equals("-1")) {
                Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Buat Model baru");
                modelForm = modelForm.createNewModel();
            } else {
                Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Ambil model dengan ID " + pkFieldValue);
                modelForm = modelForm.getModelById(pkFieldValue);
            }
            request.setAttribute("model", modelForm);
            ServletUtil.dispatch("/WEB-INF/views/" + formPage, request, response);
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(String pkFieldValue) {
        try {
            modelForm.delete(pkFieldValue);
            ServletUtil.redirect(Config.base_url + "index/" + controllerName, request, response);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void processFormData() {
        //==== STARTOF penanganan multi-part data

        isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);
            List items = null;
            try {
                items = upload.parseRequest(request);
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
            Iterator itr = items.iterator();

            while (itr.hasNext()) {
                FileItem item = (FileItem) itr.next();
                if (item.isFormField()) {

                    String name = item.getFieldName();
                    String value = item.getString();
                    formParams.put(name, value);
                } else {
                    //upload file here
                    try {
                        String itemName = item.getName();
                        //TOFIX : kalau FFox, itemName hanya nama file saja. Kalau IE, lengkap dengan nama folder.
                        String path = Config.base_path + "upload\\" + itemName;//TODO : save all to this folder || allow customization
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Try to write file " + path);
                        File savedFile = new File(path);
                        item.write(savedFile);
                        formParams.put(item.getFieldName(), savedFile.getName());
                    } catch (Exception e) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Possibly, that file doesn't exist");//TODO: better information to user
                    }
                }
            }
        }
    //==== ENDOF penanganan multi-part data
    }
    /*
     * populate a hashtable with value from form, with regards to model public field 
     * added the ability to process multi-part data (form with file field)
     */

    private Hashtable fillParams() {
        try {
            params = new Hashtable();
            Class cl = Class.forName("application.models._" + modelForm.getPlainClassName());
            //TOFIX: because we use cl.getFields(), all fields neet to be define as public
            Field[] fields = cl.getFields();
            String fieldValue;
            String fieldName;


            for (int i = 0; i < fields.length; i++) {
                fieldName = fields[i].getName();
                //not used, foreign concept is no longer needed, because we only stored to DB what is in ScaffoldClass
                //denoted by _
                //only insert if it isn't foreign field
//                if(!modelForm.isForeignField(fieldName))
//                {
                fieldValue = getFormFieldValue(fieldName);
                //TOFIX : -1 usually been used in foreign key, so if it's don't store it.... This is magic constant!!! Hehe
                if (fieldValue != null && !fieldValue.equals("-1")) { 
                    if (fieldName.equals(modelForm.getPkFieldName())) {
                        modelForm.setPkFieldValue(fieldValue);
                    } else {
                        params.put(fieldName, fieldValue);
                    }
                }
//                }
            }
            return params;
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }
    Hashtable<String, String> validation = new Hashtable();

    public void clearValidation() {
        validation.clear();
    }

    public void validationAddRule(String ruleName, String rule) {
        validation.put(ruleName, rule);
    }

    public boolean validationRun() {
        Enumeration e = validation.keys();
        boolean allPass = true;

        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Running Validation");
        while (e.hasMoreElements()) {
            String ruleName = (String) e.nextElement();
            String rule = validation.get(ruleName);
            StringTokenizer st = new StringTokenizer(rule, "|");
            String fieldValue;
            request.removeAttribute(ruleName);
            while (st.hasMoreTokens()) {
                String token = st.nextToken().toLowerCase();
                Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Rule for " + ruleName + ", " + token);
                fieldValue = getFormFieldValue(ruleName);
                if (token.equals("required")) {
                    if (fieldValue == null || "".equals(fieldValue)) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "ERROR" + ruleName + ", " + token);
                        request.setAttribute(ruleName + "_error", ruleName + " required");
                        allPass = false;
                    }
                } else if (token.contains("!=")) {
                    StringTokenizer st2 = new StringTokenizer(token, "!=");
                    String value = st2.nextToken();
                    if (fieldValue == null || value.equals(fieldValue)) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "ERROR" + ruleName + ", " + token);
                        request.setAttribute(ruleName + "_error", ruleName + " required");
                        allPass = false;
                    }
                }
            }
        }
        return allPass;
    }

    public String getFormFieldValue(String fieldName) {
        return isMultipart ? (formParams.get(fieldName) == null ? null : formParams.get(fieldName) + "") : request.getParameter(fieldName);
    }
}
