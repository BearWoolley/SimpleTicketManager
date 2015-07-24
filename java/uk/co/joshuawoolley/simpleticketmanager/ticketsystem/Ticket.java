package uk.co.joshuawoolley.simpleticketmanager.ticketsystem;

import java.util.Date;
import java.util.HashMap;

import uk.co.joshuawoolley.simpleticketmanager.enums.TicketStates;

/**
* @author Josh Woolley
*/
public class Ticket {
	
	private int ticketId;
	private TicketStates state;
	private String assignedTo;
	private String closedBy;
	private String reportingPlayer;
	private String reason;
	private String description;
	private Date dateCreated;
	private Date closedDate;
	private String location;
	private String world;
	private String server;
	private int playerAmount;
	private int commentId;
	
	private HashMap<Integer, HashMap<String, String>> comments;


	/**
	 * Create a Ticket Object
	 * 
	 * @param ticketId
	 * 				The id of the ticket
	 * @param reportingPlayer
	 * 				The player who is making the report
	 * @param reason
	 * 				The reason for the report
	 * @param description
	 * 				The description
	 * @param location
	 * 				The location of the report
	 * @param world
	 * 				The world where the report was done
	 */
	public Ticket(int ticketId, String reportingPlayer, String reason, String description, String location, String world, String server, int playerAmount) {
		this.ticketId = ticketId;
		this.state = TicketStates.OPEN;
		this.reportingPlayer = reportingPlayer;
		this.reason = reason;
		this.description = description;
		this.dateCreated = new Date();
		this.location = location;
		this.world = world;
		this.server = server;
		this.playerAmount = playerAmount;
		
		comments = new HashMap<Integer, HashMap<String, String>>();
	}

	/**
	 * @return the ticketId
	 */
	public int getTicketId() {
		return ticketId;
	}

	/**
	 * @param ticketId the ticketId to set
	 */
	public void setTicketId(int ticketId) {
		this.ticketId = ticketId;
	}

	/**
	 * @return the state
	 */
	public TicketStates getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(TicketStates state) {
		this.state = state;
	}

	/**
	 * @return the assignedTo
	 */
	public String getAssignedTo() {
		return assignedTo;
	}

	/**
	 * @param assignedTo the assignedTo to set
	 */
	public void setAssignedTo(String assignedTo) {
		this.assignedTo = assignedTo;
	}

	/**
	 * @return the closedBy
	 */
	public String getClosedBy() {
		return closedBy;
	}

	/**
	 * @param closedBy the closedBy to set
	 */
	public void setClosedBy(String closedBy) {
		this.closedBy = closedBy;
	}

	/**
	 * @return the reportingPlayer
	 */
	public String getReportingPlayer() {
		return reportingPlayer;
	}

	/**
	 * @param reportingPlayer the reportingPlayer to set
	 */
	public void setReportingPlayer(String reportingPlayer) {
		this.reportingPlayer = reportingPlayer;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason the reason to set
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	/**
	 * @return the closedDate
	 */
	public Date getClosedDate() {
		return closedDate;
	}

	/**
	 * @param closedDate the closedDate to set
	 */
	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}

	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}

	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}

	/**
	 * @return the comments
	 */
	public HashMap<Integer, HashMap<String, String>> getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(HashMap<Integer, HashMap<String, String>> comments) {
		this.comments = comments;
	}

	/**
	 * @return the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @param server the server to set
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * @return the playerAmount
	 */
	public int getPlayerAmount() {
		return playerAmount;
	}

	/**
	 * @param playerAmount the playerAmount to set
	 */
	public void setPlayerAmount(int playerAmount) {
		this.playerAmount = playerAmount;
	}

	/**
	 * @return the world
	 */
	public String getWorld() {
		return world;
	}

	/**
	 * @param world the world to set
	 */
	public void setWorld(String world) {
		this.world = world;
	}
	
	/**
	 * Add a comment into the comments HashMap
	 * @param player
	 * 			The UUID of the player
	 * @param comment
	 * 			The comment for the ticket
	 */
	public void addComment(String player, String comment) {
		HashMap<String, String> commentMap = new HashMap<String, String>();
		commentMap.put(comment, player);
		comments.put(commentId, commentMap);
		commentId++;
	}

}
