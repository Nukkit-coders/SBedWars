package net.mcpes.summit.hhm.bedwars.listener;

import cn.nukkit.block.BlockAir;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import net.mcpes.summit.hhm.bedwars.SBedWars;
import net.mcpes.summit.hhm.bedwars.SBedWarsAPI;
import net.mcpes.summit.hhm.bedwars.game.BedWars;

import static net.mcpes.summit.hhm.bedwars.SBedWars.*;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class SignListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void SignTouch(PlayerInteractEvent event) {
        if (event.getBlock().getId() == 323 || event.getBlock().getId() == 63 || event.getBlock().getId() == 68) {
            Location l = event.getBlock().getLocation();
            String pos = (int) l.getX() + ":" + (int) l.getY() + ":" + (int) l.getZ() + ":" + l.getLevel().getName();
            int room = SBedWarsAPI.getInstance().isSign(pos);
            if (room != -1) {
                if (SBedWarsAPI.getInstance().isPlayerGaming(event.getPlayer().getName()) == -1) {
                    if (touch.containsKey(event.getPlayer().getName()) && touch.get(event.getPlayer().getName()).equals(pos)) {
                        touch.remove(event.getPlayer().getName());
                        if (SBedWars.games.containsKey(room)) {
                            if (games.get(room).getGameMode() < 3) {
                                SBedWars.games.get(room).onJoin(event.getPlayer());
                            } else {
                                event.getPlayer().sendMessage(DEFAULT_TITLE + "§c游戏已经开始!");
                            }
                        } else {
                            new BedWars(room).onJoin(event.getPlayer());
                        }
                    } else {
                        event.getPlayer().sendMessage(DEFAULT_TITLE + "§c请再次点击牌子");
                        touch.put(event.getPlayer().getName(), pos);
                    }
                } else {
                    event.getPlayer().sendMessage(DEFAULT_TITLE + "§6你正在游戏中");
                }
            } else {
                if (!event.getPlayer().isOp()) return;
                BlockEntitySign sign = (BlockEntitySign) event.getBlock().getLevel().getBlockEntity(event.getBlock().getLocation());
                if (sign.getText()[0].equals("bw") && sign.getText()[1].matches("^[0-9]*[1-9][0-9]*$")) {
                    int id = Integer.valueOf(sign.getText()[1]);
                    if (rooms.containsKey(id)) {
                        SBedWarsAPI.getInstance().addSign(pos, id);
                        SBedWarsAPI.getInstance().resetSign(id, event.getBlock().getLocation());
                        event.getPlayer().sendMessage(DEFAULT_TITLE + "§6成功添加" + id + "号房间的进入木牌！");
                    } else {
                        event.getPlayer().sendMessage(DEFAULT_TITLE + "§c此房间不存在");
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void SignBreak(BlockBreakEvent event) {
        if (event.getBlock().getId() == 323 || event.getBlock().getId() == 63 || event.getBlock().getId() == 68) {
            Location l = event.getBlock().getLocation();
            String pos = (int) l.getX() + ":" + (int) l.getY() + ":" + (int) l.getZ() + ":" + l.getLevel().getName();
            int room = SBedWarsAPI.getInstance().isSign(pos);
            if (room != -1) {
                event.setCancelled(true);
                event.getBlock().getLevel().setBlock(event.getBlock().getLocation(), new BlockAir());
                SBedWarsAPI.getInstance().delSign(pos, room);
                event.getPlayer().sendMessage(DEFAULT_TITLE + "§6成功删除" + room + "号房间的进入木牌");
            }
        }
    }
}
