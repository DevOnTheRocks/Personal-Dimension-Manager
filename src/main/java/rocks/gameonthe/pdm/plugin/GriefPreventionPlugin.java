package rocks.gameonthe.pdm.plugin;

import com.google.common.collect.Lists;
import me.ryanhamshire.griefprevention.GriefPrevention;
import me.ryanhamshire.griefprevention.api.GriefPreventionApi;
import me.ryanhamshire.griefprevention.api.claim.ClaimManager;
import me.ryanhamshire.griefprevention.api.claim.TrustType;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class GriefPreventionPlugin {

  private final PersonalDimManager plugin;
  private final GriefPreventionApi gp;

  public GriefPreventionPlugin(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.gp = GriefPrevention.getApi();
  }

  public void setTrust(PersonalDimension dimension) {
    dimension.getWorld().ifPresent(world -> {
      ClaimManager claimManager = gp.getClaimManager(world);
      claimManager.getWildernessClaim().transferOwner(dimension.owner);
      claimManager.getWildernessClaim().addUserTrusts(Lists.newArrayList(dimension.members), TrustType.BUILDER);
    });
  }
}
