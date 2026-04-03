package com.hotel.view;

import com.hotel.dao.*;
import com.hotel.service.BookingService;
import com.hotel.service.HousekeepingService;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * DashboardController — The main application shell after splash.
 *
 * Layout:
 *   ┌──────────────────────────────────────────────────┐
 *   │  TOP BAR: Logo + Hotel Name + Clock + User       │
 *   ├────────┬─────────────────────────────────────────┤
 *   │        │                                         │
 *   │  SIDE  │          CONTENT AREA                   │
 *   │  BAR   │   (swaps panels based on nav click)     │
 *   │        │                                         │
 *   └────────┴─────────────────────────────────────────┘
 *
 * Navigation order (as per requirements):
 *   1. Check-In
 *   2. Available Rooms
 *   3. Check-Out
 *   4. Housekeeping
 *   5. À La Carte
 *   6. Customer Details
 *   7. Reviews
 *
 * @author Grand Vista HMS
 */
public class DashboardController {

    // ── Shared DAO / Service instances ──────────────────────────
    private final RoomDAO roomDAO = new RoomDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final HousekeepingService housekeepingService = new HousekeepingService();
    private final BookingService bookingService = new BookingService(bookingDAO, roomDAO, housekeepingService);

    // ── Layout nodes ──────────────────────────────────────────
    private StackPane contentArea;
    private Button activeNavBtn;

    // ── Color palette ─────────────────────────────────────────
    private static final String BG_DARK       = "#0f1117";
    private static final String BG_SIDEBAR    = "#13161f";
    private static final String BG_CONTENT    = "#181c27";
    private static final String ACCENT_GOLD   = "#c9a96e";
    private static final String ACCENT_BLUE   = "#4a90d9";
    private static final String TEXT_PRIMARY  = "#e8e8f0";
    private static final String TEXT_MUTED    = "#5a6a80";
    private static final String NAV_HOVER     = "rgba(201,169,110,0.1)";
    private static final String NAV_ACTIVE    = "rgba(201,169,110,0.18)";

    /**
     * Builds the complete dashboard Scene.
     */
    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_DARK + ";");

        // ── Top Bar ──────────────────────────────────────────
        root.setTop(createTopBar());

        // ── Left Sidebar ─────────────────────────────────────
        root.setLeft(createSidebar());

        // ── Content Area ─────────────────────────────────────
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: " + BG_CONTENT + ";");
        root.setCenter(contentArea);

        // Default panel: Check-In (most important)
        showPanel(createCheckInPanel());

        Scene scene = new Scene(root);
        try {
            String css = getClass().getResource("/com/hotel/css/dashboard.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ignored) { /* CSS is supplementary */ }

        return scene;
    }

    // ============================================================
    // TOP BAR
    // ============================================================

    private HBox createTopBar() {
        HBox bar = new HBox();
        bar.setPrefHeight(64);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setStyle(
            "-fx-background-color: " + BG_SIDEBAR + ";" +
            "-fx-border-color: rgba(201,169,110,0.15);" +
            "-fx-border-width: 0 0 1 0;"
        );

        // Logo circle
        Circle logoCircle = new Circle(22);
        logoCircle.setFill(Color.web(ACCENT_GOLD));
        Label logoLetter = new Label("GV");
        logoLetter.setStyle("-fx-text-fill: #0a0a1a; -fx-font-weight: bold; -fx-font-size: 14px;");
        StackPane logo = new StackPane(logoCircle, logoLetter);

        // Hotel name
        VBox nameBox = new VBox(2);
        Label name = new Label("Grand Vista");
        name.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold; -fx-font-family: Georgia;");
        Label sub = new Label("Hotel Management System");
        sub.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px; -fx-letter-spacing: 2px;");
        nameBox.getChildren().addAll(name, sub);
        HBox.setMargin(nameBox, new Insets(0, 0, 0, 12));

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Live clock + date
        VBox clockBox = createLiveClock();

        // Divider
        Rectangle div = new Rectangle(1, 36);
        div.setFill(Color.web(ACCENT_GOLD, 0.2));
        HBox.setMargin(div, new Insets(0, 20, 0, 20));

        // Admin badge
        HBox adminBadge = createAdminBadge();

        bar.getChildren().addAll(logo, nameBox, spacer, clockBox, div, adminBadge);
        return bar;
    }

    private VBox createLiveClock() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_RIGHT);

        Label timeLabel = new Label();
        timeLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 20px; -fx-font-family: 'Courier New'; -fx-font-weight: bold;");

        Label dateLabel = new Label();
        dateLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");

        // Update every second
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeLabel.setText(java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH : mm : ss")));
            dateLabel.setText(LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();

        // Initial values
        timeLabel.setText(java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH : mm : ss")));
        dateLabel.setText(LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));

        box.getChildren().addAll(timeLabel, dateLabel);
        return box;
    }

    private HBox createAdminBadge() {
        HBox badge = new HBox(10);
        badge.setAlignment(Pos.CENTER);

        Circle avatar = new Circle(18);
        avatar.setFill(Color.web(ACCENT_BLUE, 0.3));
        avatar.setStroke(Color.web(ACCENT_BLUE, 0.5));
        avatar.setStrokeWidth(1.5);

        Label icon = new Label("👤");
        icon.setStyle("-fx-font-size: 14px;");
        StackPane avatarPane = new StackPane(avatar, icon);

        VBox info = new VBox(2);
        Label adminName = new Label("Admin");
        adminName.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 13px; -fx-font-weight: bold;");
        Label role = new Label("Front Desk Manager");
        role.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 10px;");
        info.getChildren().addAll(adminName, role);

        badge.getChildren().addAll(avatarPane, info);
        return badge;
    }

    // ============================================================
    // SIDEBAR NAVIGATION
    // ============================================================

    private VBox createSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(24, 12, 24, 12));
        sidebar.setStyle("-fx-background-color: " + BG_SIDEBAR + ";" +
                         "-fx-border-color: rgba(201,169,110,0.1);" +
                         "-fx-border-width: 0 1 0 0;");

        // Section label
        Label navLabel = new Label("NAVIGATION");
        navLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 9px; -fx-letter-spacing: 3px;");
        VBox.setMargin(navLabel, new Insets(0, 0, 12, 8));

        // Navigation items — in specified order
        Button[] navBtns = {
            createNavButton("🏨", "Check-In",         () -> showPanel(createCheckInPanel())),
            createNavButton("🛏", "Available Rooms",   () -> showPanel(createAvailableRoomsPanel())),
            createNavButton("🚪", "Check-Out",         () -> showPanel(createCheckOutPanel())),
            createNavButton("🧹", "Housekeeping",      () -> showPanel(createHousekeepingPanel())),
            createNavButton("🍽", "À La Carte",        () -> showPanel(createAlaCartePanel())),
            createNavButton("👤", "Customer Details",  () -> showPanel(createCustomerDetailsPanel())),
            createNavButton("⭐", "Reviews",           () -> showPanel(createReviewsPanel())),
        };

        sidebar.getChildren().add(navLabel);

        for (Button btn : navBtns) {
            sidebar.getChildren().add(btn);
        }

        // Activate Check-In by default
        setActiveButton(navBtns[0]);

        // Bottom: hotel status strip
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox statusBox = createSidebarStatus();
        sidebar.getChildren().addAll(spacer, statusBox);

        return sidebar;
    }

    private Button createNavButton(String icon, String label, Runnable action) {
        Button btn = new Button(icon + "  " + label);
        btn.setPrefWidth(196);
        btn.setPrefHeight(44);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 14));
        applyNavStyle(btn, false);

        btn.setOnMouseEntered(e -> { if (btn != activeNavBtn) applyNavStyle(btn, true); });
        btn.setOnMouseExited(e -> { if (btn != activeNavBtn) applyNavStyle(btn, false); });

        btn.setOnAction(e -> {
            setActiveButton(btn);
            action.run();
        });

        return btn;
    }

    private void applyNavStyle(Button btn, boolean hover) {
        String bg = hover ? "rgba(201,169,110,0.07)" : "transparent";
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + (hover ? TEXT_PRIMARY : TEXT_MUTED) + ";" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;"
        );
    }

    private void setActiveButton(Button btn) {
        if (activeNavBtn != null) applyNavStyle(activeNavBtn, false);
        activeNavBtn = btn;
        btn.setStyle(
            "-fx-background-color: rgba(201,169,110,0.15);" +
            "-fx-text-fill: " + ACCENT_GOLD + ";" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;" +
            "-fx-border-color: rgba(201,169,110,0.4);" +
            "-fx-border-radius: 8px;" +
            "-fx-border-width: 0 0 0 2;" +
            "-fx-cursor: hand;"
        );
    }

    private VBox createSidebarStatus() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: rgba(201,169,110,0.07);" +
            "-fx-background-radius: 10px;" +
            "-fx-border-color: rgba(201,169,110,0.15);" +
            "-fx-border-radius: 10px;"
        );

        Label title = new Label("Hotel Status");
        title.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 11px; -fx-font-weight: bold;");

        int totalRooms = roomDAO.findAll().size();
        int available  = roomDAO.findAvailable().size();
        int occupied   = totalRooms - available;

        Label avail = new Label("✅  Available: " + available);
        avail.setStyle("-fx-text-fill: #4caf80; -fx-font-size: 12px;");

        Label occ = new Label("🔴  Occupied:  " + occupied);
        occ.setStyle("-fx-text-fill: #e06c75; -fx-font-size: 12px;");

        Label total = new Label("🏨  Total:        " + totalRooms);
        total.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        box.getChildren().addAll(title, avail, occ, total);
        return box;
    }

    // ============================================================
    // CONTENT PANEL SWITCHING
    // ============================================================

    private void showPanel(Node panel) {
        contentArea.getChildren().clear();
        panel.setOpacity(0);
        contentArea.getChildren().add(panel);

        FadeTransition ft = new FadeTransition(Duration.millis(220), panel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ============================================================
    // PANEL BUILDERS — Each returns a Node to swap into contentArea
    // ============================================================

    /** Shared panel header template */
    private VBox buildPanelHeader(String icon, String title, String subtitle) {
        VBox header = new VBox(4);
        header.setPadding(new Insets(32, 36, 20, 36));
        header.setStyle("-fx-border-color: rgba(201,169,110,0.1); -fx-border-width: 0 0 1 0;");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28px;");

        VBox textBox = new VBox(2);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 22px; -fx-font-weight: bold; -fx-font-family: Georgia;");
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");
        textBox.getChildren().addAll(titleLabel, subLabel);

        titleRow.getChildren().addAll(iconLabel, textBox);
        header.getChildren().add(titleRow);
        return header;
    }

    /** Styled form field */
    private VBox formField(String labelText, Control field) {
        VBox box = new VBox(6);
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px; -fx-letter-spacing: 1px;");
        field.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: rgba(201,169,110,0.2);" +
            "-fx-border-radius: 6px;" +
            "-fx-background-radius: 6px;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-font-size: 13px;" +
            "-fx-pref-height: 40px;"
        );
        box.getChildren().addAll(lbl, field);
        return box;
    }

    /** Primary action button (green) */
    private Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(44);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to right, #3a7d44, #4caf80);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0 28 0 28;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.88));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    /** Danger action button (red) */
    private Button dangerButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(44);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to right, #b03a3a, #e06c75);" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0 28 0 28;"
        );
        btn.setOnMouseEntered(e -> btn.setOpacity(0.88));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    /** Styled TableView */
    private <T> TableView<T> styledTable() {
        TableView<T> table = new TableView<>();
        table.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-control-inner-background: rgba(255,255,255,0.02);" +
            "-fx-table-header-border-color: rgba(201,169,110,0.15);" +
            "-fx-faint-focus-color: transparent;"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    /** Creates a styled table column */
    private <S, T> TableColumn<S, T> col(String title, String property) {
        TableColumn<S, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>(property));
        col.setStyle("-fx-font-size: 13px;");
        return col;
    }

    /** Shows an informational alert */
    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ============================================================
    // 1. CHECK-IN PANEL
    // ============================================================

    private Node createCheckInPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("🏨", "Check-In", "Register a new guest and assign a room");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox form = new VBox(20);
        form.setPadding(new Insets(28, 36, 28, 36));
        form.setMaxWidth(860);

        // ── Section: Guest Information ─────────────────────────
        Label guestSec = sectionLabel("GUEST INFORMATION");
        GridPane guestGrid = new GridPane();
        guestGrid.setHgap(20);
        guestGrid.setVgap(16);

        TextField nameField  = new TextField(); nameField.setPromptText("Full Name");
        TextField emailField = new TextField(); emailField.setPromptText("Email Address");
        TextField phoneField = new TextField(); phoneField.setPromptText("Phone Number");
        ComboBox<String> idTypeBox = new ComboBox<>();
        idTypeBox.getItems().addAll("PASSPORT", "AADHAR", "DRIVING_LICENSE", "PAN");
        idTypeBox.setPromptText("ID Proof Type");
        idTypeBox.setPrefWidth(Double.MAX_VALUE);
        TextField idNumField = new TextField(); idNumField.setPromptText("ID Proof Number");
        TextField addressField = new TextField(); addressField.setPromptText("Address");
        TextField nationalityField = new TextField("Indian"); nationalityField.setPromptText("Nationality");

        guestGrid.add(formField("FULL NAME *", nameField), 0, 0);
        guestGrid.add(formField("EMAIL ADDRESS *", emailField), 1, 0);
        guestGrid.add(formField("PHONE NUMBER *", phoneField), 0, 1);
        guestGrid.add(formField("NATIONALITY", nationalityField), 1, 1);
        guestGrid.add(formField("ID PROOF TYPE *", idTypeBox), 0, 2);
        guestGrid.add(formField("ID PROOF NUMBER *", idNumField), 1, 2);
        guestGrid.add(formField("ADDRESS", addressField), 0, 3, 2, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        guestGrid.getColumnConstraints().addAll(cc, cc);

        // ── Section: Room & Dates ──────────────────────────────
        Label roomSec = sectionLabel("ROOM & DATES");
        GridPane roomGrid = new GridPane();
        roomGrid.setHgap(20);
        roomGrid.setVgap(16);
        roomGrid.getColumnConstraints().addAll(cc, cc);

        ComboBox<String> roomBox = new ComboBox<>();
        roomBox.setPromptText("Select Available Room");
        roomBox.setPrefWidth(Double.MAX_VALUE);
        refreshRoomCombo(roomBox);

        DatePicker checkInPicker  = new DatePicker(LocalDate.now());
        DatePicker checkOutPicker = new DatePicker(LocalDate.now().plusDays(1));
        checkInPicker.setPrefWidth(Double.MAX_VALUE);
        checkOutPicker.setPrefWidth(Double.MAX_VALUE);

        Spinner<Integer> guestSpinner = new Spinner<>(1, 10, 1);
        guestSpinner.setPrefWidth(Double.MAX_VALUE);

        TextArea specialReqArea = new TextArea();
        specialReqArea.setPromptText("Any special requests or notes...");
        specialReqArea.setPrefHeight(70);
        specialReqArea.setWrapText(true);

        roomGrid.add(formField("SELECT ROOM *", roomBox), 0, 0);
        roomGrid.add(formField("NUMBER OF GUESTS", guestSpinner), 1, 0);
        roomGrid.add(formField("CHECK-IN DATE *", checkInPicker), 0, 1);
        roomGrid.add(formField("CHECK-OUT DATE *", checkOutPicker), 1, 1);
        roomGrid.add(formField("SPECIAL REQUESTS", specialReqArea), 0, 2, 2, 1);

        // Style DatePickers
        styleControl(checkInPicker);
        styleControl(checkOutPicker);
        styleControl(guestSpinner);
        styleControl(specialReqArea);

        // ── Price Preview ──────────────────────────────────────
        Label priceLabel = new Label("Estimated Bill: —");
        priceLabel.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Update price preview dynamically
        Runnable updatePrice = () -> {
            try {
                String roomStr = roomBox.getValue();
                if (roomStr == null) return;
                int roomId = extractRoomId(roomStr);
                roomDAO.findById(roomId).ifPresent(room -> {
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(
                            checkInPicker.getValue(), checkOutPicker.getValue());
                    if (nights > 0) {
                        double est = room.calculateBill((int) nights);
                        priceLabel.setText(String.format(
                            "Estimated Total: ₹%,.2f  (%d nights × ₹%.0f + charges)",
                            est, nights, room.getPricePerNight()));
                    }
                });
            } catch (Exception ignored) {}
        };

        roomBox.setOnAction(e -> updatePrice.run());
        checkInPicker.setOnAction(e -> updatePrice.run());
        checkOutPicker.setOnAction(e -> updatePrice.run());

        // ── Action Buttons ─────────────────────────────────────
        Button checkInBtn = primaryButton("✓  CONFIRM CHECK-IN");
        checkInBtn.setPrefWidth(220);

        Button clearBtn = new Button("↺  Clear Form");
        clearBtn.setPrefHeight(44);
        clearBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + ";" +
                          "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8px; -fx-cursor: hand;");
        clearBtn.setOnAction(e -> {
            nameField.clear(); emailField.clear(); phoneField.clear();
            idNumField.clear(); addressField.clear(); specialReqArea.clear();
            idTypeBox.setValue(null); roomBox.setValue(null);
            nationalityField.setText("Indian");
            checkInPicker.setValue(LocalDate.now());
            checkOutPicker.setValue(LocalDate.now().plusDays(1));
            guestSpinner.getValueFactory().setValue(1);
            priceLabel.setText("Estimated Bill: —");
            refreshRoomCombo(roomBox);
        });

        checkInBtn.setOnAction(e -> handleCheckIn(
            nameField, emailField, phoneField, idTypeBox, idNumField,
            addressField, nationalityField, roomBox,
            checkInPicker, checkOutPicker, guestSpinner, specialReqArea, priceLabel, roomBox
        ));

        HBox btnRow = new HBox(16, checkInBtn, clearBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        form.getChildren().addAll(
            guestSec, guestGrid,
            new Separator() {{ setStyle("-fx-background-color: rgba(201,169,110,0.1);"); }},
            roomSec, roomGrid,
            priceLabel, btnRow
        );

        scroll.setContent(form);
        root.getChildren().addAll(header, scroll);
        return root;
    }

    private void handleCheckIn(TextField nameF, TextField emailF, TextField phoneF,
                                ComboBox<String> idTypeBox, TextField idNumF,
                                TextField addressF, TextField natF,
                                ComboBox<String> roomBox,
                                DatePicker checkIn, DatePicker checkOut,
                                Spinner<Integer> guests, TextArea specialReq,
                                Label priceLabel, ComboBox<String> refreshTarget) {
        // Validation
        if (nameF.getText().trim().isEmpty() || emailF.getText().trim().isEmpty() ||
            phoneF.getText().trim().isEmpty() || idTypeBox.getValue() == null ||
            idNumF.getText().trim().isEmpty() || roomBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                      "Please fill all mandatory fields marked with *");
            return;
        }

        if (!emailF.getText().contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Invalid Email", "Please enter a valid email address.");
            return;
        }

        if (checkOut.getValue().isBefore(checkIn.getValue()) ||
            checkOut.getValue().isEqual(checkIn.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Dates",
                      "Check-out date must be after check-in date.");
            return;
        }

        try {
            // Save customer
            com.hotel.model.Customer customer = new com.hotel.model.Customer();
            customer.setFullName(nameF.getText().trim());
            customer.setEmail(emailF.getText().trim());
            customer.setPhone(phoneF.getText().trim());
            customer.setIdProofType(idTypeBox.getValue());
            customer.setIdProofNumber(idNumF.getText().trim());
            customer.setAddress(addressF.getText().trim());
            customer.setNationality(natF.getText().trim().isEmpty() ? "Indian" : natF.getText().trim());

            int customerId = customerDAO.save(customer);
            if (customerId <= 0) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register customer. Email may already exist.");
                return;
            }

            int roomId = extractRoomId(roomBox.getValue());

            com.hotel.model.Booking booking = bookingService.bookRoom(
                customerId, roomId,
                checkIn.getValue(), checkOut.getValue(),
                guests.getValue(), specialReq.getText()
            );

            showAlert(Alert.AlertType.INFORMATION, "Check-In Successful! 🎉",
                "Welcome to Grand Vista!\n\n" +
                "Booking Reference: " + booking.getBookingRef() + "\n" +
                "Guest: " + nameF.getText() + "\n" +
                "Room: " + roomBox.getValue() + "\n" +
                "Estimated Bill: " + booking.getFormattedAmount());

            // Clear form
            refreshRoomCombo(refreshTarget);
            priceLabel.setText("Estimated Bill: —");

        } catch (IllegalStateException ex) {
            showAlert(Alert.AlertType.ERROR, "Booking Conflict", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Check-in failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ============================================================
    // 2. AVAILABLE ROOMS PANEL (Room Cards)
    // ============================================================

    private Node createAvailableRoomsPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("🛏", "Available Rooms",
                "Browse and book from our collection of rooms");

        // Filter bar
        HBox filterBar = new HBox(12);
        filterBar.setPadding(new Insets(16, 36, 16, 36));
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-border-color: rgba(201,169,110,0.08); -fx-border-width: 0 0 1 0;");

        Label filterLabel = new Label("Filter:");
        filterLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 12px;");

        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("All Types", "STANDARD", "DELUXE", "SUITE", "PENTHOUSE");
        typeFilter.setValue("All Types");
        typeFilter.setStyle("-fx-background-color: rgba(255,255,255,0.05); " +
                            "-fx-border-color: rgba(201,169,110,0.2); -fx-border-radius: 6px; " +
                            "-fx-text-fill: " + TEXT_PRIMARY + ";");

        ComboBox<String> availFilter = new ComboBox<>();
        availFilter.getItems().addAll("All Rooms", "Available Only", "Occupied Only");
        availFilter.setValue("All Rooms");
        availFilter.setStyle(typeFilter.getStyle());

        filterBar.getChildren().addAll(filterLabel, typeFilter, availFilter);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        FlowPane cardsPane = new FlowPane(16, 16);
        cardsPane.setPadding(new Insets(24, 36, 24, 36));

        Runnable loadCards = () -> {
            cardsPane.getChildren().clear();
            java.util.List<com.hotel.model.Room> rooms = roomDAO.findAll();
            for (com.hotel.model.Room room : rooms) {
                String typeVal = typeFilter.getValue();
                if (!"All Types".equals(typeVal) && !room.getRoomType().name().equals(typeVal)) continue;
                String availVal = availFilter.getValue();
                if ("Available Only".equals(availVal) && !room.isAvailable()) continue;
                if ("Occupied Only".equals(availVal) && room.isAvailable()) continue;
                cardsPane.getChildren().add(createRoomCard(room));
            }
            if (cardsPane.getChildren().isEmpty()) {
                Label empty = new Label("No rooms match the selected filter.");
                empty.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 14px;");
                cardsPane.getChildren().add(empty);
            }
        };

        typeFilter.setOnAction(e -> loadCards.run());
        availFilter.setOnAction(e -> loadCards.run());
        loadCards.run();

        scroll.setContent(cardsPane);
        root.getChildren().addAll(header, filterBar, scroll);
        return root;
    }

    private VBox createRoomCard(com.hotel.model.Room room) {
        VBox card = new VBox(12);
        card.setPrefWidth(280);
        card.setPadding(new Insets(20));
        boolean avail = room.isAvailable();

        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.03);" +
            "-fx-border-color: " + (avail ? "rgba(76,175,80,0.25)" : "rgba(224,108,117,0.25)") + ";" +
            "-fx-border-radius: 12px;" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 4);"
        );

        // Status badge
        Label statusBadge = new Label(avail ? "● AVAILABLE" : "● OCCUPIED");
        statusBadge.setPadding(new Insets(3, 10, 3, 10));
        statusBadge.setStyle(
            "-fx-background-color: " + (avail ? "rgba(76,175,80,0.15)" : "rgba(224,108,117,0.15)") + ";" +
            "-fx-text-fill: " + (avail ? "#4caf80" : "#e06c75") + ";" +
            "-fx-background-radius: 20px;" +
            "-fx-font-size: 10px;" +
            "-fx-font-weight: bold;"
        );

        // Room number
        Label roomNum = new Label(room.getRoomType().getIcon() + "  Room " + room.getRoomNumber());
        roomNum.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Type
        Label type = new Label(room.getRoomType().getDisplayName());
        type.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 12px;");

        // Price
        Label price = new Label(String.format("₹%,.0f / night", room.getPricePerNight()));
        price.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        // Details row
        HBox details = new HBox(12);
        details.getChildren().addAll(
            detailChip("Floor " + room.getFloor()),
            detailChip("👥 " + room.getCapacity() + " guests")
        );

        // Amenities
        Label amenLabel = new Label(room.getAmenitiesDisplay());
        amenLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
        amenLabel.setWrapText(true);

        // Description
        Label desc = new Label(room.getDescription());
        desc.setStyle("-fx-text-fill: rgba(200,200,220,0.5); -fx-font-size: 11px; -fx-font-style: italic;");
        desc.setWrapText(true);

        card.getChildren().addAll(statusBadge, roomNum, type, price, details, amenLabel, desc);

        if (avail) {
            Button bookBtn = primaryButton("Book This Room");
            bookBtn.setPrefWidth(Double.MAX_VALUE);
            bookBtn.setOnAction(e -> showAlert(Alert.AlertType.INFORMATION, "Book Room",
                "To book Room " + room.getRoomNumber() + ", please use the Check-In panel."));
            card.getChildren().add(bookBtn);
        }

        // Hover lift effect
        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setStyle(card.getStyle().replace("rgba(0,0,0,0.3), 10", "rgba(0,0,0,0.5), 18"));
        });
        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
        });

        return card;
    }

    private Label detailChip(String text) {
        Label chip = new Label(text);
        chip.setPadding(new Insets(3, 10, 3, 10));
        chip.setStyle(
            "-fx-background-color: rgba(255,255,255,0.05);" +
            "-fx-text-fill: " + TEXT_MUTED + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-font-size: 11px;"
        );
        return chip;
    }

    // ============================================================
    // 3. CHECK-OUT PANEL
    // ============================================================

    private Node createCheckOutPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("🚪", "Check-Out",
                "Process guest checkout and generate final bill");

        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 36, 32, 36));

        // Search field
        Label searchLabel = sectionLabel("FIND BOOKING");
        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Enter Booking Reference (e.g. GV-2025-00123) or Guest Name");
        searchField.setPrefWidth(420);
        styleControl(searchField);

        Button searchBtn = new Button("🔍  Search");
        searchBtn.setPrefHeight(40);
        searchBtn.setStyle("-fx-background-color: rgba(74,144,217,0.2);" +
                           "-fx-border-color: rgba(74,144,217,0.4); -fx-border-radius: 6px;" +
                           "-fx-text-fill: " + ACCENT_BLUE + "; -fx-cursor: hand; -fx-padding: 0 20 0 20;");

        searchRow.getChildren().addAll(searchField, searchBtn);

        // Active bookings table
        Label activeLabel = sectionLabel("ACTIVE BOOKINGS");
        TableView<com.hotel.model.Booking> table = styledTable();

        TableColumn<com.hotel.model.Booking, String> refCol    = col("Booking Ref", "bookingRef");
        TableColumn<com.hotel.model.Booking, String> guestCol  = col("Guest Name",  "customerName");
        TableColumn<com.hotel.model.Booking, String> roomCol   = col("Room",        "roomNumber");
        TableColumn<com.hotel.model.Booking, String> typeCol   = col("Type",        "roomType");
        TableColumn<com.hotel.model.Booking, String> inCol     = col("Check-In",   "checkInStr");
        TableColumn<com.hotel.model.Booking, String> outCol    = col("Check-Out",  "checkOutStr");
        TableColumn<com.hotel.model.Booking, String> amtCol    = col("Est. Total",  "formattedAmount");

        table.getColumns().addAll(refCol, guestCol, roomCol, typeCol, inCol, outCol, amtCol);
        table.setPrefHeight(280);

        javafx.collections.ObservableList<com.hotel.model.Booking> bookings =
            javafx.collections.FXCollections.observableArrayList(bookingDAO.findByStatus("ACTIVE"));
        table.setItems(bookings);

        // Search action
        searchBtn.setOnAction(e -> {
            String query = searchField.getText().trim().toLowerCase();
            if (query.isEmpty()) {
                table.setItems(javafx.collections.FXCollections.observableArrayList(
                        bookingDAO.findByStatus("ACTIVE")));
                return;
            }
            javafx.collections.ObservableList<com.hotel.model.Booking> filtered =
                javafx.collections.FXCollections.observableArrayList();
            for (com.hotel.model.Booking b : bookingDAO.findByStatus("ACTIVE")) {
                if (b.getBookingRef().toLowerCase().contains(query) ||
                    (b.getCustomerName() != null && b.getCustomerName().toLowerCase().contains(query)) ||
                    (b.getRoomNumber() != null && b.getRoomNumber().contains(query))) {
                    filtered.add(b);
                }
            }
            table.setItems(filtered);
        });

        // Bill area
        Label billArea = new Label("Select a booking above to preview the bill.");
        billArea.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-padding: 16 0 0 0;");

        TextArea billDisplay = new TextArea();
        billDisplay.setEditable(false);
        billDisplay.setPrefHeight(200);
        billDisplay.setStyle(
            "-fx-background-color: rgba(0,0,0,0.3);" +
            "-fx-text-fill: #a0e0b0;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 13px;" +
            "-fx-border-color: rgba(201,169,110,0.2); -fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;"
        );
        billDisplay.setVisible(false);

        Button checkoutBtn = dangerButton("🚪  CONFIRM CHECK-OUT");
        checkoutBtn.setDisable(true);

        // Row selection → show bill preview
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) { checkoutBtn.setDisable(true); billDisplay.setVisible(false); return; }

            checkoutBtn.setDisable(false);
            billDisplay.setVisible(true);

            roomDAO.findById(selected.getRoomId()).ifPresent(room -> {
                java.util.List<com.hotel.model.BillingItem> extras =
                    billingDAO.findItemsByBookingId(selected.getBookingId());
                com.hotel.util.BillingCalculator.BillSummary summary =
                    com.hotel.util.BillingCalculator.computeFinalBill(
                        room, selected.getNumberOfNights(), extras);
                billDisplay.setText(summary.toReceiptString(
                    selected.getBookingRef(),
                    selected.getCustomerName(),
                    selected.getRoomNumber()));
            });
        });

        checkoutBtn.setOnAction(e -> {
            com.hotel.model.Booking selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) return;

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Check-Out");
            confirm.setHeaderText("Check out " + selected.getCustomerName() + "?");
            confirm.setContentText("Room " + selected.getRoomNumber() + " will be released.\nThis action cannot be undone.");
            confirm.showAndWait().ifPresent(resp -> {
                if (resp == ButtonType.OK) {
                    try {
                        bookingService.checkout(selected.getBookingId());
                        showAlert(Alert.AlertType.INFORMATION, "Check-Out Complete",
                            "Guest " + selected.getCustomerName() + " has been checked out.\n" +
                            "Room " + selected.getRoomNumber() + " is now being cleaned.");
                        table.setItems(javafx.collections.FXCollections.observableArrayList(
                            bookingDAO.findByStatus("ACTIVE")));
                        billDisplay.setVisible(false);
                        checkoutBtn.setDisable(true);
                    } catch (Exception ex) {
                        showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage());
                    }
                }
            });
        });

        content.getChildren().addAll(
            searchLabel, searchRow,
            activeLabel, table,
            billDisplay, checkoutBtn
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        return root;
    }

    // ============================================================
    // 4. HOUSEKEEPING PANEL
    // ============================================================

    private Node createHousekeepingPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("🧹", "Housekeeping",
                "Assign and track room cleaning tasks (multithreaded simulation)");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 36, 28, 36));

        // Room selector
        Label roomSel = sectionLabel("SELECT ROOMS TO CLEAN");
        javafx.scene.control.ListView<String> roomList = new javafx.scene.control.ListView<>();
        roomList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
        roomList.setPrefHeight(180);
        roomDAO.findAll().forEach(r ->
            roomList.getItems().add("Room " + r.getRoomNumber() + " — " + r.getRoomType().getDisplayName() +
                                    (r.isAvailable() ? " (Vacant)" : " (Post-Checkout)")));
        roomList.setStyle("-fx-background-color: rgba(255,255,255,0.03);" +
                          "-fx-border-color: rgba(201,169,110,0.2); -fx-border-radius: 8px;" +
                          "-fx-text-fill: " + TEXT_PRIMARY + ";");

        // Cleaning log
        Label logLabel = sectionLabel("REAL-TIME CLEANING LOG");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(260);
        logArea.setWrapText(true);
        logArea.setStyle(
            "-fx-background-color: rgba(0,0,0,0.4);" +
            "-fx-text-fill: #78c850;" +
            "-fx-font-family: 'Courier New';" +
            "-fx-font-size: 12px;" +
            "-fx-border-color: rgba(120,200,80,0.2);" +
            "-fx-border-radius: 8px; -fx-background-radius: 8px;"
        );

        // Bind to service log
        logArea.textProperty().bind(housekeepingService.statusLogProperty());
        logArea.textProperty().addListener((obs, o, n) -> logArea.setScrollTop(Double.MAX_VALUE));

        HBox btnRow = new HBox(16);
        Button cleanSelectedBtn = primaryButton("🧹  Clean Selected Rooms");
        Button cleanAllBtn = new Button("⚡  Clean All Vacant Rooms");
        cleanAllBtn.setPrefHeight(44);
        cleanAllBtn.setStyle("-fx-background-color: rgba(74,144,217,0.2); " +
                             "-fx-border-color: rgba(74,144,217,0.4); -fx-border-radius: 8px;" +
                             "-fx-text-fill: " + ACCENT_BLUE + "; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 20 0 20;");
        Button clearLogBtn = new Button("✕  Clear Log");
        clearLogBtn.setPrefHeight(44);
        clearLogBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + TEXT_MUTED + ";" +
                             "-fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8px; -fx-cursor: hand;");

        btnRow.getChildren().addAll(cleanSelectedBtn, cleanAllBtn, clearLogBtn);

        cleanSelectedBtn.setOnAction(e -> {
            java.util.List<String> selected = new java.util.ArrayList<>(
                roomList.getSelectionModel().getSelectedItems());
            if (selected.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "No Selection", "Please select at least one room.");
                return;
            }
            housekeepingService.clearLog();
            java.util.List<String> roomNumbers = new java.util.ArrayList<>();
            for (String s : selected) {
                // Extract room number from display string
                String roomNum = s.split(" ")[1];
                roomNumbers.add(roomNum);
            }
            housekeepingService.cleanRoomsBatch(roomNumbers,
                () -> showAlert(Alert.AlertType.INFORMATION, "Done",
                                "All selected rooms have been cleaned!"));
        });

        cleanAllBtn.setOnAction(e -> {
            housekeepingService.clearLog();
            java.util.List<String> allRooms = new java.util.ArrayList<>();
            roomDAO.findAll().forEach(r -> allRooms.add(r.getRoomNumber()));
            housekeepingService.cleanRoomsBatch(allRooms,
                () -> showAlert(Alert.AlertType.INFORMATION, "Done", "All rooms cleaned!"));
        });

        clearLogBtn.setOnAction(e -> housekeepingService.clearLog());

        content.getChildren().addAll(roomSel, roomList, btnRow, logLabel, logArea);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        return root;
    }

    // ============================================================
    // 5. À LA CARTE PANEL
    // ============================================================

    private Node createAlaCartePanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("🍽", "À La Carte",
                "Order food, beverages & services for a guest");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 36, 28, 36));

        // Booking selector
        Label bookSel = sectionLabel("SELECT ACTIVE BOOKING");
        ComboBox<String> bookingBox = new ComboBox<>();
        bookingBox.setPromptText("Choose active booking...");
        bookingBox.setPrefWidth(400);
        java.util.List<com.hotel.model.Booking> active = bookingDAO.findByStatus("ACTIVE");
        for (com.hotel.model.Booking b : active) {
            bookingBox.getItems().add(b.getBookingRef() + " — " + b.getCustomerName() + " (Room " + b.getRoomNumber() + ")");
        }
        styleControl(bookingBox);

        // Category tabs
        Label catLabel = sectionLabel("MENU CATEGORIES");
        TabPane menuTabs = new TabPane();
        menuTabs.setStyle("-fx-background-color: transparent;");
        menuTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        String[] categories = {"FOOD", "BEVERAGE", "SPA", "LAUNDRY", "MISC"};
        String[] catIcons   = {"🍽", "🍹", "💆", "👔", "✨"};

        // Order list
        javafx.collections.ObservableList<com.hotel.model.BillingItem> orderItems =
            javafx.collections.FXCollections.observableArrayList();
        TableView<com.hotel.model.BillingItem> orderTable = styledTable();
        TableColumn<com.hotel.model.BillingItem, String> nameCol    = col("Item",       "itemName");
        TableColumn<com.hotel.model.BillingItem, String> qtyCol     = col("Qty",        "quantity");
        TableColumn<com.hotel.model.BillingItem, String> priceCol   = col("Unit Price", "unitPrice");
        TableColumn<com.hotel.model.BillingItem, String> totalCol   = col("Total",      "formattedTotal");
        orderTable.getColumns().addAll(nameCol, qtyCol, priceCol, totalCol);
        orderTable.setItems(orderItems);
        orderTable.setPrefHeight(160);

        Label runningTotal = new Label("Order Total: ₹0.00");
        runningTotal.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 16px; -fx-font-weight: bold;");

        for (int i = 0; i < categories.length; i++) {
            String cat = categories[i];
            String icon = catIcons[i];
            Tab tab = new Tab(icon + " " + cat);
            tab.setStyle("-fx-text-fill: " + TEXT_PRIMARY + ";");

            FlowPane itemCards = new FlowPane(12, 12);
            itemCards.setPadding(new Insets(16));

            java.util.List<BillingDAO.MenuItemRecord> items = billingDAO.getMenuByCategory(cat);
            for (BillingDAO.MenuItemRecord item : items) {
                VBox card = new VBox(8);
                card.setPrefWidth(200);
                card.setPadding(new Insets(14));
                card.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.03);" +
                    "-fx-border-color: rgba(201,169,110,0.15);" +
                    "-fx-border-radius: 10px; -fx-background-radius: 10px; -fx-cursor: hand;"
                );

                Label itemName = new Label(item.itemName());
                itemName.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-weight: bold; -fx-font-size: 13px;");
                itemName.setWrapText(true);

                Label itemDesc = new Label(item.description());
                itemDesc.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 11px;");
                itemDesc.setWrapText(true);

                Label itemPrice = new Label(String.format("₹%.2f", item.price()));
                itemPrice.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 14px; -fx-font-weight: bold;");

                Button addBtn = new Button("+ Add");
                addBtn.setPrefWidth(Double.MAX_VALUE);
                addBtn.setStyle(
                    "-fx-background-color: rgba(201,169,110,0.15);" +
                    "-fx-text-fill: " + ACCENT_GOLD + ";" +
                    "-fx-border-color: rgba(201,169,110,0.3); -fx-border-radius: 6px;" +
                    "-fx-background-radius: 6px; -fx-cursor: hand;"
                );

                addBtn.setOnAction(e -> {
                    if (bookingBox.getValue() == null) {
                        showAlert(Alert.AlertType.WARNING, "No Booking", "Please select a booking first.");
                        return;
                    }
                    // Find booking id from selection
                    String ref = bookingBox.getValue().split(" — ")[0];
                    bookingDAO.findByRef(ref).ifPresent(booking -> {
                        com.hotel.model.BillingItem bi = new com.hotel.model.BillingItem(
                            booking.getBookingId(), item.itemName(),
                            com.hotel.model.BillingItem.Category.valueOf(cat),
                            1, item.price()
                        );
                        billingDAO.saveBillingItem(bi);
                        orderItems.add(bi);
                        double total = orderItems.stream()
                            .mapToDouble(x -> x.getTotalPrice() != null ? x.getTotalPrice() : 0)
                            .sum();
                        runningTotal.setText(String.format("Order Total: ₹%,.2f", total));
                    });
                });

                card.getChildren().addAll(itemName, itemDesc, itemPrice, addBtn);
                itemCards.getChildren().add(card);
            }

            ScrollPane tabScroll = new ScrollPane(itemCards);
            tabScroll.setFitToWidth(true);
            tabScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            tabScroll.setPrefHeight(300);
            tab.setContent(tabScroll);
            menuTabs.getTabs().add(tab);
        }

        Label orderLabel = sectionLabel("CURRENT ORDER");
        content.getChildren().addAll(
            bookSel, bookingBox,
            catLabel, menuTabs,
            orderLabel, orderTable, runningTotal
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        return root;
    }

    // ============================================================
    // 6. CUSTOMER DETAILS PANEL
    // ============================================================

    private Node createCustomerDetailsPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("👤", "Customer Details",
                "View, search and manage guest records");

        VBox content = new VBox(16);
        content.setPadding(new Insets(24, 36, 24, 36));

        // Search bar
        HBox searchBar = new HBox(12);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or phone...");
        searchField.setPrefWidth(320);
        styleControl(searchField);
        Button searchBtn = new Button("🔍  Search");
        searchBtn.setPrefHeight(40);
        searchBtn.setStyle("-fx-background-color: rgba(74,144,217,0.2); " +
                           "-fx-border-color: rgba(74,144,217,0.4); -fx-border-radius: 6px;" +
                           "-fx-text-fill: " + ACCENT_BLUE + "; -fx-cursor: hand; -fx-padding: 0 20 0 20;");

        Button refreshBtn = new Button("↺  Refresh");
        refreshBtn.setPrefHeight(40);
        refreshBtn.setStyle(searchBtn.getStyle());

        searchBar.getChildren().addAll(searchField, searchBtn, refreshBtn);

        // Table
        TableView<com.hotel.model.Customer> table = styledTable();
        TableColumn<com.hotel.model.Customer, String> nameCol  = col("Full Name",   "fullName");
        TableColumn<com.hotel.model.Customer, String> emailCol = col("Email",       "email");
        TableColumn<com.hotel.model.Customer, String> phoneCol = col("Phone",       "phone");
        TableColumn<com.hotel.model.Customer, String> idCol    = col("ID Type",     "idProofType");
        TableColumn<com.hotel.model.Customer, String> natCol   = col("Nationality", "nationality");
        TableColumn<com.hotel.model.Customer, String> dateCol  = col("Registered",  "createdAt");
        table.getColumns().addAll(nameCol, emailCol, phoneCol, idCol, natCol, dateCol);
        VBox.setVgrow(table, Priority.ALWAYS);

        javafx.collections.ObservableList<com.hotel.model.Customer> data =
            javafx.collections.FXCollections.observableArrayList(customerDAO.findAll());
        table.setItems(data);

        searchBtn.setOnAction(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) { table.setItems(data); return; }
            table.setItems(javafx.collections.FXCollections.observableArrayList(
                customerDAO.searchByName(q)));
        });
        refreshBtn.setOnAction(e -> {
            data.setAll(customerDAO.findAll());
            table.setItems(data);
            searchField.clear();
        });

        // Delete button
        Button deleteBtn = dangerButton("🗑  Delete Selected Customer");
        deleteBtn.setOnAction(e -> {
            com.hotel.model.Customer sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showAlert(Alert.AlertType.WARNING, "No Selection", "Select a customer first."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Customer");
            confirm.setContentText("Delete " + sel.getFullName() + "? This cannot be undone.");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    customerDAO.delete(sel.getCustomerId());
                    data.remove(sel);
                    showAlert(Alert.AlertType.INFORMATION, "Deleted", "Customer record removed.");
                }
            });
        });

        content.getChildren().addAll(searchBar, table, deleteBtn);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        return root;
    }

    // ============================================================
    // 7. REVIEWS PANEL
    // ============================================================

    private Node createReviewsPanel() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_CONTENT + ";");

        VBox header = buildPanelHeader("⭐", "Guest Reviews",
                "View feedback and submit new reviews");

        VBox content = new VBox(20);
        content.setPadding(new Insets(28, 36, 28, 36));

        // Average rating display
        double avg = reviewDAO.getAverageRating();
        HBox avgBox = new HBox(16);
        avgBox.setAlignment(Pos.CENTER_LEFT);
        avgBox.setPadding(new Insets(16));
        avgBox.setStyle("-fx-background-color: rgba(201,169,110,0.08);" +
                        "-fx-border-color: rgba(201,169,110,0.2);" +
                        "-fx-border-radius: 12px; -fx-background-radius: 12px;");
        Label avgNum = new Label(avg > 0 ? String.format("%.1f", avg) : "—");
        avgNum.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 48px; -fx-font-weight: bold;");
        VBox avgText = new VBox(4);
        Label avgStars = new Label(avg > 0 ? "★".repeat((int)Math.round(avg)) + "☆".repeat(5-(int)Math.round(avg)) : "☆☆☆☆☆");
        avgStars.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 20px;");
        Label avgLabel = new Label("Average Guest Rating");
        avgLabel.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px;");
        avgText.getChildren().addAll(avgStars, avgLabel);
        avgBox.getChildren().addAll(avgNum, avgText);

        // Review table
        Label reviewsLabel = sectionLabel("ALL REVIEWS");
        TableView<com.hotel.model.Review> table = styledTable();
        TableColumn<com.hotel.model.Review, String> guestCol   = col("Guest",       "customerName");
        TableColumn<com.hotel.model.Review, String> refCol     = col("Booking Ref", "bookingRef");
        TableColumn<com.hotel.model.Review, String> ratingCol  = col("Rating",      "starDisplay");
        TableColumn<com.hotel.model.Review, String> titleCol   = col("Title",       "title");
        TableColumn<com.hotel.model.Review, String> commentCol = col("Comments",    "comments");
        TableColumn<com.hotel.model.Review, String> dateCol    = col("Date",        "createdAt");
        table.getColumns().addAll(guestCol, refCol, ratingCol, titleCol, commentCol, dateCol);
        table.setPrefHeight(220);
        table.setItems(javafx.collections.FXCollections.observableArrayList(reviewDAO.findAll()));

        // Add review form
        Label addLabel = sectionLabel("SUBMIT A REVIEW");
        GridPane addForm = new GridPane();
        addForm.setHgap(20);
        addForm.setVgap(12);

        ComboBox<String> bookingCombo = new ComboBox<>();
        bookingCombo.setPromptText("Select checked-out booking...");
        bookingCombo.setPrefWidth(Double.MAX_VALUE);
        java.util.List<com.hotel.model.Booking> checkouts = bookingDAO.findByStatus("CHECKED_OUT");
        for (com.hotel.model.Booking b : checkouts) {
            bookingCombo.getItems().add(b.getBookingRef() + " — " + b.getCustomerName());
        }
        styleControl(bookingCombo);

        ComboBox<Integer> ratingCombo = new ComboBox<>();
        ratingCombo.getItems().addAll(5, 4, 3, 2, 1);
        ratingCombo.setValue(5);
        ratingCombo.setPrefWidth(Double.MAX_VALUE);
        styleControl(ratingCombo);

        TextField titleField = new TextField();
        titleField.setPromptText("Review title...");
        styleControl(titleField);

        TextArea commentField = new TextArea();
        commentField.setPromptText("Share your experience...");
        commentField.setPrefHeight(80);
        commentField.setWrapText(true);
        styleControl(commentField);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        addForm.getColumnConstraints().addAll(cc, cc);
        addForm.add(formField("BOOKING *", bookingCombo), 0, 0);
        addForm.add(formField("RATING *", ratingCombo), 1, 0);
        addForm.add(formField("TITLE", titleField), 0, 1, 2, 1);
        addForm.add(formField("COMMENTS", commentField), 0, 2, 2, 1);

        Button submitBtn = primaryButton("⭐  Submit Review");
        submitBtn.setOnAction(e -> {
            if (bookingCombo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Required", "Please select a booking.");
                return;
            }
            String ref = bookingCombo.getValue().split(" — ")[0];
            bookingDAO.findByRef(ref).ifPresent(booking -> {
                com.hotel.model.Review review = new com.hotel.model.Review(
                    booking.getBookingId(), booking.getCustomerId(),
                    ratingCombo.getValue(),
                    titleField.getText().trim(),
                    commentField.getText().trim(),
                    "GENERAL"
                );
                reviewDAO.save(review);
                showAlert(Alert.AlertType.INFORMATION, "Thank You!", "Review submitted successfully.");
                table.setItems(javafx.collections.FXCollections.observableArrayList(reviewDAO.findAll()));
                titleField.clear(); commentField.clear();
            });
        });

        content.getChildren().addAll(avgBox, reviewsLabel, table, addLabel, addForm, submitBtn);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(header, scroll);
        return root;
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + ACCENT_GOLD + "; -fx-font-size: 10px;" +
                     "-fx-letter-spacing: 3px; -fx-font-weight: bold;");
        return lbl;
    }

    private void styleControl(Control control) {
        control.setStyle(
            "-fx-background-color: rgba(255,255,255,0.04);" +
            "-fx-border-color: rgba(201,169,110,0.2);" +
            "-fx-border-radius: 6px; -fx-background-radius: 6px;" +
            "-fx-text-fill: " + TEXT_PRIMARY + ";" +
            "-fx-font-size: 13px; -fx-pref-height: 40px;"
        );
    }

    private void refreshRoomCombo(ComboBox<String> roomBox) {
        roomBox.getItems().clear();
        roomDAO.findAvailable().forEach(r ->
            roomBox.getItems().add(r.getRoomId() + ":" + r.getRoomNumber() +
                " — " + r.getRoomType().getDisplayName() +
                " (₹" + (int)r.getPricePerNight() + "/night)"));
    }

    private int extractRoomId(String comboValue) {
        if (comboValue == null) return -1;
        try { return Integer.parseInt(comboValue.split(":")[0]); }
        catch (Exception e) { return -1; }
    }
}
