package de.crafttogether.craftbahn.listener;

import com.bergerkiller.bukkit.tc.controller.MinecartMember;
import com.bergerkiller.bukkit.tc.controller.MinecartMemberStore;
import de.crafttogether.craftbahn.util.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class TrainEnterListener implements Listener {
    @EventHandler
    void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player))
            return;

        if (!(e.getVehicle() instanceof Minecart))
            return;

        Player p = (Player) e.getEntered();
        Minecart minecart = (Minecart) e.getVehicle();
        MinecartMember<?> cart = MinecartMemberStore.getFromEntity(minecart);

        if (cart != null) {
            String enterMessage = cart.getProperties().getEnterMessage();

            if (enterMessage == null || enterMessage.equalsIgnoreCase("cbDefault")) {
                // Clear enterMessage-property
                cart.getProperties().setEnterMessage(null);

                // Send custom enterMessage
                sendEnterMessage(p, cart);
            }
        }
    }

    private void sendEnterMessage(Player p, MinecartMember cart) {
        TextComponent message = Message.format("&e-------------- &c&lCraftBahn &e--------------");
        message.addExtra(Message.newLine());
        message.addExtra(Message.format("&6CraftBahn &8» &eGuten Tag, Reisender!"));
        message.addExtra(Message.newLine());
        message.addExtra(Message.format("&6CraftBahn &8» &eVerstehst du nur "));
        message.addExtra(Message.format("&c/bahnhof&e?"));

        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/bahnhof"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(Message.format("&6Informationen zum Schienennetz"))).create()));
        p.spigot().sendMessage(message);

        if (cart.getProperties().getDestination().isBlank()) {
            message = new TextComponent(Message.format("&6CraftBahn &8»"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8»&c&lHinweis:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8&cDieser Zug hat noch kein Fahrziel."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Verfügbare Fahrziele anzeigen"))).create()));
            p.spigot().sendMessage(message);
        } else {
            message = new TextComponent(Message.format("&6CraftBahn &8"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8» &eDieser Zug versucht, das Ziel:"));
            message.addExtra(Message.newLine());
            message.addExtra(Message.format("&6CraftBahn &8&f'&6&l" + cart.getProperties().getDestination() + "&f' &ezu erreichen."));

            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fahrziele"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(ChatColor.translateAlternateColorCodes('&', "&6Anderes Fahrziel auswählen")).create())));
            p.spigot().sendMessage(message);
        }

        message = new TextComponent(Message.newLine());
        message.addExtra(Message.format("&e----------------------------------------"));
        p.spigot().sendMessage(message);
    }
}
