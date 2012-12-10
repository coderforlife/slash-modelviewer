package edu.ucsd.gwt2.modelview.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectionPool
{
	private static class ConnTime
	{
		public final Connection connection;
		public final long lastuse;
		public ConnTime(Connection c, long lastuse) { this.connection = c; this.lastuse = lastuse; }
	}
	
	private final static long timeout = 60000; // 10 min
	private final String url, user, password;
	private final Queue<ConnTime> conns = new LinkedList<ConnTime>();
	private final Thread reaper = new Thread()
	{
	    private final static long delay = 300000; // 5 min
	    public void run()
	    {
	        while (true)
	        {
	           try
	           {
	              sleep(delay);
	           }
	           catch (InterruptedException e) { return; }
	           ConnectionPool.this.reapConnections();
	        }
	    }
	};

	public ConnectionPool(String url, String user, String password)
	{
		this.url = url;
		this.user = user;
		this.password = password;
		this.reaper.start();
	}

	public synchronized void reapConnections()
	{
		long stale = System.currentTimeMillis() - timeout;
		// Conns is always in order of last use, so we only have to look at the start until we find one that is okay
		while (this.conns.peek() != null)
		{
			ConnTime c = this.conns.peek();
			if (stale > c.lastuse || !validateConnection(c.connection))
			{
				closeConnection(c.connection);
				this.conns.remove();
			}
			else
			{
				break;
			}
		}
	}

	public synchronized void closeConnections() throws SQLException
	{
		while (this.conns.peek() != null)
		{
			closeConnection(this.conns.remove().connection);
		}
	}
	
	private static void closeConnection(Connection c)
	{
		try
		{
			if (c != null && !c.isClosed())
		    {
		    	c.close();
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static boolean validateConnection(Connection c)
	{
		try
		{
			c.getMetaData();
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	/**
	 * Gets a connection to the database
	 * @return a new or reused connection
	 * @throws SQLException
	 */
	public synchronized Connection getConnection() throws SQLException
	{
		return (this.conns.peek() == null) ? DriverManager.getConnection(this.url, this.user, this.password) : this.conns.remove().connection;
	}
	
	/**
	 * Returns a database connection so it can be reused or closed
	 * @param conn the connection
	 * @throws SQLException
	 */
	public synchronized void returnConnection(Connection c) throws SQLException
	{
		this.conns.offer(new ConnTime(c, System.currentTimeMillis()));
	}
}
