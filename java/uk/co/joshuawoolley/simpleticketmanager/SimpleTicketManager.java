package uk.co.joshuawoolley.simpleticketmanager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import uk.co.joshuawoolley.simpleticketmanager.command.CommandHandler;
import uk.co.joshuawoolley.simpleticketmanager.database.MySQL;
import uk.co.joshuawoolley.simpleticketmanager.database.Queries;
import uk.co.joshuawoolley.simpleticketmanager.database.SQLite;
import uk.co.joshuawoolley.simpleticketmanager.events.PlayerJoin;
import uk.co.joshuawoolley.simpleticketmanager.ticketsystem.TicketManager;

/**
 * @author Josh Woolley
 */
public class SimpleTicketManager extends JavaPlugin {
	
	public HashMap<String, String> messageData = new HashMap<String, String>();
	private Connection connection = null;
	private Messages messages;
	private TicketManager manager;

	/**
	 * Enable method
	 */
	public void onEnable() {
		getLogger().info("Simple Ticket Manager is starting up. This may take a while!");

		this.saveDefaultConfig();
		
		messages = new Messages(this);
		messageData = messages.getMessageData();

		if(this.getConfig().getBoolean("mysql")){
			String hostname = this.getConfig().getString("hostname");
			String port = this.getConfig().getString("port");
			String database = this.getConfig().getString("database");
			String username = this.getConfig().getString("username");
			String password = this.getConfig().getString("password");
			MySQL MySQL = new MySQL(this, hostname, port, database, username, password);
			try {
				connection = MySQL.openConnection();
				createMySQLTables();	
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			SQLite sqlite = new SQLite(this, "/tickets.db");
			try {
				connection = sqlite.openConnection();
				createSQLiteTables();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		manager = new TicketManager(this, new Queries(connection), this.getConfig().getInt("maxtickets"));
		try {
			manager.loadInTickets();
		} catch (SQLException e) {
			getLogger().severe("There was a error loading in the tickets from the database!");
			e.printStackTrace();
		}
		this.getCommand("report").setExecutor(new CommandHandler(this, manager));
		this.getCommand("ticket").setExecutor(new CommandHandler(this, manager));
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(new PlayerJoin(this, manager), this);
		manager.startTask();
	
		try {
	        Metrics metrics = new Metrics(this);
	        metrics.start();
	    } catch (IOException e) {
	        // Failed to submit the stats :-(
	    }
		
		getLogger().info("Simple Ticket Managers has been successfully enabled!");
	}
	
	/**
	 * Disable method
	 */
	public void onDisable() {
		manager.onDisableUpdate();
		try{
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		getLogger().info("Simple Ticket Manager has been disabled");
	}
	
	/**
	 * Update messages hashmap with newest changes
	 */
	public void reloadMessages() {
		messages.loadMessages();
	}
	
	private void createMySQLTables() {
		Queries query = new Queries(connection);
		boolean created = query.createMySQLTable();
		if(!created) {
		getLogger().info("Error while creating MySQL database table. Do you have the correct database details in the config?");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
	
	private void createSQLiteTables() {
		Queries query = new Queries(connection);
		boolean created = query.createSQLiteTable();
		if(!created) {
		getLogger().info("Error while creating SQLite database table. Please report this to the plugin developer");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}
}
