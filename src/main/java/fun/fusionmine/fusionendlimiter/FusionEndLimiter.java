package fun.fusionmine.fusionendlimiter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FusionEndLimiter extends JavaPlugin implements Listener {

    private LocalTime startTime;

    private LocalTime endTime;

    private String denyMessage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfiguration();
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    private void loadConfiguration() {
        ConfigurationSection section = getConfig().getConfigurationSection("end-world-access");
        String startTimeStr = section.getString("start-time");
        String endTimeStr = section.getString("end-time");
        denyMessage = ChatColor.translateAlternateColorCodes('&',
                section.getString("deny-message")
                        .replace("%startTime%", startTimeStr)
                        .replace("%endTime%", endTimeStr)
        );
        try {
            startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
            endTime = LocalTime.parse(endTimeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            getLogger().severe("Invalid time format in config: " + e.getMessage());
        }
    }

    private boolean isOutsideAccessTime(LocalTime currentTime) {
        return currentTime.isBefore(startTime) || currentTime.isAfter(endTime);
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (event.getTo().getWorld().getName().equals("world_the_end") && !player.hasPermission("endlimiter.exempt")) {
            LocalTime currentTime = LocalTime.now(ZoneId.of("Europe/Moscow"));
            if (isOutsideAccessTime(currentTime)) {
                event.setCancelled(true);
                player.sendMessage(denyMessage);
            }
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if(event.getTo().getWorld().getName().equals("world_the_end") && !event.getPlayer().hasPermission("endlimiter.exempt")) {
            event.setCancelled(true);
        }
    }

}
