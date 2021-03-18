package de.crafttogether.craftbahn.util;

import de.crafttogether.craftbahn.CraftBahn;
import de.crafttogether.craftbahn.destinations.Destination;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.Marker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerIcon;
import org.dynmap.markers.MarkerSet;

import java.util.UUID;

public class MarkerManager {
    public static void deleteMarker(Destination dest) {
        if (!dest.getServer().equalsIgnoreCase(CraftBahn.getInstance().getServerName()))
            return;

        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + dest.getType().name());

        if (set == null)
            return;

        Marker marker = set.findMarker(dest.getName());
        if (marker != null)
            marker.deleteMarker();
    }

    public static void createMarkerSets() {
        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();
        MarkerAPI markerApi = dynmap.getMarkerAPI();

        if (dynmap == null)
            return;

        MarkerIcon iconRail = markerApi.getMarkerIcon("ct-rail");
        MarkerIcon iconMinecart = markerApi.getMarkerIcon("ct-minecart");

        if (iconRail == null) {
            iconRail = dynmap.getMarkerAPI().createMarkerIcon("ct-rail", "ct-rail", plugin.getResource("rail.png"));
            dynmap.getMarkerAPI().getMarkerIcons().add(iconRail);
        }

        if (iconMinecart == null) {
            iconMinecart = dynmap.getMarkerAPI().createMarkerIcon("ct-minecart", "ct-minecart", plugin.getResource("minecart.png"));
            dynmap.getMarkerAPI().getMarkerIcons().add(iconMinecart);
        }

        for (Destination.DestinationType type : Destination.DestinationType.values()) {
            MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + type.name());
            String label = "Bahnhof";
            if (type.name().equals("MAIN_STATION")) {
                label = "Hauptbahnhof";
            } else if (type.name().equals("PUBLIC_STATION")) {
                label = "Bahnhof (Öffentlich)";
            } else if (type.name().equals("PLAYER_STATION")) {
                label = "Bahnhof (Spieler)";
            }
            if (set == null)
                set = dynmap.getMarkerAPI().createMarkerSet("CT_" + type.name(), label, null, true);
        }
    }

    public static boolean addMarker(Destination dest) {
        return addMarker(dest, false);
    }

    public static boolean addMarker(Destination dest, boolean updateOnly) {
        if (!dest.getServer().equalsIgnoreCase(CraftBahn.getInstance().getServerName()))
            return false;

        CraftBahn plugin = CraftBahn.getInstance();
        DynmapAPI dynmap = plugin.getDynmap();

        if (dynmap == null)
            return false;

        MarkerAPI markerApi = dynmap.getMarkerAPI();

        if (!updateOnly)
            createMarkerSets();

        MarkerSet set = dynmap.getMarkerAPI().getMarkerSet("CT_" + dest.getType().name());

        if (Bukkit.getServer().getWorld(dest.getLocation().getWorld()) == null) {
            plugin.getLogger().warning("Error: Unable to create marker for '" + dest.getName() + "'. World '" + dest.getWorld() + "' is not loaded");
            return false;
        }

        if (dest.getLocation() == null) {
            plugin.getLogger().warning("Error: Destination '" + dest.getName() + "' has no location set!");
            return false;
        }

        MarkerIcon icon = null;
        String label = null;
        String color = null;
        Boolean showOwner = Boolean.valueOf(true);
        String strParticipants = Bukkit.getOfflinePlayer(dest.getOwner()).getName() + ", ";

        for (UUID uuid : dest.getParticipants()) {
            OfflinePlayer participant = Bukkit.getOfflinePlayer(uuid);
            if (!participant.hasPlayedBefore()) continue;
            strParticipants += participant.getName() + ", ";
        }

        if (strParticipants.length() > 1)
            strParticipants = strParticipants.substring(0, strParticipants.length()-2);

        switch (dest.getType().name()) {
            case "STATION": case "MAIN_STATION": case "PUBLIC_STATION":
                color = "#ffaa00";
                icon = markerApi.getMarkerIcon("ct-rail");
                showOwner = Boolean.valueOf(false);
                break;
            case "PLAYER_STATION":
                color = "#ffff55";
                icon = markerApi.getMarkerIcon("ct-minecart");
                showOwner = Boolean.valueOf(true);
                break;
        }

        label = "<div style=\"z-index:99999\">" +
                    "<div style=\"padding:6px\">" +
                        "<h3 style=\"padding:0px;margin:0px;color:" + color + "\">" + dest.getName() + "</h3>" +
                        "<span style=\"font-weight:bold;color:#aaaaaa;\">Stations-Typ:</span> " + dest.getType() + "<br>" +
                        (showOwner.booleanValue() ? ("<span style=\"font-weight:bold;color:#aaaaaa;\">Besitzer:</span> " + strParticipants + "<br>") : "") +
                        "<span style=\"font-style:italic;font-weight:bold;color:#ffaa00\">/fahrziel <span style=\"color:#ffff55\">" + dest.getName() + "</span></span>" +
                    "</div>" +
                "</div>";

        deleteMarker(dest);

        Location loc = dest.getLocation().getBukkitLocation();
        set.createMarker(dest.getName(), label, true, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);

        return true;
    }
}
