package org.skystarmodify;

import com.earth2me.essentials.User;
import com.google.gson.JsonObject;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import de.tr7zw.nbtapi.plugin.NBTAPI;
import net.ess3.api.MaxMoneyException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.skystarmodify.exceptions.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class SkShop {
    public enum ShopType{
        SELL,
        BUY
    }

    public static class ShopInfo{
        public ItemStack item;
        public float total;
        public float price;
        public int amount;

        public ShopInfo(ItemStack item, float total, int amount, float price){
            this.item = item;
            this.total = total;
            this.amount = amount;
            this.price = price;
        }
    }

    private static final File SKSHOP_DATA_DIR = Files.pluginFileConstruct("skshopData");

    private static File getShopDataFile(ItemStack item, ShopType type, String suffix){
        String itemID = item.getType().toString();
        if (type == ShopType.SELL) itemID = itemID.toUpperCase();
        if (type == ShopType.BUY) itemID = itemID.toLowerCase();
        return SKSHOP_DATA_DIR.toPath().resolve(itemID + suffix + ".json").toFile();
    }

    private static File getShopDataFile(String file) {
        return SKSHOP_DATA_DIR.toPath().resolve(file).toFile();
    }

    private static File getNewShopDataFile(ItemStack item, ShopType type){
        String suffix = "";
        int i = 0;
        while (getShopDataFile(item, type, suffix).exists()){
            i += 1;
            suffix = Integer.toString(i + 1);
        }
        return getShopDataFile(item, type, suffix);
    }

    public static void priceChecker(float price) throws PriceException{
        if (price > 0) return;
        throw new PriceException("Price must be greater than 0");
    }

    public static void upstoreSell(ItemStack item, float price) throws PriceException, IOException {
        String itemData = NBT.itemStackToNBT(item).toString();
        File itemFileToCreate = getNewShopDataFile(item, ShopType.SELL);

        priceChecker(price);

        JsonObject shopData = new JsonObject();
        shopData.addProperty("type", "sell");
        shopData.addProperty("price", price);
        shopData.addProperty("itemData", itemData);

        Files.writeJsonToFile(itemFileToCreate, shopData);
    }

    public static void upstoreBuy(ItemStack item, float price) throws PriceException, IOException {
        String itemData = NBT.itemStackToNBT(item).toString();
        File itemFileToCreate = getNewShopDataFile(item, ShopType.BUY);

        priceChecker(price);

        JsonObject shopData = new JsonObject();
        shopData.addProperty("type", "buy");
        shopData.addProperty("price", price);
        shopData.addProperty("itemData", itemData);

        Files.writeJsonToFile(itemFileToCreate, shopData);
    }

    public static void moneyCheck(User user, float price) throws MoneyNotEnoughException {
        int compare = user.getMoney().compareTo(BigDecimal.valueOf(price));
        if (compare < 0) {
            throw new MoneyNotEnoughException("Money is not enough");
        }
    }

    public static void itemCheck(ItemStack item, int amount, Player player) throws ItemNotEnoughException {
        int itemAmount = item.getAmount();
        if (itemAmount >= amount) return;
        throw new ItemNotEnoughException("Item is not enough");
    }

    public static void addMoney(User user, float price) throws MaxMoneyException {
        BigDecimal userMoney = user.getMoney();
        user.setMoney(userMoney.add(BigDecimal.valueOf(price)));
    }

    public static void removeMoney(User user, float price) throws MaxMoneyException {
        BigDecimal userMoney = user.getMoney();
        user.setMoney(userMoney.subtract(BigDecimal.valueOf(price)));
    }

    public static boolean isItemSame(ItemStack itemA, ItemStack itemB) {
        ItemStack a = itemA.clone();
        ItemStack b = itemB.clone();
        a.setAmount(1);
        b.setAmount(1);

        return NBT.itemStackToNBT(a).equals(NBT.itemStackToNBT(b));
    }

    public static int getItemCountInPlayerInventory(Player player, ItemStack item) {
        int count = 0;
        for (ItemStack i : player.getInventory().getContents()) {
            if (i == null) continue;
            if (!isItemSame(i, item)) continue;
            count += i.getAmount();
        }

        return count;
    }

    public static void removeItemFromPlayerInventory(ItemStack item, int amount, Player player) {
        item.setAmount(amount);
        player.getInventory().removeItem(item);
    }

    public static JsonObject getItemData(String itemFile, ShopType type) throws IOException, WrongShopTypeException {
        File itemDataFile = getShopDataFile(itemFile);
        JsonObject itemDataJson = Files.readFileToJson(itemDataFile);

        String shopTypeString = itemDataJson.get("type").getAsString();
        ShopType shopType;
        try{
            shopType = ShopType.valueOf(shopTypeString.toUpperCase());
        }
        catch (IllegalArgumentException ignored) {
            throw new WrongShopTypeException("Required shop type is "+type+", but "+shopTypeString+" is given.");
        }
        if (shopType != type) throw new WrongShopTypeException("Required shop type is "+type+", but "+shopTypeString+" is given.");

        return itemDataJson;
    }

    public static ShopInfo buyItem(User user, String itemFile, int amount)
            throws MoneyNotEnoughException, IOException, WrongShopTypeException, ShopFileParseException, MaxMoneyException
    {
        JsonObject itemDataJson = getItemData(itemFile+".json", ShopType.BUY);

        ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(itemDataJson.get("itemData").getAsString()));
        if (item == null) throw new ShopFileParseException("Item data is not valid");

        item.setAmount(amount);

        float price = itemDataJson.get("price").getAsFloat();
        float total = price * amount;

        moneyCheck(user, total);
        user.getBase().getInventory().addItem(item);

        removeMoney(user, total);
        return new ShopInfo(item, total, amount, price);
    }

    public static ShopInfo sellItem(User user, String itemFile, int amount)
            throws ItemNotEnoughException, IOException, WrongShopTypeException, ShopFileParseException, MaxMoneyException {

        JsonObject itemDataJson = getItemData(itemFile+".json", ShopType.SELL);
        ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(itemDataJson.get("itemData").getAsString()));
        if (item == null) throw new ShopFileParseException("Item data is not valid");

        int playerItemCount = getItemCountInPlayerInventory(user.getBase(), item);
        if (amount == -1) amount = playerItemCount;

        if (playerItemCount < amount) throw new ItemNotEnoughException("Item is not enough");
        if (amount <= 0) throw new ItemNotEnoughException("Item is not enough");
        item.setAmount(1);

        float price = itemDataJson.get("price").getAsFloat();
        float total = price * amount;

        addMoney(user, total);
        removeItemFromPlayerInventory(item, amount, user.getBase());

        return new ShopInfo(item, total, amount, price);
    }


}
