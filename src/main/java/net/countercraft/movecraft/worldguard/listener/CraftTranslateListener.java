package net.countercraft.movecraft.worldguard.listener;

import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import net.countercraft.movecraft.worldguard.config.Config;
import net.countercraft.movecraft.worldguard.localisation.I18nSupport;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftTranslateListener implements Listener {
    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent e) {
        if(!Config.WorldGuardBlockMoveOnBuildPerm)
            return;

        if(e.getCraft().getNotificationPlayer() == null)
            return;

        for(MovecraftLocation ml : e.getNewHitBox().difference(e.getOldHitBox())) {
            if(!MovecraftWorldGuard.getInstance().getWorldGuardPlugin().canBuild(e.getCraft().getNotificationPlayer(), ml.toBukkit(e.getWorld()))) {
                e.setCancelled(true);
                e.setFailMessage(String.format(I18nSupport.getInternationalisedString( "Translation - WorldGuard - Not Permitted To Build" ) + " @ %d,%d,%d", ml.getX(), ml.getY(), ml.getZ()));
                return;
            }
        }
    }
}
