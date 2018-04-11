package rocks.gameonthe.pdm.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class LeaveCommand implements CommandExecutor {

  private final PersonalDimManager plugin;

  public CommandSpec commandSpec;

  public LeaveCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("leave a dimension that you're a member of."))
        .permission(Permission.LEAVE)
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "leave");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player) {
      User user = (User) src;
      if (plugin.getDimensionManager().getDimension(user).isPresent()) {
        PersonalDimension dim = plugin.getDimensionManager().getDimension(user).get();

        if (dim.owner.equals(user.getUniqueId())) {
          throw new CommandException(Text.of(TextColors.RED, "You may not leave a dimension you own."));
        }

        src.sendMessage(Text.of(
            TextColors.WHITE, "Are you sure you want to leave ", TextColors.BLUE, dim.getName(), TextColors.WHITE, ".", Text.NEW_LINE,
            TextColors.WHITE, "[",
            Text.builder("YES")
                .color(TextColors.GREEN)
                .onHover(TextActions.showText(Text.of(TextColors.GREEN, "Click to leave")))
                .onClick(TextActions.executeCallback(s -> {
                  dim.members.remove(user.getUniqueId());
                  src.sendMessage(Text.of(TextColors.GREEN, "You have successfully left ", TextColors.BLUE, dim.getName(), TextColors.GREEN, "."));
                })),
            TextColors.WHITE, "] [",
            Text.builder("NO")
                .color(TextColors.RED)
                .onHover(TextActions.showText(Text.of(TextColors.RED, "Click to cancel")))
                .onClick(TextActions.executeCallback(s -> src.sendMessage(Text.of(TextColors.GREEN, "Leave cancelled.")))),
            TextColors.WHITE, "]"
        ));
      } else {
        throw new CommandException(Text.of(TextColors.RED, "You must have a dimension to use this command."));
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command can only be used by a player."));
    }
    return CommandResult.success();
  }
}
