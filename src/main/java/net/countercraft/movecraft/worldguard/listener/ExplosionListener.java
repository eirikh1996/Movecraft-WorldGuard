package net.countercraft.movecraft.worldguard.listener;

import net.countercraft.movecraft.events.ExplosionEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ExplosionListener implements Listener {
    @EventHandler
    public void onExplosion(ExplosionEvent e) {
        if(MovecraftWorldGuard.getInstance().getWGUtils().allowsOtherExplosion(e.getExplosionLocation()))
            return;

        e.setCancelled(true);
    }
}
