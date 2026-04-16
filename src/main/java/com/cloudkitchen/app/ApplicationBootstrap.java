package com.cloudkitchen.app;

import com.cloudkitchen.db.DatabaseInitializer;
import com.cloudkitchen.db.DatabaseManager;
import com.cloudkitchen.repository.SqliteDishRepository;
import com.cloudkitchen.repository.SqliteIngredientRepository;
import com.cloudkitchen.repository.SqliteOrderRepository;
import com.cloudkitchen.repository.SqliteUserRepository;
import com.cloudkitchen.service.AppFacade;
import com.cloudkitchen.service.AuthService;
import com.cloudkitchen.service.InventoryService;
import com.cloudkitchen.service.OrderService;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class ApplicationBootstrap {

    public AppFacade bootstrap(Path databasePath) {
        DatabaseManager databaseManager = new DatabaseManager(databasePath);
        try (Connection connection = databaseManager.getConnection()) {
            new DatabaseInitializer().initialize(connection);
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to initialize database", exception);
        }

        SqliteIngredientRepository ingredientRepository = new SqliteIngredientRepository();
        SqliteDishRepository dishRepository = new SqliteDishRepository();
        SqliteOrderRepository orderRepository = new SqliteOrderRepository();
        SqliteUserRepository userRepository = new SqliteUserRepository();

        AuthService authService = new AuthService(databaseManager, userRepository);
        InventoryService inventoryService = new InventoryService(
                databaseManager,
                ingredientRepository,
                dishRepository,
                orderRepository);
        OrderService orderService = new OrderService(
                databaseManager,
                dishRepository,
                ingredientRepository,
                orderRepository);
        return new AppFacade(authService, inventoryService, orderService);
    }
}
