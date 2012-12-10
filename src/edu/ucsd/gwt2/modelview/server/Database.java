package edu.ucsd.gwt2.modelview.server;

//import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
//import java.util.PropertyResourceBundle;
//import java.util.ResourceBundle;

public class Database
{
	// setup connection values to the database
	public static final String DB_DRIVER = "org.postgresql.Driver";
	public static String URL = "jdbc:postgresql://dev-db.crbs.ucsd.edu:5432/ccdbv2_2";
	public static String USERNAME = "ccdbd_dev";
	public static String PASSWORD = "Test753";
	public static ConnectionPool pool = new ConnectionPool(URL, USERNAME, PASSWORD);

    // Load the driver when this class is first loaded
    static
    {
        try
        {
            Class.forName(DB_DRIVER).newInstance();
        }
        catch (ClassNotFoundException cnfx) { cnfx.printStackTrace(); }
        catch (IllegalAccessException iaex) { iaex.printStackTrace(); }
        catch (InstantiationException iex) { iex.printStackTrace(); }
    }

//	/**
//	 * Read the database configuration from a resource file.
//	 * @param path path to the resource file
//	 * @throws Exception
//	 */
//	public static void readConfig(String path) throws Exception
//	{
//  	try
//		{
//			//Now, go get the connection info from resource files
//			//Then get the real connection info from the real resource
//			ResourceBundle rb = new PropertyResourceBundle(new FileInputStream(path));
//			USERNAME = rb.getString("postgres_user");
//			PASSWORD = rb.getString("postgres_password");
//			URL = rb.getString("postgres_jdbc_url"); //connectString+HOST+":"+PORT+":"+DATABASENAME;
//			Database.pool.closeConnections();
//			Database.pool = new ConnectionPool(URL, USERNAME, PASSWORD);
//		}
//		catch (Exception e)
//		{
//			e.printStackTrace();
//			throw new Exception("Problem reading database parameters: "+e.getMessage());
//		}
//	}
    
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
