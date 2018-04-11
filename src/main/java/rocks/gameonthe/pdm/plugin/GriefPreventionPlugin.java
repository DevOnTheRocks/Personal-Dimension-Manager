package rocks.gameonthe.pdm.plugin;

import java.time.Instant;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import org.spongepowered.api.world.World;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class GriefPreventionPlugin {

  private final PersonalDimManager plugin;
  private final GriefPreventionApi gp;

  public GriefPreventionPlugin(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.gp = GriefPrevention.getApi();
  }

  public void fixDateCreated(World world) {
    PersonalDimension dim = plugin.getDimensionManager().getDimensions().get(world.getUniqueId());
    Instant instant = gp.getClaimManager(world).getWildernessClaim().getData().getDateCreated();
    if (dim != null && (Instant.ofEpochSecond(dim.created).isAfter(instant) || dim.created == 0)) {
      dim.created = instant.getEpochSecond();
      plugin.getConfigManager().save();
    }
  }

}
