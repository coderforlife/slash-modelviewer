package edu.ucsd.gwt2.modelview.server;

import java.sql.Connection;
import java.sql.SQLException;
//import java.util.PropertyResourceBundle;
//import java.util.ResourceBundle;

/**
 * Database access class. Most of this is deferred to the connection pool.
 * It requires a class called DBParams with constant strings URL, USERNAME, and PASSWORD.
 * @author Jeffrey Bush
 */
public class Database
{
	// Setup connection values to the database
	private final static ConnectionPool pool = new ConnectionPool(DBParams.URL, DBParams.USERNAME, DBParams.PASSWORD);

    /**
     * Gets a connection to the database
     * @return a new or reused connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException
    {
    	return Database.pool.getConnection();
    }
    
    /**
     * Returns a database connection so it can be reused or closed
     * @param c the connection
     * @throws SQLException
     */
    public static void returnConnection(Connection c) throws SQLException
    {
    	Database.pool.returnConnection(c);
    }
}
