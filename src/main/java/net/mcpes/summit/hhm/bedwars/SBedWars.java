package net.mcpes.summit.hhm.bedwars;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.inventory.BigShapedRecipe;
import cn.nukkit.inventory.ShapedRecipe;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemPotion;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Location;
import cn.nukkit.level.sound.AnvilFallSound;
import cn.nukkit.level.sound.ButtonClickSound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.CraftingDataPacket;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import net.mcpes.summit.hhm.bedwars.command.BedWarsCommand;
import net.mcpes.summit.hhm.bedwars.config.RoomConfig;
import net.mcpes.summit.hhm.bedwars.data.RoomData;
import net.mcpes.summit.hhm.bedwars.game.BedWars;
import net.mcpes.summit.hhm.bedwars.listener.PlayerListener;
import net.mcpes.summit.hhm.bedwars.listener.SettingListener;
import net.mcpes.summit.hhm.bedwars.listener.SignListener;
import net.mcpes.summit.hhm.bedwars.utils.FileFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class SBedWars extends PluginBase implements SBedWarsAPI {
    public static final String DEFAULT_TITLE = "§l§e[§6S§eB§de§3d§6W§aa§4r§5s§e] §6";
    public static final String VERSION = "1.0.0";
    public static final String GAMESTATUS_WAIT = "§6等待加入";
    public static final String GAMESTATUS_SOON = "§6即将开始";
    public static final String GAMESTATUS_START = "§6游戏开始";
    public static final String GAMESTATUS_STOP = "§6游戏停止";
    public static final String GAMESTATUS_RELOAD = "§6重载房间";
    public static HashMap<Integer, BedWars> games = new HashMap<>();
    public static HashMap<Integer, RoomData> rooms = new HashMap<>();
    public static HashMap<Integer, HashMap<String, ArrayList<Player>>> players = new HashMap<>();
    public static HashMap<String, Integer> gaming = new HashMap<>();
    public static HashMap<String, String> touch = new HashMap<>();
    public static HashMap<String, RoomData> add = new HashMap<>();
    public static ArrayList<ShapedRecipe> bedWarsComposes = new ArrayList<>();
    public static CraftingDataPacket craftingDataPacket = new CraftingDataPacket();
    public static Item gold;
    public static Item silver;
    public static Item copper;
    private static SBedWars instance;

    public static SBedWars getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        gold = Item.get(266, 0, 1);
        gold.setCustomName(DEFAULT_TITLE + "§e金");
        silver = Item.get(265, 0, 1);
        silver.setCustomName(DEFAULT_TITLE + "§f银");
        copper = Item.get(336, 0, 1);
        copper.setCustomName(DEFAULT_TITLE + "§6铜");
        this.initConfig();
        this.getServer().getLogger().info(DEFAULT_TITLE + VERSION + " 加载成功");
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new SettingListener(), this);
        this.getServer().getPluginManager().registerEvents(new SignListener(), this);
        this.getServer().getCommandMap().register("bw", new BedWarsCommand());
        this.loadWorlds();
        this.loadAllRoom();
        this.registerComposes();
        this.getServer().getLogger().info(DEFAULT_TITLE + VERSION + " 启动成功");
    }

    @Override
    public void onDisable() {
        this.getServer().getLogger().info(DEFAULT_TITLE + VERSION + " 关闭成功");
    }

    private void initConfig() {
        new File(this.getDataFolder() + "/rooms/").mkdirs();
        new File(this.getDataFolder() + "/worlds/").mkdirs();
    }

    @Override
    public void loadAllRoom() {
        String[] rns = new File(this.getDataFolder() + "/rooms/").list();
        if (rns == null) return;
        for (String rn : rns) {
            int r = Integer.valueOf(rn.replace(".yml", ""));
            if (this.loadRoom(r)) {
                this.getServer().getLogger().info(DEFAULT_TITLE + "加载" + r + "号房间成功!");
            } else {

                this.getServer().getLogger().info(DEFAULT_TITLE + "加载" + r + "号房间失败!");
            }
        }
    }

    @Override
    public boolean loadRoom(int id) {
        try {
            RoomConfig cr = new RoomConfig(id);
            this.getServer().loadLevel(cr.getConfig().getString("game-world"));
            if (this.getServer().isLevelLoaded(cr.getConfig().getString("game-world"))) {
                RoomData data = new RoomData(id);
                data.setResourcesType(cr.getConfig().getInt("resourcesType"));
                data.setShopType(cr.getConfig().getInt("shopType"));
                data.setDisName(cr.getConfig().getString("disName"));
                data.setMin(cr.getConfig().getInt("min-player"));
                data.setMax(cr.getConfig().getInt("max-player"));
                data.setWaitTime(cr.getConfig().getInt("wait-time"));
                data.setWaitPos(cr.getConfig().getString("wait-pos"));
                data.setGameTime(cr.getConfig().getInt("game-time"));
                data.setGameWorld(cr.getConfig().getString("game-world"));
                data.setStopPos(cr.getConfig().getString("stop-pos"));
                data.setVoidKill(cr.getConfig().getInt("voidKill"));
                data.setTeamData((HashMap<Integer, HashMap<String, Object>>) cr.getConfig().get("teamData"), 1);
                data.setSignPos((ArrayList) cr.getConfig().get("sign-pos"));
                data.setGoldPos((ArrayList) cr.getConfig().get("gold-pos"));
                data.setSilverPos((ArrayList) cr.getConfig().get("silver-pos"));
                data.setCopperPos((ArrayList) cr.getConfig().get("copper-pos"));
                data.setGoldDropSpeed(cr.getConfig().getInt("gold-drop-speed"));
                data.setSilverDropSpeed(cr.getConfig().getInt("silver-drop-speed"));
                data.setCopperDropSpeed(cr.getConfig().getInt("copper-drop-speed"));
                if (data.getResourcesType() == 2) {
                    data.setGoldToExp(cr.getConfig().getInt("gold-to-exp"));
                    data.setSilverToExp(cr.getConfig().getInt("silver-to-exp"));
                    data.setCopperToExp(cr.getConfig().getInt("copper-to-exp"));
                }
                data.setGameLevel();
                data.setGoldLocation();
                data.setSignLocation();
                data.setWaitLocation();
                data.setStopLocation();
                data.setSilverLocation();
                data.setCopperLocation();
                rooms.put(id, data);
            } else return false;
            return true;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int getFreeID() {
        int max = 0;
        String[] rns = new File(this.getDataFolder() + "/rooms/").list();
        if (rns == null) return 1;
        int length = 0;
        for (String rn : rns) {
            if (max < Integer.valueOf(rn.replace(".yml", ""))) {
                max = Integer.valueOf(rn.replace(".yml", ""));
            }
            length++;
        }
        if (max <= length) {
            return max + 1;
        }
        for (int i = 1; i < max; i++) {
            if (!new File(this.getDataFolder() + "/rooms/" + i + ".yml").exists()) {
                return i;
            }
        }
        return max + 1;
    }

    @Override
    public void loadWorlds() {
        String[] ws = new File(this.getDataFolder() + "/worlds/").list();
        if (ws == null) return;
        for (String w : ws) {
            if (new File(this.getDataFolder() + "/worlds/" + w).isFile()) continue;
            try {
                this.loadWorld(w);
                this.getServer().getLogger().info(DEFAULT_TITLE + "成功加载地图:" + w);
            } catch (IOException e) {
                e.printStackTrace();
                this.getServer().getLogger().info(DEFAULT_TITLE + "加载地图:" + w + "失败");
            }
        }
    }

    @Override
    public void loadWorld(String worldName) throws IOException {
        if (!this.getServer().isLevelLoaded(worldName)) {
            FileFunction.copy(new File(this.getDataFolder() + "/worlds/" + worldName + "/").getCanonicalPath(), this.getDataFolder().getParentFile().getParentFile() + "/worlds/" + worldName + "/");
            this.getServer().loadLevel(worldName);
        } else {
            try {
                Map<Long, Player> players = Server.getInstance().getLevelByName(worldName).getPlayers();
                if (players.size() > 0) {
                    players.forEach((k, v) -> {
                        v.teleport(Server.getInstance().getDefaultLevel().getSpawnLocation());
                    });
                }
            } catch (Exception e) {
                // 屏蔽
            }
            this.getServer().unloadLevel(this.getServer().getLevelByName(worldName));
            FileFunction.remove(new File(this.getDataFolder().getParentFile().getParentFile() + "/worlds/" + worldName + "/"));
            FileFunction.copy(new File(this.getDataFolder() + "/worlds/" + worldName + "/").getCanonicalPath(), this.getDataFolder().getParentFile().getParentFile() + "/worlds/" + worldName + "/");
            this.getServer().loadLevel(worldName);
        }
    }

    @Override
    public void broadcastMessage(ArrayList<Player> p, String msg) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + msg);
            }
        }
    }

    @Override
    public void broadcastMessage2(ArrayList<Player> p, String msg,Player player) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + " "+player.getName() + " >> " +msg);
            }
        }
    }

    @Override
    public void broadcastMessage3(ArrayList<Player> p, String msg,Player player) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + TextFormat.colorize("&e大喊 &6>> &d队伍: &b"+rooms.get(SBedWarsAPI.getInstance().isPlayerGaming(player.getName())).getTeamData().get(games.get(SBedWarsAPI.getInstance().isPlayerGaming(player.getName())).getTeam(player.getName())).get("name")+" &e&l"+player.getName() + " &6>> &4" +msg));
            }
        }
    }

    @Override
    public void broadcastTitle(ArrayList<Player> p, int fadeIn, int stay, int fadeOut, String msg, String twoMsg) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.setTitleAnimationTimes(fadeIn, stay, fadeOut);
                if (twoMsg != null) {
                    pl.setSubtitle(twoMsg);
                }
                pl.sendTitle(msg);
                pl.resetTitleSettings();
            }
        }
    }

    @Override
    public void broadcastSound(ArrayList<Player> p, int type) {
        switch (type) {
            case 1:
                for (Player pl : p) {
                    if (pl.isOnline())
                        pl.getLevel().addSound(new ButtonClickSound(pl.getLocation()));
                }
                break;
            case 2:
                for (Player pl : p) {
                    if (pl.isOnline())
                        pl.getLevel().addSound(new AnvilFallSound(pl.getLocation()));
                }
                break;
        }
    }

    @Override
    public void broadcastTip(ArrayList<Player> p, String msg) {
        for (Player pl : p) {
            if (pl.isOnline())
                pl.sendTip(msg);
        }
    }

    @Override
    public void broadcastSpeak(ArrayList<Player> p, String pn, String msg) {
        for (Player pl : p) {
            pl.sendMessage(DEFAULT_TITLE + "§7<§b" + pn + "§7>§6: " + msg);
        }
    }

    @Override
    public void broadcastTeamSpeak(ArrayList<String> p, String pn, String msg) {
        for (String name : p) {
            Player pl = this.getServer().getPlayerExact(name);
            pl.sendMessage(DEFAULT_TITLE + "§7<§b" + pn + "§7>§6: " + msg);
        }
    }

    @Override
    public void broadcastTeamMessage(ArrayList<String> p, String msg) {
        ArrayList<Player> players = new ArrayList<>();
        for (String name : p) {
            Player pl = this.getServer().getPlayerExact(name);
            players.add(pl);
        }
        this.broadcastMessage(players, msg);
    }

    @Override
    public int isPlayerGaming(String name) {
        return gaming.getOrDefault(name, -1);
    }

    @Override
    public int isSign(String pos) {
        for (RoomData data : rooms.values()) {
            for (String b : data.getSignPos()) {
                if (pos.equals(b)) return data.getId();
            }
        }
        return -1;
    }

    @Override
    public void addSign(String pos, int id) {
        RoomConfig cr = new RoomConfig(id);
        ArrayList<String> sign = (ArrayList<String>) cr.getConfig().get("sign-pos");
        sign.add(pos);
        cr.getConfig().set("sign-pos", sign);
        cr.getConfig().save();
        RoomData data = rooms.get(id);
        data.setSignPos(sign);
        data.setSignLocation();
    }

    @Override
    public void delSign(String pos, int id) {
        RoomConfig cr = new RoomConfig(id);
        ArrayList<String> sign = (ArrayList<String>) cr.getConfig().get("sign-pos");
        sign.remove(pos);
        cr.getConfig().set("sign-pos", sign);
        cr.getConfig().save();
        RoomData data = rooms.get(id);
        data.setSignPos(sign);
        data.setSignLocation();
    }

    @Override
    public void resetSign(int id, Location pos) {
        BlockEntity entity = pos.level.getBlockEntity(pos);
        if (!(entity instanceof BlockEntitySign)) return;
        BlockEntitySign sign = (BlockEntitySign) entity;
        String status;
        if (games.containsKey(id)) {
            switch (games.get(id).getGameMode()) {
                default:
                    status = "§c未知错误";
                    break;
                case 1:
                    status = GAMESTATUS_WAIT;
                    break;
                case 2:
                    status = GAMESTATUS_SOON;
                    break;
                case 3:
                    status = GAMESTATUS_START;
                    break;
                case 4:
                    status = GAMESTATUS_STOP;
                    break;
                case 5:
                    status = GAMESTATUS_RELOAD;
                    break;
            }
            sign.setText(DEFAULT_TITLE, status, "§l§b房间: §e" + id, "§l§c<§f---§b" + games.get(id).getAllPlayers().size() + "§7/" + rooms.get(id).getMax() + "§f---§c>");
        } else {
            sign.setText(DEFAULT_TITLE, GAMESTATUS_WAIT, "§l§b房间: §e" + id, "§l§c<§f---§b0§7/" + rooms.get(id).getMax() + "§f---§c>");
        }
    }

    @Override
    public void registerComposes() {
        Item sandStone18 = Item.get(24, 0, 18);
        sandStone18.setCustomName(DEFAULT_TITLE + "§f沙石");
        Item sandStone2 = Item.get(24, 0, 2);
        sandStone2.setCustomName(DEFAULT_TITLE + "§f沙石");
        bedWarsComposes.add(new BigShapedRecipe(
                sandStone2, "XXX", "XCX", "XXX"
        ).setIngredient("X", Item.get(0)).setIngredient("C", copper));//沙石X2
        bedWarsComposes.add(new BigShapedRecipe(
                sandStone18, "CCC", "CCC", "CCC"
        ).setIngredient("C", copper));//沙石X18
        Item woodStick = Item.get(280, 0, 1);
        Enchantment woodStickEnchant = Enchantment.get(19);
        woodStickEnchant.setLevel(1);
        woodStick.addEnchantment(woodStickEnchant);
        woodStick.setDamage(2);
        woodStick.setCustomName(DEFAULT_TITLE + "§f木棍");
        bedWarsComposes.add(new BigShapedRecipe(
                woodStick, "CXC", "CCC", "CCC"
        ).setIngredient("C", copper).setIngredient("X", Item.get(0)));//木棍击退1 伤害2
        Item silverStick = Item.get(280, 0, 1);
        silverStick.setCustomName(DEFAULT_TITLE + "§f棍子(加强版)");
        Enchantment silverStickEnchant = Enchantment.get(19);
        silverStickEnchant.setLevel(3);
        silverStick.addEnchantment(silverStickEnchant);
        silverStick.setDamage(4);
        bedWarsComposes.add(new BigShapedRecipe(
                silverStick, "XSX", "SSS", "SSS"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//烈焰棒击退3 伤害4
        Item goHome = Item.get(337, 0, 1);
        goHome.setCustomBlockData(new CompoundTag().putString("bedWars", "goHome"));
        goHome.setCustomName(DEFAULT_TITLE + "§f回城火药");
        bedWarsComposes.add(new BigShapedRecipe(
                goHome, "SXS", "SXS", "SSS"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//回城火药
        /*Item tnt = Item.get(46, 0, 1);
        tnt.setCustomBlockData(new CompoundTag().putString("bedWars", "tnt"));
        tnt.setCustomName(DEFAULT_TITLE + "§fTNT");
        bedWarsComposes.add(new BigShapedRecipe(
                tnt, "GXG", "GXG", "XGX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//TNT*/
        Item cobweb = Item.get(Item.COBWEB, 0, 1);
        cobweb.setCustomName(DEFAULT_TITLE + "§f蜘蛛网");
        bedWarsComposes.add(new BigShapedRecipe(
                cobweb, "SSS", "XXX", "XXX"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//蜘蛛网
        Item bowLv1 = Item.get(261, 0, 1);
        bowLv1.setCustomName(DEFAULT_TITLE + "§f弓 LV.1");
        Enchantment bowLv1Enchant = Enchantment.get(Enchantment.ID_BOW_INFINITY);
        bowLv1.addEnchantment(bowLv1Enchant);
        bedWarsComposes.add(new BigShapedRecipe(
                bowLv1, "GXX", "GXX", "GXX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//弓1
        Item bowLv2 = Item.get(261, 0, 1);
        bowLv2.setCustomName(DEFAULT_TITLE + "§f弓 LV.2");
        Enchantment bowLv2Enchant1 = Enchantment.get(Enchantment.ID_BOW_INFINITY);
        Enchantment bowLv2Enchant2 = Enchantment.get(Enchantment.ID_BOW_KNOCKBACK);
        bowLv2Enchant2.setLevel(2);
        bowLv2.addEnchantment(bowLv2Enchant1);
        bowLv2.addEnchantment(bowLv2Enchant2);
        bedWarsComposes.add(new BigShapedRecipe(
                bowLv2, "GGX", "GGX", "GGX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//弓2
        Item bowLv3 = Item.get(261, 0, 1);
        bowLv3.setCustomName(DEFAULT_TITLE + "§f弓 LV.3");
        Enchantment bowLv3Enchant1 = Enchantment.get(Enchantment.ID_BOW_INFINITY);
        bowLv3Enchant1.setLevel(1);
        Enchantment bowLv3Enchant2 = Enchantment.get(Enchantment.ID_BOW_KNOCKBACK);
        bowLv3Enchant2.setLevel(2);
        Enchantment bowLv3Enchant3 = Enchantment.get(Enchantment.ID_BOW_FLAME);
        bowLv3Enchant3.setLevel(1);
        bowLv3.addEnchantment(bowLv3Enchant1);
        bowLv3.addEnchantment(bowLv3Enchant2);
        bowLv3.addEnchantment(bowLv3Enchant3);
        bedWarsComposes.add(new BigShapedRecipe(
                bowLv3, "GGG", "GGG", "GGG"
        ).setIngredient("G", gold));//弓3
        Item ironBoots = Item.get(Item.IRON_BOOTS, 0, 1);
        ironBoots.setCustomName(DEFAULT_TITLE + "§f铁鞋-与铁头盔一起穿戴可防御击退");
        ironBoots.setCustomBlockData(new CompoundTag().putString("bedWars", "ironBoots"));
        bedWarsComposes.add(new BigShapedRecipe(
                ironBoots, "SXS", "GSG", "GSG"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)).setIngredient("G", gold));//铁鞋
        Item ironHelmet = Item.get(Item.IRON_HELMET, 0, 1);
        ironHelmet.setCustomName(DEFAULT_TITLE + "§f铁头盔-与铁鞋一起穿戴可防御击退");
        ironHelmet.setCompoundTag(new CompoundTag().putString("bedWars", "ironHelmet"));
        bedWarsComposes.add(new BigShapedRecipe(
                ironHelmet, "GGG", "GXG", "SSS"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)).setIngredient("G", gold));//铁鞋
        Item chainChestPlate = Item.get(Item.CHAIN_CHESTPLATE, 0, 1);
        chainChestPlate.setCompoundTag(new CompoundTag().putString("bedWars", "chainChestPlate"));
        chainChestPlate.setCustomName(DEFAULT_TITLE + "§f锁链甲");
        bedWarsComposes.add(new BigShapedRecipe(
                chainChestPlate, "SXS", "SSS", "SSS"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//锁链甲
        Item chest = Item.get(Item.CHEST, 0, 1);
        chest.setCustomName(DEFAULT_TITLE + "§f箱子");
        bedWarsComposes.add(new BigShapedRecipe(
                chest, "XXS", "XXX", "XXX"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//箱子
        Item trappedChest = Item.get(Item.TRAPPED_CHEST, 0, 1);
        trappedChest.setCustomName(DEFAULT_TITLE + "§f陷阱箱-破坏提示");
        trappedChest.setCompoundTag(new CompoundTag().putString("bedWars", "trappedChest"));
        bedWarsComposes.add(new BigShapedRecipe(
                trappedChest, "XXG", "XXX", "XXX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//陷阱箱
        Item effectHealth45 = Item.get(Item.POTION, ItemPotion.REGENERATION, 1);
        effectHealth45.setCustomName(DEFAULT_TITLE + "§f生命恢复 45秒");
        bedWarsComposes.add(new BigShapedRecipe(
                effectHealth45, "GGG", "XGX", "XXX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//生命恢复45s
        Item effectHealth120 = Item.get(Item.POTION, ItemPotion.REGENERATION_LONG, 1);
        effectHealth120.setCustomName(DEFAULT_TITLE + "§f生命恢复 120秒");
        bedWarsComposes.add(new BigShapedRecipe(
                effectHealth120, "GGG", "GGG", "XXX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//生命恢复120s
        Item effectSpeed180 = Item.get(Item.POTION, ItemPotion.SPEED_LONG, 1);
        effectSpeed180.setCustomName(DEFAULT_TITLE + "§f速度 180秒");
        bedWarsComposes.add(new BigShapedRecipe(
                effectSpeed180, "SSS", "GGG", "SXS"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)).setIngredient("G",gold));//速度3m
        Item goldSword = Item.get(Item.GOLD_SWORD, 0, 1);
        Item ironSword = Item.get(Item.IRON_SWORD, 0, 1);
        goldSword.setCustomName(DEFAULT_TITLE + "§f金剑");
        ironSword.setCustomName(DEFAULT_TITLE + "§f铁剑");
        bedWarsComposes.add(new BigShapedRecipe(
                goldSword, "XGX", "GGG", "XGX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//金剑
        bedWarsComposes.add(new BigShapedRecipe(
                ironSword, "XGX", "GGG", "XGX"
        ).setIngredient("G", silver).setIngredient("X", Item.get(0)));//铁剑
        Item steak = Item.get(Item.STEAK, 0, 1);
        steak.setCustomName(DEFAULT_TITLE + "§f熟牛肉");
        bedWarsComposes.add(new BigShapedRecipe(
                steak, "CXX", "XCX", "XXC"
        ).setIngredient("C", copper).setIngredient("X", Item.get(0)));//牛肉
        Item goldenApple = Item.get(Item.GOLDEN_APPLE);
        goldenApple.setCustomName(DEFAULT_TITLE + "§f金苹果");
        bedWarsComposes.add(new BigShapedRecipe(
                goldenApple, "SSS", "SXS", "SSS"
        ).setIngredient("S", gold).setIngredient("X", Item.get(0)));//金苹果
        Item goldPickAxe = Item.get(Item.GOLD_PICKAXE, 0, 1);
        goldPickAxe.setCustomName(DEFAULT_TITLE + "§f金镐");
        Item ironPickAxe = Item.get(Item.IRON_PICKAXE, 0, 1);
        ironPickAxe.setCustomName(DEFAULT_TITLE + "§f铁镐");
        Item stonePickAxe = Item.get(Item.STONE_PICKAXE, 0, 1);
        stonePickAxe.setCustomName(DEFAULT_TITLE + "§f石镐");
        bedWarsComposes.add(new BigShapedRecipe(
                stonePickAxe, "CCC", "XCX", "XCX"
        ).setIngredient("C", copper).setIngredient("X", Item.get(0)));//石镐
        bedWarsComposes.add(new BigShapedRecipe(
                ironPickAxe, "SSS", "XSX", "XSX"
        ).setIngredient("S", silver).setIngredient("X", Item.get(0)));//铁镐
        bedWarsComposes.add(new BigShapedRecipe(
                goldSword, "GGG", "XGX", "XGX"
        ).setIngredient("G", gold).setIngredient("X", Item.get(0)));//金镐
        {
            Item cap = Item.get(Item.LEATHER_CAP, 0, 1);
            cap.setCustomName(DEFAULT_TITLE + "§f皮革头盔");
            Item tunic = Item.get(Item.LEATHER_TUNIC, 0, 1);
            tunic.setCustomName(DEFAULT_TITLE + "§f皮革甲");
            Item pants = Item.get(Item.LEATHER_PANTS, 0, 1);
            pants.setCustomName(DEFAULT_TITLE + "§f皮革裤");
            Item boots = Item.get(Item.LEATHER_BOOTS, 0, 1);
            boots.setCustomName(DEFAULT_TITLE + "§f皮革鞋");
            bedWarsComposes.add(new BigShapedRecipe(
                    cap, "CCC", "CXC", "XXX"
            ).setIngredient("C", copper).setIngredient("X", Item.get(0)));
            bedWarsComposes.add(new BigShapedRecipe(
                    tunic, "CXC", "CCC", "CCC"
            ).setIngredient("C", copper).setIngredient("X", Item.get(0)));
            bedWarsComposes.add(new BigShapedRecipe(
                    pants, "CCC", "CXC", "CXC"
            ).setIngredient("C", copper).setIngredient("X", Item.get(0)));
            bedWarsComposes.add(new BigShapedRecipe(
                    boots, "XXX", "CXC", "CXC"
            ).setIngredient("C", copper).setIngredient("X", Item.get(0)));
        }
        craftingDataPacket.cleanRecipes = true;
        for (ShapedRecipe recipe : bedWarsComposes) {
            this.getServer().getCraftingManager().registerRecipe(recipe);
            craftingDataPacket.addShapedRecipe(recipe);
        }
    }
}
