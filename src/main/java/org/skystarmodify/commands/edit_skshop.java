package org.skystarmodify.commands;

import com.earth2me.essentials.Essentials;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.FloatArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skystarmodify.Chat;
import org.skystarmodify.Files;
import org.skystarmodify.LangResource;
import org.skystarmodify.SkyStarModify;
import org.itemutils.ItemUtils;
import net.md_5.bungee.api.chat.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;

public class edit_skshop {
    private LangResource langRes;
    private File skshopData = Files.pluginFileConstruct("skshopData");
    public edit_skshop() throws FileSystemException{
        SkyStarModify ssm = (SkyStarModify) Bukkit.getServer().getPluginManager().getPlugin("SkyStarModify");
        this.langRes = ssm.langRes;

        if (!skshopData.isDirectory()){
            skshopData.delete();
            if (!skshopData.mkdir()){
                throw new FileSystemException(skshopData.toPath().toUri().toString());
            }
        }

        Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");

        new CommandAPICommand("edit_skshop")
                .withPermission(CommandPermission.OP)
                .withSubcommand(new CommandAPICommand("buy")
                        .withArguments(new FloatArgument(langRes.lang.price))
                        .executes((sender,args) -> {
                            sender.sendMessage(langRes.lang.skShopHeader);
                            sender.sendMessage(langRes.lang.skShopActionType+langRes.lang.skShopUpstore);
                            ItemStack handItem = ((Player)sender).getInventory().getItemInMainHand();
                            String itemID = handItem.getType().toString().toLowerCase();
                            String suffix = "";
                            String itemData = ItemUtils.ItemToData(handItem);
                            int i = 0;
                            while (Files.fileResolve(skshopData,itemID+suffix+".json").exists()){
                                i+=1;
                                suffix = Integer.toString(i+1);
                            }
                            File itemFile = Files.fileResolve(skshopData,itemID+suffix+".json");
                            Float price = (Float) args.get(0);
                            if (price == null || price <= 0 ){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopSinglePriceError);

                                return;
                            }
                            try{
                                JsonObject shopData = new JsonObject();
                                shopData.addProperty("type","buy");
                                shopData.addProperty("price",price);
                                shopData.addProperty("itemData",itemData);
                                Files.writeJsonToFile(itemFile,shopData);
                                sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.buy);
                                sender.spigot().sendMessage(new ComponentBuilder().append(langRes.lang.skShopItemName).append(Chat.createAsItemTag(handItem)).create());
                                sender.sendMessage(langRes.lang.skShopSinglePrice+price);
                            }
                            catch (IOException e){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skUpstoreFsError);
                                return;
                            }
                        })
                )
                .withSubcommand(new CommandAPICommand("sell")
                        .withArguments(new FloatArgument(langRes.lang.price))
                        .executes((sender,args) -> {
                            sender.sendMessage(langRes.lang.skShopHeader);
                            sender.sendMessage(langRes.lang.skShopActionType+langRes.lang.skShopUpstore);
                            ItemStack handItem = ((Player)sender).getInventory().getItemInMainHand();
                            String itemID = handItem.getType().toString();
                            String suffix = "";
                            String itemData = ItemUtils.ItemToData(handItem);
                            int i = 0;
                            while (Files.fileResolve(skshopData,itemID+suffix+".json").exists()){
                                i+=1;
                                suffix = Integer.toString(i+1);
                            }
                            File itemFile = Files.fileResolve(skshopData,itemID+suffix+".json");
                            Float price = (Float) args.get(0);
                            if (price == null || price <= 0 ){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopSinglePriceError);

                                return;
                            }
                            try{
                                JsonObject shopData = new JsonObject();
                                shopData.addProperty("type","sell");
                                shopData.addProperty("price",price);
                                shopData.addProperty("itemData",itemData);
                                Files.writeJsonToFile(itemFile,shopData);

                                sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.sell);
                                sender.spigot().sendMessage(new ComponentBuilder().append(langRes.lang.skShopItemName).append(Chat.createAsItemTag(handItem)).create());
                                sender.sendMessage(langRes.lang.skShopSinglePrice+price);
                            }
                            catch (IOException e){
                                sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skUpstoreFsError);
                                return;
                            }
                        })
                )
                .register();
    }
}
