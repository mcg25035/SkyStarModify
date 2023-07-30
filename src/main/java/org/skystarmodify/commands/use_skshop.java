package org.skystarmodify.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.*;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageEmbed;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.MessageFormat;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.itemutils.ItemUtils;
import org.skystarmodify.Chat;
import org.skystarmodify.Files;
import org.skystarmodify.LangResource;
import org.skystarmodify.SkyStarModify;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class use_skshop {
    private LangResource langRes;
    private File skshopData = Files.pluginFileConstruct("skshopData");
    public void sendEmbed(String player,String type,String item,String singlePrice,String amount,String total){

        String processedLang = langRes.lang.skShopTradeType.replace("§7","").replace(" : ","");

        MessageFormat embed = new MessageFormat();
        embed.setAuthorName(langRes.lang.skShopDiscordTitle);
        embed.setAuthorImageUrl("https://media.discordapp.net/attachments/909358727449149445/1106960556134518834/IMG_3048.png");
        embed.setFooterIconUrl("https://cdn.discordapp.com/avatars/492908862647697409/bf4ff10c052a338db04647dd23a70e62?size=1024");
        embed.setImageUrl("https://media.discordapp.net/attachments/959102610751770624/1123909634630111243/image.png");
        embed.setTitle(langRes.lang.skShopDiscordContent.replace("%player_name%",player));
        MessageEmbed.Field playerF = new MessageEmbed.Field(langRes.lang.player,player,true,true);
        MessageEmbed.Field typeF = new MessageEmbed.Field(processedLang,type,true,true);
        MessageEmbed.Field singlePriceF = new MessageEmbed.Field(
                langRes.lang.singlePrice,singlePrice,true,true);
        MessageEmbed.Field amountF = new MessageEmbed.Field(
                langRes.lang.amount,amount,true,true);
        MessageEmbed.Field totalF = new MessageEmbed.Field(langRes.lang.total,total,true,true);
        MessageEmbed.Field itemF = new MessageEmbed.Field(langRes.lang.item,item,true,true);
        MessageEmbed.Field source = new MessageEmbed.Field("","圖源 : https://www.pixiv.net/artworks/107457031",false,true);
        embed.setFields(List.of(playerF,typeF,itemF,singlePriceF,amountF,totalF,source));
        embed.setFooterText(langRes.lang.skShopDiscordFooter);
        embed.setTimestamp(Instant.now());
        embed.setColorRaw(16745472);
        Message discordMessage = DiscordSRV.translateMessage(embed, (content, needsEscape) -> content);
        TextChannel target = DiscordUtil.getTextChannelById("1037708046736044132");
        DiscordUtil.queueMessage(target,discordMessage);
    }
    public use_skshop() throws FileSystemException {

        SkyStarModify ssm = (SkyStarModify) Bukkit.getServer().getPluginManager().getPlugin("SkyStarModify");
        this.langRes = ssm.langRes;

        if (!skshopData.isDirectory()){
            skshopData.delete();
            if (!skshopData.mkdir()){
                throw new FileSystemException(skshopData.toPath().toUri().toString());
            }
        }

        Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");


        new CommandAPICommand("use_skshop")
                .withPermission(CommandPermission.NONE)
                .withSubcommand(
                        new CommandAPICommand("buy")
                                .withArguments(new StringArgument(langRes.lang.item))
                                .withArguments(new IntegerArgument(langRes.lang.amount))
                                .executes((sender,args)->{
                                    sender.sendMessage(langRes.lang.skShopHeader);
                                    File itemDataFile = Files.fileResolve(skshopData,((String)args.get(0))+".json");
                                    User essPlayer = ess.getUser((Player)sender);
                                    BigDecimal userMoney = essPlayer.getMoney();
                                    if (!itemDataFile.exists()){
                                        sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                        sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopItemDNEError);
                                        return;
                                    }
                                    try {
                                        JsonObject itemData = Files.readFileToJson(itemDataFile);
                                        if (itemData.get("type").getAsString().equals("sell")){
                                            sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                            sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopTypeError);
                                            return;
                                        }
                                        BigDecimal itemPrice = itemData.get("price").getAsBigDecimal();
                                        BigDecimal total = itemPrice.multiply(BigDecimal.valueOf((int) args.get(1)));

                                        if (userMoney.compareTo(total) < 0){
                                            sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                            sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopMoneyNotEnoughError);
                                            return;
                                        }
                                        ItemStack item = ItemUtils.dataToItem(itemData.get("itemData").getAsString());
                                        int amount = ((int) args.get(1));
                                        item.setAmount(amount);
                                        ItemStack left = ((Player) sender).getInventory().addItem(item).get(0);
                                        if (left != null){
                                            amount -= left.getAmount();
                                        }
                                        if (amount == 0){
                                            sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                            sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopBackpackFullError);
                                            return;
                                        }
                                        item.setAmount(64);
                                        sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.buy);
                                        sender.spigot().sendMessage(new ComponentBuilder().append(langRes.lang.skShopItemName).append(Chat.createAsItemTag(item)).create());
                                        sender.sendMessage(langRes.lang.skShopAmount+amount);

                                        BigDecimal totalPrice = BigDecimal.valueOf(amount).multiply(itemPrice);
                                        essPlayer.setMoney(essPlayer.getMoney().subtract(totalPrice));
                                        sender.sendMessage(langRes.lang.skShopTotal+totalPrice);
                                        sendEmbed(
                                                sender.getName(),
                                                langRes.lang.buy,
                                                item.getType().toString().toLowerCase(),
                                                itemPrice.toString(),
                                                Integer.toString(amount),
                                                totalPrice.toString()
                                        );

                                    }
                                    catch (Exception e){
                                        sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                        sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopItemFsError);
                                    }
                                })
                )
                .withSubcommand(
                        new CommandAPICommand("sell")
                                .withArguments(new StringArgument(langRes.lang.item))
                                .withArguments(new StringArgument("all/setAmount"))
                                .withArguments(new IntegerArgument(langRes.lang.amount).setOptional(true))
                                .executes((sender,args)->{
                                    sender.sendMessage(langRes.lang.skShopHeader);
                                    int amount = 0;
                                    if (((String)args.get(1)).equals("all")){
                                        amount = 2304;
                                    }
                                    else if (((String)args.get(1)).equals("setAmount") && !(Objects.isNull(args.get(2)))){
                                        amount = (int) args.get(2);
                                    }
                                    else{
                                        sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                        sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopCmdArgError);
                                        return;
                                    }
                                    File itemDataFile = Files.fileResolve(skshopData,((String)args.get(0))+".json");
                                    if (!itemDataFile.exists()){
                                        sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                        sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopItemDNEError);
                                        return;
                                    }
                                    try {
                                        JsonObject itemData = Files.readFileToJson(itemDataFile);
                                        if (itemData.get("type").getAsString().equals("buy")){
                                            sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                            sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopTypeError);
                                            return;
                                        }
                                        BigDecimal itemPrice = itemData.get("price").getAsBigDecimal();
                                        ItemStack item = ItemUtils.dataToItem(itemData.get("itemData").getAsString());
                                        item.setAmount(amount);
                                        HashMap<Integer, ItemStack> left = ((Player) sender).getInventory().removeItem(item);
                                        if (!left.isEmpty()){
                                            amount -= left.get(0).getAmount();
                                        }
                                        if (amount == 0){
                                            sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                            sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopItemNotEnoughError);
                                            return;
                                        }
                                        User essPlayer = ess.getUser((Player) sender);
                                        BigDecimal userMoney = essPlayer.getMoney();
                                        BigDecimal total = itemPrice.multiply(BigDecimal.valueOf(amount));
                                        essPlayer.setMoney(userMoney.add(total));
                                        item.setAmount(64);
                                        sender.sendMessage(langRes.lang.skShopTradeType+langRes.lang.sell);
                                        sender.spigot().sendMessage(new ComponentBuilder().append(langRes.lang.skShopItemName).append(Chat.createAsItemTag(item)).create());
                                        sender.sendMessage(langRes.lang.skShopAmount+amount);
                                        sender.sendMessage(langRes.lang.skShopTotal+total);
                                        sender.sendMessage(langRes.lang.skShopBal+essPlayer.getMoney());
                                        sendEmbed(sender.getName(),langRes.lang.sell,item.getType().toString().toLowerCase(),itemPrice.toString(),Integer.toString(amount),total.toString());
                                    }
                                    catch (Exception e){
                                        sender.sendMessage(langRes.lang.skShopMessageType+langRes.lang.error);
                                        sender.sendMessage(langRes.lang.skShopDetails+langRes.lang.skShopItemFsError);
                                    }

                                })
                )
                .register();


    }
}
