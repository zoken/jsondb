com.fline.modules.jsondb
=========================

### Summary

This project contains two version... First, use Mysql_Gen and JavaBean to generate Sql. Second, use SQLResource(Table Reousrce is replaced) and jsonobject to generate all sqls.

### Modules

1. Mysql executor
  Mysql_Instance with default config file named mysql.properties or applicationContext-tp-datasource-dbcp.extensionpoint.properties.
  Certainly, you can create your own mysql_instance with your config file.
2. SQL generator
  generate Sql divide three steps:
  * add sql jsonobject into json config file
  * load SQLResource by JSONResourceParser
  * get sql using method SQLResoruce/getNormalSql by json params.
  
### Example
  
    private static final String ADD_ATTACH_RELATION = "Review.json/addAttachRelation";
    ...
    SQLResource resource = new SQLResource(ADD_ATTACH_RELATION);
    List<String> attachmentSqls = new ArrayList<String>();
    for (String attachid : attachids) {
      JSONObject params = new JSONObject();
      params.put("reviewrequest_id", reviewid);
      params.put("fileattachment_id", attachid);
      String sql = resource.getQuerySql(params);
      attachmentSqls.add(sql);
    }
    mysql_instance.executeInserts(attachmentSqls);
