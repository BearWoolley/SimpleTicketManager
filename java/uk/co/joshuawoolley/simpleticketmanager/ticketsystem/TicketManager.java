package uk.co.joshuawoolley.simpleticketmanager.ticketsystem;

import static java.lang.Math.round;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import uk.co.joshuawoolley.simpleticketmanager.SimpleTicketManager;
import uk.co.joshuawoolley.simpleticketmanager.database.Queries;
import uk.co.joshuawoolley.simpleticketmanager.enums.TicketStates;

/**
* @author Josh Woolley
*/
public class TicketManager {
	
	private SimpleTicketManager plugin;
	private Queries query;
	
	private int currentTicketId = 1;
	private String tag;
	
	private int ticketId;
	
	private ArrayList<Ticket> allTickets;
	private ArrayList<Ticket> ticketsToUpdate;
	private ArrayList<Ticket> commentsToUpdate;
	
	/**
	 * Create manager to control Tickets
	 * 
	 * @param plugin
	 * 			Main SimpleTicketManager instance
	 * @param query
	 * 			Queries object to call database queries
	 */
	public TicketManager(SimpleTicketManager plugin, Queries query) {
		this.plugin = plugin;
		this.query = query;
		allTickets = new ArrayList<Ticket>();
		ticketsToUpdate = new ArrayList<Ticket>();
		commentsToUpdate = new ArrayList<Ticket>();
		
		tag = ChatColor.translateAlternateColorCodes('&',plugin.messageData.get("tag"));
	}
	
	/**
	 * Create a new ticket and add it to allTickets
	 * 
	 * @param player
	 * 			Player doing report
	 * @param reason
	 * 			Reason for report
	 * @param description
	 * 			Description of report
	 * @param l
	 * 			Location of report
	 * @param server
	 * 			Server the report was done on
	 */
	public void createTicket(CommandSender sender, String player, String reason, String description, Location l, String server, int playerAmount) {
		String world = l.getWorld().toString();
		String tempWorld[] = world.split("=");
		world = tempWorld[1].substring(0, tempWorld[1].length() - 1);

		long x = round(l.getX());
		long y = round(l.getY());
		long z = round(l.getZ());
		String location = "X:" + x + " Y:" + y + " Z:" + z;
		
		Ticket newTicket = new Ticket(currentTicketId, player, reason, description, location, world, server, playerAmount);
		ticketId = newTicket.getTicketId();
		
		allTickets.add(newTicket);
		ticketsToUpdate.add(newTicket);
		currentTicketId++;
		sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createTicket"))));
		
		for(Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("ticket.use")) {
				p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("adminUpdate"))));
			}
		}
	}
	
	/**
	 * Create a new ticket and add it to allTickets
	 * 
	 * @param player
	 * 			Player doing report
	 * @param ticketState 
	 * @param reason
	 * 			Reason for report
	 * @param description
	 * 			Description of report
	 * @param location
	 * 			Location of report
	 * @param createdDate 
	 * @param world
	 * 			World of report
	 * @param server
	 * 			Server the report was done on
	 */
	public Ticket createTicket(String player, TicketStates ticketState, String reason, String description, String location, Date createdDate, String world, String server, int playerAmount) {
		Ticket newTicket = new Ticket(currentTicketId, player, reason, description, location, world, server, playerAmount);
		newTicket.setState(ticketState);
		newTicket.setDateCreated(createdDate);
		
		allTickets.add(newTicket);
		currentTicketId++;
		return newTicket;
	}
	
	/**
	 * Create a comment for a specified ticket
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param player
	 * 			The UUID of the player
	 * @param comment
	 * 			The comment to add to the ticket
	 * @param id
	 * 			The ticket id
	 */
	public void createComment(CommandSender sender, String player, String comment, int id) {
		Ticket ticket = getTicket(id);
		if (ticket != null) {
			ticket.addComment(player, comment);
			commentsToUpdate.add(ticket);
			ticketId = ticket.getTicketId();
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createComment"))));
		}
	}
	
	/**
	 * Task to keep updating database
	 */
	@SuppressWarnings("deprecation")
	public void startTask() {
		long delay = plugin.getConfig().getLong("updateTime") * 20;
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new BukkitRunnable() {
            public void run(){
            	boolean successful = false;
            	boolean noneUpdated = false;
        		if (!ticketsToUpdate.isEmpty()) {
        			Iterator<Ticket> it = ticketsToUpdate.iterator();
        				while (it.hasNext()) {
        					Ticket ticket = it.next();
        					if (ticket.getState().equals(TicketStates.OPEN)) {
            					try {
									if (!query.checkIfTicketExists(ticket.getTicketId())) {
										successful = query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
									} else {
										successful = query.setUnclaimed(ticket.getTicketId());
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
            				} else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
            					successful = query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
            				} else if (ticket.getState().equals(TicketStates.CLOSED)) {
            					successful = query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
            				}
            				if (!successful) {
            					Bukkit.getLogger().severe("[SimpleTicketManager] There was a problem with updating 1 ticket to the database");
            				}
            				it.remove();
            				noneUpdated = true;
        				}
        				Bukkit.getLogger().info("[SimpleTicketManager] Database has sucessfully updated");
        		}
        		if (!commentsToUpdate.isEmpty()) {
        			Iterator<Ticket> it = commentsToUpdate.iterator();
    				while (it.hasNext()) {
    					Ticket ticket = it.next();
    					HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
    					for (Entry<Integer, HashMap<String, String>> entryComments : commentsMap.entrySet()) {
    						HashMap<String, String> comments = entryComments.getValue();
    						for (Entry<String, String> entry : comments.entrySet()) {
        						try {
    								if(!query.checkIfCommentExists(entry.getValue(),entry.getKey(), ticket.getTicketId())) {
    									query.insertComment(entry.getValue(),entry.getKey(), ticket.getTicketId());
    									noneUpdated = true;
    								}
    							} catch (SQLException e) {
    								e.printStackTrace();
    							}
        				    }
    					}
    					it.remove();
    				}
    				Bukkit.getLogger().info("[SimpleTicketManager] Database has sucessfully updated");
        		}
        		if (!noneUpdated) {
        			query.keepConnectionAlive();
        			Bukkit.getLogger().info("[SimpleTicketManager] Database connection has been kept alive");
        		}
			}
		}, 0L, delay);

	}
	
	/**
	 * Set a ticket to assigned
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param player
	 * 			The player being assigned
	 * @param id
	 * 			The ticket id
	 */
	public void setTicketAssigned(CommandSender sender, String player, int id) {
		Ticket ticket = getTicket(id);
		ticketId = id;
		if (ticket != null) {
			if (ticket.getState().equals(TicketStates.OPEN)) {
				ticket.setAssignedTo(player);
				ticket.setState(TicketStates.ASSIGNED);
				ticketsToUpdate.add(ticket);
				sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("claimTicket"))));
			} else {
				sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyClaimed"))));
			}
		} else {
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
		}
	}
	
	/**
	 * Set a ticket to closed
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param player
	 * 			The player being assigned
	 * @param id
	 * 			The ticket id
	 */
	public void setTicketClosed(CommandSender sender, String player, int id) {
		Ticket ticket = getTicket(id);
		ticketId = id;
		if (ticket != null) {
			if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				boolean ableToClose = false;
				if (!plugin.getConfig().getBoolean("allowotherstoclose")) {
					if (ticket.getAssignedTo().equals(player)) {
						ableToClose = true;
					}
				}
				if (ableToClose) {
					ticket.setClosedBy(player);
					ticket.setClosedDate(new Date());
					ticket.setState(TicketStates.CLOSED);
					ticketsToUpdate.add(ticket);
					sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closeTicket"))));
					if (plugin.getConfig().getBoolean("closenotice")) {
						for (Player p : Bukkit.getServer().getOnlinePlayers()) {
							if (p.hasPermission("ticket.admin")) {
								p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("closeNotice")), player)));
							}
						}
					}
				}
			} else {
				sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyClosed"))));
			}
		} else {
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
		}
	}
	
	/**
	 * Unclaim a ticket
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param id
	 * 			The ticket id
	 */
	public void setUnclaimed(CommandSender sender, int id) {
		Ticket ticket = getTicket(id);
		ticketId = id;
		if (ticket != null) {
			if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				ticket.setAssignedTo(null);
				ticket.setState(TicketStates.OPEN);
				ticketsToUpdate.add(ticket);
				sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("unclaimTicket"))));
			} else {
				sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("alreadyUnclaimed"))));
			}
		} else {
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
		}
	}
	
	/**
	 * Print out information about a ticket
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param id
	 * 			The ticket id
	 */
	public void printTicketInfo(CommandSender sender, int id) {
		Ticket ticket = getTicket(id);
		ticketId = id;
		if (ticket != null) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketTitle"))));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketId"))) + ticket.getTicketId());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketStatus"))) + ticket.getState().toString());
			
			if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
			} else if (ticket.getState().equals(TicketStates.CLOSED)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAssignedTo"))) + getName(ticket.getAssignedTo()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClosedBy"))) + getName(ticket.getClosedBy()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClosedDate"))) + ticket.getClosedDate().toString());
			}
			sender.sendMessage("");
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketReportingPlayer"))) + getName(ticket.getReportingPlayer()));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketReason"))) + ticket.getReason());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketDescription"))) + ticket.getDescription());
			if (plugin.getConfig().getBoolean("players")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketAmount"))) + ticket.getPlayerAmount());
			}
			if (plugin.getConfig().getBoolean("bungeecord")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketServer"))) + ticket.getServer());
			}
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketDateCreated"))) + ticket.getDateCreated().toString());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketLocation"))) + ticket.getLocation() + " World: " + ticket.getWorld());
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkSize(checkMessages(plugin.messageData.get("ticketComments")), ticket.getComments().size())));
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketFooter"))));
		} else {
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
		}
	}
	
	/**
	 * Teleport a player to the report
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param id
	 * 			The ticket id
	 */
	public void teleportPlayer(CommandSender sender, int id) {
		Ticket ticket = getTicket(id);
		if (ticket != null) {
			Player p = (Player) sender;
			String location = ticket.getLocation();
			String temp[] = location.split(" ");
			String tempx[] = temp[0].split(":");
			String tempy[] = temp[1].split(":");
			String tempz[] = temp[2].split(":");
			Double x = Double.parseDouble(tempx[1].trim());
			Double y = Double.parseDouble(tempy[1].trim());
			Double z = Double.parseDouble(tempz[1].trim());
			Location l = new Location(Bukkit.getWorld(ticket.getWorld()), x, y, z);
			p.teleport(l);
			p.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("teleport"))));
		}
	}
	
	/**
	 * Print ticket stats
	 * 
	 * @param sender
	 * 			The CommandSender
	 */
	public void printTicketStats(CommandSender sender) {
		int open = 0;
		int assigned = 0;
		int closed = 0;
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.OPEN)) {
				open++;
			} else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				assigned++;
			} else if (ticket.getState().equals(TicketStates.CLOSED)) {
				closed++;
			}
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsTitle"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsOpen"))) + open);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsAssigned"))) + assigned);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsClosed"))) + closed);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("statsFooter"))));
	}
	
	/**
	 * Print Open tickets
	 * 
	 * @param sender
	 * 			The CommandSender
	 */
	public void printOpenTickets(CommandSender sender) {
		ArrayList<Ticket> tickets = getTicketsOpen();
		String view = "";
		if (!tickets.isEmpty()) {
			for (Ticket ticket : tickets) {
				view = view + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openId"))) + ticket.getTicketId() + "\n";
			}
		} else {
			view = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openNoTickets")));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openTitle"))));
		sender.sendMessage(view);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openInfo"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openClaim"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("openFooter"))));
	}
	
	/**
	 * Print Assigned Tickets for a player
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param player
	 * 			The uuid of the player
	 */
	public void printAssignedTickets(CommandSender sender, String player) {
		ArrayList<Ticket> tickets = getTicketsAssignedForPlayer(player);
		String view = "";
		if (!tickets.isEmpty()) {
			for (Ticket ticket : tickets) {
				view = view + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedId"))) + ticket.getTicketId() + "\n";
			}
		} else {
			view = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedNoTickets")));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedTitle"))));
		sender.sendMessage(view);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedInfo"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("assignedFooter"))));
	}
	
	/**
	 * Print all the tickets for all players
	 * 
	 * @param sender
	 * 			The CommandSender
	 */
	public void printAllAssignedTickets(CommandSender sender) {
		ArrayList<Ticket> tickets = getTicketsAssignedForAllPlayers();
		String view = "";
		if (!tickets.isEmpty()) {
			for (Ticket ticket : tickets) {
				ticketId = ticket.getTicketId();
				view = view + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("allAssignedId")), ticket.getAssignedTo())) + "\n";
			}
		} else {
			view = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedNoTickets")));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedTitle"))));
		sender.sendMessage(view);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedInfo"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allAssignedFooter"))));
	}
	
	/**
	 * Print closed tickets for a user
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param player
	 * 			The players uuid
	 */
	public void printClosedTickets(CommandSender sender, String player) {
		ArrayList<Ticket> tickets = getTicketsClosedForPlayer(player);
		String view = "";
		if (!tickets.isEmpty()) {
			for (Ticket ticket : tickets) {
				view = view + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedId"))) + ticket.getTicketId() + "\n";
			}
		} else {
			view = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedNoTickets")));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedTitle"))));
		sender.sendMessage(view);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedInfo"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("closedFooter"))));
	}
	
	/**
	 * Print all closed tickets for all users
	 * 
	 * @param sender
	 * 			The CommandSender
	 */
	public void printAllClosedTickets(CommandSender sender) {
		ArrayList<Ticket> tickets = getTicketsClosedForAllPlayers();
		String view = "";
		if (!tickets.isEmpty()) {
			for (Ticket ticket : tickets) {
				ticketId = ticket.getTicketId();
				view = view + ChatColor.translateAlternateColorCodes('&', checkPlayer(checkMessages(plugin.messageData.get("allClosedId")), ticket.getClosedBy())) + "\n";
			}
		} else {
			view = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedNoTickets")));
		}
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedTitle"))));
		sender.sendMessage(view);
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedInfo"))));
		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("allClosedFooter"))));
	}
	
	/**
	 * Prints out the comments for a ticket
	 * 
	 * @param sender
	 * 			The CommandSender
	 * @param id
	 * 			The ticket id
	 */
	public void printComments(CommandSender sender, int id) {
		Ticket ticket = getTicket(id);
		if (ticket != null) {
			String commentsList = "";
			ticketId = ticket.getTicketId();
			HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
			if (!commentsMap.isEmpty()) {
				for (int i = 0; i < commentsMap.size(); i++) {
					HashMap<String, String> comments = commentsMap.get(i);
					for (Entry<String, String> entry : comments.entrySet()) {
							String uuid = entry.getValue();
							String comment = entry.getKey();
							commentsList = commentsList + ChatColor.translateAlternateColorCodes('&', checkComment(checkPlayer(checkMessages(plugin.messageData.get("comment")), uuid), comment)) + "\n";
					}
				}
			} else {
				commentsList = ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noComments")));
			}
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("commentsTitle"))));
			sender.sendMessage(commentsList);
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("commentsFooter"))));
		} else {
			sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noId"))));
		}
	}
	
	/**
	 * Get the amount of open tickets
	 * 
	 * @return amount of open tickets
	 */
	public int getOpenTicketsSize() {
		ArrayList<Ticket> tickets = getTicketsOpen();
		return tickets.size();
	}
	
	/**
	 * Load in all the tickets from the database
	 * @throws SQLException
	 */
	public void loadInTickets() throws SQLException {
		ResultSet rs = query.loadAllTickets();
		if (rs != null) {
			while (rs.next()) {
				String id = rs.getString("ticket_id");
				String player = rs.getString("uuid");
				TicketStates ticketState = TicketStates.valueOf(rs.getString("status"));
				String reason = rs.getString("reason");
				String description = rs.getString("description");
				Date createdDate = new Date(rs.getLong("created_date"));
				String location = rs.getString("location");
				String world = rs.getString("world");
				String server = rs.getString("server_name");
				int playerAmount = rs.getInt("player_amount");
				
				currentTicketId = Integer.parseInt(id);
				
				Ticket ticket = createTicket(player, ticketState, reason, description, location, createdDate, world, server, playerAmount);
				
				ResultSet set = query.loadAllComments(ticket.getTicketId());
				if (set != null) {
					while (set.next()) {
						String uuid = set.getString("uuid");
						String comment = set.getString("comment");
						ticket.addComment(uuid, comment);
					}
				}
				
				if (ticketState.equals(TicketStates.ASSIGNED)) {
					String assignedTo = rs.getString("assigned_to");
					ticket.setAssignedTo(assignedTo);
				} else if (ticketState.equals(TicketStates.CLOSED)) {
					String closedBy = rs.getString("closed_by");
					Date closedDate = rs.getDate("closed_date");
					ticket.setClosedBy(closedBy);
					ticket.setClosedDate(closedDate);
				}
			}
		}
	}
	
	/**
	 * Update the database on shutdown
	 */
	public void onDisableUpdate() {
		if (ticketsToUpdate.size() > 0) {
			for(Ticket ticket : ticketsToUpdate) {
				if (ticket.getState().equals(TicketStates.OPEN)) {
					try {
						if (query.checkIfTicketExists(ticket.getTicketId())) {
							query.insertTicket(ticket.getReportingPlayer(), ticket.getReason(), ticket.getDescription(), ticket.getPlayerAmount(), ticket.getWorld(), ticket.getDateCreated().getTime(), ticket.getLocation(), ticket.getServer());
						} else {
							query.setUnclaimed(ticket.getTicketId());
						}
					} catch (SQLException e) {
						e.printStackTrace();
						Bukkit.getLogger().severe("There was a problem with updating 1 ticket to the database");
					}
				} else if (ticket.getState().equals(TicketStates.ASSIGNED)) {
					query.setAssigned(ticket.getAssignedTo(), ticket.getTicketId());
				} else if (ticket.getState().equals(TicketStates.CLOSED)) {
					query.setClosed(ticket.getClosedBy(), ticket.getAssignedTo(), ticket.getTicketId(), ticket.getClosedDate().getTime());
				}
			}
		}
		if (!commentsToUpdate.isEmpty()) {
			Iterator<Ticket> it = commentsToUpdate.iterator();
			while (it.hasNext()) {
				Ticket ticket = it.next();
				HashMap<Integer, HashMap<String, String>> commentsMap = ticket.getComments();
				for (Entry<Integer, HashMap<String, String>> entryComments : commentsMap.entrySet()) {
					HashMap<String, String> comments = entryComments.getValue();
					for (Entry<String, String> entry : comments.entrySet()) {
						try {
							if(!query.checkIfCommentExists(entry.getValue(),entry.getKey(), ticket.getTicketId())) {
								query.insertComment(entry.getValue(),entry.getKey(), ticket.getTicketId());
							}
						} catch (SQLException e) {
							e.printStackTrace();
						}
				    }
				}
				it.remove();
			}
		}
		Bukkit.getLogger().info("[SimpleTicketManager] Database has been successfully updated");
	}
	
	private Ticket getTicket(int id) {
		Ticket selectedTicket = null;
		for (Ticket ticket : allTickets) {
			if (ticket.getTicketId() == id) {
				selectedTicket = ticket;
				break;
			}
		}
		return selectedTicket;
	}
	
	private ArrayList<Ticket> getTicketsOpen() {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.OPEN)) {
				tickets.add(ticket);
			}
		}
		return tickets;
	}
	
	private ArrayList<Ticket> getTicketsAssignedForPlayer(String player) {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				if (ticket.getAssignedTo().equals(player)) {
					tickets.add(ticket);
				}
			}
		}
		return tickets;
	}
	
	private ArrayList<Ticket> getTicketsAssignedForAllPlayers() {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.ASSIGNED)) {
				tickets.add(ticket);
			}
		}
		return tickets;
	}
	
	private ArrayList<Ticket> getTicketsClosedForPlayer(String player) {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.CLOSED)) {
				if (ticket.getClosedBy().equals(player)) {
					tickets.add(ticket);
				}
			}
		}
		return tickets;
	}
	
	private ArrayList<Ticket> getTicketsClosedForAllPlayers() {
		ArrayList<Ticket> tickets = new ArrayList<Ticket>();
		for (Ticket ticket : allTickets) {
			if (ticket.getState().equals(TicketStates.CLOSED)) {
				tickets.add(ticket);
			}
		}
		return tickets;
	}
	
	private String checkMessages(String message) {
		if (message.contains("%id")) {
			String newMessage = message.replace("%id", "" + ticketId);
			return newMessage;
		} else {
			return message;
		}
	}
	
	private String checkSize(String message, int size) {
		if (message.contains("%amount")) {
			String newMessage = message.replace("%amount", "" + size);
			return newMessage;
		} else {
			return message;
		}
	}
	
	private String checkPlayer(String message, String player) {
		if (message.contains("%player")) {
			String newMessage = message.replace("%player", getName(player));
			return newMessage;
		} else {
			return message;
		}
	}
	
	private String checkComment(String message, String comment) {
		if (message.contains("%comment")) {
			String newMessage = message.replace("%comment", comment);
			return newMessage;
		} else {
			return message;
		}
	}
	
	private String getName(String uuid) {
		return Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid)).getName();
	}

}
 