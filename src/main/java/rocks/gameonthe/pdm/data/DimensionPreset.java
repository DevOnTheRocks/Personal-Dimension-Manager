package rocks.gameonthe.pdm.data;

import java.util.Optional;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

@ConfigSerializable
public class DimensionPreset {

  @Setting
  private String name;

  @Setting
  private String description;

  @Setting(value = "level-type")
  private String levelType;

  @Setting(value = "generator-settings")
  private ConfigurationNode generatorSettings;

  private DimensionPreset() {

  }

  private DimensionPreset(String name, String description, String levelType) {
    this.name = name;
    this.description = description;
    this.levelType = levelType;
  }

  public static DimensionPresetBuilder builder() {
    return new DimensionPresetBuilder();
  }

  public Text getName() {
    return TextSerializers.FORMATTING_CODE.deserialize(name);
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
  }

  public Optional<String> getLevelType() {
    return Optional.ofNullable(levelType);
  }

  public DataContainer getGeneratorSettings() {
    return DataTranslators.CONFIGURATION_NODE.translate(generatorSettings);
  }

  public void setGeneratorSettings(DataContainer container) {
    this.generatorSettings = DataTranslators.CONFIGURATION_NODE.translate(container);
  }

  public static class DimensionPresetBuilder {

    private String name;
    private String description;
    private String levelType;

    DimensionPresetBuilder() {
    }

    public DimensionPresetBuilder name(String name) {
      this.name = name;
      return this;
    }

    public DimensionPresetBuilder description(String description) {
      this.description = description;
      return this;
    }

    public DimensionPresetBuilder levelType(String levelType) {
      this.levelType = levelType;
      return this;
    }

    public DimensionPreset build() {
      return new DimensionPreset(name, description, levelType);
    }

    public String toString() {
      return "DimensionPreset.DimensionPresetBuilder(name=" + this.name + ", description=" + this.description
          + ", levelType=" + this.levelType + ")";
    }
  }
}
