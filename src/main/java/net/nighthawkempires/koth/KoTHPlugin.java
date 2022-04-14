package net.nighthawkempires.koth;

import net.nighthawkempires.koth.commands.KoTHCommand;
import net.nighthawkempires.koth.koth.registry.FKoTHRegistry;
import net.nighthawkempires.koth.koth.registry.KoTHRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class KoTHPlugin extends JavaPlugin {

    public static Plugin plugin;

    public static KoTHRegistry koTHRegistry;

    public void onEnable() {
        plugin = this;
        koTHRegistry = new FKoTHRegistry();
        getKoTHRegistry().getConfig().startInterval();

        registerCommands();
        registerListeners();
    }

    public void onDisable() {

    }

    public void registerCommands() {
        this.getCommand("koth").setExecutor(new KoTHCommand());
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(getKoTHRegistry().getConfig(), this);
    }

    public static KoTHRegistry getKoTHRegistry() {
        return koTHRegistry;
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
