package uk.co.joshuawoolley.simpleticketmanager.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import uk.co.joshuawoolley.simpleticketmanager.ticketsystem.Ticket;

public class SimpleTicketEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    
    private final Ticket ticket;
    private final String action;

    /**
     *
     * @param action
     * @param ticket
     */
    public SimpleTicketEvent(final String action, final Ticket ticket) {
        this.ticket = ticket;
        this.action = action;
    }

    public Ticket getTicket() {
        return this.ticket;
    }
    
    public String getAction() {
        return action;
    }

    /**
     *
     * @return
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     *
     * @return
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
