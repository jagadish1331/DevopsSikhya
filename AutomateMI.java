import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;

import java.io.Console;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class AutomateMI {
    public static MongoClient mongoClient;
    public static MongoDatabase database;

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);
        System.out.println("Enter user name(racf):");
        String userName = sc.nextLine();        //UAT
        System.out.println("\nEnter user password:");
        String userP = sc.nextLine();

        //PROD
        /*Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance");
            System.exit(0);
        }
        char[] userP = console.readPassword("Enter yours password: ");*/

        String encodeP = URLEncoder.encode(String.valueOf(userP), StandardCharsets.UTF_8.toString());//URL Encoding in password
        //prod connection string
        String client_url = "mongodb://" + userName + "%40EUROPA.RBSGRP.NET:" + encodeP + "@ecpvm011528.server.banksvcs.net:27019,ecpvm011527.server.banksvcs.net:27019,ecpvm011529.server.banksvcs.net:27019/retail_eforms_db?authMechanism=PLAIN&readPreference=primary&authSource=%24external&tls=true&tlsCAFile=%5C%5CGBMLVFILFS01N02.rbsres01.net%5Chome5%24%5CRajNk%5CProfile%5CDesktop%5Cca.pem";
        //uat connection string
        //String client_url = "mongodb://retail_eforms_uat_user:Password1@devecpvm010101.server.rbsgrp.net:27017,devecpvm010096.server.rbsgrp.net:27017,devecpvm010102.server.rbsgrp.net:27017/retail_eforms_uat_db?authSource=admin&readPreference=primary&appname=MongoDB%20Compass&ssl=false";
        ConnectionString connectionString = new ConnectionString(client_url);
        MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(connectionString).retryWrites(true).build();
        mongoClient = MongoClients.create(settings);

        database = mongoClient.getDatabase("retail_eforms_db"); //prod DB
        //database = mongoClient.getDatabase("retail_eforms_uat_db"); //uat DB

        System.out.println("\nDate from(YYYY-MM-DD HH:MM:SS):");
        String dateG = sc.nextLine();
        System.out.println("\nDate to(YYYY-MM-DD HH:MM:SS):");
        String dateL = sc.nextLine();
        System.out.println("\n1. To get the failure forms. \n2. To get the successful forms. \n3. To get the all the form submission. \nPlease select");
        int formSubmit = sc.nextInt();
        System.out.println("\n1. To get the mail submission forms. \n2. To get the kafka submission forms. \n3. Both \nPlease select");
        int formFail = sc.nextInt();

        //List of non mail/kafka forms
        String[] noMail = {"RB_DCACallTagging", "RB_CallChecklist-AfterCallAction", "RB_UnsecuredCallCheck", "RB_BranchAccClosure"};
        JSONArray NoMailForm = new JSONArray();
        NoMailForm.addAll(Arrays.asList(noMail));

        LocalDateTime gmtG = LocalDateTime.parse(dateG, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ZonedDateTime instantG = ZonedDateTime.of(gmtG, ZoneId.of("GMT"));
        LocalDateTime gDate = instantG.withZoneSameInstant(ZoneId.of("Europe/London")).toLocalDateTime();

        LocalDateTime gmtL = LocalDateTime.parse(dateL, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        ZonedDateTime instantL = ZonedDateTime.of(gmtL, ZoneId.of("GMT"));
        LocalDateTime lDate = instantL.withZoneSameInstant(ZoneId.of("Europe/London")).toLocalDateTime();

        Boolean noTempl = false;

        MongoCollection<Document> collection = database.getCollection("runtime-form-definition");

        try {
            for (int f = 0; ((formSubmit == 3 && f < 1) || (formFail == 3 && f < 2) || (formFail != 3 && f < 1)); f++) {
                JSONArray formID = new JSONArray();
                String dat = "";
                if (formFail == 1 || (formFail == 3 && f == 0)) {
                    dat = "Mail";
                } else if (formFail == 2 || (formFail == 3 && f == 1)) {
                    dat = "Kafka";
                }
                System.out.println("\nFor " + dat + "\nEnter 'All' to get all forms. \nEnter 'Form ID' to get the particular forms. \nNote: Provide 'End' to stop. ");
                for (; (formID.size() == 0 || (!formID.contains("All") && !formID.contains("End"))); ) {
                    formID.add(sc.nextLine());
                }

                ArrayList<Document> resultForm = new ArrayList<>();
                if (formID.contains("All") && formFail == 3 && formSubmit == 3) {//All
                    resultForm = collection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.eq("isTemplate", noTempl))
                    )).into(new ArrayList<>());
                } else if (!formID.contains("All") && (formFail == 2 || (formFail == 3 && f == 1))) {//kafka
                    resultForm = collection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.and(Filters.eq("isTemplate", noTempl),
                                    (Filters.in("uuid", formID)) )),
                            Aggregates.lookup("forms-rule-template-data", "ruleTemplateDataReference.uuid", "uuid", "kafka-event"),
                            Aggregates.match(Filters.and(Filters.eq("kafka-event.ruleTemplateName", "kafka-event-partitioning"),
                                    Filters.not(Filters.size("kafka-event.rows", 0)))//{"kafka-event.rows":{"$not":{"$size":0}}}
                            )
                    )).into(new ArrayList<>());
                } else if (formID.contains("All") && (formFail == 2 || (formFail == 3 && f == 1))) {//kafka-all
                    resultForm = collection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.eq("isTemplate", noTempl)),
                            Aggregates.lookup("forms-rule-template-data", "ruleTemplateDataReference.uuid", "uuid", "kafka-event"),
                            Aggregates.match(Filters.and(Filters.eq("kafka-event.ruleTemplateName", "kafka-event-partitioning"),
                                    Filters.not(Filters.size("kafka-event.rows", 0)))//{"kafka-event.rows":{"$not":{"$size":0}}}
                            )
                    )).into(new ArrayList<>());
                } else if (!formID.contains("All") && (formFail == 1 || (formFail == 3 && f == 0))) {//mail
                    resultForm = collection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.and(Filters.eq("isTemplate", noTempl),
                                    Filters.exists("extensionDefinition"),
                                    Filters.nin("uuid", NoMailForm),
                                    (Filters.in("uuid", formID))
                            ))
                    )).into(new ArrayList<>());
                } else if (formID.contains("All") && (formFail == 1 || (formFail == 3 && f == 0))) {//mail-all
                    resultForm = collection.aggregate(Arrays.asList(
                            Aggregates.match(Filters.and(Filters.eq("isTemplate", noTempl),
                                    Filters.exists("extensionDefinition"),
                                    (Filters.nin("uuid", NoMailForm))
                            ))
                    )).into(new ArrayList<>());
                } else {
                    //do nothing
                }


                String filename = "";
                if (formFail == 3 && formSubmit == 3) {//all form file name
                    filename = "AllFormDetails.xlsx";
                } else if (formFail == 1 || (formFail == 3 && f == 0)) {//mail file name
                    filename = "MailFormDetails.xlsx";
                } else if (formFail == 2 || (formFail == 3 && f == 1)) {//kafka file name
                    filename = "KafkaFormDetails.xlsx";
                }

                XSSFWorkbook workbook = new XSSFWorkbook();

                if (resultForm.size() != 0) {
                    for (int countForm = 0; countForm < resultForm.size(); countForm++) {//Looping all the forms
                        JSONParser parseData = new JSONParser();
                        JSONObject formDetails = (JSONObject) parseData.parse(resultForm.get(countForm).toJson());

                        JSONArray stepDetails = (JSONArray) formDetails.get("stepDefinitions");
                        int k = 0, rowCount = 1;
                        int colCount = 0;

                        Boolean noRecord = true; //Fetch failure record
                        MongoCollection<Document> collectionEventData = database.getCollection("runtime-form-data");
                        ArrayList<Document> resultEventData = new ArrayList<>();
                        if (formSubmit == 3) {//all query
                            resultEventData = collectionEventData.aggregate(Arrays.asList(
                                    Aggregates.match(Filters.and(Filters.eq("formDefinitionReference.uuid", formDetails.get("uuid").toString()),
                                            Filters.gt("dateCreated", gDate),
                                            Filters.lt("dateCreated", lDate))),
                                    Aggregates.project(
                                            Document.parse("{_id:0,data:1,uuid:1,dateCreated:{$dateToString:{ format: '%Y-%m-%d %H:%M',date:'$dateCreated'}}}")
                                    ),
                                    Aggregates.group("$uuid", Accumulators.addToSet("data", "$data"),
                                            Accumulators.addToSet("dateCreated", "$dateCreated"))
                            )).into(new ArrayList<>());
                        } else if ((formFail == 1 || (formFail == 3 && f == 0)) && formSubmit == 1) {//mail failure query
                            resultEventData = collectionEventData.aggregate(Arrays.asList(
                                    Aggregates.match(Filters.and(Filters.eq("formDefinitionReference.uuid", formDetails.get("uuid").toString()),
                                            Filters.gt("dateCreated", gDate),
                                            Filters.lt("dateCreated", lDate))),
                                    Aggregates.project(
                                            Document.parse("{_id:0,data:1,uuid:1,dateCreated:{$dateToString:{ format: '%Y-%m-%d %H:%M',date:'$dateCreated'}}}")
                                    ),
                                    Aggregates.lookup("event-store", "uuid", "references.uuid", "event"),
                                    Aggregates.match(Filters.and(
                                            Filters.eq("event.body.eventClass", "SUBMITTED"),
                                            //Filters.ne("header.schemaId", "RULE_FIRED_EVENT"),
                                            Filters.or(Filters.ne("event.body.eventClass", "SEND_SUCCESS"))
                                    )),
                                    Aggregates.group("$uuid", Accumulators.addToSet("data", "$data"),
                                            Accumulators.addToSet("dateCreated", "$dateCreated"))
                            )).into(new ArrayList<>());
                        } else if ((formFail == 1 || (formFail == 3 && f == 0)) && formSubmit == 2) {//mail success query
                            resultEventData = collectionEventData.aggregate(Arrays.asList(
                                    Aggregates.match(Filters.and(Filters.eq("formDefinitionReference.uuid", formDetails.get("uuid").toString()),
                                            Filters.gt("dateCreated", gDate),
                                            Filters.lt("dateCreated", lDate))),
                                    Aggregates.project(
                                            Document.parse("{_id:0,data:1,uuid:1,dateCreated:{$dateToString:{ format: '%Y-%m-%d %H:%M',date:'$dateCreated'}}}")
                                    ),
                                    Aggregates.lookup("event-store", "uuid", "references.uuid", "event"),
                                    Aggregates.match(Filters.and(
                                            Filters.eq("event.body.eventClass", "SUBMITTED"),
                                            //Filters.ne("header.schemaId", "RULE_FIRED_EVENT"),
                                            Filters.eq("event.body.eventClass", "SEND_SUCCESS")
                                    )),
                                    Aggregates.group("$uuid", Accumulators.addToSet("data", "$data"),
                                            Accumulators.addToSet("dateCreated", "$dateCreated"))
                            )).into(new ArrayList<>());
                        } else if ((formFail == 2 || (formFail == 3 && f == 1)) && formSubmit == 2) { //kafka success
                            resultEventData = collectionEventData.aggregate(Arrays.asList(
                                    Aggregates.match(Filters.and(Filters.eq("formDefinitionReference.uuid", formDetails.get("uuid").toString()),
                                            Filters.gt("dateCreated", gDate),
                                            Filters.lt("dateCreated", lDate))),
                                    Aggregates.project(
                                            Document.parse("{_id:0,data:1,uuid:1,dateCreated:{$dateToString:{ format: '%Y-%m-%d %H:%M',date:'$dateCreated'}}}")
                                    ),
                                    Aggregates.lookup("event-store", "uuid", "references.uuid", "event"),
                                    Aggregates.match(Filters.and(Filters.eq("event.body.eventClass", "SUBMITTED"),
                                            Filters.eq("event.header.schemaId", "KAFKA_PUBLISH_EVENT"),
                                            Filters.eq("event.body.eventClass", "SENT"))),
                                    Aggregates.group("$uuid", Accumulators.addToSet("data", "$data"),
                                            Accumulators.addToSet("dateCreated", "$dateCreated"))
                            )).into(new ArrayList<>());
                        } else if ((formFail == 2 || (formFail == 3 && f == 1)) && formSubmit == 1) {//kafka failure query
                            resultEventData = collectionEventData.aggregate(Arrays.asList(
                                    Aggregates.match(Filters.and(Filters.eq("formDefinitionReference.uuid", formDetails.get("uuid").toString()),
                                            Filters.gt("dateCreated", gDate),
                                            Filters.lt("dateCreated", lDate))),
                                    Aggregates.project(
                                            Document.parse("{_id:0,data:1,uuid:1,dateCreated:{$dateToString:{ format: '%Y-%m-%d %H:%M',date:'$dateCreated'}}}")
                                    ),
                                    Aggregates.lookup("event-store", "uuid", "references.uuid", "event"),
                                    Aggregates.match(Filters.or(Filters.and(Filters.eq("event.body.eventClass", "SUBMITTED"),
                                                    Filters.and(Filters.eq("event.header.schemaId", "KAFKA_PUBLISH_EVENT"),
                                                            Filters.ne("event.body.eventClass", "SENT"))),
                                            (Filters.and(
                                                    Filters.eq("event.header.schemaId", "RULE_FIRED_EVENT"),
                                                    Filters.ne("event.body.eventClass", "SUBMITTED"))))//{"kafka-event.rows":{"$not":{"$size":0}}}
                                    ),
                                    Aggregates.group("$uuid", Accumulators.addToSet("data", "$data"),
                                            Accumulators.addToSet("dateCreated", "$dateCreated"))
                            )).into(new ArrayList<>());
                        }
                        if (resultEventData.size() > 0) {
                            Boolean refAdded = false;
                            Sheet refSheet = workbook.createSheet(formDetails.get("uuid").toString());//Details sheet creation
                            Row refRowHead = refSheet.createRow((short) 0);
                            Font headerFont = workbook.createFont();
                            headerFont.setBold(true);//Excel Bold Char
                            CellStyle styleBold = workbook.createCellStyle();
                            styleBold.setFont(headerFont);

                            CellStyle styleBorder = workbook.createCellStyle();
                            styleBorder.setBorderBottom(BorderStyle.THIN);//Excel Broder
                            styleBorder.setBottomBorderColor(IndexedColors.BLACK.getIndex());
                            styleBorder.setBorderRight(BorderStyle.THIN);
                            styleBorder.setRightBorderColor(IndexedColors.BLACK.getIndex());
                            styleBorder.setBorderTop(BorderStyle.THIN);
                            styleBorder.setTopBorderColor(IndexedColors.BLACK.getIndex());
                            styleBorder.setBorderLeft(BorderStyle.THIN);
                            styleBorder.setLeftBorderColor(IndexedColors.BLACK.getIndex());

                            Cell headerComp = refRowHead.createCell(colCount);
                            colCount++;
                            headerComp.setCellStyle(styleBorder);
                            headerComp.setCellStyle(styleBold);
                            headerComp.setCellValue("Component Name");

                            Cell headerDate = refRowHead.createCell(colCount);
                            colCount++;
                            headerDate.setCellStyle(styleBorder);
                            headerDate.setCellStyle(styleBold);
                            headerDate.setCellValue("Received Date");

                            do {
                                for (int j = 0; j < stepDetails.size(); j++) {
                                    JSONObject jobj31 = (JSONObject) stepDetails.get(j);
                                    if (Integer.toString(k).equals(jobj31.get("sequenceNumber").toString())) {
                                        JSONObject jobj3 = (JSONObject) stepDetails.get(j);
                                        if (jobj3.get("name").toString().contains("Summary") || jobj3.get("name").toString().contains("Thank you page") || jobj3.get("name").toString().contains("Before you begin") || jobj3.get("name").toString().contains("Review and submit")) {
                                            //do nothing, skipping summary, thank You,Before you begin, Review and submit steps
                                        } else {
                                            JSONObject dataRef = (JSONObject) jobj3.get("dataSchemaReference");
                                            JSONObject uiRef = (JSONObject) jobj3.get("uiSchemaReference");

                                            MongoCollection<Document> collectionData = database.getCollection("runtime-data-schema");
                                            ArrayList<Document> resultData = collectionData.aggregate(Arrays.asList(new Document("$match",
                                                    new Document("uuid", dataRef.get("uuid").toString())))).into(new ArrayList<>());
                                            JSONObject jobjData = (JSONObject) parseData.parse(resultData.get(0).toJson());
                                            JSONObject jobjDataSch = (JSONObject) jobjData.get("schema");
                                            JSONObject jobjDataPro = (JSONObject) jobjDataSch.get("properties");

                                            MongoCollection<Document> collectionUI = database.getCollection("runtime-ui-schema");
                                            ArrayList<Document> resultUI = collectionUI.aggregate(Arrays.asList(new Document("$match",
                                                    new Document("uuid", uiRef.get("uuid").toString())))).into(new ArrayList<>());
                                            JSONObject jobjUI = (JSONObject) parseData.parse(resultUI.get(0).toJson());
                                            JSONObject jobjUISch = (JSONObject) jobjUI.get("schema");

                                            String prevTitle = "";
                                            for (int i = 0; i < jobjUISch.get("ui:order").toString().split(",").length; i++) { //for ordering component
                                                if (noRecord) {
                                                    System.out.println("Total no. of " + formDetails.get("uuid").toString() + ": " + resultEventData.size());
                                                }
                                                if (i == 0 && (formDetails.get("isSteppedForm").toString()).equalsIgnoreCase("true")) {
                                                    Cell titleCell = refRowHead.createCell(colCount);
                                                    titleCell.setCellStyle(styleBorder);
                                                    titleCell.setCellStyle(styleBold);
                                                    titleCell.setCellValue("Step Name: " + jobj3.get("name").toString());
                                                    refSheet.addMergedRegion(new CellRangeAddress(0, resultEventData.size(), colCount, colCount));
                                                    colCount++;
                                                }
                                                noRecord = false;

                                                String elementName = jobjUISch.get("ui:order").toString().split(",")[i].replace("[", "").replace("]", "").replace("\"", "");
                                                if (!elementName.equals("*")) {
                                                    if (jobjDataPro.containsKey(elementName)) {
                                                        JSONObject keyvalue = (JSONObject) jobjDataPro.get(elementName);
                                                        if (!keyvalue.containsValue("notification") && !keyvalue.containsValue("AttachmentArray")) {
                                                            if (keyvalue.containsKey("text")) {//message component
                                                                String msg = removeTags((String) keyvalue.get("text"));
                                                                if (msg != null && !msg.isEmpty()) {
                                                                    Cell titleCell = refRowHead.createCell(colCount);
                                                                    titleCell.setCellStyle(styleBorder);
                                                                    titleCell.setCellStyle(styleBold);
                                                                    titleCell.setCellValue(msg);
                                                                }
                                                            } else {
                                                                String title = ((String) keyvalue.get("title")).trim();
                                                                if (!title.isEmpty() && !prevTitle.equalsIgnoreCase(title)) {
                                                                    Cell titleCell = refRowHead.createCell(colCount);
                                                                    titleCell.setCellStyle(styleBorder);
                                                                    titleCell.setCellStyle(styleBold);
                                                                    titleCell.setCellValue(title);
                                                                    prevTitle = title;
                                                                } else {
                                                                    colCount--;
                                                                }

                                                                for (int l = 0; l < resultEventData.size(); l++) {
                                                                    JSONObject data1 = (JSONObject) parseData.parse(resultEventData.get(l).toJson());
                                                                    if (!refAdded) {
                                                                        Row refRowCont = refSheet.createRow(rowCount);
                                                                        Cell cellRef = refRowCont.createCell(0);
                                                                        cellRef.setCellValue("Reference: " + data1.get("_id").toString());//UUID
                                                                        cellRef.setCellStyle(styleBorder);
                                                                        cellRef.setCellStyle(styleBold);

                                                                        String date =  ((JSONArray) data1.get("dateCreated")).get(0).toString();//submitted form data
                                                                        Cell cellDate = refRowCont.createCell(1);
                                                                        cellDate.setCellValue(date);//Received Date
                                                                        cellDate.setCellStyle(styleBorder);
                                                                        cellDate.setCellStyle(styleBold);
                                                                    }
                                                                    JSONObject data = (JSONObject) (((JSONArray) data1.get("data")).get(0));//submitted form data
                                                                    Row row = refSheet.getRow(l + 1);
                                                                    Cell valueCell = row.getCell(colCount);
                                                                    if (valueCell == null || "-".equals(valueCell.getStringCellValue())) {
                                                                        valueCell = row.createCell(colCount);
                                                                    } else {
                                                                        continue;
                                                                    }
                                                                    if (data.containsKey(elementName) && data.get(elementName)!=null) {
                                                                        if (((JSONObject) jobjDataPro.get(elementName)).containsKey("items")) {
                                                                            JSONObject spCompData = (JSONObject) ((JSONObject) ((JSONObject) jobjDataPro.get(elementName)).get("items")).get("properties");
                                                                            final String[] s = {data.get(elementName).toString()};
                                                                            spCompData.keySet().forEach(keyStr ->
                                                                            {
                                                                                JSONObject keyV = (JSONObject) spCompData.get(keyStr);
                                                                                s[0] = s[0].replaceAll((String) keyStr, (String) keyV.get("title"));
                                                                            });
                                                                            valueCell.setCellStyle(styleBorder);
                                                                            valueCell.setCellValue(s[0].replace("[", "").replace("]", "").replace("\"", ""));
                                                                        } else {
                                                                            valueCell.setCellStyle(styleBorder);
                                                                            if (data.get(elementName).toString().contains("phoneNumber")) {
                                                                                valueCell.setCellValue(((JSONObject) (data.get(elementName))).get("phoneNumber").toString());
                                                                            } else {
                                                                                valueCell.setCellValue(data.get(elementName).toString());
                                                                            }
                                                                        }
                                                                    } else {
                                                                        valueCell.setCellStyle(styleBorder);
                                                                        valueCell.setCellValue("-");
                                                                    }
                                                                    rowCount++;
                                                                }
                                                                refAdded = true;
                                                            }
                                                            colCount++;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                k = k + 1;
                            } while (k < stepDetails.size());
                            if (!noRecord) {
                                for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {//to adjust the column size
                                    for (int colNum = 0; colNum < refRowHead.getLastCellNum(); colNum++) {
                                        workbook.getSheetAt(sheetNum).autoSizeColumn(colNum);
                                    }
                                }
                                refSheet.createFreezePane(1,1);
                            }
                        }
                    }
                    FileOutputStream fileOut = new FileOutputStream(filename);
                    workbook.write(fileOut);
                    fileOut.close();
                    System.out.println("Data has been generated!");
                    workbook.close();
                } else {
                    System.out.println("No Data!");
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR!! " + e);
        }

    }

    private static String removeTags(String input) {
        org.jsoup.nodes.Document doc = Jsoup.parse(input);
        doc.select("head").empty();
        return doc.text().trim();
    }
}