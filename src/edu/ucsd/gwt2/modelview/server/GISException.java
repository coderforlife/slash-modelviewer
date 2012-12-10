package edu.ucsd.gwt2.modelview.server;

/**
 * An exception raised when reading invalid GIS WKB format data.
 * @author Jeffrey Bush
 */
public class GISException extends Exception
{
	private static final long serialVersionUID = 1235216596836303945L;
    public GISException() { super(); }
    public GISException(String message) { super(message); }
    public GISException(String s, Exception ex) { super(s, ex); }
}
