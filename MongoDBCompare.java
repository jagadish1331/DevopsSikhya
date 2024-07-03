import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoDBCompare {
  public static void main(String[] args) {
    String formId = "RB_AccountRemediation";
    String uatConnectionString = "mongodb://retail_eforms_uat_user:Password1@devecpvm010101.server.rbsgrp.net:27017,devecpvm010096.server.rbsgrp.net:27017,devecpvm010102.server.rbsgrp.net:27017/retail_eforms_uat_db?authSource=admin&readPreference=primary&ssl=false";
    String prodConnectionString = "mongodb://sanyasc%40EUROPA.RBSGRP.NET:Rock5860%40Kappa@ecpvm011528.server.banksvcs.net:27019,ecpvm011527.server.banksvcs.net:27019,ecpvm011529.server.banksvcs.net:27019/retail_eforms_db?authMechanism=PLAIN&readPreference=primary&authSource=%24external&tls=true&tlsCAFile=C%3A%5Cdev%5Cca.pem";
    MongoClient uatClient = null;
    MongoClient prodClient = null;
    try {
      uatClient = MongoClients.create(uatConnectionString);
      MongoDatabase uatDatabase = uatClient.getDatabase("retail_eforms_uat_db");
      MongoCollection<Document> uatCollection = uatDatabase.getCollection("runtime-form-definition-revs");
      prodClient = MongoClients.create(prodConnectionString);
      MongoDatabase prodDatabase = prodClient.getDatabase("retail_eforms_db");
      MongoCollection<Document> prodCollection = prodDatabase.getCollection("runtime-form-definition-revs");
      Map<String, String> schemaToStepNameMap = fetchStepDefinitions(uatDatabase, formId);
      List<Bson> pipeline = Arrays.asList(new Bson[] { Aggregates.match(Filters.eq("uuid", formId)), 
            Aggregates.sort(Sorts.descending(new String[] { "version" })), Aggregates.limit(1), 
            Aggregates.project((Bson)(new Document("DataSchema", "$stepDefinitions.dataSchemaReference.uuid"))
              .append("DataSchemaVersion", "$stepDefinitions.dataSchemaReference.version")
              .append("UiSchema", "$stepDefinitions.uiSchemaReference.uuid")
              .append("UiSchemaVersion", "$stepDefinitions.uiSchemaReference.version")
              .append("RuleSchema", "$stepDefinitions.ruleDefinitionReference.uuid")
              .append("RuleSchemaVersion", "$stepDefinitions.ruleDefinitionReference.version")
              .append("RootRuleSchema", "$ruleDefinitionReference.uuid")
              .append("RootRuleSchemaVersion", "$ruleDefinitionReference.version")
              .append("IntegrationRules", "$ruleTemplateDataReference.uuid")
              .append("IntegrationRuleVersions", "$ruleTemplateDataReference.version")
              .append("FormVersion", "$version")
              .append("EmailTemplate", "$extensionDefinition.templateFormDefinitionReference.uuid")
              .append("EmailTemplateVersion", "$extensionDefinition.templateFormDefinitionReference.version")) });
      Document uatResult = (Document)uatCollection.aggregate(pipeline).first();
      Document prodResult = (Document)prodCollection.aggregate(pipeline).first();
      if (uatResult != null && prodResult != null) {
        compareSchemas("DataSchema", "DataSchemaVersion", uatResult, prodResult, schemaToStepNameMap);
        compareSchemas("UiSchema", "UiSchemaVersion", uatResult, prodResult, schemaToStepNameMap);
        compareSchemas("RuleSchema", "RuleSchemaVersion", uatResult, prodResult, schemaToStepNameMap);
        compareSchemas("IntegrationRules", "IntegrationRuleVersions", uatResult, prodResult, schemaToStepNameMap);
        compareSchemas("EmailTemplate", "EmailTemplateVersion", uatResult, prodResult, schemaToStepNameMap);
        compareRootAndEmailTemplates("RootRuleSchema", "RootRuleSchemaVersion", uatResult, prodResult, schemaToStepNameMap);
        compareRootAndEmailTemplates("EmailTemplate", "EmailTemplateVersion", uatResult, prodResult, schemaToStepNameMap);
      } else {
        System.out.println(formId + " doesn't exist in any one or both environments");
      } 
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (uatClient != null)
        uatClient.close(); 
      if (prodClient != null)
        prodClient.close(); 
    } 
  }
  
  private static Map<String, String> fetchStepDefinitions(MongoDatabase database, String formId) {
    MongoCollection<Document> formDefinitionCollection = database.getCollection("runtime-form-definition-revs");
    Document formDefinition = (Document)formDefinitionCollection.find(Filters.eq("uuid", formId)).first();
    Map<String, String> schemaToStepNameMap = new HashMap<>();
    if (formDefinition != null) {
      List<Document> stepDefinitions = formDefinition.getList("stepDefinitions", Document.class);
      for (Document step : stepDefinitions) {
        String stepName = step.getString("name");
        String dataSchemaUuid = ((Document)step.get("dataSchemaReference", Document.class)).getString("uuid");
        String uiSchemaUuid = ((Document)step.get("uiSchemaReference", Document.class)).getString("uuid");
        String ruleSchemaUuid = ((Document)step.get("ruleDefinitionReference", Document.class)).getString("uuid");
        schemaToStepNameMap.put(dataSchemaUuid, stepName);
        schemaToStepNameMap.put(uiSchemaUuid, stepName);
        schemaToStepNameMap.put(ruleSchemaUuid, stepName);
      } 
    } 
    return schemaToStepNameMap;
  }
  
  private static void compareSchemas(String schemaField, String versionField, Document uatResult, Document prodResult, Map<String, String> schemaToStepNameMap) {
    Object uatSchemasObj = uatResult.get(schemaField);
    Object uatVersionsObj = uatResult.get(versionField);
    Object prodSchemasObj = prodResult.get(schemaField);
    Object prodVersionsObj = prodResult.get(versionField);
    if (uatSchemasObj instanceof List && uatVersionsObj instanceof List && prodSchemasObj instanceof List && prodVersionsObj instanceof List) {
      List<String> uatSchemas = (List<String>)uatSchemasObj;
      List<Integer> uatVersions = (List<Integer>)uatVersionsObj;
      List<String> prodSchemas = (List<String>)prodSchemasObj;
      List<Integer> prodVersions = (List<Integer>)prodVersionsObj;
      for (int i = 0; i < uatSchemas.size(); i++) {
        String uatSchema = uatSchemas.get(i);
        Integer uatVersion = uatVersions.get(i);
        int prodIndex = prodSchemas.indexOf(uatSchema);
        if (prodIndex != -1) {
          Integer prodVersion = prodVersions.get(prodIndex);
          int versionDifference = uatVersion.intValue() - prodVersion.intValue();
          if (versionDifference < 0)
            if (schemaField.equals("IntegrationRules")) {
              System.out.println("******************************************************************************************************");
              System.out.println(schemaField + " | UUID:" + uatSchema + " | VERSION_DIFFERENCE=" + versionDifference);
            } else {
              String stepName = schemaToStepNameMap.getOrDefault(uatSchema, "Unknown Step!");
              System.out.println(schemaField + " | UUID:" + uatSchema + " | STEP_NAME:" + stepName + " | VERSION_DIFFERENCE=" + versionDifference);
            }  
        } 
      } 
    } 
  }
  
  private static void compareRootAndEmailTemplates(String schemaField, String versionField, Document uatResult, Document prodResult, Map<String, String> schemaToStepNameMap) {
    String uatSchema = uatResult.getString(schemaField);
    Integer uatVersion = uatResult.getInteger(versionField);
    String prodSchema = prodResult.getString(schemaField);
    Integer prodVersion = prodResult.getInteger(versionField);
    System.out.println("******************************************************************************************************");
    if (uatSchema != null && prodSchema != null && uatVersion != null && prodVersion != null) {
      int versionDifference = uatVersion.intValue() - prodVersion.intValue();
      if (versionDifference <= 0)
        System.out.println(schemaField + " | UUID:" + uatSchema + " | VERSION_DIFFERENCE=" + versionDifference); 
    } else {
      System.err.println("One of the retrieved fields is null:");
      System.err.println(schemaField + " in UAT:" + uatSchema + ", in PROD:" + prodSchema);
      System.err.println(versionField + " in UAT:" + uatVersion + ", in PROD:" + prodVersion);
    } 
  }
}
