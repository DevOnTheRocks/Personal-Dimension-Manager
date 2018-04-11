package rocks.gameonthe.pdm.data;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

@ConfigSerializable
public class PersonalDimension {

  private PersonalDimension(UUID id, UUID owner, Text name) {
    this.id = id;
    this.owner = owner;
    this.name = TextSerializers.FORMATTING_CODE.serialize(name);
  }

  private PersonalDimension() {

  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    UUID id;
    UUID owner;
    Text name;

    public Builder id(UUID id) {
      this.id = id;
      return this;
    }

    public Builder world(WorldProperties world) {
      this.id = world.getUniqueId();
      return this;
    }

    public Builder owner(UUID owner) {
      this.owner = owner;
      return this;
    }

    public Builder owner(User owner) {
      this.owner = owner.getUniqueId();
      return this;
    }

    public Builder name(Text name) {
      this.name = name;
      return this;
    }

    public PersonalDimension build() {
      Preconditions.checkNotNull(id);
      Preconditions.checkNotNull(owner);
      Preconditions.checkNotNull(name);
      return new PersonalDimension(id, owner, name);
    }
  }

  @Setting("id")
  public UUID id;

  @Setting("name")
  public String name = "Unknown";

  @Setting("owner")
  public UUID owner;

  @Setting("members")
  public Set<UUID> members = Sets.newHashSet();

  @Setting("created")
  public Long created = Instant.now().getEpochSecond();

  @Setting("previous-location")
  public Map<UUID, Vector3i> previousLocation = Maps.newHashMap();

  public Text getName() {
    return TextSerializers.FORMATTING_CODE.deserialize(name);
  }

  public void setName(Text name) {
    this.name = TextSerializers.FORMATTING_CODE.serialize(name);
  }

  public Optional<World> getWorld() {
    return Sponge.getServer().getWorld(id);
  }

  public Optional<WorldProperties> getWorldProperties() {
    return Sponge.getServer().getWorldProperties(id);
  }

  public Date getDateCreated() {
    return Date.from(Instant.ofEpochSecond(created));
  }
}
