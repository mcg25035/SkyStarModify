package org.skystarmodify;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
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
    }

    @Override
    public void onDisable() {
    }
}
