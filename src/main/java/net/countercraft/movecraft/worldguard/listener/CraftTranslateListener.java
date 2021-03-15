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

        if(e.getCraft().getNotificationPlayer() == null || e.getNewHitBox().isEmpty())
            return;

        if(MovecraftWorldGuard.getInstance().getWGUtils().canTranslate(e.getCraft().getNotificationPlayer(), e.getCraft().getW(), e.getNewHitBox()))
            return;

        MovecraftLocation location = e.getCraft().getHitBox().getMidPoint();
        for(MovecraftLocation ml : e.getNewHitBox()) {
            if(!MovecraftWorldGuard.getInstance().getWGUtils().canTranslate(e.getCraft().getNotificationPlayer(), ml.toBukkit(e.getWorld()))) {
                location = ml;
                break;
            }
        }

        e.setCancelled(true);
        e.setFailMessage(String.format(I18nSupport.getInternationalisedString( "Translation - WorldGuard - Not Permitted To Build" ) + " @ %d,%d,%d", location.getX(), location.getY(), location.getZ()));
    }
}
