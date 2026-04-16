package com.cloudkitchen.ui;

import com.cloudkitchen.model.DishAvailability;
import com.cloudkitchen.model.Ingredient;
import com.cloudkitchen.model.IngredientRequirementStatus;
import com.cloudkitchen.model.InventorySummary;
import com.cloudkitchen.model.OrderRecord;
import com.cloudkitchen.model.RecipeItem;
import com.cloudkitchen.model.User;
import com.cloudkitchen.service.AppFacade;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static class MetricCardViewModel {
        private final String label;
        private final String value;
        private final String hint;

        private MetricCardViewModel(String label, String value, String hint) {
            this.label = label;
            this.value = value;
            this.hint = hint;
        }

        private String getLabel() {
            return label;
        }

        private String getValue() {
            return value;
        }

        private String getHint() {
            return hint;
        }
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");
    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
    private static AppFacade appFacade;

    private Stage stage;

    private final FlowPane metricsPane = new FlowPane();
    private final VBox lowStockBox = new VBox(10);
    private final ListView<Ingredient> ingredientListView = new ListView<>();
    private final ListView<DishAvailability> dishListView = new ListView<>();
    private final ListView<DishAvailability> adminDishListView = new ListView<>();
    private final ListView<OrderRecord> adminOrderListView = new ListView<>();
    private final ListView<OrderRecord> customerOrderListView = new ListView<>();
    private final Label userContextLabel = new Label();
    private final VBox customerHeroBox = new VBox(10);
    private final VBox adminHeroBox = new VBox(10);

    public static void setAppFacade(AppFacade facade) {
        appFacade = facade;
    }

    @Override
    public void start(Stage primaryStage) {
        if (appFacade == null) {
            throw new IllegalStateException("App facade must be initialized before launching the UI.");
        }
        this.stage = primaryStage;
        showLoginScene();
    }

    private void showLoginScene() {
        Label eyebrow = new Label("Cloud Kitchen Inventory Management System");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label("Login and choose your portal");
        title.getStyleClass().add("page-title");

        Label subtitle = new Label("Admin can manage inventory and see all customers. Customers can order and see only their own history.");
        subtitle.getStyleClass().add("page-subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button adminButton = new Button("Login as Admin");
        adminButton.getStyleClass().add("primary-button");
        adminButton.setOnAction(event -> attemptLogin(usernameField.getText(), passwordField.getText(), User.Role.ADMIN));

        Button customerButton = new Button("Login as Customer");
        customerButton.getStyleClass().add("secondary-button");
        customerButton.setOnAction(event -> attemptLogin(usernameField.getText(), passwordField.getText(), User.Role.CUSTOMER));

        VBox credentials = new VBox(12, usernameField, passwordField, new HBox(12, adminButton, customerButton));
        credentials.getStyleClass().add("card");
        credentials.setPadding(new Insets(24));

        Label demoTitle = new Label("Demo credentials");
        demoTitle.getStyleClass().add("section-title");

        Label demoText = new Label(
                "Admin: admin / admin123\nCustomer 1: aarav / cust123\nCustomer 2: diya / cust123");
        demoText.getStyleClass().add("muted-text");

        VBox demoCard = new VBox(8, demoTitle, demoText);
        demoCard.getStyleClass().add("card");
        demoCard.setPadding(new Insets(24));

        TextField registerNameField = new TextField();
        registerNameField.setPromptText("Full name");

        TextField registerUsernameField = new TextField();
        registerUsernameField.setPromptText("Create username (min 4 chars)");

        PasswordField registerPasswordField = new PasswordField();
        registerPasswordField.setPromptText("Create password (min 6 chars)");

        Button registerButton = new Button("Register Customer");
        registerButton.getStyleClass().add("primary-button");
        registerButton.setOnAction(event -> {
            try {
                appFacade.registerCustomer(
                        registerNameField.getText(),
                        registerUsernameField.getText(),
                        registerPasswordField.getText());
                registerNameField.clear();
                registerUsernameField.clear();
                registerPasswordField.clear();
                showInfo("Registration complete", "Customer account created. They can now use the customer login button.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        VBox registerCard = new VBox(
                8,
                buildSectionTitle("New Customer Registration", "Create a new customer login directly from the welcome screen."),
                registerNameField,
                registerUsernameField,
                registerPasswordField,
                registerButton);
        registerCard.getStyleClass().add("card");
        registerCard.setPadding(new Insets(24));

        VBox root = new VBox(18, eyebrow, title, subtitle, credentials, demoCard, registerCard);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER_LEFT);
        root.getStyleClass().add("login-shell");

        Scene scene = new Scene(root, 920, 620);
        scene.getStylesheets().add(getClass().getResource("/com/cloudkitchen/ui/app.css").toExternalForm());
        stage.setTitle("Cloud Kitchen Login");
        stage.setScene(scene);
        stage.show();
    }

    private void attemptLogin(String username, String password, User.Role selectedPortal) {
        try {
            appFacade.login(username, password, selectedPortal);
            showPortalScene();
        } catch (Exception exception) {
            showError(exception.getMessage());
        }
    }

    private void showPortalScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-shell");
        if (appFacade.getCurrentSession().isAdminPortal()) {
            root.getStyleClass().add("admin-shell");
        } else {
            root.getStyleClass().add("customer-shell");
        }
        root.setTop(buildHeader());
        root.setCenter(buildPortalContent());

        refreshUi();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/com/cloudkitchen/ui/app.css").toExternalForm());
        stage.setTitle("Cloud Kitchen Portal");
        stage.setScene(scene);
        stage.show();
    }

    private VBox buildHeader() {
        User.Session session = appFacade.getCurrentSession();

        Label eyebrow = new Label("Cloud Kitchen Control Center");
        eyebrow.getStyleClass().add("eyebrow");

        Label title = new Label(session.isAdminPortal()
                ? "Admin command center with complete order visibility"
                : "Customer ordering hub with personal history");
        title.getStyleClass().add("page-title");

        userContextLabel.getStyleClass().add("page-subtitle");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("secondary-button");
        logoutButton.setOnAction(event -> {
            appFacade.logout();
            showLoginScene();
        });

        HBox topRow = new HBox(12, new VBox(8, eyebrow, title, userContextLabel), new Region(), logoutButton);
        HBox.setHgrow(topRow.getChildren().get(1), Priority.ALWAYS);

        VBox header = new VBox(18, topRow);
        if (session.isAdminPortal()) {
            metricsPane.setHgap(16);
            metricsPane.setVgap(16);
            metricsPane.setManaged(true);
            metricsPane.setVisible(true);
            header.getChildren().add(metricsPane);
        } else {
            metricsPane.getChildren().clear();
            metricsPane.setManaged(false);
            metricsPane.setVisible(false);
        }
        header.setPadding(new Insets(24, 28, 10, 28));
        return header;
    }

    private TabPane buildPortalContent() {
        User.Session session = appFacade.getCurrentSession();
        TabPane tabPane = new TabPane();
        if (session.isAdminPortal()) {
            tabPane.getTabs().add(buildAdminDashboardTab());
            tabPane.getTabs().add(buildInventoryTab());
            tabPane.getTabs().add(buildMenuManagementTab());
            tabPane.getTabs().add(buildOrdersTab(true));
        } else {
            tabPane.getTabs().add(buildCustomerMenuTab());
            tabPane.getTabs().add(buildOrdersTab(false));
        }
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        BorderPane.setMargin(tabPane, new Insets(0, 28, 28, 28));
        return tabPane;
    }

    private Tab buildAdminDashboardTab() {
        adminOrderListView.setCellFactory(listView -> new OrderCell(true));
        adminOrderListView.setPlaceholder(buildEmptyLabel("No customer orders yet."));
        adminHeroBox.getStyleClass().add("card");
        adminHeroBox.getStyleClass().add("admin-hero");
        adminHeroBox.setPadding(new Insets(22));

        VBox lowStockSection = new VBox(
                10,
                buildSectionTitle("Low Stock Watchlist", "Ingredients that need immediate restocking."),
                lowStockBox);
        lowStockSection.getStyleClass().add("card");
        lowStockSection.setPadding(new Insets(18));

        VBox orderSection = new VBox(
                10,
                buildSectionTitle("Latest Customer Orders", "Admin can see every order placed across all customers."),
                adminOrderListView);
        orderSection.getStyleClass().add("card");
        orderSection.setPadding(new Insets(18));
        VBox.setVgrow(adminOrderListView, Priority.ALWAYS);

        HBox dashboardBody = new HBox(18, lowStockSection, orderSection);
        HBox.setHgrow(lowStockSection, Priority.ALWAYS);
        HBox.setHgrow(orderSection, Priority.ALWAYS);

        VBox panel = new VBox(18, adminHeroBox, dashboardBody);
        panel.setPadding(new Insets(24));

        return new Tab("Dashboard", panel);
    }

    private Tab buildInventoryTab() {
        ingredientListView.setCellFactory(listView -> new IngredientCell());
        ingredientListView.setPlaceholder(buildEmptyLabel("No active ingredients available."));
        VBox inventoryPanel = buildColumn("Ingredient Inventory", "Live quantities, thresholds, and status chips.", ingredientListView);
        VBox.setVgrow(ingredientListView, Priority.ALWAYS);

        VBox managementPanel = new VBox(16);
        managementPanel.getStyleClass().add("card");
        managementPanel.getStyleClass().add("admin-form-card");
        managementPanel.setPadding(new Insets(18));

        Label restockTitle = new Label("Restock Ingredient");
        restockTitle.getStyleClass().add("section-title");

        Label selectedIngredientLabel = new Label("No ingredient selected");
        selectedIngredientLabel.getStyleClass().add("selection-label");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity to add");

        Button restockButton = new Button("Restock Ingredient");
        restockButton.getStyleClass().add("primary-button");
        restockButton.setOnAction(event -> {
            Ingredient selectedIngredient = ingredientListView.getSelectionModel().getSelectedItem();
            if (selectedIngredient == null) {
                showError("Choose an ingredient before restocking.");
                return;
            }
            try {
                appFacade.restockIngredient(selectedIngredient.getId(), Double.parseDouble(quantityField.getText().trim()));
                quantityField.clear();
                refreshUi();
            } catch (NumberFormatException exception) {
                showError("Enter a valid numeric quantity.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        ingredientListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                selectedIngredientLabel.setText("No ingredient selected");
            } else {
                selectedIngredientLabel.setText(newValue.getName() + " currently at "
                        + DECIMAL_FORMAT.format(newValue.getQuantity()) + " " + newValue.getUnit());
            }
        });

        Separator separator = new Separator();

        TextField ingredientNameField = new TextField();
        ingredientNameField.setPromptText("Ingredient name");

        TextField ingredientUnitField = new TextField();
        ingredientUnitField.setPromptText("Unit (g/ml/pcs)");

        TextField ingredientQuantityField = new TextField();
        ingredientQuantityField.setPromptText("Opening quantity");

        TextField ingredientThresholdField = new TextField();
        ingredientThresholdField.setPromptText("Low-stock threshold");

        ingredientListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                ingredientNameField.setText(newValue.getName());
                ingredientUnitField.setText(newValue.getUnit());
                ingredientQuantityField.setText(DECIMAL_FORMAT.format(newValue.getQuantity()));
                ingredientThresholdField.setText(DECIMAL_FORMAT.format(newValue.getThresholdQuantity()));
            }
        });

        Button addIngredientButton = new Button("Add Ingredient");
        addIngredientButton.getStyleClass().add("primary-button");
        addIngredientButton.setOnAction(event -> {
            try {
                appFacade.addIngredient(
                        ingredientNameField.getText(),
                        ingredientUnitField.getText(),
                        Double.parseDouble(ingredientQuantityField.getText().trim()),
                        Double.parseDouble(ingredientThresholdField.getText().trim()));
                ingredientNameField.clear();
                ingredientUnitField.clear();
                ingredientQuantityField.clear();
                ingredientThresholdField.clear();
                refreshUi();
            } catch (NumberFormatException exception) {
                showError("Enter valid numeric values for ingredient quantity and threshold.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        Button updateIngredientButton = new Button("Update Selected Ingredient");
        updateIngredientButton.getStyleClass().add("primary-button");
        updateIngredientButton.setOnAction(event -> {
            Ingredient selectedIngredient = ingredientListView.getSelectionModel().getSelectedItem();
            if (selectedIngredient == null) {
                showError("Choose an ingredient before updating it.");
                return;
            }
            try {
                appFacade.updateIngredient(
                        selectedIngredient.getId(),
                        ingredientNameField.getText(),
                        ingredientUnitField.getText(),
                        Double.parseDouble(ingredientQuantityField.getText().trim()),
                        Double.parseDouble(ingredientThresholdField.getText().trim()));
                refreshUi();
            } catch (NumberFormatException exception) {
                showError("Enter valid numeric values for ingredient quantity and threshold.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        Button removeIngredientButton = new Button("Deactivate Ingredient");
        removeIngredientButton.getStyleClass().add("secondary-button");
        removeIngredientButton.setOnAction(event -> {
            Ingredient selectedIngredient = ingredientListView.getSelectionModel().getSelectedItem();
            if (selectedIngredient == null) {
                showError("Choose an ingredient before removing it.");
                return;
            }
            if (!confirmAction(
                    "Remove Ingredient",
                    "Remove " + selectedIngredient.getName() + " from active inventory?",
                    "Any dish depending on it will become unavailable until the ingredient is added back.")) {
                return;
            }
            try {
                appFacade.removeIngredient(selectedIngredient.getId());
                refreshUi();
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        managementPanel.getChildren().addAll(
                restockTitle,
                selectedIngredientLabel,
                quantityField,
                restockButton,
                separator,
                buildSectionTitle("Add New Ingredient", "Expand inventory with a new stock item."),
                ingredientNameField,
                ingredientUnitField,
                ingredientQuantityField,
                ingredientThresholdField,
                addIngredientButton,
                updateIngredientButton,
                removeIngredientButton);

        HBox content = new HBox(18, inventoryPanel, managementPanel);
        content.setPadding(new Insets(24));
        HBox.setHgrow(inventoryPanel, Priority.ALWAYS);

        return new Tab("Inventory", content);
    }

    private Tab buildMenuManagementTab() {
        adminDishListView.setCellFactory(listView -> new AdminDishCell());
        adminDishListView.setPlaceholder(buildEmptyLabel("No active dishes in the menu."));
        VBox menuPanel = buildColumn("Active Menu", "Live dishes currently available to customers.", adminDishListView);
        VBox.setVgrow(adminDishListView, Priority.ALWAYS);

        TextField dishNameField = new TextField();
        dishNameField.setPromptText("Dish name");

        TextField dishCategoryField = new TextField();
        dishCategoryField.setPromptText("Category");

        TextField dishPriceField = new TextField();
        dishPriceField.setPromptText("Price");

        TextArea recipeArea = new TextArea();
        recipeArea.setPromptText("Recipe format:\nBasmati Rice=220\nCooking Oil=20\nChicken=180");
        recipeArea.setPrefRowCount(7);

        adminDishListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                dishNameField.setText(newValue.getDish().getName());
                dishCategoryField.setText(newValue.getDish().getCategory());
                dishPriceField.setText(DECIMAL_FORMAT.format(newValue.getDish().getPrice()));
                recipeArea.setText(formatRecipeForEditor(newValue.getDish().getRecipeItems()));
            }
        });

        Button addDishButton = new Button("Add to Menu");
        addDishButton.getStyleClass().add("primary-button");
        addDishButton.setOnAction(event -> {
            try {
                appFacade.addDish(
                        dishNameField.getText(),
                        dishCategoryField.getText(),
                        Double.parseDouble(dishPriceField.getText().trim()),
                        parseRecipeItems(recipeArea.getText()));
                dishNameField.clear();
                dishCategoryField.clear();
                dishPriceField.clear();
                recipeArea.clear();
                refreshUi();
            } catch (NumberFormatException exception) {
                showError("Enter a valid numeric price for the dish.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        Button updateDishButton = new Button("Update Selected Dish");
        updateDishButton.getStyleClass().add("primary-button");
        updateDishButton.setOnAction(event -> {
            DishAvailability selectedDish = adminDishListView.getSelectionModel().getSelectedItem();
            if (selectedDish == null) {
                showError("Choose a dish before updating it.");
                return;
            }
            try {
                appFacade.updateDish(
                        selectedDish.getDish().getId(),
                        dishNameField.getText(),
                        dishCategoryField.getText(),
                        Double.parseDouble(dishPriceField.getText().trim()),
                        parseRecipeItems(recipeArea.getText()));
                refreshUi();
            } catch (NumberFormatException exception) {
                showError("Enter a valid numeric price for the dish.");
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        Button removeDishButton = new Button("Deactivate Dish");
        removeDishButton.getStyleClass().add("secondary-button");
        removeDishButton.setOnAction(event -> {
            DishAvailability selectedDish = adminDishListView.getSelectionModel().getSelectedItem();
            if (selectedDish == null) {
                showError("Choose a dish before removing it.");
                return;
            }
            if (!confirmAction(
                    "Remove Dish",
                    "Remove " + selectedDish.getDish().getName() + " from the active menu?",
                    "Customers will no longer see this dish in the menu.")) {
                return;
            }
            try {
                appFacade.removeDish(selectedDish.getDish().getId());
                refreshUi();
            } catch (Exception exception) {
                showError(exception.getMessage());
            }
        });

        VBox formPanel = new VBox(
                12,
                buildSectionTitle("Add New Dish", "Create a new menu item and attach its recipe."),
                dishNameField,
                dishCategoryField,
                dishPriceField,
                recipeArea,
                addDishButton,
                updateDishButton,
                removeDishButton);
        formPanel.getStyleClass().add("card");
        formPanel.getStyleClass().add("admin-form-card");
        formPanel.setPadding(new Insets(18));

        HBox content = new HBox(18, menuPanel, formPanel);
        content.setPadding(new Insets(24));
        HBox.setHgrow(menuPanel, Priority.ALWAYS);
        return new Tab("Menu Control", content);
    }

    private Tab buildCustomerMenuTab() {
        dishListView.setCellFactory(listView -> new DishCell());
        dishListView.setPlaceholder(buildEmptyLabel("No dishes are available in the menu right now."));
        customerHeroBox.getStyleClass().add("card");
        customerHeroBox.getStyleClass().add("customer-hero");
        customerHeroBox.setPadding(new Insets(22));

        VBox wrapper = new VBox(16,
                customerHeroBox,
                buildSectionTitle("Tonight's Menu", "Chef-led selections prepared fresh for the current service."),
                dishListView);
        wrapper.setPadding(new Insets(24));
        VBox.setVgrow(dishListView, Priority.ALWAYS);
        return new Tab("Menu", wrapper);
    }

    private Tab buildOrdersTab(boolean adminView) {
        ListView<OrderRecord> view = adminView ? adminOrderListView : customerOrderListView;
        view.setCellFactory(listView -> new OrderCell(adminView));
        view.setPlaceholder(buildEmptyLabel(adminView ? "No customer orders yet." : "You have not placed any orders yet."));
        VBox wrapper = new VBox(10,
                buildSectionTitle(
                        adminView ? "All Customer Orders" : "My Order History",
                        adminView
                                ? "Every customer order is visible here for monitoring and reporting."
                                : "This tab shows only the logged-in customer's order history."),
                view);
        wrapper.setPadding(new Insets(24));
        VBox.setVgrow(view, Priority.ALWAYS);
        return new Tab(adminView ? "Orders" : "My Orders", wrapper);
    }

    private void refreshUi() {
        User.Session session = appFacade.getCurrentSession();
        userContextLabel.setText("Logged in as " + session.getUser().getFullName() + " (" + session.getUser().getUsername()
                + ") - Portal: " + session.getSelectedPortal().name());

        if (session.isAdminPortal()) {
            populateAdminHero();
            InventorySummary summary = appFacade.getSummary();
            metricsPane.getChildren().setAll(
                    buildMetricCard(new MetricCardViewModel("Ingredients", String.valueOf(summary.getTotalIngredients()),
                            summary.getLowStockIngredients() + " low stock")),
                    buildMetricCard(new MetricCardViewModel("Dishes Live", summary.getAvailableDishes() + "/" + summary.getTotalDishes(),
                            "recipe-aware availability")),
                    buildMetricCard(new MetricCardViewModel("Orders Processed", String.valueOf(summary.getTotalOrders()),
                            "visible across all customers")),
                    buildMetricCard(new MetricCardViewModel("Revenue", "Rs " + DECIMAL_FORMAT.format(summary.getTotalRevenue()),
                            "tracked in SQLite")));
            ingredientListView.getItems().setAll(appFacade.getIngredients());
            adminDishListView.getItems().setAll(appFacade.getDishAvailability());
            adminOrderListView.getItems().setAll(appFacade.getRecentOrders(20));
            populateLowStockBox();
        } else {
            populateCustomerHero();
            dishListView.getItems().setAll(appFacade.getDishAvailability());
            customerOrderListView.getItems().setAll(appFacade.getCurrentCustomerOrders(20));
        }
    }

    private void populateCustomerHero() {
        Label title = new Label("A refined table experience, prepared on demand");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "Browse signature dishes, place your order quietly, and revisit only your own dining history.");
        subtitle.getStyleClass().add("page-subtitle");

        Label serviceNote = new Label("Seasonal menu | Small-batch preparation | Customer privacy first");
        serviceNote.getStyleClass().add("customer-hero-note");

        customerHeroBox.getChildren().setAll(title, subtitle, serviceNote);
    }

    private void populateAdminHero() {
        Label title = new Label("Manage menu, stock, and customer activity");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label(
                "Use this control panel to keep the menu current, maintain ingredient availability, and monitor order flow.");
        subtitle.getStyleClass().add("page-subtitle");

        Label note = new Label("Admin tools | Inventory control | Menu operations");
        note.getStyleClass().add("admin-hero-note");

        adminHeroBox.getChildren().setAll(title, subtitle, note);
    }

    private void populateLowStockBox() {
        lowStockBox.getChildren().clear();
        List<Ingredient> lowStockIngredients = appFacade.getLowStockIngredients();
        if (lowStockIngredients.isEmpty()) {
            Label healthyLabel = new Label("Inventory is healthy. No ingredients are currently below threshold.");
            healthyLabel.getStyleClass().add("good-status");
            lowStockBox.getChildren().add(wrapCard(healthyLabel));
            return;
        }
        for (Ingredient ingredient : lowStockIngredients) {
            Label row = new Label(ingredient.getName() + " - "
                    + DECIMAL_FORMAT.format(ingredient.getQuantity()) + " " + ingredient.getUnit()
                    + " left - threshold " + DECIMAL_FORMAT.format(ingredient.getThresholdQuantity()));
            row.getStyleClass().add("warning-status");
            lowStockBox.getChildren().add(wrapCard(row));
        }
    }

    private VBox buildColumn(String title, String subtitle, Region content) {
        VBox box = new VBox(10, buildSectionTitle(title, subtitle), content);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(18));
        return box;
    }

    private VBox buildSectionTitle(String title, String subtitle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("section-subtitle");

        return new VBox(4, titleLabel, subtitleLabel);
    }

    private StackPane wrapCard(Label label) {
        StackPane pane = new StackPane(label);
        pane.getStyleClass().add("mini-card");
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        pane.setPadding(new Insets(14, 16, 14, 16));
        return pane;
    }

    private VBox buildMetricCard(MetricCardViewModel card) {
        Label label = new Label(card.getLabel());
        label.getStyleClass().add("metric-label");
        Label value = new Label(card.getValue());
        value.getStyleClass().add("metric-value");
        Label hint = new Label(card.getHint());
        hint.getStyleClass().add("metric-hint");
        VBox box = new VBox(8, label, value, hint);
        box.getStyleClass().add("metric-card");
        box.setMinWidth(220);
        return box;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Action could not be completed");
        alert.setHeaderText("Please review the request");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean confirmAction(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    private Label buildEmptyLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-text");
        return label;
    }

    private class IngredientCell extends ListCell<Ingredient> {
        @Override
        protected void updateItem(Ingredient item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label name = new Label(item.getName());
            name.getStyleClass().add("row-title");
            Label meta = new Label("In stock: " + DECIMAL_FORMAT.format(item.getQuantity()) + " " + item.getUnit()
                    + " - Threshold: " + DECIMAL_FORMAT.format(item.getThresholdQuantity()));
            meta.getStyleClass().add(item.isLowStock() ? "warning-status" : "muted-text");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label status = new Label(item.isLowStock() ? "LOW" : "OK");
            status.getStyleClass().add(item.isLowStock() ? "chip-warning" : "chip-ok");
            HBox row = new HBox(12, new VBox(4, name, meta), spacer, status);
            row.getStyleClass().add("list-row");
            row.setAlignment(Pos.CENTER_LEFT);
            setGraphic(row);
        }
    }

    private class DishCell extends ListCell<DishAvailability> {
        @Override
        protected void updateItem(DishAvailability item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label title = new Label(item.getDish().getName());
            title.getStyleClass().add("row-title");

            Label course = new Label(item.getDish().getCategory().toUpperCase());
            course.getStyleClass().add("menu-course");

            Label meta = new Label("Rs " + DECIMAL_FORMAT.format(item.getDish().getPrice()));
            meta.getStyleClass().add("muted-text");

            Label description = new Label(buildDishDescription(item));
            description.getStyleClass().add("recipe-text");
            description.setWrapText(true);

            Label availability = new Label(item.isAvailable() ? "Available for service" : "Sold out for this service");
            availability.getStyleClass().add(item.isAvailable() ? "good-status" : "warning-status");

            Spinner<Integer> quantitySpinner = new Spinner<>();
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    1, Math.max(item.getMaxServingsPossible(), 1), 1));
            quantitySpinner.setDisable(!item.isAvailable());

            Button orderButton = new Button(item.isAvailable() ? "Place Order" : "Unavailable");
            orderButton.getStyleClass().add(item.isAvailable() ? "primary-button" : "secondary-button");
            orderButton.setDisable(!item.isAvailable());
            orderButton.setOnAction(event -> {
                try {
                    appFacade.placeOrder(item.getDish().getId(), quantitySpinner.getValue());
                    refreshUi();
                } catch (Exception exception) {
                    showError(exception.getMessage());
                }
            });

            HBox controls = new HBox(10, quantitySpinner, orderButton);
            controls.setAlignment(Pos.CENTER_LEFT);

            VBox content = new VBox(10, course, title, meta, description, availability, controls);
            content.getStyleClass().add("list-row");
            content.getStyleClass().add("menu-card");
            setGraphic(content);
        }
    }

    private class AdminDishCell extends ListCell<DishAvailability> {
        @Override
        protected void updateItem(DishAvailability item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label title = new Label(item.getDish().getName());
            title.getStyleClass().add("row-title");
            Label meta = new Label(item.getDish().getCategory() + " - Rs "
                    + DECIMAL_FORMAT.format(item.getDish().getPrice())
                    + " - " + (item.isAvailable() ? "Live" : "Out of stock"));
            meta.getStyleClass().add(item.isAvailable() ? "muted-text" : "warning-status");
            Label recipe = new Label(formatRecipe(item.getIngredientStatuses()));
            recipe.getStyleClass().add("recipe-text");
            recipe.setWrapText(true);
            VBox row = new VBox(6, title, meta, recipe);
            row.getStyleClass().add("list-row");
            setGraphic(row);
        }
    }

    private class OrderCell extends ListCell<OrderRecord> {
        private final boolean adminView;

        private OrderCell(boolean adminView) {
            this.adminView = adminView;
        }

        @Override
        protected void updateItem(OrderRecord item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            Label title = new Label(item.getDishName() + " x" + item.getQuantity());
            title.getStyleClass().add("row-title");
            String extra = adminView ? " - Customer: " + item.getCustomerName() : "";
            Label meta = new Label("Rs " + DECIMAL_FORMAT.format(item.getTotalPrice()) + extra
                    + " - " + item.getOrderedAt().format(ORDER_TIME_FORMAT));
            meta.getStyleClass().add("muted-text");
            VBox row = new VBox(4, title, meta);
            row.getStyleClass().add("list-row");
            setGraphic(row);
        }
    }

    private String formatRecipe(List<IngredientRequirementStatus> statuses) {
        StringBuilder builder = new StringBuilder("Recipe: ");
        for (int index = 0; index < statuses.size(); index++) {
            IngredientRequirementStatus status = statuses.get(index);
            if (index > 0) {
                builder.append(" | ");
            }
            builder.append(status.getIngredientName())
                    .append(" ")
                    .append(DECIMAL_FORMAT.format(status.getRequiredQuantity()))
                    .append(status.getUnit())
                    .append(" (stock ")
                    .append(DECIMAL_FORMAT.format(status.getAvailableQuantity()))
                    .append(status.getUnit())
                    .append(")");
        }
        return builder.toString();
    }

    private String buildDishDescription(DishAvailability item) {
        String dishName = item.getDish().getName().toLowerCase();
        if (dishName.contains("biryani")) {
            return "Slow-layered aromatics, delicate spice depth, and a composed finish designed for a rich dinner service.";
        }
        if (dishName.contains("paneer")) {
            return "A refined vegetarian signature with a balanced texture, warm spice profile, and elegant plating character.";
        }
        if (dishName.contains("fried rice")) {
            return "Wok-finished comfort with polished seasoning, restrained richness, and a lighter premium feel.";
        }
        if (dishName.contains("chicken")) {
            return "A bold chef-crafted plate with savory depth, polished heat, and a generous main-course presence.";
        }
        return "Chef-curated preparation with a premium finish and an intentionally balanced flavor profile.";
    }

    private String formatRecipeForEditor(List<RecipeItem> recipeItems) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < recipeItems.size(); index++) {
            RecipeItem recipeItem = recipeItems.get(index);
            if (index > 0) {
                builder.append("\n");
            }
            builder.append(recipeItem.getIngredientName())
                    .append("=")
                    .append(DECIMAL_FORMAT.format(recipeItem.getRequiredQuantity()));
        }
        return builder.toString();
    }

    private List<RecipeItem> parseRecipeItems(String recipeText) {
        if (recipeText == null || recipeText.isBlank()) {
            throw new IllegalArgumentException("Recipe is required. Use one line per ingredient as Name=Quantity.");
        }
        List<Ingredient> ingredients = appFacade.getIngredients();
        List<RecipeItem> recipeItems = new ArrayList<>();
        String[] lines = recipeText.split("\\R");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("=");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Recipe format must be Ingredient Name=Quantity.");
            }
            String ingredientName = parts[0].trim();
            double quantity;
            try {
                quantity = Double.parseDouble(parts[1].trim());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("Invalid quantity for ingredient: " + ingredientName);
            }
            Ingredient ingredient = ingredients.stream()
                    .filter(item -> item.getName().equalsIgnoreCase(ingredientName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown ingredient in recipe: " + ingredientName));
            recipeItems.add(new RecipeItem(ingredient.getId(), ingredient.getName(), ingredient.getUnit(), quantity));
        }
        if (recipeItems.isEmpty()) {
            throw new IllegalArgumentException("Recipe must contain at least one ingredient.");
        }
        return recipeItems;
    }
}
