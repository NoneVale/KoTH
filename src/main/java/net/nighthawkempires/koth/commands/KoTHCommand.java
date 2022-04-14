package net.nighthawkempires.koth.commands;

import net.nighthawkempires.core.CorePlugin;
import net.nighthawkempires.core.lang.Messages;
import net.nighthawkempires.koth.KoTHPlugin;
import net.nighthawkempires.koth.koth.KoTHModel;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.*;

public class KoTHCommand implements CommandExecutor {

    private String[] help = {
            CorePlugin.getMessages().getMessage(Messages.CHAT_HEADER),
            translateAlternateColorCodes('&', "&8Command&7: KoTH   &8-   [Optional], <Required>"),
            CorePlugin.getMessages().getMessage(Messages.CHAT_FOOTER),
            CorePlugin.getMessages().getCommand("koth", "help", "Show this help menu."),
            CorePlugin.getMessages().getCommand("koth", "start", "Start a KoTH."),
            CorePlugin.getMessages().getCommand("koth", "time", "Check how long until next KoTH."),
            CorePlugin.getMessages().getCommand("koth", "toggle", "Toggle KoTH on/off."),
            CorePlugin.getMessages().getCommand("koth", "duration <minutes>", "Set the KoTH duration."),
            CorePlugin.getMessages().getCommand("koth", "interval <hours>", "Set the KoTH interval."),
            CorePlugin.getMessages().getCommand("koth", "location add", "Add spawn location for KoTH."),
            CorePlugin.getMessages().getCommand("koth", "location list", "List KoTH spawn locations."),
            CorePlugin.getMessages().getCommand("koth", "location remove <id>", "Remove KoTH spawn location."),
            CorePlugin.getMessages().getMessage(Messages.CHAT_FOOTER)
    };

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        KoTHModel koTHModel = KoTHPlugin.getKoTHRegistry().getConfig();
        if (sender instanceof Player player) {
            if (!player.hasPermission("ne.koth")) {
                if (koTHModel.getNextKoth() == 0L) {
                    player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH is currently active! Don't be afraid to join in on the action!"));
                    return true;
                } else {
                    player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "There is " + GOLD + koTHModel.timeLeft() + GRAY + " until the next KoTH begins."));
                    return true;
                }
            }

            KoTHModel koTH = KoTHPlugin.getKoTHRegistry().getConfig();

            switch (args.length) {
                case 0 -> {
                    player.sendMessage(help);
                    return true;
                }
                case 1 -> {
                    switch (args[0].toLowerCase()) {
                        case "help" -> {
                            player.sendMessage(help);
                            return true;
                        }
                        case "start" -> {
                            koTH.start();
                            return true;
                        }
                        case "time" -> {
                            if (koTHModel.getNextKoth() == 0L) {
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH is currently active! Don't be afraid to join in on the action!"));
                                return true;
                            } else {
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "There is " + GOLD + koTHModel.timeLeft() + GRAY + " until the next KoTH begins."));
                                return true;
                            }
                        }
                        case "toggle" -> {
                            if (koTH.isEnabled()) {
                                koTH.setEnabled(false);
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH has been disabled."));
                            } else {
                                koTH.setEnabled(true);
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH has been enabled."));
                            }
                            return true;
                        }
                        default -> {
                            player.sendMessage(CorePlugin.getMessages().getChatTag(Messages.INVALID_SYNTAX));
                            return true;
                        }
                    }
                }
                case 2 -> {
                    switch (args[0].toLowerCase()) {
                        case "duration" -> {
                            String numString = args[1];
                            if (!NumberUtils.isNumber(numString)) {
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(RED + "You must provide a valid number."));
                                return true;
                            }

                            int num = Integer.parseInt(numString);

                            koTH.setDurationMinutes(num);
                            player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "The KoTH duration has been set to " + GOLD + num + " minutes" + GRAY + "."));
                            return true;
                        }
                        case "interval" -> {
                            String numString = args[1];
                            if (!NumberUtils.isNumber(numString)) {
                                player.sendMessage(CorePlugin.getMessages().getChatMessage(RED + "You must provide a valid number."));
                                return true;
                            }

                            int num = Integer.parseInt(numString);

                            koTH.setIntervalHours(num);
                            player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "The KoTH interval has been set to " + GOLD + num + " hours" + GRAY + "."));
                            return true;
                        }
                        case "location" -> {
                            switch (args[1].toLowerCase()) {
                                case "add" -> {
                                    Location location = player.getLocation();
                                    koTH.addLocation(location);
                                    player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "You have added location " + locationName(location) + GRAY + " to the KoTH spawn list."));
                                    return true;
                                }
                                case "list" -> {
                                    String[] list = {
                                            CorePlugin.getMessages().getMessage(Messages.CHAT_HEADER),
                                            translateAlternateColorCodes('&', "&8List&7: KoTH Spawn Locations"),
                                            CorePlugin.getMessages().getMessage(Messages.CHAT_FOOTER),
                                    };

                                    player.sendMessage(list);
                                    for (int i = 0; i < koTH.getLocations().size(); i++) {
                                        player.sendMessage(DARK_GRAY + " - " + GOLD + i + locationName(koTH.getLocations().get(i).toLocation()));
                                    }
                                    player.sendMessage(CorePlugin.getMessages().getMessage(Messages.CHAT_FOOTER));
                                    return true;
                                }
                                default -> {
                                    player.sendMessage(CorePlugin.getMessages().getChatTag(Messages.INVALID_SYNTAX));
                                    return true;
                                }
                            }
                        }
                        default -> {
                            player.sendMessage(CorePlugin.getMessages().getChatTag(Messages.INVALID_SYNTAX));
                            return true;
                        }
                    }
                }
                case 3 -> {
                    if (args[0].toLowerCase().equals("location") && args[1].toLowerCase().equals("remove")) {
                        String numString = args[2];
                        if (!NumberUtils.isDigits(numString)) {
                            player.sendMessage(CorePlugin.getMessages().getChatMessage(RED + "You must provide a valid number."));
                            return true;
                        }

                        int num = Integer.parseInt(numString);

                        if (num < 0 || num >= koTH.getLocations().size()) {
                            player.sendMessage(CorePlugin.getMessages().getChatMessage(RED + "The ID must be between 0 and " + (koTH.getLocations().size() - 1) + GRAY + "."));
                            return true;
                        }

                        Location location = koTH.getLocations().get(num).toLocation();
                        player.sendMessage(CorePlugin.getMessages().getChatMessage(GRAY + "KoTH spawn location " + locationName(location) + GRAY + " with ID "
                                + GOLD + num + GRAY + " has been removed."));
                        koTH.removeLocation(num);
                        return true;
                    } else {
                        player.sendMessage(CorePlugin.getMessages().getChatTag(Messages.INVALID_SYNTAX));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String locationName(Location location) {
        return DARK_GRAY + "[" + GREEN + location.getWorld().getName() + DARK_GRAY + ", " + GOLD + location.getBlockX() + DARK_GRAY
                + ", " + location.getBlockY() + DARK_GRAY + ", " + GOLD + location.getBlockZ() + DARK_GRAY + "]";
    }
}