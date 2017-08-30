package net.mcpes.summit.hhm.bedwars;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.item.Item;
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
import java.util.*;

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
    public static HashMap<Integer, HashMap<String, HashSet<Player>>> players = new HashMap<Integer, HashMap<String, HashSet<Player>>>();
    public static HashMap<String, Integer> gaming = new HashMap<>();
    public static HashMap<String, String> touch = new HashMap<>();
    public static HashMap<String, RoomData> add = new HashMap<>();
    public static CraftingDataPacket craftingDataPacket = new CraftingDataPacket();
    public static Item gold;
    public static Item silver;
    public static Item diamond;
    public static Item emerald;
    private static SBedWars instance;
    public static Item[] showItem = new Item[4];

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
        diamond = Item.get(264, 0, 1);
        diamond.setCustomName(DEFAULT_TITLE + "§b钻石");
        emerald = Item.get(388, 0, 1);
        emerald.setCustomName(DEFAULT_TITLE + "§2绿宝石");
        showItem[0] = gold.setCustomBlockData(new CompoundTag().putBoolean("show", true));
        showItem[1] = silver.setCustomBlockData(new CompoundTag().putBoolean("show", true));
        showItem[2] = diamond.setCustomBlockData(new CompoundTag().putBoolean("show", true));
        showItem[3] = emerald.setCustomBlockData(new CompoundTag().putBoolean("show", true));
        gold = Item.get(266, 0, 1);
        gold.setCustomName(DEFAULT_TITLE + "§e金");
        silver = Item.get(265, 0, 1);
        silver.setCustomName(DEFAULT_TITLE + "§f银");
        diamond = Item.get(264, 0, 1);
        diamond.setCustomName(DEFAULT_TITLE + "§b钻石");
        emerald = Item.get(388, 0, 1);
        emerald.setCustomName(DEFAULT_TITLE + "§2绿宝石");
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
                data.setSignPos(new HashSet<>((ArrayList) cr.getConfig().get("sign-pos")));
                data.setGoldPos(new HashSet<>((ArrayList) cr.getConfig().get("gold-pos")));
                data.setSilverPos(new HashSet<>((ArrayList) cr.getConfig().get("silver-pos")));
                data.setDiamondPos(new HashSet<>((ArrayList) cr.getConfig().get("diamond-pos")));
                data.setEmeraldPos(new HashSet<>((ArrayList) cr.getConfig().get("emerald-pos")));
                data.setGameLevel();
                data.setGoldLocation();
                data.setSignLocation();
                data.setWaitLocation();
                data.setStopLocation();
                data.setSilverLocation();
                data.setEmeraldLocation();
                data.setDiamondeLocation();
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
    public void broadcastMessage(Set<Player> p, String msg) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + msg);
            }
        }
    }

    @Override
    public void broadcastMessage2(Set<Player> p, String msg, Player player) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + " "+player.getName() + " >> " +msg);
            }
        }
    }

    @Override
    public void broadcastMessage3(Set<Player> p, String msg, Player player) {
        for (Player pl : p) {
            if (pl.isOnline()) {
                pl.sendMessage(DEFAULT_TITLE + TextFormat.colorize("&e大喊 &6>> &d队伍: &b"+rooms.get(SBedWarsAPI.getInstance().isPlayerGaming(player.getName())).getTeamData().get(games.get(SBedWarsAPI.getInstance().isPlayerGaming(player.getName())).getTeam(player.getName())).get("name")+" &e&l"+player.getName() + " &6>> &4" +msg));
            }
        }
    }

    @Override
    public void broadcastTitle(Set<Player> p, int fadeIn, int stay, int fadeOut, String msg, String twoMsg) {
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
    public void broadcastSound(Set<Player> p, int type) {
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
    public void broadcastTip(Set<Player> p, String msg) {
        for (Player pl : p) {
            if (pl.isOnline())
                pl.sendTip(msg);
        }
    }

    @Override
    public void broadcastSpeak(Set<Player> p, String pn, String msg) {
        for (Player pl : p) {
            pl.sendMessage(DEFAULT_TITLE + "§7<§b" + pn + "§7>§6: " + msg);
        }
    }

    @Override
    public void broadcastTeamSpeak(Set<String> p, String pn, String msg) {
        for (String name : p) {
            Player pl = this.getServer().getPlayerExact(name);
            pl.sendMessage(DEFAULT_TITLE + "§7<§b" + pn + "§7>§6: " + msg);
        }
    }

    @Override
    public void broadcastTeamMessage(Set<String> p, String msg) {
        Set<Player> players = new HashSet<>();
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
        HashSet<String> sign = (HashSet<String>) cr.getConfig().get("sign-pos");
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
        HashSet<String> sign = (HashSet<String>) cr.getConfig().get("sign-pos");
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
}
