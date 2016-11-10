package de.l3s.source;

import java.io.File;
import java.io.FileReader;

public class DBFileRow implements DataRow {


	String category;
	String filename;
	String fileid;
	String text;
	
	
		

	public DBFileRow(String category, String filename, String fileid, String text) {
		super();
		this.category = category;
		this.filename = filename;
		this.fileid = fileid;
		this.text = text;
	}

	@Override
	public String getText() throws FowerReadException {
		// TODO Auto-generated method stub
		return text;
	}

	@Override
	public String getCategory() throws FowerReadException {
		// TODO Auto-generated method stub
		return category;
	}

	@Override
	public String getDocstrid() throws FowerReadException {
		// TODO Auto-generated method stub
		return filename;
	}

	@Override
	public String getDocid() throws FowerReadException {
		// TODO Auto-generated method stub
		return fileid;
	}

}
