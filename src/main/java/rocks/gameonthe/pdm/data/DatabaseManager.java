package rocks.gameonthe.pdm.data;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Maps;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import rocks.gameonthe.pdm.PersonalDimManager;

public class DatabaseManager {

  private final SqlService sql = Sponge.getServiceManager().provideUnchecked(SqlService.class);
  private DataSource dataSource;

  public DatabaseManager(String path) throws SQLException {
    dataSource = sql.getDataSource("jdbc:h2:" + path);
    PersonalDimManager.getLogger().info("Successfully connected to {}.", path);

    Flyway flyway = Flyway.configure().dataSource(dataSource).load();
    flyway.migrate();
  }

  // CREATE
  public boolean create(PersonalDimension dimension) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("INSERT INTO DIMENSIONS VALUES (?,?,?,?);")) {
      statement.setObject(1, dimension.id);
      statement.setObject(2, dimension.owner);
      statement.setString(3, dimension.name);
      statement.setTimestamp(4, dimension.created);
      return statement.execute();
    } catch (SQLException e) {
      PersonalDimManager.getLogger().error("Error saving new dim.", e);
      return false;
    }
  }

  // READ
  public Map<UUID, PersonalDimension> load() {
    Map<UUID, PersonalDimension> map = Maps.newHashMap();
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      ResultSet dimensions = statement.executeQuery("SELECT * FROM DIMENSIONS");

      while (dimensions.next()) {
        // Dimension Data
        PersonalDimension dim = PersonalDimension.builder()
            .id(dimensions.getObject(1, UUID.class))
            .owner(dimensions.getObject(2, UUID.class))
            .name(dimensions.getString(3))
            .build();
        dim.created = dimensions.getTimestamp(4);

        try (Statement s = connection.createStatement()) {
          // Member Data
          ResultSet members = s.executeQuery("SELECT PLAYER FROM MEMBERS WHERE DIMENSION = '" + dim.id + "'");
          while (members.next()) {
            dim.members.add(members.getObject(1, UUID.class));
          }
          // Location Data
          ResultSet locations = s.executeQuery("SELECT PLAYER, X, Y, Z FROM LOCATIONS WHERE DIMENSION = '" + dim.id + "'");
          while (locations.next()) {
            dim.previousLocation.put(
                locations.getObject(1, UUID.class),
                new Vector3i(
                    locations.getInt(2),
                    locations.getInt(3),
                    locations.getInt(4)
                ));
          }
        }

        // Add to map
        map.put(dim.id, dim);
      }
    } catch (SQLException e) {
      PersonalDimManager.getLogger().error("Error loading dim.", e);
    }
    return map;
  }

  // UPDATE
  public boolean save(PersonalDimension dimension) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("MERGE INTO DIMENSIONS(ID, OWNER, NAME) VALUES (?,?,?)")) {
      statement.setObject(1, dimension.id);
      statement.setObject(2, dimension.owner);
      statement.setString(3, dimension.name);
      statement.execute();
      try (PreparedStatement s = connection.prepareStatement("MERGE INTO MEMBERS(DIMENSION, PLAYER) VALUES (?,?)")) {
        // Members
        for (UUID member: dimension.members) {
          s.setObject(1, dimension.id);
          s.setObject(2, member);
          s.execute();
        }
      }
      try (PreparedStatement s = connection.prepareStatement("MERGE INTO LOCATIONS(DIMENSION, PLAYER, X, Y, Z) VALUES (?,?,?,?,?)")) {
        // Locations
        for (Entry<UUID, Vector3i> loc : dimension.previousLocation.entrySet()) {
          s.setObject(1, dimension.id);
          s.setObject(2, loc.getKey());
          s.setInt(3, loc.getValue().getX());
          s.setInt(4, loc.getValue().getY());
          s.setInt(5, loc.getValue().getZ());
          s.execute();
        }
      }
    } catch (SQLException e) {
      PersonalDimManager.getLogger().error("Error updating dim.", e);
    }

    return false;
  }

  // REMOVE
  public boolean delete(PersonalDimension dimension) {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("DELETE FROM DIMENSIONS WHERE ID = ?")){
      statement.setObject(1, dimension.id);
      return statement.execute();
    } catch (SQLException e) {
      PersonalDimManager.getLogger().error("Error removing dim.", e);
    }

    return false;
  }
}
