package net.mcpes.summit.hhm.bedwars.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import net.mcpes.summit.hhm.bedwars.SBedWars;
import net.mcpes.summit.hhm.bedwars.SBedWarsAPI;
import net.mcpes.summit.hhm.bedwars.config.RoomConfig;
import net.mcpes.summit.hhm.bedwars.data.RoomData;
import net.mcpes.summit.hhm.bedwars.game.BedWars;
import net.mcpes.summit.hhm.bedwars.utils.FileFunction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static net.mcpes.summit.hhm.bedwars.SBedWars.*;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public class BedWarsCommand extends Command {
    public BedWarsCommand() {
        super("bw");
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(DEFAULT_TITLE + "你不是一个玩家!");
            return false;
        }
        Player player = (Player) sender;
        if (strings.length == 0) {
            sender.sendMessage(DEFAULT_TITLE + "输入错误,请输入/bw help 查看帮助");
            return false;
        }
        int id;
        switch (strings[0]) {
            case "join":
                if (strings.length != 2) {
                    sender.sendMessage(DEFAULT_TITLE + "输入错误,请输入/bw help 查看帮助");
                    return false;
                }
                id = SBedWarsAPI.getInstance().isPlayerGaming(player.getName());
                if (id != -1) {
                    sender.sendMessage(DEFAULT_TITLE + "你已经在游戏中了,无法退出");
                    return false;
                }
                try {
                    id = Integer.valueOf(strings[1]);
                    if (!rooms.containsKey(id)) {
                        sender.sendMessage(DEFAULT_TITLE + "没有此房间,请确保输入正确");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(DEFAULT_TITLE + "输入错误,ID必须为一个整数");
                    return false;
                }
                if (!games.containsKey(id)) {
                    new BedWars(id).onJoin(player);
                }
                if (games.get(id).getGameMode() <= 2) {
                    games.get(id).onJoin(player);
                }
                return true;
            case "quit":
                id = SBedWarsAPI.getInstance().isPlayerGaming(player.getName());
                if (id != -1) {
                    games.get(id).onQuit(player, true);
                    return true;
                } else {
                    sender.sendMessage(DEFAULT_TITLE + "你不在游戏中,无法退出");
                }
                break;
            case "help":
                sender.sendMessage(TextFormat.YELLOW+"====SBed War====");
                sender.sendMessage(TextFormat.GREEN+"help - 查询帮助");
                sender.sendMessage(TextFormat.GREEN+"join <房间id> - 加入房间");
                sender.sendMessage(TextFormat.GREEN+"quit - 退出房间");
                sender.sendMessage(TextFormat.GREEN+"set - 设置房间(支持多房间)");
                break;
            case "set":
                if (!sender.isOp()) {
                    sender.sendMessage(DEFAULT_TITLE + "你不是OP");
                    return false;
                }
                if (add.containsKey(sender.getName())) {
                    RoomData data = add.get(sender.getName());
                    switch (strings.length) {
                        case 1:
                            switch (data.getSet()) {
                                case 1:
                                    data.setWaitPos(((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                    data.setSet(2);
                                    sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set 来设置结束地点");
                                    break;
                                case 2:
                                    data.setStopPos(((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                    data.setSet(3);
                                    sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set team <队伍数量> 来设置队伍数量");
                                    break;
                            }
                            return true;
                        case 2:
                            switch (strings[1]) {
                                case "team":
                                    switch (data.getSet()) {
                                        case 4:
                                            HashMap<Integer, HashMap<String, Object>> team = data.getTeamData();
                                            if (team.containsKey(data.getCanTeam())) {
                                                HashMap<String, Object> td = team.get(data.getCanTeam());
                                                if (!td.containsKey("name")) {
                                                    if (td.containsKey("shopPos")) {
                                                        sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步是输入/bw set team name <队伍名字>来设置第" + (data.getCanTeam() + 1) + "个队伍的名字");
                                                    }
                                                    if (td.containsKey("bedPos")) {
                                                        td.put("shopPos", ((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                                        team.put(data.getCanTeam(), td);
                                                        data.setTeamData(team, 2);
                                                        sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set team name <队伍名字>来设置第" + (data.getCanTeam() + 1) + "个队伍的名字");
                                                    }
                                                }
                                            } else {
                                                if (Server.getInstance().getDefaultLevel().getName().equals(player.getLevel().getName())) {
                                                    sender.sendMessage(DEFAULT_TITLE + "游戏世界不能为主世界!");
                                                    return false;
                                                }
                                                HashMap<String, Object> td = new HashMap<>();
                                                data.setGameWorld(player.getLevel().getName());
                                                td.put("gamePos", ((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                                team.put(data.getCanTeam(), td);
                                                data.setTeamData(team, 2);
                                                sender.sendMessage(DEFAULT_TITLE + "成功,接下来请点击核心（床方块或其他）来设置第" + (data.getCanTeam() + 1) + "个队伍的床地点");
                                            }
                                            break;
                                        default:
                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                            break;
                                    }
                                    break;
                                case "gold":
                                    switch (data.getSet()) {
                                        case 5:
                                            ArrayList<String> gold = data.getGoldPos();
                                            gold.add(((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                            data.setGoldPos(gold);
                                            sender.sendMessage(DEFAULT_TITLE + "成功!如果想继续设置金的掉落请继续输入/bw set gold ,否则请输入/bw set silver 来设置银的掉落!");
                                            break;
                                        default:
                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                            break;
                                    }
                                    break;
                                case "silver":
                                    switch (data.getSet()) {
                                        case 5:
                                            ArrayList<String> silver = data.getSilverPos();
                                            silver.add(((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                            data.setSilverPos(silver);
                                            sender.sendMessage(DEFAULT_TITLE + "成功!如果想继续设置银的掉落请继续输入/bw set silver ,否则请输入/bw set copper 来设置铜的掉落!");
                                            break;
                                        default:
                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                            break;
                                    }
                                    break;
                                case "copper":
                                    switch (data.getSet()) {
                                        case 5:
                                            ArrayList<String> copper = data.getCopperPos();
                                            copper.add(((int) player.getX()) + ":" + ((int) player.getY()) + ":" + ((int) player.getZ()) + ":" + player.getLevel().getName());
                                            data.setCopperPos(copper);
                                            sender.sendMessage(DEFAULT_TITLE + "成功!如果想继续设置铜的掉落请继续输入/bw set copper ,否则请输入/bw set type <1/2> 设置游戏模式,1为金银铜掉落物品兑换,2为经验模式,现在为测试阶段,只有1可用");
                                            break;
                                        default:
                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                            break;
                                    }
                                    break;
                            }
                            break;
                        case 3:
                            switch (strings[1]) {
                                case "type":
                                    switch (data.getSet()) {
                                        case 5:
                                            try {
                                                int type = Integer.valueOf(strings[2]);
                                                if (type != 1 && type != 2) {
                                                    sender.sendMessage(DEFAULT_TITLE + "你输入的数字不为1或2!");
                                                    return false;
                                                }
                                                data.setResourcesType(type);
                                                int rid = data.getId();
                                                try {
                                                    RoomConfig rc = new RoomConfig(rid);
                                                    rc.addRoom(data);
                                                }catch (NoClassDefFoundError e){
                                                    e.printStackTrace();
                                                }
                                                add.remove(sender.getName());
                                                try {
                                                    Server.getInstance().unloadLevel(Server.getInstance().getLevelByName(data.getGameWorld()));
                                                    FileFunction.copy(Server.getInstance().getDataPath() + "/worlds/" + data.getGameWorld() + "/", new File(SBedWars.getInstance().getDataFolder() + "/worlds/" + data.getGameWorld() + "/").getCanonicalPath());
                                                    FileFunction.remove(new File(Server.getInstance().getDataPath() + "/worlds/" + data.getGameWorld() + "/"));
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                sender.sendMessage(DEFAULT_TITLE + "成功设置" + rid + "号房间!,请在插件配置文件的" + rid + ".yml中进行后续的设置!");
                                            } catch (NumberFormatException e) {
                                                sender.sendMessage(DEFAULT_TITLE + "你输入的数字不为1或2!");
                                            }
                                            break;
                                        default:
                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                            break;
                                    }
                                    break;
                                case "team":
                                    switch (data.getSet()) {
                                        case 3:
                                            try {
                                                data.setTeamCount(Integer.valueOf(strings[2]));
                                            } catch (NumberFormatException e) {
                                                sender.sendMessage(DEFAULT_TITLE + "你输入的数量不是一个正整数,要求为一个正整数并且大于等于2");
                                            }
                                            data.setSet(4);
                                            data.setCanTeam(0);
                                            sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set team 来设置第1个队伍的出生点");
                                            break;
                                    }
                                    break;
                            }
                            break;
                        case 4:
                            switch (strings[1]) {
                                case "team":
                                    switch (data.getSet()) {
                                        case 4:
                                            switch (strings[2]) {
                                                case "name"://bw set team name <队伍名字>
                                                    HashMap<Integer, HashMap<String, Object>> team = data.getTeamData();
                                                    if (team.containsKey(data.getCanTeam())) {
                                                        HashMap<String, Object> td = team.get(data.getCanTeam());
                                                        if (td.containsKey("shopPos")) {
                                                            td.put("name", strings[3]);
                                                            team.put(data.getCanTeam(), td);
                                                            if (data.getCanTeam() + 1 < data.getTeamCount()) {
                                                                data.setCanTeam(data.getCanTeam() + 1);
                                                                sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set team 来设置第" + (data.getCanTeam() + 1) + "个队伍的出生点");
                                                            } else {
                                                                sender.sendMessage(DEFAULT_TITLE + "成功,队伍信息全部设置完毕!接下来请输入/bw set gold 来设置金的掉落地点!");
                                                                data.setSet(5);
                                                            }
                                                            data.setTeamData(team, 2);
                                                        } else {
                                                            sender.sendMessage(DEFAULT_TITLE + "失败!你的下一步并不是这个");
                                                        }
                                                    }
                                                    break;
                                            }
                                            break;
                                    }
                                    break;
                            }
                            break;
                    }

                } else {
                    RoomData data = new RoomData(SBedWarsAPI.getInstance().getFreeID());
                    data.setSet(1);
                    add.put(sender.getName(), data);
                    sender.sendMessage(DEFAULT_TITLE + "成功,接下来请输入/bw set 来设置等待地点");
                }
                break;
        }
        return false;
    }
}
