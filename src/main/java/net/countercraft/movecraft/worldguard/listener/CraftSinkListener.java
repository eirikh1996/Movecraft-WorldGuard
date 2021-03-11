package net.countercraft.movecraft.worldguard.listener;

import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.worldguard.MovecraftWorldGuard;
import net.countercraft.movecraft.worldguard.config.Config;
import net.countercraft.movecraft.worldguard.localisation.I18nSupport;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CraftSinkListener implements Listener {
    @EventHandler
    public void onCraftSink(CraftSinkEvent e) {
        if(!Config.WorldGuardBlockSinkOnPVPPerm)
            return;

        if(MovecraftWorldGuard.getInstance().getWGUtils().isPVPAllowed(e.getCraft().getW(), e.getCraft().getHitBox()))
            return;

        e.setCancelled(true);
        Player p = e.getCraft().getNotificationPlayer();
        if(p == null)
            return;

        p.sendMessage(I18nSupport.getInternationalisedString("Player - Craft should sink but PVP is not allowed in this WorldGuard region" ));
    }
}
