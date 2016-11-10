package de.l3s.source;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Stack;

import de.l3s.db.DB;

public class DataSource {

	static boolean driverup = false;

	enum sourcetype {
		fs, db
	}

	sourcetype thissourcetype;
	private Connection con;
	private String dblabel;
	private String dbhost;
	private String dbusername;
	private String dbpass;
	private String tablename;
	private String backgroundtablename;
	private File sourcedir;

	public DataSource(String dblabel, String tablename, String backgroundtablename) {
		thissourcetype = sourcetype.db;
		this.dblabel = dblabel;
		this.tablename = tablename;
		this.backgroundtablename = backgroundtablename;
		// con=DB.getConnection(dblabel, (String)null);

	}

	public DataSource(String dbhost, String dbusername, String dbpass, String tablename, String backgroundtablename) {
		thissourcetype = sourcetype.db;
		this.dbhost = dbhost;
		this.dbusername = dbusername;
		this.dbpass = dbpass;
		this.tablename = tablename;
		this.backgroundtablename = backgroundtablename;
		// con=DB.getConnection(dblabel, (String)null);

	}

	public DataSource(File filedir) {
		thissourcetype = sourcetype.fs;
		this.sourcedir = filedir;
	}

	boolean mainset = true;

	public void selectMainSet() throws FowerReadException {
		mainset = true;
		connect();
	}

	public void selectBackgroundSet() throws FowerReadException {
		mainset = false;
		connect();
	}

	public void connect() throws FowerReadException {
		if (thissourcetype == sourcetype.db) {
			try {
				if (!driverup) {

					Class.forName("com.mysql.jdbc.Driver");

					driverup = true;
				}
				if (this.con == null) {
					if (dblabel != null) {
						con = DB.getConnection(dblabel, null);
					} else {
						if (dbhost != null && dbusername != null && dbpass != null) {
							con = DB.getConnection(dbhost, dbusername, dbpass);
						}
					}
				}

				PreparedStatement pstmt = con
						.prepareStatement("SELECT * FROM " + (mainset ? tablename : backgroundtablename) + " WHERE 1");

				thisrs = pstmt.executeQuery();
			} catch (Exception e) {
				throw new FowerReadException(e);
			}
		}else if (thissourcetype == sourcetype.fs) {
			
			Stack<File> stack=new Stack<>();
			ArrayList<File> filelist=new ArrayList<>();
			stack.add(sourcedir);
			
			while(!stack.empty())
			{
				File f = stack.pop();
				if(f.isDirectory())
				{
					stack.addAll(Arrays.asList(f.listFiles()));
				}else
				{
					if(f.getName().endsWith(".txt"))
					{
						filelist.add(f);
					}
				}
			}
			it=new TextLineIterator(filelist);
			
			
		}
	}
	TextLineIterator it;
	
	public static Connection getConnectionByLable(String databaselabel)
			throws ClassNotFoundException, SQLException, IOException {
		if (!driverup) {

			Class.forName("com.mysql.jdbc.Driver");

		}
		return DB.getConnection(databaselabel, null);
	}

	public static Connection getConnectionBy(String dbhost) throws ClassNotFoundException, SQLException, IOException {
		if (!driverup) {

			Class.forName("com.mysql.jdbc.Driver");

		}
		return DB.getConnection(dbhost, (String) null);
	}

	DataRow thisrow = null;
	private ResultSet thisrs;

	public boolean hasNext() throws FowerReadException {

		if (thissourcetype == sourcetype.db) {

			try {
				return thisrs.next();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new FowerReadException(e);
			}
		} else if (thissourcetype == sourcetype.fs) {
return it.hasNext();
		}
		return false;

	}

	public DataRow getRow() {
		if (thissourcetype == sourcetype.db) {
			return new DBDataRow(thisrs);
		} else if (thissourcetype == sourcetype.fs) {
			return it.next();
		}
		return null;
	}

	public boolean hasBackgroundKnowledge() {
		// TODO Auto-generated method stub
		return backgroundtablename != null;
	}

}
