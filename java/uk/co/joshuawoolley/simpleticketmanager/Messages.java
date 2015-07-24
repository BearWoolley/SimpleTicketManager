package uk.co.joshuawoolley.simpleticketmanager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
* @author Josh Woolley
*/
public class Messages {
	
	private SimpleTicketManager plugin;
	public HashMap<String, String> messageData;
	
	/**
	 * Constuctor for all Messages
	 * 
	 * @param instance
	 * 			SimpleTicketManager instance
	 */
	public Messages(SimpleTicketManager instance) {
		plugin = instance;
		messageData = new HashMap<String, String>();
	}
	
	/**
	 * Get the Message Data HashMap
	 * 
	 * @return messageData
	 */
	public HashMap<String, String> getMessageData() {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		if (!f.exists()) {
			try {
				f.createNewFile();
				saveMessages();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return loadMessages();
	}
	
	/**
	 * Load the HashMap with all the latest messages from the config
	 * 
	 * @return messageData
	 */
	public HashMap<String, String> loadMessages() {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		for (String message : config.getConfigurationSection("").getKeys(false)) {
			messageData.put(message, config.getString(message));
		}
		return messageData;
	}
	
	private void saveMessages(){
		setMessage("tag", "&6[&4Ticket Manager&6]");
		setMessage("createTicket", "&bTicket has successfully been created for you with the ticket id &6%id&b. Use this id when adding comments.");
		setMessage("createComment", "&bComment has successfully been added to ticket &6%id");
		setMessage("adminUpdate", "&bA new report has been submitted! To view report do &c/ticket info %id");
		setMessage("reportHelp", "&bThe following commands are available;");
		setMessage("createHelp", "&b/report <reason> <description>  &6To open a report");
		setMessage("ticketHelpTitle", "&bThe following commands are available");
		setMessage("ticketHelp1", "&b/ticket view <open|asssigned|closed> <all>  &6To view which tickets are open");
		setMessage("ticketHelp2", "&b/ticket info <ticket id>  &6To view more information about a report");
		setMessage("ticketHelp3", "&b/ticket claim <ticket id>  &6Claim a ticket for you to resolve");
		setMessage("ticketHelp4", "&b/ticket close <ticket id>  &6Close a ticket once you resolved the report");
		setMessage("ticketHelp5", "&b/ticket teleport <ticket id>  &6Teleport to the location of the report");
		setMessage("ticketHelp6", "&b/ticket stats  &6View how many reports are open, assigned or closed");
		setMessage("noId", "&bNo ticket with that ID");
		setMessage("claimTicket", "&bYou have successfully claimed this ticket");
		setMessage("alreadyClaimed", "&bThis ticket has already been claimed");
		setMessage("alreadyClosed", "&bThis ticket has already been closed");
		setMessage("alreadyUnclaimed", "&bThis ticket can't be unclaimed");
		setMessage("closeTicket", "&bYou have successfully closed this ticket");
		setMessage("unclaimTicket", "&bYou have successfully unclaimed this ticket");
		setMessage("teleport", "&bYou have been teleported to the location of the report");
		setMessage("closeNotice", "&bTicket &6%id&b has just been closed by &6%player");
		setMessage("ticketTitle", "&b==============&4Information&b==============");
		setMessage("ticketId", "&aTicket ID: &6");
		setMessage("ticketStatus", "&aStatus of ticket: &6");
		setMessage("ticketAssignedTo", "&aAssigned To: &6");
		setMessage("ticketClosedBy", "&aClosed By: &6");
		setMessage("ticketClosedDate", "&aClosed Date: &6");
		setMessage("ticketReportingPlayer", "&aReporting Player: &6");
		setMessage("ticketReason", "&aReason: &6");
		setMessage("ticketDescription", "&aDescription: &6");
		setMessage("ticketAmount", "&aAmount of Players online: &6");
		setMessage("ticketServer", "&aServer: &6");
		setMessage("ticketDateCreated", "&aDate of report: &6");
		setMessage("ticketLocation", "&aLocation of report: &6");
		setMessage("ticketComments", "&aComments: &6%amount comment(s)");
		setMessage("ticketFooter", "&b=====================================");
		setMessage("statsTitle", "&b==============&4Stats&b==============");
		setMessage("statsOpen", "&aOpen: &6");
		setMessage("statsAssigned", "&aAssigned: &6");
		setMessage("statsClosed", "&aClosed: &6");
		setMessage("statsFooter", "&b================================");
		setMessage("openTitle", "&b==============&4Open Tickets&b==============");
		setMessage("openId", "&aTicket ID: &6");
		setMessage("openInfo", "&aDo /ticket info <number>  To view more info on a report");
		setMessage("openClaim", "&aDo /ticket claim <number>  To assign the ticket to yourself");
		setMessage("openFooter", "&b=======================================");
		setMessage("assignedTitle", "&b==============&4Assigned Tickets&ab==============");
		setMessage("assignedId", "&aTicket ID: &6");
		setMessage("assignedInfo", "&aDo /ticket info <number>  To view more info on a report");
		setMessage("assignedFooter", "&b=======================================");
		setMessage("allAssignedTitle", "&b==============&4All Assigned Tickets&b==============");
		setMessage("allAssignedId", "&aTicket ID: &6%id &ais assigned to &6%player");
		setMessage("allAssignedInfo", "&aDo /ticket info <number>  To view more info on a report");
		setMessage("allAssignedFooter", "&b=======================================");
		setMessage("closedTitle", "&b==============&4Closed Tickets&b==============");
		setMessage("closedId", "&aTicket ID: &6");
		setMessage("closedInfo", "&aDo /ticket info <number>  To view more info on a report");
		setMessage("closedFooter", "&b=======================================");
		setMessage("allClosedTitle", "&b==============&4All Closed Tickets&b==============");
		setMessage("allClosedId", "&aTicket ID: &6%id &awas closed by &6%player");
		setMessage("allClosedInfo", "&aDo /ticket info <number>  To view more info on a report");
		setMessage("allClosedFooter", "&b=======================================");
		setMessage("commentsTitle", "&b==============&4Comments for Ticket %id&b==============");
		setMessage("comment", "&6%player: &b%comment");
		setMessage("commentsFooter", "&b==============================================");
		setMessage("openNoTickets", "&6No Open tickets");
		setMessage("assignedNoTickets", "&6You have no assigned tickets");
		setMessage("allAssignedNoTickets", "&6No assigned tickets");
		setMessage("closedNoTickets", "&6You have no closed tickets");
		setMessage("allClosedNoTickets", "&6No closed tickets");
		setMessage("noComments", "&6No comments for this ticket");
		setMessage("loginNotice", "&bThere are currently %amount open tickets that need assigning!");
		setMessage("createFailed", "&bFailed to create ticket. Please contact a Admin if this problem continues");	
		setMessage("failClaimTicket", "&bFailed to claim this ticket, please contact a admin if this continues to happen!");
		setMessage("failCloseTicket", "&bFailed to close this ticket, please contact a admin if this continues to happen!");
		setMessage("ticketInfoError", "&4ERROR! You must enter /ticket info <id>");
		setMessage("ticketClaimError", "&4ERROR! You must enter /ticket claim <id>");
		setMessage("ticketCloseError", "&4ERROR! You must enter /ticket close <id>");
		setMessage("ticketTeleportError", "&4ERROR! You must enter /ticket teleport <id>");
		setMessage("ticketReload", "&bYou have successfully reloaded the configs!");
		setMessage("noPermission", "&4You do not have permission to use this command!");
		setMessage("mustBePlayer", "&4You must be a player to use this command");
	}
	
	private void setMessage(String name, String message) {
		File f = new File(plugin.getDataFolder() + File.separator + "messages.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(f);
		if (!config.isSet(name)) {
			config.set(name, message);
			try {
				config.save(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
