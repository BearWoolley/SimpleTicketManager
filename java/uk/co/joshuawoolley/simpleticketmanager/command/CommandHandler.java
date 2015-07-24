package uk.co.joshuawoolley.simpleticketmanager.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.co.joshuawoolley.simpleticketmanager.SimpleTicketManager;
import uk.co.joshuawoolley.simpleticketmanager.ticketsystem.TicketManager;

public class CommandHandler implements CommandExecutor {

	private SimpleTicketManager plugin;
	private TicketManager manager;

	private String tag = "";
	private int id;

	/**
	 * Command Handler to handle all commands
	 * 
	 * @param instance
	 * 			SimpleTicketManager instance
	 * @param manager
	 * 			TicketManager instance
	 */
	public CommandHandler(SimpleTicketManager instance, TicketManager manager) {
		plugin = instance;
		this.manager = manager;
		tag = ChatColor.translateAlternateColorCodes('&',plugin.messageData.get("tag"));
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label,String[] args) {
		if (cmd.getName().equalsIgnoreCase("report")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("report.use")) {
					if (args.length > 0) {
						String description = "";
						for (int i = 1; i < args.length; i++) {
							description = description + " " + args[i].toString();
						}
						manager.createTicket(sender, player.getUniqueId().toString(), args[0], description, player.getLocation(), plugin.getServer().getServerName(), Bukkit.getOnlinePlayers().size());
					} else {
						sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("reportHelp"))));
						sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("createHelp"))));
					}
				} else {
					sender.sendMessage(checkMessages(plugin.messageData.get("noPermission")));
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("mustBePlayer"))));
				return false;
			}
		} else if (cmd.getName().equalsIgnoreCase("ticket")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length > 0) {
					if (args[0].equalsIgnoreCase("view")) {
						if (player.hasPermission("ticket.view")) {
							if (args.length > 1) {
								if (args[1].equalsIgnoreCase("open")) {
									manager.printOpenTickets(sender);
								} else if (args[1].equalsIgnoreCase("assigned")) {
									if (args.length > 2) {
										if (args[2].equalsIgnoreCase("all")) {
											manager.printAllAssignedTickets(sender);
										}
									} else {
										manager.printAssignedTickets(sender, player.getUniqueId().toString());
									}
								} else if (args[1].equalsIgnoreCase("closed")) {
									if (args.length > 2) {
										if (args[2].equalsIgnoreCase("all")) {
											manager.printAllClosedTickets(sender);
										}
									} else {
										manager.printClosedTickets(sender, player.getUniqueId().toString());
									}
								}
							} else {
								sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp1"))));
							}
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("info")) {
						if (player.hasPermission("ticket.info")) {
							if (args.length > 1) {
								id = Integer.parseInt(args[1]);
								manager.printTicketInfo(sender, id);
							} else {
								sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketInfoError"))));
							}
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("comment")) {
						if (player.hasPermission("ticket.comments")) {
							if (args.length > 1) {
								if (args[1].equalsIgnoreCase("add")) {
									id = Integer.parseInt(args[2]);
									String comment = "";
									for (int i = 3; i < args.length; i++) {
										comment = comment + " " + args[i].toString();
									}
									manager.createComment(sender, player.getUniqueId().toString(), comment, id);
								} else if (args[1].equalsIgnoreCase("view")) {
									id = Integer.parseInt(args[2]);
									manager.printComments(sender, id);
								}
							}
						}
					} else if (args[0].equalsIgnoreCase("claim")) {
						if (player.hasPermission("ticket.admin")) {
							if (args.length > 1) {
								id = Integer.parseInt(args[1]);
								manager.setTicketAssigned(sender, player.getUniqueId().toString(), id);
							} else {
								sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketClaimError"))));
							}
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("close")) {
						if (player.hasPermission("ticket.admin")) {
							if (args.length > 1) {
								id = Integer.parseInt(args[1]);
								manager.setTicketClosed(sender, player.getUniqueId().toString(), id);
							} else {
								sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketCloseError"))));
							}
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("teleport")) {
						if (player.hasPermission("ticket.teleport")) {
							if (args.length > 1) {
								id = Integer.parseInt(args[1]);
								manager.teleportPlayer(sender, id);
							} else {
								sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketTeleportError"))));
							}
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("stats")) {
						if (player.hasPermission("ticket.stats")) {
							manager.printTicketStats(sender);
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					} else if (args[0].equalsIgnoreCase("unclaim")) {
						if (player.hasPermission("ticket.admin")) {
							manager.setUnclaimed(sender, id);
						}
					} else if (args[0].equalsIgnoreCase("reload")) {
						if (player.hasPermission("ticket.reload")) {
							plugin.reloadConfig();
							plugin.reloadMessages();
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("ticketReload")));
						} else {
							sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("noPermission"))));
						}
					}
				} else {
					sender.sendMessage(tag + ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelpTitle"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp1"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp2"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp3"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp4"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp5"))));
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("ticketHelp6"))));
				}
				return true;
			} else {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', checkMessages(plugin.messageData.get("mustBePlayer"))));
				return false;
			}
		} else {
			return false;
		}
	}

	private String checkMessages(String message) {
		if (message.contains("%id")) {
			String newMessage = message.replace("%id", "" + id);
			return newMessage;
		} else {
			return message;
		}
	}

}
