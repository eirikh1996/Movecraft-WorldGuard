package net.countercraft.movecraft.worldguard;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import net.countercraft.movecraft.worldguard.config.Config;
import net.countercraft.movecraft.worldguard.listener.CraftRotateListener;
import net.countercraft.movecraft.worldguard.listener.CraftTranslateListener;
import net.countercraft.movecraft.worldguard.localisation.I18nSupport;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public final class MovecraftWorldGuard extends JavaPlugin {
    private static MovecraftWorldGuard instance;

    public static MovecraftWorldGuard getInstance() {
        return instance;
    }

    private WorldGuardPlugin worldGuardPlugin;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // TODO other languages
        String[] languages = {"en"};
        for (String s : languages) {
            if (!new File(getDataFolder()  + "/localisation/movecraftworldguardlang__"+ s +".properties").exists()) {
                saveResource("localisation/movecraftworldguardlang_"+ s +".properties", false);
            }
        }
        Config.Locale = getConfig().getString("Locale", "en");
        I18nSupport.init();

        Plugin wgPlugin = getServer().getPluginManager().getPlugin("WorldGuard");
        if(wgPlugin == null || !(wgPlugin instanceof  WorldGuardPlugin)) {
            getLogger().log(Level.SEVERE, I18nSupport.getInternationalisedString("Startup - WG Not Found"));
            getServer().shutdown();
        }
        getLogger().log(Level.INFO, I18nSupport.getInternationalisedString("Startup - WG Found"));
        Config.WorldGuardBlockMoveOnBuildPerm = getConfig().getBoolean("WorldGuardBlockMoveOnBuildPerm", true);
        Config.WorldGuardBlockSinkOnPVPPerm = getConfig().getBoolean("WorldGuardBlockSinkOnPVPPerm", true);
        getLogger().log(Level.INFO, "Settings: WorldGuardBlockMoveOnBuildPerm - {0}, WorldGuardBlockSinkOnPVPPerm - {1}", new Object[]{Config.WorldGuardBlockMoveOnBuildPerm, Config.WorldGuardBlockSinkOnPVPPerm});
        worldGuardPlugin = (WorldGuardPlugin) wgPlugin;

        getServer().getPluginManager().registerEvents(new CraftRotateListener(), this);
        getServer().getPluginManager().registerEvents(new CraftTranslateListener(), this);

        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public WorldGuardPlugin getWorldGuardPlugin() {
        return worldGuardPlugin;
    }
}
