package rocks.gameonthe.pdm.config;

import java.io.IOException;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import rocks.gameonthe.pdm.PersonalDimManager;

public class ConfigManager {

  private final Logger logger;
  private ObjectMapper<GlobalConfig>.BoundInstance configMapper;
  private ConfigurationLoader<CommentedConfigurationNode> loader;

  public ConfigManager(PersonalDimManager plugin) {
    this.logger = plugin.getLogger();
    this.loader = plugin.getConfigLoader();
    try {
      this.configMapper = ObjectMapper.forObject(plugin.getConfig());
    } catch (ObjectMappingException e) {
      e.printStackTrace();
    }

    this.load();
  }

  /**
   * Saves the serialized config to file
   */
  public void save() {
    try {
      SimpleCommentedConfigurationNode out = SimpleCommentedConfigurationNode.root();
      this.configMapper.serialize(out);
      this.loader.save(out);
    } catch (ObjectMappingException | IOException e) {
      logger.error("Failed to save config.", e);
    }
  }

  /**
   * Loads the configs into serialized objects, for the configMapper
   */
  public void load() {
    try {
      this.configMapper.populate(this.loader.load());
    } catch (ObjectMappingException | IOException e) {
      logger.error("Failed to load config.", e);
    }
  }
}