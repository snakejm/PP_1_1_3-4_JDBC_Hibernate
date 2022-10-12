package jm.task.core.jdbc.dao;

import jm.task.core.jdbc.model.User;
import jm.task.core.jdbc.util.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoJDBCImpl implements UserDao {
    private final Util util;

    public UserDaoJDBCImpl() {
        util = Util.getInstance();
    }

    public void createUsersTable() {
        try (var connection = util.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE USERS (" +
                    "  `ID` INT NOT NULL AUTO_INCREMENT," +
                    "  `NAME` VARCHAR(45) NOT NULL," +
                    "  `LASTNAME` VARCHAR(45) NOT NULL," +
                    "  `AGE` TINYINT NOT NULL," +
                    "  PRIMARY KEY (`ID`)," +
                    "  UNIQUE INDEX `ID_UNIQUE` (`ID` ASC) VISIBLE);");
        } catch (SQLException e) {
            System.err.println("Невозможно создать таблицу - таблица уже создана");
        }
    }

    public void dropUsersTable() {
        try (var connection = util.getConnection(); var statement = connection.createStatement()) {
            statement.execute("DROP TABLE USERS");
        } catch (SQLException e) {
            System.err.println("Невозможно удалить таблицу - таблица отсутствует");
        }
    }

    public void saveUser(String name, String lastName, byte age) {
        String sql = "INSERT INTO USERS (NAME, LASTNAME, AGE) VALUES (?, ?, ?)";
        try (var connection = util.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);
                preparedStatement.setString(1, name);
                preparedStatement.setString(2, lastName);
                preparedStatement.setByte(3, age);
                preparedStatement.executeUpdate();
                connection.commit();
                System.out.printf("User с именем – %s добавлен в базу данных\n", name);
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                connection.rollback();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeUserById(long id) {
        String sql = "DELETE FROM USERS WHERE ID=?";
        try (var connection = util.getConnection(); var preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, id);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS";
        try (var connection = util.getConnection(); var statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getLong("ID"));
                user.setName(resultSet.getString("NAME"));
                user.setLastName(resultSet.getString("LASTNAME"));
                user.setAge(resultSet.getByte("AGE"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public void cleanUsersTable() {
        try (var connection = util.getConnection(); var statement = connection.createStatement()) {
            statement.execute("DELETE FROM USERS");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
