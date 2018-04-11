package rocks.gameonthe.pdm.command;

import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.PatternMatchingCommandElement;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import rocks.gameonthe.pdm.PersonalDimManager;
import rocks.gameonthe.pdm.data.PersonalDimension;

public class DimensionArgument extends PatternMatchingCommandElement {

  protected DimensionArgument(@Nullable Text key) {
    super(key);
  }

  @Override
  protected Iterable<String> getChoices(CommandSource source) {
    return PersonalDimManager.getInstance().getDimensionManager().getDimensions().values().stream()
        .map(d -> d.getName().toPlain()).collect(Collectors.toList());
  }

  @Override
  protected PersonalDimension getValue(String choice) throws IllegalArgumentException {
    UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
    return PersonalDimManager.getInstance().getDimensionManager().getDimensions().values().stream()
        .filter(d -> d.getName().toPlain().equalsIgnoreCase(choice) ||
            uss.get(d.owner).isPresent() && uss.get(d.owner).get().getName().equalsIgnoreCase(choice)
        ).findAny()
        .orElseThrow(() -> new IllegalArgumentException(choice));
  }
}
