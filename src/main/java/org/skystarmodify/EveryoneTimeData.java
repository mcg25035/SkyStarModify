package org.skystarmodify;

import com.booksaw.betterTeams.PlayerRank;
import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.google.gson.JsonObject;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.MessageFormat;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.time.Instant;
import java.util.*;

public class EveryoneTimeData {
    private static LangResource langRes = ((SkyStarModify) Bukkit.getPluginManager().getPlugin("SkyStarModify")).langRes;
    public static class team{
        public static class player{
            public long onlineTime;
            public team playerTeam;
            public UUID playerUUID;
            public player(long onlineTime, team playerTeam, UUID playerUUID){
                this.onlineTime = onlineTime;
                this.playerTeam = playerTeam;
                this.playerUUID = playerUUID;
            }
        }
        public String teamName;
        public List<team.player> players = new ArrayList<>();
        team(UUID teamUUID){
            List<TeamPlayer> teamPlayers = new ArrayList<>();
            Team currentTeam = Team.getTeam(teamUUID);
            teamName = currentTeam.getName();
            for (TeamPlayer i : currentTeam.getRank(PlayerRank.DEFAULT)){
                teamPlayers.add(i);
            }
            for (TeamPlayer i : currentTeam.getRank(PlayerRank.ADMIN)){
                teamPlayers.add(i);
            }
            for (TeamPlayer i : currentTeam.getRank(PlayerRank.OWNER)){
                teamPlayers.add(i);
            }
            for (TeamPlayer i : teamPlayers){
                UUID playerUUID = i.getPlayer().getUniqueId();
                File playerTimeDatas = PlayerTimeData.playerTimeDatas;
                File playerTimeData = Files.fileResolve(playerTimeDatas,playerUUID.toString()+".json");
                if (!playerTimeData.exists()){
                    continue;
                }
                try {
                    JsonObject playerData = Files.readFileToJson(playerTimeData);
                    Long onlineTime = playerData.get("time").getAsLong();

                    team.player player = new player(onlineTime,this,playerUUID);
                    this.players.add(player);
                }catch (Exception ignored){
                    continue;
                }
            }
        }
        team(List<UUID> players){
            this.teamName = "No team";
            File playerTimeDatas = PlayerTimeData.playerTimeDatas;
            for (UUID i : players){
                try {
                    File playerTimeData = Files.fileResolve(playerTimeDatas, i.toString() + ".json");
                    JsonObject playerData = Files.readFileToJson(playerTimeData);
                    Long onlineTime = playerData.get("time").getAsLong();
                    this.players.add(new player(onlineTime, this, i));
                }catch (Exception ignored){
                    continue;
                }
            }
        }

    }
    public void sendEmbed(List<Map<UUID,Map<Long,team>>> data){
        MessageFormat embed = new MessageFormat();
        embed.setAuthorName(langRes.lang.skPlayerOnlineTimeSystemTitle);
        embed.setAuthorImageUrl("https://media.discordapp.net/attachments/909358727449149445/1106960556134518834/IMG_3048.png");
        embed.setFooterIconUrl("https://cdn.discordapp.com/avatars/492908862647697409/bf4ff10c052a338db04647dd23a70e62?size=1024");
        embed.setTitle(langRes.lang.skYesterdayPlayersOnlineTimeData);
        String players = "";
        String allOnlineTime = "";
        String teams = "";
        for (Map<UUID,Map<Long,team>> i : data){
            Map.Entry<UUID,Map<Long,team>> that =  i.entrySet().stream().findFirst().get();
            players+=(Bukkit.getServer().getOfflinePlayer(that.getKey()).getName()+"\n");
            Map.Entry<Long,team> thatInThat = that.getValue().entrySet().stream().findFirst().get();
            allOnlineTime+=(thatInThat.getKey()+"\n");
            teams+=(thatInThat.getValue().teamName+"\n");
        }
        MessageEmbed.Field player = new MessageEmbed.Field(langRes.lang.player,players,true,true);
        MessageEmbed.Field time = new MessageEmbed.Field(langRes.lang.skTime,allOnlineTime,true,true);
        MessageEmbed.Field team = new MessageEmbed.Field(langRes.lang.skTeam,teams,true,true);
        embed.setFields(List.of(player,time,team));
        embed.setFooterText(langRes.lang.skShopDiscordFooter);
        embed.setTimestamp(Instant.now());
        embed.setColorRaw(8767231);
        Message discordMessage = DiscordSRV.translateMessage(embed, (content, needsEscape) -> content);
        TextChannel target = DiscordUtil.getTextChannelById("1134978894408196197");
        DiscordUtil.queueMessage(target,discordMessage);
    }
    public EveryoneTimeData(){
        List<UUID> waitReset = new ArrayList<>();
        for (PlayerTimeData i : PlayerTimeData.players.values()){
            i.playerQuit();
            waitReset.add(i.player);
        }
        for (UUID i : waitReset){
            new PlayerTimeData(i);
        }
        File[] playerFiles = PlayerTimeData.playerTimeDatas.listFiles();
        List<UUID> players = new ArrayList<>();
        for (File i : playerFiles){
            if (!i.isFile()){
                continue;
            }
            players.add(UUID.fromString(i.getName().replaceAll("\\.json","")));
        }
        Map<UUID,Team> rawTeamData = Team.getTeamManager().getLoadedTeamListClone();
        List<Map<UUID,Map<Long,team>>> data = new ArrayList<>();
        List<team> allTeamNeedProcess = new ArrayList<>();
        for (Map.Entry<UUID,Team> i : rawTeamData.entrySet()) {
            team itInI = new team(i.getKey());
            allTeamNeedProcess.add(itInI);
            for (team.player ii : itInI.players){
                players.remove(ii.playerUUID);
            }
        }
        team noTeamPlayers = new team(players);
        allTeamNeedProcess.add(noTeamPlayers);
        for (team it : allTeamNeedProcess){
            for (team.player ii : it.players){
                Map<Long,team> timeAndTeam = new HashMap<>();
                timeAndTeam.put(ii.onlineTime,it);
                Map<UUID, Map<Long,team>> teamFinalData = new HashMap<>();
                teamFinalData.put(ii.playerUUID,timeAndTeam);
                data.add(teamFinalData);
            }
        }
        sendEmbed(data);
        for (File i : playerFiles){
            if (!i.isFile()){
                continue;
            }
            i.delete();
        }
    }
}
