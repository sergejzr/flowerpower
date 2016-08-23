package stars4all.tweets;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBReader {
	public static void main(String[] args) {
		
		try {
			Connection con = getConnection();
			Statement st = con.createStatement();
			
			ResultSet rs = st.executeQuery("SELECT * FROM eclipse.tweet ORDER BY created;");
			while(rs.next())
			{
				String tweet=rs.getString("id");
				String time=rs.getString("created");
				
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static Connection getConnection() throws SQLException, ClassNotFoundException {
		   Class.forName("com.mysql.jdbc.Driver");
		/*	Connection con = DB.getConnection("jdbc:mysql://mysql.l3s.uni-hannover.de?characterEncoding=utf8","flickrattractive");
		*/
		Connection con = DriverManager.getConnection(
				"jdbc:mysql://localhost/eclipse?characterEncoding=utf8", "root",
				"root");
		return con;
		//con.createStatement().execute("use twittercrawl");
	
		
	}
}
