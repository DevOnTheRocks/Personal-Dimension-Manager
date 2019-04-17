package rocks.gameonthe.pdm;

import static rocks.gameonthe.pdm.PluginInfo.AUTHORS;
import static rocks.gameonthe.pdm.PluginInfo.DESCRIPTION;
import static rocks.gameonthe.pdm.PluginInfo.GP_VERSION;
import static rocks.gameonthe.pdm.PluginInfo.ID;
import static rocks.gameonthe.pdm.PluginInfo.NAME;
import static rocks.gameonthe.pdm.PluginInfo.SPONGE_API;
import static rocks.gameonthe.pdm.PluginInfo.VERSION;

import com.google.inject.Inject;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Optional;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import rocks.gameonthe.pdm.command.DimManagerCommand;
import rocks.gameonthe.pdm.config.ConfigManager;
import rocks.gameonthe.pdm.config.GlobalConfig;
import rocks.gameonthe.pdm.data.DatabaseManager;
import rocks.gameonthe.pdm.data.DimensionManager;
import rocks.gameonthe.pdm.data.DimensionPreset;
import rocks.gameonthe.pdm.listener.PlayerListener;
import rocks.gameonthe.pdm.plugin.GriefPreventionPlugin;

@Plugin(
    id = ID,
    name = NAME,
    version = VERSION,
    description = DESCRIPTION,
    authors = AUTHORS,
    dependencies = {
        @Dependency(id = "spongeapi", version = SPONGE_API),
        @Dependency(id = "griefprevention", version = GP_VERSION, optional = true)
    }
)
public class PersonalDimManager {

  private static PersonalDimManager instance;

  @Inject
  private PluginContainer pluginContainer;

  @Inject
  private Logger logger;

  @Inject
  @ConfigDir(sharedRoot = false)
  private Path configDir;
  @Inject
  @DefaultConfig(sharedRoot = false)
  private ConfigurationLoader<CommentedConfigurationNode> configLoader;
  private ConfigManager configManager;
  private GlobalConfig config;

  @Inject
  private Metrics2 metrics;

  private DatabaseManager databaseManager;
  private DimensionManager dimensionManager;

  private GriefPreventionPlugin griefPrevention;

  @Listener
  public void onGameConstruction(GameConstructionEvent event) {
    instance = this;
  }

  @Listener
  public void onPreInitialization(GamePreInitializationEvent event) {
    logger.info("{} {} is initializing...", NAME, VERSION);

    config = new GlobalConfig();
    configManager = new ConfigManager(this);
    configManager.save();

    try {
      databaseManager = new DatabaseManager(configDir + "/pdm");
    } catch (SQLException e) {
      logger.error("Unable to connect to DB", e);
    }

    dimensionManager = new DimensionManager(this);
  }

  @Listener
  public void onAboutToStart(GameAboutToStartServerEvent event) {
    new DimManagerCommand(this);

    Sponge.getEventManager().registerListeners(this, new PlayerListener(this));
    //Sponge.getEventManager().registerListeners(this, new WorldListener(this));

    if (Sponge.getPluginManager().isLoaded("griefprevention")) {
      griefPrevention = new GriefPreventionPlugin(this);
      Sponge.getEventManager().registerListeners(this, griefPrevention);
    }
  }

  @Listener
  public void onReload(GameReloadEvent event) {
    reload();
  }

  public void reload() {
    // Load Plugin Config
    configManager.load();
  }

  public static PersonalDimManager getInstance() {
    return instance;
  }

  public static Logger getLogger() {
    return instance.logger;
  }

  public GlobalConfig getConfig() {
    return this.config;
  }

  public void setConfig(GlobalConfig config) {
    this.config = config;
  }

  public Path getConfigDir() {
    return configDir;
  }

  public ConfigManager getConfigManager() {
    return configManager;
  }

  public ConfigurationLoader<CommentedConfigurationNode> getConfigLoader() {
    return configLoader;
  }

  public DatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public DimensionManager getDimensionManager() {
    return dimensionManager;
  }

  public Optional<GriefPreventionPlugin> getGriefPrevention() {
    return Optional.ofNullable(griefPrevention);
  }
}
