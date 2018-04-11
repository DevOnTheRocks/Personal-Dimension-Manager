package rocks.gameonthe.pdm.data;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;
import rocks.gameonthe.pdm.PersonalDimManager;

public class DimensionManager {

  private final PersonalDimManager plugin;

  public DimensionManager(PersonalDimManager plugin) {
    this.plugin = plugin;
  }

  public Map<UUID, PersonalDimension> getDimensions() {
    return plugin.getConfig().getData();
  }

  public Optional<PersonalDimension> getDimension(User user) {
    return getDimensions().values().stream()
        .filter(dim -> dim.owner.equals(user.getUniqueId()) || dim.members.contains(user.getUniqueId()))
        .findAny();
  }

  public PersonalDimension createDimension(User user, Text name) throws TextMessageException {
    // Create Dimension
    WorldProperties template = plugin.getConfig().getTemplate()
        .orElseThrow(() -> new TextMessageException(Text.of(TextColors.RED, "World template could not be loaded.")))
        .getProperties();

    try {
      Sponge.getServer().copyWorld(template, user.getName()).handle((world, e) -> {

        if (e == null && world.isPresent()) {
          // Create Dimension Data
          PersonalDimension dim = PersonalDimension.builder()
              .world(world.get())
              .owner(user)
              .name(name)
              .build();

          // Save the new dim
          getDimensions().put(world.get().getUniqueId(), dim);
          plugin.getConfigManager().save();
          plugin.getLogger().info("Successfully create dimension for {}.", user.getName());
        } else {
          plugin.getLogger().error("Error creating dimension.", e);
        }

        return world;
      }).get();
    } catch (InterruptedException | ExecutionException e) {
      plugin.getLogger().error("An error occurred creating a new dimension.", e);
      throw new TextMessageException(Text.of(TextColors.RED, "An error occurred creating a new dimension."));
    }

    return getDimension(user).orElseThrow(() -> new TextMessageException(Text.of(TextColors.RED, "Dimension not ready.")));
  }

  public void deleteDimension(User user) throws TextMessageException {
    PersonalDimension dim = getDimension(user)
        .orElseThrow(() -> new TextMessageException(Text.of(TextColors.RED, "Dimension not found.")));
    deleteDimension(dim);
  }

  public void deleteDimension(PersonalDimension dim) throws TextMessageException {
    WorldProperties properties = dim.getWorldProperties()
        .orElseThrow(() -> new TextMessageException(Text.of(TextColors.RED, "World not found.")));

    try {
      if (dim.getWorld().isPresent()) {
        Sponge.getServer().unloadWorld(dim.getWorld().get());
      }
      if (Sponge.getServer().deleteWorld(properties).get()) {
        getDimensions().remove(properties.getUniqueId());
        plugin.getConfigManager().save();
        plugin.getLogger().info("Successfully removed {}.", dim.getName());
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      plugin.getLogger().error("Error removing dimension.", e);
      throw new TextMessageException(Text.of(TextColors.RED, "Unable to delete ", dim.getName(), "."));
    }
  }

  public Optional<World> loadWorld(User user) {
    if (getDimension(user).isPresent()) {
      return Sponge.getServer().loadWorld(getDimension(user).get().id);
    }
    return Optional.empty();
  }

  public boolean unloadInactiveWorld(User user) {
    if (getDimension(user).isPresent() && getDimension(user).get().getWorld().isPresent()) {
      if (Sponge.getServer().getPlayer(getDimension(user).get().owner).isPresent()
          || getDimension(user).get().members.stream().anyMatch(m -> Sponge.getServer().getPlayer(m).isPresent())) {
        return false;
      }
      return Sponge.getServer().unloadWorld(getDimension(user).get().getWorld().get());
    }
    return false;
  }

  public boolean sendToWorldSafely(PersonalDimension dimension, Player player, boolean forceSpawn) {
    if (!dimension.getWorld().isPresent()) {
      return false;
    }
    World world = dimension.getWorld().get();
    if (forceSpawn) {
      return player.setLocationSafely(world.getSpawnLocation());
    } else {
      Location<World> location = new Location<>(
          world, dimension.previousLocation.getOrDefault(player.getUniqueId(), world.getSpawnLocation().getBlockPosition())
      );
      return player.setLocationSafely(location);
    }
  }

  public boolean sendToWorldSafely(PersonalDimension dimension, Player player) {
    return sendToWorldSafely(dimension, player, false);
  }
}
