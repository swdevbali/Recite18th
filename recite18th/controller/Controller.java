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

HISTORY:
1) Forgot of when 1st created this file
2) August 4, 2011 = about to add reporting feature using iText
http://www.geek-tutorials.com/java/itext/servlet_jsp_output_pdf.php0C
 */
package recite18th.controller;

import recite18th.model.Model;
import recite18th.util.ServletUtil;
import java.lang.reflect.Field;
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

//authorization
import recite18th.util.LoginUtil;

//reporting

import java.sql.*;
import recite18th.library.Db;
import application.config.*;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Controller extends HttpServlet {

    protected Model modelForm;
    protected Model modelRow;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected String viewPage;
    protected String formPage;
    private Hashtable params; //parameter for sql INSERT/UPDATE manipulation
    private HashMap formParams = new HashMap(); //parameter populate by form. I make it private, I want to make sure all field data get by method getFormFieldValue
    protected List row = new ArrayList();
    protected String controllerName;
    protected String sqlViewDataPerPage;
    boolean isMultipart;
    //authorization things...
    protected boolean isNeedAuthorization = false;//default controller is free
    protected String authorList = null;//if isNeedAuthorization is true, then this mean all authorized user can access this controller. IDEA: controller level || method level authorization :)

    public void doSave() {
        modelForm.save(fillParams());
    }

    public void goAfterSave() {
        ServletUtil.redirect(Config.base_url + "index/" + controllerName, request, response);
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
            //restored inputted values
            input("-2");//TOFIX : somehow, I can not move the -2 case code from input() into this place...
        }
    }

    protected void initSqlViewDataPerPage() {
        Model model = initModel();
        if (sqlViewDataPerPage == null) {
            sqlViewDataPerPage = "select * from " + model.getTableName();
        }
    }

    protected Model initModel() {
        Model model;
        if (modelRow == null) {
            model = modelForm;
        } else {
            model = modelRow;
        }
        return model;
    }
    /*
    Extracted from index() to allow getting default list of model be called from another method, e.g print()
     */

    protected List getDefaultListOfModel() {
        row = null;
        Model model = initModel();;

        if (model != null) {
            initSqlViewDataPerPage();
            row = model.getDataPerPage(sqlViewDataPerPage);
        }
        return row;
    }

    /**
     * Open main view for this controller
     */
    public void index() {
        // check whether we need to prepare database model to be displayed
        row = getDefaultListOfModel();
        if (row != null) {
            request.setAttribute("row", row);
        }

        // AUTHORIZATION MODULE : check whether current authorization is enough
        // TODO : A more flexible approach.
        try {
            if (!isNeedAuthorization)//doesn't need authorization
            {
                ServletUtil.dispatch("/WEB-INF/views/" + viewPage, request, response);
            } else {
                if (isNeedAuthorization && LoginUtil.isLogin(request))//need authorization and already login
                {
                    if (authorList == null || "".equals(authorList))//.. but with no authorlist defined
                    {
                        ServletUtil.dispatch("/WEB-INF/views/" + viewPage, request, response);
                    } else //..with authorlist defined
                    {
                        String role = LoginUtil.getLoginRole(request);
                        //TOFIX : not just contains(), but split it, and compare each component of it
                        if (authorList.contains(role)) {
                            ServletUtil.dispatch("/WEB-INF/views/" + viewPage, request, response);
                        }
                    }

                } else if (isNeedAuthorization && !LoginUtil.isLogin(request)) //need authorization and not login
                {
                    ServletUtil.redirect(Config.base_url + "index/" + Config.loginController, request, response);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            ServletUtil.dispatch(Config.base_url + Config.page404, request, response);
        }
    }

    /**
     * open another view for this controller
     * @param page
     */
    public void index(String page) {
        ServletUtil.dispatch("/WEB-INF/views/" + page, request, response);
        /*String oldViewPage = viewPage;
        viewPage = page;
        index();
        viewPage = oldViewPage;*/
    }

    public void input(String pkFieldValue) {
        try {
            if (pkFieldValue.equals("-2")) {
                //masukkan nilai yg tadi dimasukkan.. hadeuh...            
                //Drawbacks : semua field harus didefinisikan jenis validasinya. dan itu ga baik. TODO : ubah ke formParams
                //dan sesuaikan dengan ada/tidaknya fieldnya dari model. Jika ada, baru diset. Jika tidak, berarti kontrol lain,e.g, Submit
                //SOLVED! Already use formParams to refill the value

                Enumeration e = validation.keys();
                Model.session = request.getSession();//store session here
                modelForm = modelForm.createNewModel();
                while (e.hasMoreElements()) {
                    //TOFIX : restore PK Field Value and Foreign Field. 
                    //SOLVED. PK Field restored by refilled the value using formParams, whilst Foreign Field restored by adding translated value in corresponding model class
                    String ruleName = (String) e.nextElement();
                    String value = getFormFieldValue(ruleName);
                    try {
                        PropertyUtils.setSimpleProperty(modelForm, ruleName, value);
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "validasi error, mengisi kembali " + ruleName + ", dengan value = " + value);
                    } catch (Exception exception) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, exception.getMessage());
                    }
                }

                String key = null, value = null;
                Iterator it = formParams.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    Logger.getLogger(Controller.class.getName()).log(Level.INFO, pairs.getKey() + " = " + pairs.getValue());

                    try {
                        key = pairs.getKey() + "";
                        value = pairs.getValue() + "";
                        if (key.equals("hidden_" + modelForm.getPkFieldName())) {
                            PropertyUtils.setSimpleProperty(modelForm, modelForm.getPkFieldName(), value);
                        } else {
                            PropertyUtils.setSimpleProperty(modelForm, key, value);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "prop error = " + key + ", " + value);
                    }
                }
            } else if (pkFieldValue == null || pkFieldValue.equals("") || pkFieldValue.equals("-1")) {
                Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Buat Model baru");
                Model.session = request.getSession();
                modelForm = modelForm.createNewModel();
            } else {
                Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Ambil model dengan ID " + pkFieldValue);
                Model.session = request.getSession();
                modelForm = modelForm.getModelById(pkFieldValue);
            }

            //TODO : maybe automatified here. But currently it's suffice ... (or being autogenerated by Synch.java.. that's it! later...)
            //expand for FOREIGN KEY label... No need. Model subclass just derived its corresponding _***Model.java, 
            //..and add a property that translate its FK field (using Db.findValue is suffice. 

            request.setAttribute("model", modelForm);
            ServletUtil.dispatch("/WEB-INF/views/" + formPage, request, response);
        } catch (Exception ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void delete(String pkFieldValue) {
        modelForm.delete(pkFieldValue);
        ServletUtil.redirect(Config.base_url + "index/" + controllerName, request, response);
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void processFormData() {
        //==== START OF penanganan multi-part data

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
                        //make sure upload folder exist
                        File fDir = new File(Config.base_path + "upload");
                        if (!fDir.exists()) {
                            fDir.mkdir();
                        }
                        String path = Config.base_path + "upload" + Config.path_delimiter + itemName;//TODO : save all to this folder || allow customization
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


            // NEXT : Rephrase this
            for (int i = 0; i < fields.length; i++) {
                fieldName = fields[i].getName();
                fieldValue = getFormFieldValue(fieldName);

                if (fieldName.equals(modelForm.getPkFieldName())) {
                    modelForm.setPkFieldValue(getFormFieldValue("hidden_" + fieldName));
                    Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Putting field primary key from {0} into params", "hidden_" + fieldName);
                    params.put("hidden_" + fieldName, getFormFieldValue("hidden_" + fieldName));
                }

                if (fieldValue != null) {
                    params.put(fieldName, fieldValue);
                    Logger.getLogger(Controller.class.getName()).log(Level.INFO, "Putting field form {0} into params", fieldName);
                }
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
                } else if (token.equals("integer_bigger_than_zero")) {

                    int iValue = 0;
                    try {
                        iValue = Integer.parseInt(fieldValue);
                    } catch (NumberFormatException nfe) {
                        allPass = false;
                    }

                    if (iValue <= 0) {
                        allPass = false;
                    }
                    if (!allPass) {
                        Logger.getLogger(Controller.class.getName()).log(Level.INFO, "ERROR" + ruleName + ", " + token);
                        request.setAttribute(ruleName + "_error", ruleName + " must be non negative");
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

    public void print() {
        /** thanks to http://www.java2s.com/Code/Java/PDF-RTF/DemonstratesthecreatingPDFinportraitlandscape.htm
         * QUICK FIX : do landscape
         */
        response.setContentType("application/pdf"); // Code 1
        Document document = new Document(PageSize.A1.rotate());
        try {
            PdfWriter writer = PdfWriter.getInstance(document,
                    response.getOutputStream()); // Code 2
            document.open();

            // various fonts
            BaseFont bf_helv = BaseFont.createFont(BaseFont.HELVETICA, "Cp1252", false);
            BaseFont bf_times = BaseFont.createFont(BaseFont.TIMES_ROMAN, "Cp1252", false);
            BaseFont bf_courier = BaseFont.createFont(BaseFont.COURIER, "Cp1252", false);
            BaseFont bf_symbol = BaseFont.createFont(BaseFont.SYMBOL, "Cp1252", false);

            String headerImage = Config.base_path + "images/report-logo.gif";

            Image imghead = Image.getInstance(headerImage);
            imghead.setAbsolutePosition(0, 0);
            PdfContentByte cbhead = writer.getDirectContent();
            PdfTemplate tpLogo = cbhead.createTemplate(600, 300);
            tpLogo.addImage(imghead);

            PdfTemplate tpTitle = cbhead.createTemplate(1100, 300);
            String txtHeader = "BADAN KEPEGAWAIAN DAERAH PEMERINTAH DAERAH";//Config.application_title;
            tpTitle.beginText();
            tpTitle.setFontAndSize(bf_times, 36);
            tpTitle.showText(txtHeader);
            tpTitle.endText();

            PdfTemplate tpTitle2 = cbhead.createTemplate(900, 300);
            String txtHeader2 = "         KABUPATEN BANTUL YOGYAKARTA";
            tpTitle2.beginText();
            tpTitle2.setFontAndSize(bf_times, 36);
            tpTitle2.showText(txtHeader2);
            tpTitle2.endText();

            PdfTemplate tpAlamat = cbhead.createTemplate(1000, 400);
            tpAlamat.beginText();
            tpAlamat.setFontAndSize(bf_times, 24);
            tpAlamat.showText("Alamat : Jln. R. W. Monginsidi No. 01 Kompleks Parasamya Bantul, Telp. (0274) 367509");
            tpAlamat.endText();


            DateFormat df = new SimpleDateFormat("dd MMM yyyy");
            java.util.Date dt = new java.util.Date();
            PdfTemplate tp3 = cbhead.createTemplate(600, 300);
            tp3.beginText();
            tp3.setFontAndSize(bf_times, 16);

            tp3.showText("Tanggal : " + df.format(dt));
            tp3.endText();


            cbhead.addTemplate(tpLogo, 800, 1500);//logo
            cbhead.addTemplate(tpTitle, 1000, 1580);
            cbhead.addTemplate(tpTitle2, 1000, 1540);
            cbhead.addTemplate(tpAlamat, 1000, 1500);//alamat
            cbhead.addTemplate(tp3, 270, 1500);//tanggal

            HeaderFooter header = new HeaderFooter(
                    new Phrase(cbhead + "", new Font(bf_helv)), false);
            header.setAlignment(Element.ALIGN_CENTER);

            document.setHeader(header);



            //PdfContentByte cb = writer.getDirectContent();
            Paragraph par = new Paragraph("\n\n\n\n\n\n\nLAPORAN DATA SELURUH " + controllerName.toUpperCase() + "\n");
            par.getFont().setStyle(Font.BOLD);
            par.getFont().setSize(18);
            par.setAlignment("center");
            document.add(par);
            document.add(new Paragraph("\n\n"));


            // get data
            initSqlViewDataPerPage();
            PreparedStatement pstmt = Db.getCon().prepareStatement(sqlViewDataPerPage, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = pstmt.executeQuery();
            ResultSetMetaData metaColumn = resultSet.getMetaData();
            int nColoumn = metaColumn.getColumnCount();
            // thanks to set cell width http://www.jexp.ru/index.php/Java/PDF_RTF/Table_Cell_Size#Setting_Cell_Widths
            if (nColoumn > 0) {
                Model model = initModel();
                String tableName = model.getTableName();
                // create table header
                //     float[] widths = {1, 4};
                PdfPTable table;// = new PdfPTable(nColoumn);
                PdfPCell cell = new PdfPCell(new Paragraph("Daftar " + controllerName));

                Hashtable hashModel = TableCustomization.getTable(model.getTableName());

                int ncolumnHeader = nColoumn + 1; // +1 because of row. number
                if (hashModel != null) {
                    ncolumnHeader = Integer.parseInt("" + hashModel.get("columnCount")) + 1;
                }
                table = new PdfPTable(ncolumnHeader);
                //float[] widths = {0.1f, 0.1f, 0.05f, 0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f,0.75f};
                //table.setWidths(widths);
                //table.setWidthPercentage(30);
                cell.setColspan(ncolumnHeader);

                table.addCell("No.");

                if (hashModel != null) {
                    Enumeration k = hashModel.keys();
                    while (k.hasMoreElements()) {
                        String key = (String) k.nextElement();
                        if (key.equals("columnCount")) {
                            continue;
                        }
                        PdfPCell cellCol = new PdfPCell(new Paragraph(hashModel.get(key) + ""));
                        cellCol.setNoWrap(true);
                        cellCol.setMinimumHeight(50);

                        cellCol.setHorizontalAlignment(Element.ALIGN_CENTER);

                        table.addCell(cellCol);
                    }
                } else {
                    for (int i = 1; i < ncolumnHeader; i++) {
                        System.out.println("DATA = " + metaColumn.getColumnName(i));
                        PdfPCell cellCol = new PdfPCell(new Paragraph(metaColumn.getColumnName(i) + ""));
                        cellCol.setHorizontalAlignment(Element.ALIGN_CENTER);
                        table.addCell(cellCol);
                    }
                }

//                for (int i = 1; i < ncolumnHeader+1; i++) {
//                    System.out.println("DATA DB = " + metaColumn.getColumnName(i));
//
//                    if (hashModel == null) {
//                        table.addCell(metaColumn.getColumnName(i));
//                    } else {
//                        if (hashModel.get(i) != null) {
//                            System.out.println("DATA = " + metaColumn.getColumnName(i));
//                            PdfPCell cellCol = new PdfPCell(new Paragraph(hashModel.get(metaColumn.getColumnName(i)) + ""));
//                            cellCol.setHorizontalAlignment(Element.ALIGN_CENTER);
//
//                            table.addCell(cellCol);
//
//                        }
//                    }
//                }


                //iterate all columns : table data
                resultSet.beforeFirst();
                int row = 1;
                while (resultSet.next()) {
                    System.out.println(row);
                    cell = new PdfPCell(new Paragraph(row + ""));
                    table.addCell(cell);
                    if (hashModel != null) {//skip dulu u/ kasus ga pny class kustomasi table
                        Enumeration k = hashModel.keys();
                        while (k.hasMoreElements()) {
                            String key = (String) k.nextElement();
                            if (key.equals("columnCount")) {
                                continue;
                            }
                            table.addCell(resultSet.getObject(key) + "");
                        }
                    } else {
                        for (int i = 1; i < ncolumnHeader; i++) {
                            System.out.println("DATA = " + metaColumn.getColumnName(i));
                            PdfPCell cellCol = new PdfPCell(new Paragraph(resultSet.getObject(metaColumn.getColumnName(i)) + ""));
                            cellCol.setHorizontalAlignment(Element.ALIGN_CENTER);
                            table.addCell(cellCol);
                        }
                    }
//                    for (int i = 1; i < nColoumn; i++) {
//                        System.out.println("DB Column = " + metaColumn.getColumnName(i));
//                        if (hashModel == null) {
//                            table.addCell(resultSet.getObject(i) + "");
//                        } else {
//                            if (hashModel.get(metaColumn.getColumnName(i)) != null) {
//                                System.out.println(metaColumn.getColumnName(i) + ", PDF = " + resultSet.getObject(metaColumn.getColumnName(i)) + "");
//                                table.addCell(resultSet.getObject(metaColumn.getColumnName(i)) + "");
//                            }
//                        }
//                    }
                    row++;
                }

                document.add(table);
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
