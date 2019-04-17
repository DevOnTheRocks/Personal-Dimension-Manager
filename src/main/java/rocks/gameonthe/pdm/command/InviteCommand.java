package rocks.gameonthe.pdm.command;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
import rocks.gameonthe.pdm.Permission;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class InviteCommand implements CommandExecutor {

  private final PersonalDimManager plugin;

  public static final Table<User, User, PersonalDimension> invites = HashBasedTable.create();

  private static final Text DIMENSION = Text.of("dimension");
  private static final Text TARGET = Text.of("target");

  public CommandSpec commandSpec;

  public InviteCommand(PersonalDimManager plugin) {
    this.plugin = plugin;
    this.commandSpec = CommandSpec.builder()
        .description(Text.of("invite members to a dimension."))
        .permission(Permission.INVITE)
        .arguments(
            GenericArguments.optionalWeak(GenericArguments.requiringPermission(new DimensionArgument(DIMENSION), Permission.INVITE_OTHERS)),
            GenericArguments.user(TARGET),
            GenericArguments.flags().permissionFlag(Permission.INVITE_FORCE, "f").buildWith(GenericArguments.none())
        )
        .executor(this)
        .build();
    Sponge.getCommandManager().register(plugin, commandSpec);
    DimManagerCommand.addChildCommand(commandSpec, "invite");
  }

  @Override
  public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
    if (src instanceof Player) {
      final User user = (User) src;
      final PersonalDimension dim = args.<PersonalDimension>getOne(DIMENSION).orElse(plugin.getDimensionManager().getDimension(user).orElse(null));
      final User target = args.<User>getOne(TARGET).orElseThrow(() -> new CommandException(Text.of("A target must be provided")));

      if (dim != null) {
        if (!dim.owner.equals(user.getUniqueId()) && !src.hasPermission(Permission.INVITE_OTHERS)) {
          throw new CommandException(Text.of(TextColors.RED, "Only the dimension owner may send invites."));
        }

        if (!args.hasAny("f")) {
          invites.put(user, target, dim);
          src.sendMessage(Text.of(TextColors.GREEN, "An invite was sent to ", TextColors.BLUE, target.getName(), TextColors.GREEN, "."));

          if (target.getPlayer().isPresent()) {
            target.getPlayer().get().sendMessage(getInviteMessage(user, target));
          }
        } else {
          dim.members.add(target.getUniqueId());
          plugin.getGriefPrevention().ifPresent(gp -> gp.setTrust(dim));
          plugin.getDatabaseManager().save(dim);
          src.sendMessage(Text.of(
              TextColors.GOLD, target.getName(), TextColors.GREEN, " successful added to ", TextColors.BLUE, dim.getName(), TextColors.GREEN, "."
          ));
        }
      } else {
        throw new CommandException(Text.of(TextColors.RED, "You must have a dimension to use this command."));
      }
    } else {
      throw new CommandException(Text.of(TextColors.RED, "This command can only be used by a player."));
    }
    return CommandResult.success();
  }

  public static Text getInviteMessage(User user, User target) {
    return Text.of(
        TextColors.GOLD, user.getName(), " has invited you to their personal dimension.", Text.NEW_LINE,
        Text.of(TextColors.WHITE, "[", TextColors.GREEN, "Accept", TextColors.WHITE, "]").toBuilder()
            .onHover(TextActions.showText(Text.of("Click to accept")))
            .onClick(TextActions.executeCallback(src -> {
              PersonalDimension dim = invites.remove(user, target);
              if (dim != null) {
                dim.members.add(target.getUniqueId());
                PersonalDimManager.getInstance().getGriefPrevention().ifPresent(gp -> gp.setTrust(dim));
                PersonalDimManager.getInstance().getDatabaseManager().save(dim);
                src.sendMessage(Text.of("Invite accepted. Would you like to Teleport their now?"));
                src.sendMessage(Text.of(
                    Text.of(TextColors.WHITE, "[", TextColors.GREEN, "YES", TextColors.WHITE, "] ").toBuilder()
                        .onHover(TextActions.showText(Text.of("Click to teleport.")))
                        .onClick(TextActions.executeCallback(s -> {
                          if (s instanceof Player) {
                            PersonalDimManager.getInstance().getDimensionManager().sendToWorldSafely(dim, (Player) s);
                          }
                        })),
                    Text.of(TextColors.WHITE, "[", TextColors.RED, "NO", TextColors.WHITE, "]")
                ));
              } else {
                src.sendMessage(Text.of("Invite is no longer valid."));
              }
            })),
        Text.of(TextColors.WHITE, "[", TextColors.RED, "Deny", TextColors.WHITE, "]").toBuilder()
            .onHover(TextActions.showText(Text.of("Click to deny")))
            .onClick(TextActions.executeCallback(src -> {
              invites.remove(user, target);
              src.sendMessage(Text.of("Invite denied."));
            }))
    );
  }
}
