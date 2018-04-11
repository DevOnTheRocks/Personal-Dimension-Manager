package rocks.gameonthe.pdm.command;

import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class ListCommand implements CommandExecutor {

  private final PersonalDimManager plugin;

  public CommandSpec commandSpec;

  public ListCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("display a list of all personal dimensions."))
        .permission(Permission.LIST)
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "list");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (plugin.getDimensionManager().getDimensions().isEmpty()) {
      throw new CommandException(Text.of(TextColors.RED, "There are no dimensions to display."));
    }
    final UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
    final List<Text> dimensions = Lists.newArrayList();

    plugin.getDimensionManager().getDimensions().values().stream()
        .sorted(Comparator.comparing(PersonalDimension::getDateCreated))
        .forEach(d -> {
      Text members = Text.of(TextColors.GOLD, uss.get(d.owner).isPresent() ? uss.get(d.owner).get().getName() : "Unknown User");
      for (UUID member : d.members) {
        String name = uss.get(member).isPresent() ? uss.get(member).get().getName() : "Unknown User";
        members = members.concat(Text.of(Text.NEW_LINE, TextColors.BLUE, name));
      }
      dimensions.add(Text.of(
          TextColors.WHITE, "[", d.getWorld().isPresent()
              ? Text.builder("✓").color(TextColors.GREEN).onHover(TextActions.showText(Text.of("World loaded")))
              : Text.builder("✗").color(TextColors.GRAY).onHover(TextActions.showText(Text.of("World unloaded"))),
          TextColors.WHITE, "] ",
          TextColors.BLUE, d.getName(),
          TextColors.WHITE, " - ", TextColors.GOLD, Text.of(d.members.size() + 1, " member(s)").toBuilder().onHover(TextActions.showText(members)),
          TextColors.WHITE, " - ", TextColors.GRAY, DateFormat.getInstance().format(d.getDateCreated())
      ));
    });
    if (src instanceof Player) {
      PaginationList.builder()
          .title(Text.of("Personal Dimensions"))
          .padding(Text.of(TextStyles.STRIKETHROUGH, "-"))
          .contents(dimensions)
          .sendTo(src);
    } else {
      dimensions.forEach(src::sendMessage);
    }

    return CommandResult.success();
  }
}
