package net.mcpes.summit.hhm.bedwars.config;

import cn.nukkit.utils.SimpleConfig;
import net.mcpes.summit.hhm.bedwars.SBedWars;

/**
 * @author hhm
 * @date 2017/7/22
 * @since SBedWars
 */

public class MasterConfig extends SimpleConfig {
    @Path(value = "保存背包")
    public boolean saveBag = false;

    public MasterConfig() {
        super(SBedWars.getInstance());
    }
}
