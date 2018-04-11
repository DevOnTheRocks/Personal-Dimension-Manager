package rocks.gameonthe.pdm.listener;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.World;
import rocks.gameonthe.pdm.PersonalDimManager;

public class WorldListener {

  private final PersonalDimManager plugin;

  public WorldListener(PersonalDimManager plugin) {
    this.plugin = plugin;
  }

  @Listener
  public void onWorldLoad(LoadWorldEvent event, @Getter("getTargetWorld") World world) {
    // Don't run for world not managed by this plugin
    if (!plugin.getDimensionManager().getDimensions().containsKey(world.getUniqueId())) {
      return;
    }

    plugin.getGriefPrevention().ifPresent(gp -> gp.fixDateCreated(world));

  }

  @Listener
  public void onWorldUnload(UnloadWorldEvent event, @Getter("getTargetWorld") World world) {
    // Don't run for world not managed by this plugin
    if (!plugin.getDimensionManager().getDimensions().containsKey(world.getUniqueId())) {
      return;
    }

    // Do stuff
  }

}
