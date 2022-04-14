package net.nighthawkempires.koth.koth;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.nighthawkempires.core.CorePlugin;
import net.nighthawkempires.core.datasection.DataSection;
import net.nighthawkempires.core.datasection.Model;
import net.nighthawkempires.core.location.SavedLocation;
import net.nighthawkempires.core.server.ServerType;
import net.nighthawkempires.core.user.UserModel;
import net.nighthawkempires.koth.KoTHPlugin;
import net.nighthawkempires.races.RacesPlugin;
import net.nighthawkempires.regions.RegionsPlugin;
import net.nighthawkempires.regions.schematic.SchematicModel;
import org.apache.logging.log4j.core.Core;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.bukkit.ChatColor.*;
import static org.bukkit.ChatColor.DARK_GRAY;

public class KoTHModel implements Model, Listener {

    private boolean enabled;

    private int durationMinutes;
    private int intervalHours;

    private List<SavedLocation> locations;

    private int intervalTaskId = 0;
    private int kothTaskId = 0;

    private Location corner1 = null;
    private Location corner2 = null;

    private long nextKoth = 0L;

    private Player king = null;

    public KoTHModel() {
        this.enabled = false;

        this.durationMinutes = 5;
        this.intervalHours = 8;

        this.locations = Lists.newArrayList();
    }

    public KoTHModel(DataSection data) {
        this.enabled = data.getBoolean("enabled");

        this.durationMinutes = data.getInt("duration-minutes");
        this.intervalHours = data.getInt("interval-hours");

        this.locations = Lists.newArrayList();
        for (Map<String, Object> map : data.getMapList("locations")) {
            SavedLocation location = new SavedLocation(map);
            if (location.getWorld() != null) {
                this.locations.add(location);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        KoTHPlugin.getKoTHRegistry().register(this);
    }

    public List<SavedLocation> getLocations() {
        return locations;
    }

    public void addLocation(Location location) {
        this.locations.add(SavedLocation.fromLocation(location, false));
        KoTHPlugin.getKoTHRegistry().register(this);
    }

    public void removeLocation(int index) {
        this.locations.remove(index);
        KoTHPlugin.getKoTHRegistry().register(this);
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
        KoTHPlugin.getKoTHRegistry().register(this);
    }

    public int getIntervalHours() {
        return intervalHours;
    }

    public void setIntervalHours(int intervalHours) {
        this.intervalHours = intervalHours;
        KoTHPlugin.getKoTHRegistry().register(this);
    }

    public long getNextKoth() {
        return nextKoth;
    }

    public void start() {
        nextKoth = 0L;
        Random random = new Random();
        Location spawn = locations.get(random.nextInt(locations.size())).toLocation();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(CorePlugin.getMessages().getChatMessage(ChatColor.GRAY + "KoTH has started at " + locationName(spawn) + GRAY + "."));
        }

        Location corner1 = spawn.clone().subtract(10, 10, 10);
        Location corner2 = spawn.clone().add(10, 10, 10);
        SchematicModel existingLandscape = new SchematicModel("temp_koth_landscape", corner1, corner2);

        SchematicModel koth = RegionsPlugin.getSchematicRegistry().getSchematic("aa");
        this.corner1 = spawn.clone().subtract(koth.getCenterLength(), 1, koth.getCenterWidth());
        this.corner2 = spawn.clone().add(koth.getCenterLength(), koth.getHeight() + 1, koth.getCenterWidth());
        koth.paste(spawn.clone().add(0, 1, 0));

        Bukkit.getScheduler().cancelTask(intervalTaskId);
        Bukkit.getScheduler().scheduleSyncDelayedTask(KoTHPlugin.getPlugin(), () -> {
            if (king != null) {
                UserModel kingModel = CorePlugin.getUserRegistry().getUser(king.getUniqueId());
                net.nighthawkempires.races.user.UserModel kingModelRaces = RacesPlugin.getUserRegistry().getUser(king.getUniqueId());

                kingModel.addTokens(10);
                kingModel.addServerBalance(ServerType.SURVIVAL, 250.0);
                kingModelRaces.addPerkPoints(2);
                king.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "You have been rewarded " + GOLD + " 10 Tokens" + GRAY
                        + ", " + GOLD + "2 Perk Points" + GRAY + ", and " + GREEN + "$" + YELLOW + "250.0" + GRAY + " for being the king of the hill."));

                Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH has ended! " + GREEN + king.getName() + GRAY + " is the king of the hill!"));
            }
            existingLandscape.paste(spawn.clone().subtract(0, 9, 0));

            this.corner1 = null;
            this.corner2 = null;
            this.king = null;

            startInterval();
        }, getDurationMinutes() * 1200L);
        for (int i = getDurationMinutes() - 1; i > 0; i--) {
            int finalI = i;
            if (i == getDurationMinutes() - 1) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(KoTHPlugin.getPlugin(), () ->
                                Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GRAY + "There are " + GOLD
                                        + 30 + " seconds" + GRAY + " before KoTH ends, don't be afraid to join in on the action!")),
                        (i * 1200L) + 600);
            }
            Bukkit.getScheduler().scheduleSyncDelayedTask(KoTHPlugin.getPlugin(), () ->
                    Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GRAY + "There are " + GOLD
                            + (getDurationMinutes() - finalI) + " minutes" + GRAY + " before KoTH ends, don't be afraid to join in on the action!")),
                    i * 1200L);
        }
    }

    public void startInterval() {
        this.nextKoth = System.currentTimeMillis() + (3600000L * intervalHours);
        intervalTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(KoTHPlugin.getPlugin(),
                this::start, (long) getIntervalHours() * 20 * 60 * 60);
    }

    public boolean inKoth(Location location) {
        if (corner1 != null && corner2 != null) {
            if (location.getWorld() != corner1.getWorld()) return false;

            int x1 = corner1.getBlockX(), x2 = corner2.getBlockX(), x3 = location.getBlockX();
            int y1 = corner1.getBlockY(), y2 = corner2.getBlockY(), y3 = location.getBlockY();
            int z1 = corner1.getBlockZ(), z2 = corner2.getBlockZ(), z3 = location.getBlockZ();

            if (x1 > x2)  {
                x2 = x1;
                x1 = corner2.getBlockX();
            }

            if (y1 > y2) {
                y2 = y1;
                y1 = corner2.getBlockY();
            }

            if (z1 > z2) {
                z2 = z1;
                z1 = corner2.getBlockZ();
            }

            return (x1 <= x3 && x3 <= x2) && (y1 <= y3 && y3 <= y2) && (z1 <= z3 && z3 <= z2);
        }
        return false;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (inKoth(event.getTo())) {
            if (king == null) {
                king = event.getPlayer();
                Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GREEN + king.getName() + GRAY + " is now the king of the hill."));
            }
        } else {
            if (king != null) {
                if (king == event.getPlayer()) {
                    king = null;
                    Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GREEN + event.getPlayer().getName() + GRAY + " is no longer the king of the hill."));
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (event.getEntity() == king) {
            king = null;
            Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GREEN + event.getEntity().getName() + GRAY + " is no longer the king of the hill."));
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getPlayer() == king) {
            king = null;
            Bukkit.broadcastMessage(CorePlugin.getMessages().getChatMessage(GREEN + event.getPlayer().getName() + GRAY + " is no longer the king of the hill."));
        }
    }

    @Override
    public String getKey() {
        return "koth";
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = Maps.newHashMap();

        map.put("enabled", enabled);

        map.put("duration-minutes", durationMinutes);
        map.put("interval-hours", intervalHours);

        List<Map<String, Object>> locations = Lists.newArrayList();
        for (SavedLocation location : this.locations) {
            locations.add(location.serialize());
        }

        map.put("locations", locations);

        return map;
    }

    private String locationName(Location location) {
        return DARK_GRAY + "[" + GREEN + location.getWorld().getName() + DARK_GRAY + ", " + GOLD + location.getBlockX() + DARK_GRAY
                + ", " + GOLD + location.getBlockY() + DARK_GRAY + ", " + GOLD + location.getBlockZ() + DARK_GRAY + "]";
    }

    public String timeLeft() {
        long difference = nextKoth - System.currentTimeMillis();

        int seconds = (int) (difference / 1000) % 60 ;
        int minutes = (int) ((difference / (1000*60)) % 60);
        int hours   = (int) ((difference / (1000*60*60)) % 24);
        int days = (int) (difference / (1000*60*60*24));

        StringBuilder timeLeft = new StringBuilder();

        if (days > 0) {
            timeLeft.append(days).append(" days, ");
        }

        if (hours > 0) {
            timeLeft.append(hours).append(" hours, ");
        }

        if (minutes > 0) {
            timeLeft.append(minutes).append(" minutes, ");
        }

        if (seconds > 0) {
            timeLeft.append(seconds).append(" seconds");
        }

        String time = timeLeft.toString().trim();
        if (time.endsWith(","))
            time = time.substring(0, time.length() - 1);

        return time;
    }
}
