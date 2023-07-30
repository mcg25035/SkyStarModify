package org.skystarmodify;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SkyStarModify extends JavaPlugin {
    public File pluginDir = getDataFolder();
    public LangResource langRes;
    public Map<String, Object> config;
    public Instant today;
    public int day;
    public void checkPluginFile() throws IOException {

        if (!pluginDir.exists()){
            pluginDir.mkdirs();
        }
        if (!pluginDir.isDirectory()){
            pluginDir.delete();
            pluginDir.mkdirs();
        }
        File configFileReal = pluginDir.toPath().resolve("config.yml").toFile();
        if (!configFileReal.exists()){
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
        if (!configFileReal.isFile()){
            boolean ignored = configFileReal.delete();
            URL configFileTemplate = getClass().getResource("/config.yml");
            assert configFileTemplate != null;
            FileUtils.copyURLToFile(configFileTemplate,configFileReal);
        }
    }

    @Override
    public void onEnable(){
        // Plugin startup logic
        for (Player i : Bukkit.getServer().getOnlinePlayers()){
            UUID player = i.getUniqueId();
            if (!PlayerTimeData.players.containsKey(player)){
                new PlayerTimeData(player);
            }
        }

        try{
            checkPluginFile();
        }
        catch (IOException e){
            Bukkit.getLogger().throwing(this.getName(),"IOException",e);
            return;
        }


        File configFile = pluginDir.toPath().resolve("config.yml").toFile();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(configFile);
        } catch (Exception e) {}
        this.config = (new Yaml()).load(inputStream);


        URL langFile = SkyStarModify.class.getResource("lang/" +config.get("lang")+".json");
        try {
            InputStream textSource = SkyStarModify.class.getClassLoader().getResourceAsStream("lang/"+config.get("lang")+".json");
            String fileData = new String(textSource.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject langJson = new Gson().fromJson(fileData , JsonObject.class);
            langRes = new LangResource(langJson);
        } catch (IOException e) {}

        Reflections reflections = new Reflections("org.skystarmodify.commands" ,new SubTypesScanner(false));
        Set<Class<?>> commands = reflections.getSubTypesOf(Object.class);
        for (Class<?> c : commands){
            try {
                c.getDeclaredConstructor().newInstance();
                Bukkit.getLogger().info("[SkyStarModify] Loading command - "+c.getName());
            } catch (Exception e) {
                Bukkit.getLogger().warning("[SkyStarModify] Can not load command - "+c.getName());
                Bukkit.getLogger().warning(e.toString());
                e.printStackTrace();
            }
        }
        Bukkit.getServer().getPluginManager().registerEvents(new Events(),this);
        File timeData = Files.pluginFileConstruct("timeData");
        if (!timeData.exists()){
            String timeStamp = Timestamp.from(Instant.now()).toString();
            try {
                Files.writeFile(timeData, timeStamp);
            }catch (Exception ignored){}
        }
        try {
            day = Timestamp.valueOf(Files.readFile(timeData)).toInstant().atZone(ZoneOffset.systemDefault()).getDayOfMonth();
        }catch (Exception ignored){}
        Bukkit.getScheduler().runTaskTimer(this,()->{
            int today = Instant.now().atZone(ZoneOffset.systemDefault()).getDayOfMonth();
            if (today != day){
                String timeStamp = Timestamp.from(Instant.now()).toString();
                try {
                    Files.writeFile(timeData, timeStamp);
                }catch (Exception ignored){}
                day = today;
                new EveryoneTimeData();
            }
        },3600,20);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        for (Map.Entry i : PlayerTimeData.players.entrySet()){
            ((PlayerTimeData)i.getValue()).playerQuit();
        }
    }
}
