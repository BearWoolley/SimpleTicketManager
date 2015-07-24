package uk.co.joshuawoolley.simpleticketmanager.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queries {
	
	private Connection connection = null;
	
	private String mysqlSQL = "CREATE TABLE IF NOT EXISTS tickets ( ticket_id INT (6) NOT NULL AUTO_INCREMENT, uuid VARCHAR (40), status VARCHAR (20) NOT NULL DEFAULT 'OPEN', reason VARCHAR (20) NOT NULL, description VARCHAR (50) NOT NULL, server_name VARCHAR (50) NOT NULL, player_amount smallint NOT NULL, world VARCHAR (30), created_date bigint NOT NULL , location VARCHAR (50), assigned_to VARCHAR (50) NULL, closed_by VARCHAR (50) NULL, closed_date bigint NULL, PRIMARY KEY (ticket_id));";
	private String sqliteSQL = "CREATE TABLE IF NOT EXISTS tickets ( ticket_id INTEGER PRIMARY KEY, uuid VARCHAR (40), status VARCHAR (20) NOT NULL DEFAULT 'OPEN', reason VARCHAR (20) NOT NULL, description VARCHAR (50) NOT NULL, server_name VARCHAR (50) NOT NULL, player_amount smallint NOT NULL, world VARCHAR (30), created_date bigint NOT NULL, location VARCHAR (50), assigned_to VARCHAR (50) NULL, closed_by VARCHAR (50) NULL, closed_date bigint NULL);";
	
	private String commentsMysqlSQL = "CREATE TABLE IF NOT EXISTS comments (comment_id INT (6) NOT NULL AUTO_INCREMENT, ticket_id INT (6) NOT NULL, uuid VARCHAR(40) NOT NULL, comment VARCHAR(255) NOT NULL, PRIMARY KEY (comment_id));";
	private String commentsSqliteSQL = "CREATE TABLE IF NOT EXISTS comments (comment_id INTEGER PRIMARY KEY, ticket_id INTEGER NOT NULL, uuid VARCHAR(40) NOT NULL, comment VARCHAR(255) NOT NULL);";
	
	/**
	 * Queries for Database
	 * 
	 * @param connection
	 * 			The connection to the database
	 */
	public Queries(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Creates the MySQL tables
	 * 
	 * @return successful
	 */
	public boolean createMySQLTable() {
		boolean successful = false;
		if (executeUpdate(mysqlSQL) && executeUpdate(commentsMysqlSQL)) {
			successful = true;
		}
		return successful;
	}
	
	/**
	 * Creates the SQLite tables
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean createSQLiteTable() {
		boolean successful = false;
		if (executeUpdate(sqliteSQL) && executeUpdate(commentsSqliteSQL)) {
			successful = true;
		}
		return successful;
	}
	
	/**
	 * Execute a update against the database
	 * 
	 * @param sql
	 * 			The sql query to execute
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean executeUpdate(String sql) {
		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Execute a query against the database
	 * 
	 * @param sql
	 * 			The sql query to execute
	 * 
	 * @return the ResultSet
	 */
	public ResultSet executeQuery(String sql) {
		try {
			Statement statement = connection.createStatement();
			ResultSet res = statement.executeQuery(sql);
			return res;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * Insert a ticket into the database
	 * 
	 * @param uuid
	 * 			The players uuid
	 * @param reason
	 * 			Reason for the report
	 * @param description
	 * 			Description of the report
	 * @param playerAmount
	 * 			Amount of players online
	 * @param world
	 * 			The world of report
	 * @param date
	 * 			The date of the report
	 * @param location
	 * 			The location of the report
	 * @param server
	 * 			The server of the report
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean insertTicket(String uuid, String reason, String description, int playerAmount, String world, long date, String location, String server) {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO tickets (uuid, reason, description, server_name, player_amount, world, created_date, location) VALUES ('" + uuid + "', ?, ?, ?, '" + playerAmount + "', '" + world + "', '" + date + "', '" + location + "');");
			ps.setString(1, reason);
			ps.setString(2, description);
			ps.setString(3, server);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Insert a comment into the database
	 * 
	 * @param player
	 * 			The players uuid
	 * @param comment
	 * 			The comment
	 * @param id
	 * 			The ticket id
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean insertComment(String player, String comment, int id) {
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement("INSERT INTO comments (uuid, ticket_id, comment) VALUES (?, ?, ?);");
			ps.setString(1, player);
			ps.setInt(2, id);
			ps.setString(3, comment);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Set a ticket assigned
	 * 
	 * @param player
	 * 			Players uuid
	 * @param id
	 * 			The ticket id
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean setAssigned(String player, int id) {
		String sql = "UPDATE tickets SET status='ASSIGNED', assigned_to='" + player + "' WHERE ticket_id='" + id + "';";
		return executeUpdate(sql);
	}
	
	/**
	 * Set a ticket closed
	 * 
	 * @param player
	 * 			The players uuid who is closing the ticket
	 * @param assignedTo
	 * 			The players uuid who is assigned to the ticket
	 * @param id
	 * 			The ticket id
	 * @param date
	 * 			The date of closing
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean setClosed(String player, String assignedTo, int id, long date) {
		String sql = "UPDATE tickets SET status='CLOSED', assigned_to='" + assignedTo + "', closed_by='" + player + "', closed_date='" + date + "' WHERE ticket_id='" + id + "';";
		return executeUpdate(sql);
	}
	
	/**
	 * Unclaim a ticket
	 * 
	 * @param id
	 * 			The id of the ticket
	 * 
	 * @return true if successful or false if unsuccessful
	 */
	public boolean setUnclaimed(int id) {
		String sql = "UPDATE tickets SET status='OPEN', assigned_to='NULL' WHERE ticket_id='" + id + "';";
		return executeUpdate(sql);
	}
	
	/**
	 * Keeps the connection alive to the database with a basic query
	 */
	public void keepConnectionAlive() {
		String sql = "SELECT COUNT(status) AS Amount FROM tickets WHERE status = 'OPEN';"; //Small query to keep the connection open
		executeQuery(sql);
	}
	
	/**
	 * Get all the tickets from the database
	 * 
	 * @return all tickets
	 */
	public ResultSet loadAllTickets() {
		String sql = "SELECT * FROM tickets;";
		return executeQuery(sql);
	}
	
	/**
	 * Get all the comments from the database for a specific id
	 * 
	 * @param id
	 * 			The ticket id
	 * @return all comments for the id
	 */
	public ResultSet loadAllComments(int id) {
		String sql = "SELECT * FROM comments WHERE ticket_id = '" + id + "' ORDER BY comment_id;";
		return executeQuery(sql);
	}
	
	/**
	 * Check if a ticket exists in the database
	 * 
	 * @param id
	 * 			The ticket id
	 * @return true if ticket exists or false if it doesn't
	 * 
	 * @throws SQLException
	 * 			Thrown if there's a error with the sql
	 */
	public boolean checkIfTicketExists(int id) throws SQLException {
		String sql = "SELECT 1 FROM tickets WHERE ticket_id='" + id + "';";
		ResultSet rs = executeQuery(sql);
		return rs.next();
	}
	
	/**
	 * Check to see if the comment already exists
	 * 
	 * @param player
	 * 			The players uuid
	 * @param comment
	 * 			The comment
	 * @param id
	 * 			The ticket id
	 * 
	 * @return true if comment exists or false if it doesn't
	 * 
	 * @throws SQLException
	 * 			Thrown if there's a error with the sql
	 */
	public boolean checkIfCommentExists(String player, String comment, int id) throws SQLException {
		String sql = "SELECT 1 FROM comments WHERE uuid = '" + player + "' AND ticket_id = '" + id + "' AND comment = '" + comment + "';";
		ResultSet rs = executeQuery(sql);
		return rs.next();
	}
	
	/**
	 * Close the connection to the database
	 * 
	 * @throws SQLException
	 * 			Thrown if there's a problem closing the connection
	 */
	public void closeConnection() throws SQLException {
		connection.close();
	}
}
