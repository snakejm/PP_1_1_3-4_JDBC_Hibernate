package jm.task.core.jdbc;

import jm.task.core.jdbc.service.UserService;
import jm.task.core.jdbc.service.UserServiceImpl;
import jm.task.core.jdbc.util.Util;

public class Main {
    public static void main(String[] args) {
        try (var util = Util.getInstance()) {
            UserService userService = new UserServiceImpl();
            userService.createUsersTable();
            userService.saveUser("Rick", "Sanchez", (byte) 70);
            userService.saveUser("Morty", "Smith", (byte) 14);
            userService.saveUser("Bart", "Simpson", (byte) 11);
            userService.saveUser("Stan", "Marsh", (byte) 9);
            System.out.println(userService.getAllUsers());
            userService.cleanUsersTable();
            userService.dropUsersTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
