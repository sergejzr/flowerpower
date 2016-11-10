package de.l3s.db;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;





public class DB {
	
	public static Connection getConnection(String host,String dbname) throws ClassNotFoundException, SQLException, IOException
	{
		
		Properties props = loadProperties(host);

			   String driver = "com.mysql.jdbc.Driver";
			   if (driver != null) {
			       Class.forName(driver) ;
			   }

			   String url = props.getProperty(host+".jdbc.url");
			   String username = props.getProperty(host+".jdbc.username");
			   String password = props.getProperty(host+".jdbc.password");

			  Connection con = DriverManager.getConnection(url+"?useCompression=true", username, password);
			  if(dbname!=null){
			  con.setCatalog(dbname);
			  }
			  return con;
		
	}
	public static Connection getConnection(String dbhost,String username,String password) throws ClassNotFoundException, SQLException, IOException
	{
		
	

			   String driver = "com.mysql.jdbc.Driver";
			   if (driver != null) {
			       Class.forName(driver) ;
			   }

			   String url = dbhost;


			  Connection con = DriverManager.getConnection(url+"?useCompression=true", username, password);
			  return con;
		
	}
	public static Connection getLocalConnection() throws ClassNotFoundException, SQLException, IOException
	{
		return getConnection("localhost",null);
	}
	public static Connection getLocalConnection(String db) throws ClassNotFoundException, SQLException, IOException
	{
		return getConnection("localhost",db);
	} Connection getConnection(String host) throws ClassNotFoundException, SQLException, IOException
	{
		return getConnection(host,null);
	}
	
	private static Properties loadProperties(String host) throws IOException {
		Properties props = new Properties();
		   FileInputStream in = 
				      new FileInputStream(new File(new File(System.getProperty("user.home"),".javaconfig"),"connection.props"));
				   props.load(in);
				   in.close();
				   return props;
	}





public static void main(String[] args) {
	
	try {
		
		Connection con = DB.getConnection("localhost", "citizenscience");
		Statement st = con.createStatement();
		ResultSet rs = st.executeQuery("SHOW DATABASES;");
		while(rs.next())
		{
			System.out.println(rs.getString(1));
		}
	} catch (ClassNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	

	
}

}
