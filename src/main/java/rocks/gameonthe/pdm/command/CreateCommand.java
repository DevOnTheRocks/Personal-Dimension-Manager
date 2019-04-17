package rocks.gameonthe.pdm.command;

import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TextMessageException;
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.DimensionPreset;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class CreateCommand implements CommandExecutor {

  private final PersonalDimManager plugin;
  private static final Text USER = Text.of("user");
  private static final Text NAME = Text.of("name");

  public CommandSpec commandSpec;

  public CreateCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("create a personal dimension."))
        .permission(Permission.CREATE)
        .arguments(
            GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.user(USER), Permission.CREATE_OTHERS)),
            GenericArguments.optional(GenericArguments.text(NAME, TextSerializers.FORMATTING_CODE, true))
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "create");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player || args.hasAny(USER)) {
      User user = args.<User>getOne(USER).orElse((User) src);
      Text name = args.<Text>getOne(NAME).orElse(Text.of(user.getName(), "'s World"));
      if (plugin.getDimensionManager().getDimension(user).isPresent()) {
        throw new CommandException(Text.of(TextColors.RED,
            src.equals(user) ? "You" : user.getName(), " already ",
            src.equals(user) ? "have" : "has", " a dimension!"
        ));
      } else {
        PaginationList.builder()
            .title(Text.of("World Presets"))
            .padding(Text.of(TextStyles.STRIKETHROUGH, "-"))
            .contents(plugin.getConfig().getPresets().stream()
                .map(preset -> preset.getName().toBuilder().onClick(TextActions.executeCallback(createDimension(preset, user, name))).build())
                .collect(Collectors.toList())
            )
            .sendTo(src);
//        try {
//          PersonalDimension dim = plugin.getDimensionManager().createDimensionFromTemplate(user, name);
//          onDimensionReady(src, user, dim);
//        } catch (TextMessageException e) {
//          throw new CommandException(e.getText() != null ? e.getText() : Text.EMPTY);
//        }
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command requires a [user] argument to be run by a non-player."));
    }
    return CommandResult.success();
  }

  private Consumer<CommandSource> createDimension(DimensionPreset preset, User user, Text name) {
    return src -> {
      if (plugin.getDimensionManager().getDimension(user).isPresent()) {
        src.sendMessage(Text.of(TextColors.RED,
            src.equals(user) ? "You" : user.getName(), " already ",
            src.equals(user) ? "have" : "has", " a dimension!"
        ));
        return;
      }
      try {
        PersonalDimension dim = plugin.getDimensionManager().createDimensionFromPreset(preset, user, name);
        onDimensionReady(src, user, dim);
      } catch (TextMessageException e) {
        src.sendMessage(e.getText() != null ? e.getText() : Text.EMPTY);
      }
    };
  }

  private void onDimensionReady(CommandSource src, User user, PersonalDimension dim) {
    plugin.getDimensionManager().loadWorld(user);
    src.sendMessage(Text.of(
        TextColors.GREEN, "Dimension created successfully! Would you like to teleport their now?", Text.NEW_LINE,
        Text.of(TextColors.WHITE, "[", TextColors.GREEN, "Yes", TextColors.WHITE, "] ").toBuilder()
            .onHover(TextActions.showText(Text.of("Click to teleport.")))
            .onClick(TextActions.executeCallback(s -> {
              if (s instanceof Player) {
                plugin.getDimensionManager().sendToWorldSafely(dim, (Player) s);
              }
            })),
        Text.of(TextColors.WHITE, "[", TextColors.RED, "No", TextColors.WHITE, "]")
    ));
  }
}
