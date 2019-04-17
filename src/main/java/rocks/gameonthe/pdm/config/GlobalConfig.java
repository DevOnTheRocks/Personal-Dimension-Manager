package rocks.gameonthe.pdm.config;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.Dimension;
import org.spongepowered.api.world.World;
import rocks.gameonthe.pdm.data.DimensionPreset;

@ConfigSerializable
public class GlobalConfig {

  @Setting("spawn-world")
  private String spawn = "world";

  @Setting("world-template")
  private String template = "DIM-1";

  @Setting("world-presets")
  private List<DimensionPreset> presets = Lists.newArrayList();

  public Optional<World> getSpawn() {
    return Sponge.getServer().getWorld(spawn);
  }

  public void setSpawn(World world) {
    this.spawn = world.getName();
  }

  public Optional<World> getTemplate() {
    return Sponge.getServer().getWorld(template);
  }

  public void setTemplate(World world) {
    this.template = world.getName();
  }

  public List<DimensionPreset> getPresets() {
    return presets;
  }
}
