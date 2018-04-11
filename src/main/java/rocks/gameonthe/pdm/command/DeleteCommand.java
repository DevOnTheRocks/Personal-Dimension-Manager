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
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.TextMessageException;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class DeleteCommand implements CommandExecutor {

  private final PersonalDimManager plugin;
  private static final Text DIMENSION = Text.of("dimension");

  public CommandSpec commandSpec;

  public DeleteCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("delete a personal dimension."))
        .permission(Permission.DELETE)
        .arguments(GenericArguments.optional(GenericArguments.requiringPermission(new DimensionArgument(DIMENSION), Permission.DELETE_OTHERS)))
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "delete");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player || args.hasAny(DIMENSION)) {
      PersonalDimension dim = args.<PersonalDimension>getOne(DIMENSION).orElse(plugin.getDimensionManager().getDimension((User) src).orElse(null));
      if (dim == null) {
        throw new CommandException(Text.of(TextColors.RED, "You must have a dimension to use this command!"));
      } else {
        if (src instanceof Player && dim.owner.equals(((Player) src).getUniqueId()) || src.hasPermission(Permission.DELETE_OTHERS)) {
          src.sendMessage(Text.of(
              TextColors.WHITE, "Are you sure you want to delete ", TextColors.BLUE, dim.getName(), TextColors.WHITE, ".", Text.NEW_LINE,
              TextColors.WHITE, "[",
              Text.builder("YES")
                  .color(TextColors.GREEN)
                  .onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to delete")))
                  .onClick(TextActions.executeCallback(s -> {
                    try {
                      plugin.getDimensionManager().deleteDimension(dim);
                      src.sendMessage(Text.of(TextColors.GREEN, "You have successfully deleted ", TextColors.BLUE, dim.getName(), TextColors.GREEN, "."));
                    } catch (TextMessageException e) {
                      src.sendMessage(e.getText() != null ? e.getText() : Text.EMPTY);
                    }
                  })),
              TextColors.WHITE, "] [",
              Text.builder("NO")
                  .color(TextColors.RED)
                  .onHover(TextActions.showText(Text.of(TextColors.RED, "Click to cancel")))
                  .onClick(TextActions.executeCallback(s -> src.sendMessage(Text.of(TextColors.GREEN, "Delete cancelled.")))),
              TextColors.WHITE, "]"
          ));
        } else {
          throw new CommandException(Text.of(TextColors.RED, "Only the dimension owner may delete a dimension."));
        }
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command requires a [user] argument to be run by a non-player."));
    }
    return CommandResult.success();
  }
}
