package net.mcpes.summit.hhm.bedwars.listener;

import cn.nukkit.Player;
import cn.nukkit.block.BlockTrappedChest;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.inventory.CraftItemEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemClay;
import cn.nukkit.level.Location;
import cn.nukkit.level.sound.AnvilFallSound;
import cn.nukkit.utils.TextFormat;
import net.mcpes.summit.hhm.bedwars.SBedWars;
import net.mcpes.summit.hhm.bedwars.SBedWarsAPI;
import net.mcpes.summit.hhm.bedwars.game.BedWars;

import java.util.HashMap;

import static net.mcpes.summit.hhm.bedwars.SBedWars.*;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class PlayerListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onQuit(PlayerQuitEvent event) {
        int id = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (id != -1) {
            //TODO:bag
            games.get(id).onQuit(event.getPlayer(), false);
        }

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        event.getPlayer().setCheckMovement(false);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onChat(PlayerChatEvent event) {
        int id = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (id != -1) {
            event.setCancelled(true);
            if (event.getMessage().substring(0, 1).equals("!")) {
                SBedWarsAPI.getInstance().broadcastSpeak(games.get(id).getAllPlayers(), event.getPlayer().getName(), event.getMessage().substring(1, event.getMessage().length()));
            } else {
                try {
                    if(event.getMessage().contains("@")){
                        SBedWarsAPI.getInstance().broadcastMessage3(games.get(id).getAllPlayers(),event.getMessage(),event.getPlayer());
                        return;
                    }
                    SBedWarsAPI.getInstance().broadcastTeamSpeak(games.get(id).getTeam(games.get(id).getTeam(event.getPlayer().getName())), event.getPlayer().getName(), event.getMessage());
                }catch (NullPointerException e){
                    SBedWarsAPI.getInstance().broadcastMessage2(games.get(id).getAllPlayers(),event.getMessage(),event.getPlayer());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onBlockBreak(BlockBreakEvent event) {
        String n = event.getPlayer().getName();
        int i = SBedWarsAPI.getInstance().isPlayerGaming(n);
        if (i != -1) {
            if (games.get(i).getGameMode() != 3) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(DEFAULT_TITLE + "§6非游戏开始时不允许破坏方块！");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onImmuneDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            if(event.getDamager().getLevel().getFolderName().equals("lobby")){
                event.setCancelled();
                return;
            }
            Player entity = (Player) event.getEntity();//防御者
            Player damager = (Player) event.getDamager();
            int a = SBedWarsAPI.getInstance().isPlayerGaming(entity.getName());
            int b = SBedWarsAPI.getInstance().isPlayerGaming(damager.getName());
            if (a != -1 || b != -1) {
                if (games.get(a).getGameMode() != 3 || games.get(b).getGameMode() != 3) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    damager.sendMessage(DEFAULT_TITLE + "§6非开始时不准进行攻击！");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName()) != -1) {
            if (!event.getPlayer().isOp()) {
                if (event.getMessage().length() < 4 && (!event.getMessage().equals("/bw"))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(DEFAULT_TITLE + "§6游戏内不允许使用/bw 之外的其他所有命令");
                    return;
                }
                if ((!event.getMessage().substring(0, 4).equals("/bw "))) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(DEFAULT_TITLE + "§6游戏内不允许使用/bw 之外的其他所有命令");
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Item inHand = event.getPlayer().getInventory().getItemInHand();
        if (!(inHand instanceof ItemClay)) return;
        if (event.getAction() != PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (room <= 0) return;
        if (games.get(room).getGameMode() != 3) return;
        BedWars game = games.get(room);
        event.getPlayer().getInventory().removeItem(inHand);
        event.getPlayer().teleport((Location) rooms.get(room).getTeamData().get(game.getTeam(event.getPlayer().getName())).get("gameLocation"));
        event.getPlayer().sendMessage(DEFAULT_TITLE + "你成功使用了回城火药!");
    }

    @EventHandler
    public void onBreakCore(BlockBreakEvent event) {
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (room <= 0) return;
        if (games.get(room).getGameMode() != 3) return;
        int playerTeam = games.get(room).getTeam(event.getPlayer().getName());
        int coreTeam = -1;
        for (Integer teamID : rooms.get(room).getTeamData().keySet()) {
            Location block = event.getBlock().getLocation();
            Location core = (Location) rooms.get(room).getTeamData().get(teamID).get("bedLocation");
            String pos1 = (int) block.x + ":" + ((int) block.y) + ":" + (int) block.z + ":" + block.level.getName();
            String pos2 = (int) core.x + ":" + (int) core.y + ":" + (int) core.z + ":" + core.level.getName();
            if (pos1.equals(pos2)) {
                coreTeam = teamID;
                break;
            }
        }
        if (coreTeam == playerTeam) {
            event.getPlayer().sendMessage(DEFAULT_TITLE + "你不能破坏你自己家里的核心");
            event.setCancelled(true);
            return;
        }
        if (coreTeam != -1) {
            rooms.get(room).getTeamData().get(coreTeam).put("kill", true);
            event.setDrops(null);
            SBedWarsAPI.getInstance().broadcastTitle(games.get(room).getAllPlayers(), 0, 50 , 0, "", TextFormat.colorize("&a队伍: &d" + rooms.get(room).getTeamData().get(playerTeam).get("name") + "&b的核心被破坏"));
            SBedWarsAPI.getInstance().broadcastMessage(games.get(room).getAllPlayers(), "队伍:" + rooms.get(room).getTeamData().get(playerTeam).get("name") + "的核心被破坏,此队伍将不可重生");
        }
    }

    @EventHandler
    public void onBreakChest(BlockBreakEvent event) {
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (room <= 0) return;
        if (games.get(room).getGameMode() != 3) return;
        if (!(event.getBlock() instanceof BlockTrappedChest)) return;
        BlockTrappedChest block = (BlockTrappedChest) event.getBlock();
        int playerTeam = games.get(room).getTeam(event.getPlayer().getName());
        String pos = block.getLocation().toString();
        if (!games.get(room).chestData.containsKey(pos)) return;
        int chestTeam = games.get(room).chestData.get(pos);
        if (playerTeam == chestTeam) return;
        SBedWarsAPI.getInstance().broadcastTeamMessage(games.get(room).teams.get(chestTeam), "基地的陷阱箱被破坏了,是不是有人在偷城?请回去查看!");
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onCraft(CraftItemEvent event){
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (room <= 0) return;
        if (games.get(room).getGameMode() != 3) return;
        event.getPlayer().getLevel().addSound(new AnvilFallSound(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDie(PlayerDeathEvent event) {
        String name = event.getEntity().getName();
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getEntity().getName());
        if (room <= 0) return;
        if (games.get(room).getGameMode() != 3) return;
        if (event.getEntity().getKiller() != null) {
            event.getEntity().getKiller().getLevel().addSound(new AnvilFallSound(event.getEntity().getKiller()));
            SBedWarsAPI.getInstance().broadcastMessage(games.get(room).getAllPlayers(), "玩家" + name + "被" + event.getEntity().getKiller().getName() + "杀死了");
        } else {
            SBedWarsAPI.getInstance().broadcastMessage(games.get(room).getAllPlayers(), "玩家" + name + "死了");
        }
        int playerTeam = games.get(room).getTeam(event.getEntity().getName());
        HashMap<String, Object> teamData = rooms.get(room).getTeamData().get(playerTeam);
        if (teamData.containsKey("kill") && (boolean) teamData.get("kill")) {
            SBedWarsAPI.getInstance().broadcastMessage(games.get(room).getAllPlayers(), "最终击杀！");
            event.getEntity().sendMessage(DEFAULT_TITLE + "你的队伍核心不在,你不能够重生!");
            event.getEntity().getInventory().clearAll();
            event.setCancelled(true);
            games.get(room).onQuit(event.getEntity(), true);
        } else {
            event.getEntity().sendMessage(DEFAULT_TITLE + "重生!");
            event.setKeepInventory(false);
            event.getEntity().getInventory().clearAll();
        }
        for (Item item : event.getDrops()) {
            if(item.getId() == 265 || item.getId() == 266 || item.getId() == 336) {
                event.getEntity().level.dropItem(event.getEntity().getLocation(), item);
            }
        }
        event.setDrops(new Item[]{});
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if ((!(event.getEntity() instanceof Player)) || (!(event.getDamager() instanceof Player))) return;
        Item helmet = ((Player) event.getEntity()).getInventory().getHelmet();
        Item boots = ((Player) event.getEntity()).getInventory().getBoots();
        if (helmet == null || boots == null || helmet.getCustomBlockData() == null || boots.getCustomBlockData() == null)
            return;
        if (helmet.getCustomBlockData().contains("bedWars") && helmet.getCustomBlockData().getString("bedWars").equals("ironHelmet") && boots.getCustomBlockData().contains("bedWars") && boots.getCustomBlockData().getString("bedWars").equals("ironBoots")) {
            event.setKnockBack(0);
        }
    }

    @EventHandler
    public void onPick(PlayerItemHeldEvent event) {
        int room = SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName());
        if (room != -1) {
            boolean flag = false;
            for (Item item : SBedWars.showItem) {
                if (event.getItem() == item) flag = true;
            }
            if (flag) {
                event.setCancelled(true);
            }
        }

    }
}
