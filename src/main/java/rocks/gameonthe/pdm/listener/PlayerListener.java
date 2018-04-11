package rocks.gameonthe.pdm.listener;

import java.util.concurrent.TimeUnit;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.command.InviteCommand;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class PlayerListener {

  private final PersonalDimManager plugin;

  public PlayerListener(PersonalDimManager plugin) {
    this.plugin = plugin;
  }

  @Listener
  public void onPlayerConnect(ClientConnectionEvent.Join event, @First Player player) {
    // Load any personal dimension
    plugin.getDimensionManager().loadWorld(player);
    // Send any pending invites
    if (InviteCommand.invites.containsColumn(player)) {
      InviteCommand.invites.column(player).forEach((u, d) -> player.sendMessage(InviteCommand.getInviteMessage(u, player)));
    }
  }

  @Listener
  public void onPlayerDisconnect(ClientConnectionEvent.Disconnect event, @First User user) {
    Sponge.getScheduler().createTaskBuilder()
        .delay(30, TimeUnit.SECONDS)
        .execute(() -> plugin.getDimensionManager().unloadInactiveWorld(user))
        .submit(plugin);
  }

  @Listener
  public void onPlayerTeleport(MoveEntityEvent.Teleport event, @First Player player) {
    PersonalDimension dim = plugin.getDimensionManager().getDimensions().get(event.getFromTransform().getExtent().getUniqueId());
    if (dim != null) {
      dim.previousLocation.put(player.getUniqueId(), event.getFromTransform().getPosition().toInt());
      plugin.getConfigManager().save();
    }
  }

  @Listener
  public void onDeath(RespawnPlayerEvent event, @Getter("getTargetEntity") Player player) {
    if (!event.isBedSpawn() && plugin.getDimensionManager().getDimensions().containsKey(event.getFromTransform().getExtent().getUniqueId())) {
      event.setToTransform(new Transform<>(event.getFromTransform().getExtent().getSpawnLocation()));
    }
  }
}
