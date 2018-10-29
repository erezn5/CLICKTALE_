package com.clicktale.pipeline.framework.helpers

trait SqlServerConnectionStringParser {
  /**
    * Converts Sql Server connection string to jdbc connection details
    *
    * @param connectionString Sql Server connection string. E.G. "data source=somekindofaserver.us-west-2.rds.amazonaws.com,1433;initial catalog=CoreAdministration;persist security info=True;user id=admin;password=apassword;MultipleActiveResultSets=True;App=EntityFramework"
    * @return (jdbcUrl, user, password)
    */
  def parseSqlServerConnectionString(connectionString: String): (String, String, String) = {
    // Example connection string: "data source=somekindofaserver.us-west-2.rds.amazonaws.com,1433;initial catalog=CoreAdministration;persist security info=True;user id=admin;password=apassword;MultipleActiveResultSets=True;App=EntityFramework"
    val KeyValue="""([^=]+)=([^;]+);?""".r
    val values = (for(KeyValue(k,v) <- KeyValue.findAllIn(connectionString)) yield k.toLowerCase -> v).toMap
    val jdbcUrl = values("data source").replace(',',':')
    val db = values("initial catalog")
    (s"jdbc:sqlserver://$jdbcUrl;DatabaseName=$db", values("user id"), values("password"))
  }
}