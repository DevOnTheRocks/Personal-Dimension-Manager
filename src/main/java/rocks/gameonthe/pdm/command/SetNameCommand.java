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
import org.spongepowered.api.text.serializer.TextSerializers;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class SetNameCommand implements CommandExecutor {

  private final PersonalDimManager plugin;

  private static final Text USER = Text.of("user");
  private static final Text NAME = Text.of("name");

  public CommandSpec commandSpec;

  public SetNameCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("set the name of your dimension."))
        .permission(Permission.SET_NAME)
        .arguments(
            GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.user(USER), Permission.DELETE_OTHERS)),
            GenericArguments.text(NAME, TextSerializers.FORMATTING_CODE, true)
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "setname", "nameset");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player || args.hasAny(USER)) {
      User user = args.<User>getOne(USER).orElse((User) src);
      Text name = args.<Text>getOne(NAME).orElse(Text.of(user.getName(), "'s World"));
      if (!plugin.getDimensionManager().getDimension(user).isPresent()) {
        throw new CommandException(Text.of(TextColors.RED, src.equals(user) ? "You" : user.getName(), " must have a dimension!"));
      } else {
        PersonalDimension dim = plugin.getDimensionManager().getDimension(user).get();
        if (!dim.owner.equals(user.getUniqueId()) && !src.hasPermission(Permission.DELETE_OTHERS)) {
          throw new CommandException(Text.of(TextColors.RED, "Only the dimension owner may delete a dimension."));
        }

        dim.setName(name);
        plugin.getConfigManager().save();
        src.sendMessage(
            Text.of(TextColors.GREEN, "Your dimension has successfully been renamed to ", TextColors.BLUE, dim.getName(), TextColors.GREEN, "."));
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command requires a [user] argument to be run by a non-player."));
    }
    return CommandResult.success();
  }
}
