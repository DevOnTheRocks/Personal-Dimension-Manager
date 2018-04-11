package rocks.gameonthe.pdm.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class TeleportCommand implements CommandExecutor {

  private final PersonalDimManager plugin;

  private static final Text USER = Text.of("user");

  public CommandSpec commandSpec;

  public TeleportCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("teleport to your personal dimension."))
        .permission(Permission.TELEPORT)
        .arguments(
            GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.user(USER), Permission.TELEPORT_OTHERS)),
            GenericArguments.flags().permissionFlag(Permission.TELEPORT_SPAWN, "s", "-spawn").buildWith(GenericArguments.none())
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "tp", "teleport");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player) {
      Player player = (Player) src;
      User user = args.<User>getOne(USER).orElse(player);
      if (plugin.getDimensionManager().getDimension(user).isPresent()) {
        PersonalDimension dim = plugin.getDimensionManager().getDimension(user).get();
        World world = plugin.getDimensionManager().loadWorld(user)
            .orElseThrow(() -> new CommandException(Text.of(TextColors.RED, "Unable to locate target world.")));
        if (!args.hasAny("s") && world.equals(player.getWorld())) {
          throw new CommandException(Text.of(TextColors.RED, "You are already in ", TextColors.BLUE, dim.getName(), TextColors.RED, "."));
        }
        if (!plugin.getDimensionManager().sendToWorldSafely(dim, player, args.hasAny("s"))) {
          throw new CommandException(
              Text.of(TextColors.RED, "Unable to safely teleport you to ", TextColors.BLUE, dim.getName(), TextColors.RED, "."));
        }
      } else {
        throw new CommandException(Text.of(TextColors.RED, "You must have a dimension to use this command."));
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command can only be used by a player."));
    }
    return CommandResult.success();
  }
}
