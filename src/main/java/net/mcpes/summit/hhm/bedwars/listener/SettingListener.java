package net.mcpes.summit.hhm.bedwars.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Location;
import net.mcpes.summit.hhm.bedwars.data.RoomData;

import java.util.HashMap;

import static net.mcpes.summit.hhm.bedwars.SBedWars.DEFAULT_TITLE;
import static net.mcpes.summit.hhm.bedwars.SBedWars.add;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class SettingListener implements Listener {
    @EventHandler
    public void BedTouch(PlayerInteractEvent event) {
        if (!add.containsKey(event.getPlayer().getName())) return;
        RoomData data = add.get(event.getPlayer().getName());
        if (data.getSet() != 4) return;
        Location l = event.getBlock().getLocation();
        String pos = (int) l.getX() + ":" + (int) l.getY() + ":" + (int) l.getZ() + ":" + l.getLevel().getName();
        HashMap<Integer, HashMap<String, Object>> team = data.getTeamData();
        if (team.containsKey(data.getCanTeam())) {
            HashMap<String, Object> td = team.get(data.getCanTeam());
            if (!td.containsKey("name")) {
                if (td.containsKey("shopPos") || td.containsKey("bedPos")) {
                    event.getPlayer().sendMessage(DEFAULT_TITLE + "失败!你的下一步不是这个");
                    return;
                }
                td.put("bedPos", pos);
                team.put(data.getCanTeam(), td);
                data.setTeamData(team, 2);
                event.getPlayer().sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set team 来设置第" + (data.getCanTeam() + 1) + "个队伍的商店地点（工作台）");
            }
        } else event.getPlayer().sendMessage(DEFAULT_TITLE + "失败!你的下一步不是这个");
    }
}
