package org.skystarmodify;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class Events implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        UUID player = e.getPlayer().getUniqueId();
        new PlayerTimeData(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){
        UUID player = e.getPlayer().getUniqueId();
        if (!PlayerTimeData.players.containsKey(player)){
            return;
        }
        PlayerTimeData.players.get(e.getPlayer().getUniqueId()).playerQuit();

    }
}
