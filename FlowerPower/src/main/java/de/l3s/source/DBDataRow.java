package de.l3s.source;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDataRow implements DataRow {

	private ResultSet rs;

	public DBDataRow(ResultSet rs) {
		this.rs=rs;
	}

	@Override
	public String getText() throws FowerReadException {
		// TODO Auto-generated method stub
		try {
			return rs.getString("lem_nouns");
		} catch (SQLException e) {
			throw new FowerReadException(e);
		}
	}

	@Override
	public String getCategory() throws FowerReadException {
		// TODO Auto-generated method stub
		try {
			return rs.getString("category");
		} catch (SQLException e) {
			throw new FowerReadException(e);
		}
	}

	@Override
	public String getDocstrid() throws FowerReadException {
		// TODO Auto-generated method stub
		try {
			return rs.getString("document_id");
		} catch (SQLException e) {
			throw new FowerReadException(e);
		}
	}

	@Override
	public String getDocid() throws FowerReadException {
		try {
			return rs.getInt("id")+"";
		} catch (SQLException e) {
			throw new FowerReadException(e);
		}
	}
	

}

