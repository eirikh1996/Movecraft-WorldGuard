package net.countercraft.movecraft.worldguard.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            if(!canBuild(p, ml.toBukkit(w)))
                return false;
        }
        return true;
    }

    private boolean canBuild(Player p, Location loc) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.queryState(BukkitAdapter.adapt(loc), wgPlugin.wrapPlayer(p), Flags.BUILD) == StateFlag.State.ALLOW;
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
            if(r.getFlag(Flags.PVP) == StateFlag.State.DENY)
                return false;
        }
        return true;
    }

    public boolean isOtherExplosionAllowed(Location loc) {
        ApplicableRegionSet set = getApplicableRegions(loc);
        for(ProtectedRegion r : set) {
            if(r.getFlag(Flags.OTHER_EXPLOSION) == StateFlag.State.DENY)
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
                if(r.getFlag(Flags.TNT) == StateFlag.State.DENY)
                    return false;
            }
        }
        return true;
    }


    /**
     * Movecraft-Warfare Features
     */

    // Siege Features

    public boolean craftFullyInRegion(String regionName, World w, Craft craft) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return false;

        for(MovecraftLocation ml : getHitboxCorners(craft.getHitBox())) {
            if(!r.contains(ml.getX(), ml.getY(), ml.getZ()))
                return false;
        }
        return true;
    }

    public void clearAndSetOwnership(String regionName, World w, UUID owner) {
        ProtectedRegion region = getRegion(regionName, w);
        if(region == null)
            return;

        DefaultDomain newOwners = new DefaultDomain();
        newOwners.addPlayer(owner);
        region.setOwners(newOwners);
        region.setMembers(newOwners);
    }

    public Set<String> getRegions(Location loc) {
        ApplicableRegionSet regionSet = getApplicableRegions(loc);
        HashSet<String> stringSet = new HashSet<>();
        for(ProtectedRegion r : regionSet) {
            stringSet.add(r.getId());
        }
        return stringSet;
    }

    // Assault Features

    public boolean isInRegion(Location loc) {
        return getApplicableRegions(loc).size() > 0;
    }

    public boolean regionExists(String regionName, World w) {
        return getRegion(regionName, w) != null;
    }

    public boolean ownsAssaultableRegion(Player p) {
        LocalPlayer lp = wgPlugin.wrapPlayer(p);
        RegionManager manager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(p.getWorld()));
        if(manager == null)
            return false;

        for(ProtectedRegion r : manager.getRegions().values()) {
            if(r.isOwner(lp) && r.getFlag(Flags.TNT) == StateFlag.State.DENY)
                return true;
        }
        return false;
    }

    @Nullable
    public String getAssaultableRegion(Location loc, HashSet<String> exclusions) {
        for(ProtectedRegion r : getApplicableRegions(loc)) {
            if(r.getFlag(Flags.TNT) != StateFlag.State.DENY || r.getOwners().size() == 0)
                continue;

            if(exclusions.contains(r.getId()))
                continue;

            return r.getId();
        }
        return null;
    }

    @Nullable
    public Set<UUID> getUUIDOwners(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        return r.getOwners().getUniqueIds();
    }

    @Nullable
    public Set<UUID> getUUIDMembers(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        return r.getMembers().getUniqueIds();
    }

    public void setTNTAllow(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return;

        r.setFlag(Flags.TNT, StateFlag.State.ALLOW);
    }

    public void setTNTDeny(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return;

        r.setFlag(Flags.TNT, StateFlag.State.DENY);
    }

    public void clearOwners(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return;

        r.getOwners().clear();
    }

    @Nullable
    public String getRegionOwnerList(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        StringBuilder output = new StringBuilder();
        boolean first = true;
        for(UUID uuid : r.getOwners().getUniqueIds()) {
            if(!first)
                output.append(", ");
            else
                first = false;

            OfflinePlayer ofp = Bukkit.getOfflinePlayer(uuid);
            if(ofp == null)
                output.append(uuid);
            else
                output.append(ofp.getName());
        }
        for(String player : r.getOwners().getPlayers()) {
            if(!first)
                output.append(", ");
            else
                first = false;

            output.append(player);
        }
        return output.toString();
    }

    public boolean addOwners(String regionName, World w, Set<String> owners) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return false;

        DefaultDomain regionOwners = r.getOwners();

        for(String ownerName : owners) {
            if(ownerName.length() > 16) {
                regionOwners.addPlayer(UUID.fromString(ownerName));
            }
            else {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(ownerName);
                if(offlinePlayer == null)
                    continue;

                regionOwners.addPlayer(offlinePlayer.getUniqueId());
            }
        }
        return true;
    }

    public boolean isMember(String regionName, World w, Player p) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return false;

        LocalPlayer lp = wgPlugin.wrapPlayer(p);
        return r.isMember(lp) || r.isOwner(lp);
    }

    public boolean isTNTDenied(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return false;

        return r.getFlag(Flags.TNT) == StateFlag.State.DENY;
    }

    @Nullable
    public MovecraftLocation getMinLocation(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        return vectorToMovecraftLocation(r.getMinimumPoint());
    }

    @Nullable
    public MovecraftLocation getMaxLocation(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        return vectorToMovecraftLocation(r.getMaximumPoint());
    }

    @Nullable
    public Queue<Chunk> getChunksInRegion(String regionName, World w) {
        ProtectedRegion region = getRegion(regionName, w);
        if(region == null)
            return null;

        Queue<Chunk> chunks = new LinkedList<>();
        for(int x = (int) Math.floor(region.getMinimumPoint().getBlockX() / 16.0); x < Math.floor(region.getMaximumPoint().getBlockX() / 16.0) + 1; x++) {
            for(int z = (int) Math.floor(region.getMinimumPoint().getBlockZ() / 16.0); z < Math.floor(region.getMaximumPoint().getBlockZ() / 16.0) + 1; z++) {
                chunks.add(w.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    @Nullable
    public IsInRegion getIsInRegion(String regionName, World w) {
        ProtectedRegion r = getRegion(regionName, w);
        if(r == null)
            return null;

        return new IsInRegion(r);
    }

    public boolean regionContains(String regionName, @NotNull Location l) {
        ProtectedRegion r = getRegion(regionName, l.getWorld());
        if(r == null)
            return false;

        return r.contains(l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }


    /**
     * Generic features
     */

    @Nullable
    private ProtectedRegion getRegion(String regionName, World w) {
        RegionManager regions = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(w));
        if(regions == null)
            return null;

        return regions.getRegion(regionName);
    }

    @NotNull
    private ApplicableRegionSet getApplicableRegions(Location loc) {
        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        return query.getApplicableRegions(BukkitAdapter.adapt(loc));
    }

    @NotNull
    private MovecraftLocation vectorToMovecraftLocation(@NotNull BlockVector3 v) {
        return new MovecraftLocation(v.getBlockX(), v.getBlockY(), v.getBlockZ());
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
