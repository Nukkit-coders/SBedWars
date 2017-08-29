package net.mcpes.summit.hhm.bedwars;

import cn.nukkit.Player;
import cn.nukkit.level.Location;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author hhm
 * @date 2017/7/13
 * @since SBedWars
 */

public interface SBedWarsAPI {
    static SBedWarsAPI getInstance() {
        return SBedWars.getInstance();
    }

    void loadAllRoom();

    boolean loadRoom(int id);

    int getFreeID();

    void loadWorlds();

    void loadWorld(String worldName) throws IOException;

    void broadcastMessage(ArrayList<Player> p, String msg);

    void broadcastMessage2(ArrayList<Player> p, String msg,Player player);

    void broadcastMessage3(ArrayList<Player> p, String msg,Player player);

    void broadcastTitle(ArrayList<Player> p, int fadeIn, int stay, int fadeOut, String msg, String twoMsg);

    void broadcastSound(ArrayList<Player> p, int type);

    void broadcastTip(ArrayList<Player> p, String msg);

    void broadcastSpeak(ArrayList<Player> p, String pn, String msg);

    void broadcastTeamSpeak(ArrayList<String> p, String pn, String msg);

    void broadcastTeamMessage(ArrayList<String> p, String msg);

    int isPlayerGaming(String name);

    int isSign(String pos);

    void addSign(String pos, int id);

    void resetSign(int id, Location pos);

    void delSign(String pos, int id);
}