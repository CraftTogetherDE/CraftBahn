package de.crafttogether.craftbahn.speedometer;

import com.bergerkiller.bukkit.tc.controller.MinecartGroup;
import de.crafttogether.CraftBahnPlugin;
import de.crafttogether.craftbahn.util.TCHelper;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Speedometer implements Runnable {
    private List<SpeedData> trains;
    private List<DebugParticle> debugParticles;
    private BukkitTask task;

    public static class DebugParticle {
        public String trainName;
        public Location location;
        public Particle particle;
        public Object data;

        public DebugParticle(String trainName, Location location, Particle particle, Object data) {
            this.trainName = trainName;
            this.location = location;
            this.particle = particle;
            this.data = data;
        }
    }

    public Speedometer() {
        this.trains = new ArrayList<>();
        this.debugParticles = new ArrayList<>();
        this.task = Bukkit.getScheduler().runTaskTimer(CraftBahnPlugin.plugin, this, 20L, 5L);
    }

    @Override
    public void run() {
        if (debugParticles.size() < 1)
            return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission("craftbahn.debug")) continue;

            for (Iterator<DebugParticle> it = debugParticles.iterator(); it.hasNext(); ) {
                DebugParticle particle = it.next();

                if (TCHelper.getTrain(particle.trainName) == null)
                    debugParticles.remove(particle);

                else if (particle.location.getChunk().isLoaded())
                    p.spawnParticle(particle.particle, particle.location, 1, particle.data);
            }
        }

        if (trains.size() < 1)
            return;

        updateData();
        sendActionBars();
    }

    public void stop() {
        if (this.task != null)
            this.task.cancel();
    }

    public void add(String trainName) {
        if (get(trainName) != null) return;
        trains.add(new SpeedData(trainName));
    }

    public SpeedData get(String trainName) {
        List<SpeedData> data = trains.stream()
                .filter(speedData -> speedData.getTrainName().equals(trainName))
                .distinct()
                .toList();

        if (data.size() < 1)
            return null;

        return data.get(0);
    }

    public void remove(String trainName) {
        SpeedData data = get(trainName);
        if (data == null) return;

        // Clear actionbar for all players
        MinecartGroup train = TCHelper.getTrain(trainName);
        TCHelper.sendActionbar(train, Component.text(""));

        trains.remove(data);
    }

    public void sendActionBars() {
        for (SpeedData data : trains) {
            MinecartGroup group = TCHelper.getTrain(data.getTrainName());

            Component message;
            String destinationName = data.getDestinationName();

            double realVelocity = data.getVelocity();
            double smoothedVelocity = data.getSmoothVelocity();
            double distance = data.getDistance();

            if (realVelocity > 0) {
                int minuten, sekunden;
                int time = (int) (distance / smoothedVelocity);

                sekunden = time % 60;
                minuten = (time-sekunden) / 60;

                if (distance > 5) {
                    if (time > 3)
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\" §8| §6ETA: §e%d:%02d", realVelocity, distance, destinationName, minuten, sekunden));
                    else
                        message = Component.text(String.format("§e%.1f §6Blöcke/s §8| §e%.0f §6Blöcke bis \"§e%s\"", realVelocity, distance, destinationName));
                }

                else
                    message = Component.text(String.format("§e%.1f §6Blöcke/s", realVelocity));
            }

            else if (distance > 5)
                message = Component.text(String.format("§e%.0f §6Blöcke bis \"%s\"", distance, destinationName));
            else
                message = Component.text("");

            TCHelper.sendActionbar(group, "craftbahn.speedometer", message);
        }
    }

    private void updateData() {
        for (SpeedData data : trains)
            data.update();
    }

    public List<DebugParticle> getDebugParticles() {
        return debugParticles;
    }
}