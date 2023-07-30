package org.skystarmodify;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

public class PlayerTimeData {
    private SkyStarModify main = (SkyStarModify) Bukkit.getPluginManager().getPlugin("SkyStarModify");
    public static File playerTimeDatas = Files.pluginFileConstruct("playerTimeDatas");
    private Instant joinTime;
    private Instant quitTime;
    public UUID player;
    public static HashMap<UUID,PlayerTimeData> players = new HashMap<>();
    PlayerTimeData(UUID player) {
        if (!playerTimeDatas.exists()){
            playerTimeDatas.mkdirs();
        }
        joinTime = Instant.now();
        this.player = player;
        PlayerTimeData.players.put(player,this);
    }
    public void playerQuit(){
        quitTime = Instant.now();
        File playerTimeData = Files.fileResolve(playerTimeDatas,player.toString()+".json");
        Long playerOnlineTime = Duration.between(joinTime,quitTime).toSeconds();
        if (!playerTimeData.exists()){
            JsonObject newJson = new JsonObject();
            newJson.addProperty("time",playerOnlineTime);
            try {
                Files.writeJsonToFile(playerTimeData, newJson);
            }catch (Exception ignored){

            }
        }
        try {
            JsonObject json = Files.readFileToJson(playerTimeData);
            Long playerLastOnlineTime = json.get("time").getAsLong();
            json.addProperty("time", (playerOnlineTime + playerLastOnlineTime));
            Files.writeJsonToFile(playerTimeData, json);
        }catch (Exception ignored){}
        PlayerTimeData.players.remove(this.player);
    }

}
