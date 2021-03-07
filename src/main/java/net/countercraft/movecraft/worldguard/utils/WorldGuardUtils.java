package net.countercraft.movecraft.worldguard.utils;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public class WorldGuardUtils {
    private WorldGuardPlugin wgPlugin;

    public boolean init(Plugin plugin) {
        if(plugin == null || !(plugin instanceof  WorldGuardPlugin)) {
            return false;
        }
        wgPlugin = (WorldGuardPlugin) plugin;
        return true;
    }

    public boolean canTranslate(Player p, Location loc) {
        return canBuild(p, loc);
    }

    public boolean canRotate(Player p, Location loc) {
        return canBuild(p, loc);
    }

    public boolean canBuild(Player p, Location loc) {
        return wgPlugin.canBuild(p, loc);
    }

    public boolean allowsOtherExplosion(Location loc) {
        ApplicableRegionSet set = getApplicableRegions(loc);
        for(ProtectedRegion r : set) {
            if(r.getFlag(DefaultFlag.OTHER_EXPLOSION) == StateFlag.State.DENY)
                return false;
        }
        return true;
    }

    public boolean regionExists(String regionName, World world) {
        return getRegion(regionName, world) != null;
    }

    @Nullable
    public Queue<Chunk> getChunksInRegion(String regionName, World world) {
        ProtectedRegion region = getRegion(regionName, world);
        if(region == null)
            return null;

        Queue<Chunk> chunks = new LinkedList<>();
        for(int x = (int) Math.floor(region.getMinimumPoint().getBlockX() / 16.0); x < Math.floor(region.getMaximumPoint().getBlockX() / 16.0) + 1; x++) {
            for(int z = (int) Math.floor(region.getMinimumPoint().getBlockZ() / 16.0); z < Math.floor(region.getMaximumPoint().getBlockZ() / 16.0) + 1; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    @Nullable
    private ProtectedRegion getRegion(String regionName, World world) {
        return wgPlugin.getRegionManager(world).getRegion(regionName);
    }

    @NotNull
    private ApplicableRegionSet getApplicableRegions(Location loc) {
        Vector vector = BukkitUtil.toVector(loc);
        return wgPlugin.getRegionManager(loc.getWorld()).getApplicableRegions(vector);
    }
}
