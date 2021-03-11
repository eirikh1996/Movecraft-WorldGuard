package net.countercraft.movecraft.worldguard.utils;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
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


    /**
     * Internal Features
     */

    public boolean canTranslate(Player p, World w, HitBox hitbox) {
        // TODO: Translate flags
        return canBuild(p, w, hitbox);
    }

    public boolean canTranslate(Player p, Location loc) {
        // TODO: Translate flags
        return canBuild(p, loc);
    }

    public boolean canRotate(Player p, World w, HitBox hitbox) {
        // TODO: Rotate flags
        return canBuild(p, w, hitbox);
    }

    public boolean canRotate(Player p, Location loc) {
        // TODO: Translate flags
        return canBuild(p, loc);
    }

    private boolean canBuild(Player p, World w, HitBox hitbox) {
        for(MovecraftLocation ml : getHitboxCorners(hitbox)) {
            if(!wgPlugin.canBuild(p, ml.toBukkit(w)))
                return false;
        }
        return true;
    }

    private boolean canBuild(Player p, Location loc) {
        return wgPlugin.canBuild(p, loc);
    }

    public boolean isPVPAllowed(World w, HitBox hitBox) {
        for(MovecraftLocation ml : getHitboxCorners(hitBox)) {
            if(!isPVPAllowed(ml.toBukkit(w)))
                return false;
        }
        return true;
    }

    public boolean isPVPAllowed(Location loc) {
        ApplicableRegionSet set = getApplicableRegions(loc);
        for(ProtectedRegion r : set) {
            if(r.getFlag(DefaultFlag.PVP) == StateFlag.State.DENY)
                return false;
        }
        return true;
    }

    public boolean isOtherExplosionAllowed(Location loc) {
        ApplicableRegionSet set = getApplicableRegions(loc);
        for(ProtectedRegion r : set) {
            if(r.getFlag(DefaultFlag.OTHER_EXPLOSION) == StateFlag.State.DENY)
                return false;
        }
        return true;
    }


    /**
     * Movecraft-Combat Features
     */

    public boolean isTNTAllowed(World w, HitBox hitBox) {
        for(MovecraftLocation ml : getHitboxCorners(hitBox)) {
            ApplicableRegionSet set = getApplicableRegions(ml.toBukkit(w));
            for(ProtectedRegion r : set) {
                if(r.getFlag(DefaultFlag.TNT) == StateFlag.State.DENY)
                    return false;
            }
        }
        return true;
    }


    /**
     * Movecraft-Warfare Features
     */

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


    /**
     * Generic features
     */

    @Nullable
    private ProtectedRegion getRegion(String regionName, World world) {
        return wgPlugin.getRegionManager(world).getRegion(regionName);
    }

    @NotNull
    private ApplicableRegionSet getApplicableRegions(Location loc) {
        Vector vector = BukkitUtil.toVector(loc);
        return wgPlugin.getRegionManager(loc.getWorld()).getApplicableRegions(vector);
    }

    /**
     *
     * @param hitbox HitBox to check
     * @return ~27 "corners" of the hitbox.  This drastically reduces the workload for checking a large craft's hitbox.
     * For small crafts, this may be smaller than 27.
     */
    @NotNull
    private HashSet<MovecraftLocation> getHitboxCorners(@NotNull HitBox hitbox) {
        HashSet<MovecraftLocation> corners = new HashSet<>();
        for(int x : new int[]{hitbox.getMinX(), hitbox.getMidPoint().getX(), hitbox.getMaxX()}) {
            for(int y : new int[]{hitbox.getMinY(), hitbox.getMidPoint().getY(), hitbox.getMaxY()}) {
                for(int z : new int[]{hitbox.getMinZ(), hitbox.getMidPoint().getZ(), hitbox.getMaxZ()}) {
                    corners.add(new MovecraftLocation(x, y, z));
                }
            }
        }
        return corners;
    }
}
