package rocks.gameonthe.pdm.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.PluginInfo;

public class DimManagerCommand implements CommandExecutor {

  private final PersonalDimManager plugin;
  private static final Map<List<String>, CommandCallable> children = Maps.newHashMap();

  public CommandSpec commandSpec;

  public DimManagerCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    registerChildren();
    this.commandSpec = CommandSpec.builder()
        .children(children)
        .childArgumentParseExceptionFallback(false)
        .arguments(GenericArguments.optional(GenericArguments.literal(Text.of("help"), "help")))
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec, "pdm", "dm", "dimensionmanger");
  }

  public static void addChildCommand(CommandCallable spec, String... alias) {
    children.put(Arrays.asList(alias), spec);
  }

  private void registerChildren() {
    children.clear();
    new DeleteCommand(plugin);
    new CreateCommand(plugin);
    new InviteCommand(plugin);
    new LeaveCommand(plugin);
    new ListCommand(plugin);
    new SetNameCommand(plugin);
    new TeleportCommand(plugin);
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    List<Text> help = Lists.newArrayList();
    children.forEach((a, c) -> help.add(Text.of(
        "/pdm ", a.get(0), " ", c.getUsage(src).concat(c.getUsage(src).isEmpty() ? Text.EMPTY : Text.of(" ")),
        "- ", c.getShortDescription(src).orElse(Text.of("no description provided.")))
    ));
    help.sort(Comparator.comparing(Text::toPlain));
    if (src instanceof Player) {
      PaginationList.builder()
          .title(Text.of(PluginInfo.NAME, " Help"))
          .padding(Text.of(TextStyles.STRIKETHROUGH, "-"))
          .contents(help)
          .sendTo(src);
    } else {
      help.forEach(src::sendMessage);
    }
    return CommandResult.success();
  }
}
