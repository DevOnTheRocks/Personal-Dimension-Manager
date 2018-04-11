package rocks.gameonthe.pdm.config;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.world.World;
import rocks.gameonthe.pdm.data.PersonalDimension;

@ConfigSerializable
public class GlobalConfig {

  @Setting("spawn-world")
  private String spawn = "world";

  @Setting("world-template")
  private String template = "DIM-1";

  @Setting("data")
  private Map<UUID, PersonalDimension> data = Maps.newHashMap();

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

  public Map<UUID, PersonalDimension> getData() {
    return data;
  }
}
