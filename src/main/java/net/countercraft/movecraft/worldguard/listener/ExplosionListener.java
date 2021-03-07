package net.countercraft.movecraft.worldguard.listener;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.countercraft.movecraft.events.ExplosionEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExplosionListener implements Listener {
    @EventHandler
    public void onExplosion(ExplosionEvent e) {
        ApplicableRegionSet set = MovecraftWorldGuard.getInstance().getWorldGuardPlugin().getRegionManager(e.getExplosionLocation().getWorld()).getApplicableRegions(e.getExplosionLocation());
        if(set.allows(DefaultFlag.OTHER_EXPLOSION))
            return;

        e.setCancelled(true);
    }
}
