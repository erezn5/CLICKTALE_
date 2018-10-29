package helpers;

import java.sql.*;

public class MSSQLServer {

    String connectionUrl = "jdbc:sqlserver://core-qa-04.ca7ahzlrglji.us-east-1.rds.amazonaws.com:1433;"+"user=sa;password=Pe6AvT4c1f2cH05f";

    Connection con = null;
    Statement stmt = null;
    ResultSet rs = null;

    public MSSQLServer() throws SQLException {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            con = DriverManager.getConnection(connectionUrl);
            String SQL = "select * from CoreAdministration.dbo.ProjectRecordingConfiguration";
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL);

            while(rs.next()){
                System.out.println(rs.getString(4) + " " + rs.getString(6));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
