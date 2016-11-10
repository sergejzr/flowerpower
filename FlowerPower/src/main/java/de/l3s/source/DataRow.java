package de.l3s.source;

public interface DataRow {

	public String getText() throws FowerReadException;

	public String getCategory() throws FowerReadException;

	public String getDocstrid() throws FowerReadException;

	public String getDocid() throws FowerReadException;

}
