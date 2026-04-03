package com.hotel.view;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * SplashController — Creates the luxury hotel landing/splash screen.
 *
 * This is the first screen the user sees. It features:
 *   - An animated gradient background (luxury dark theme)
 *   - Floating ambient orb animations
 *   - Hotel branding with fade-in animations
 *   - A styled "Book Now" CTA button
 *   - Star rating display
 *
 * @author Grand Vista HMS
 * @version 1.0.0
 */
public class SplashController {

    /**
     * Builds and returns the complete splash Scene.
     */
    public Scene createScene() {
        StackPane root = new StackPane();
        root.setPrefSize(1200, 750);

        // ── Layer 1: Deep gradient background ──────────────────
        Pane backgroundPane = createBackground();

        // ── Layer 2: Animated floating orbs ────────────────────
        Pane orbLayer = createOrbLayer();

        // ── Layer 3: Decorative grid pattern ───────────────────
        Pane gridPattern = createGridPattern();

        // ── Layer 4: Content overlay ────────────────────────────
        VBox contentBox = createContentBox();

        // ── Layer 5: Bottom tagline strip ──────────────────────
        HBox bottomStrip = createBottomStrip();
        StackPane.setAlignment(bottomStrip, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(backgroundPane, orbLayer, gridPattern, contentBox, bottomStrip);

        Scene scene = new Scene(root, 1200, 750);
        scene.getStylesheets().add(getClass().getResource("/com/hotel/css/splash.css") != null
                ? getClass().getResource("/com/hotel/css/splash.css").toExternalForm()
                : "");

        return scene;
    }

    // ── Background ──────────────────────────────────────────────

    private Pane createBackground() {
        Pane pane = new Pane();
        pane.setPrefSize(1200, 750);

        // Deep navy-to-black gradient
        Rectangle bg = new Rectangle(1200, 750);
        LinearGradient grad = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#0a0a1a")),
                new Stop(0.5, Color.web("#0d1b2e")),
                new Stop(1.0, Color.web("#030810")));
        bg.setFill(grad);
        pane.getChildren().add(bg);
        return pane;
    }

    // ── Animated Orbs ───────────────────────────────────────────

    private Pane createOrbLayer() {
        Pane pane = new Pane();
        pane.setPrefSize(1200, 750);
        pane.setMouseTransparent(true);

        double[][] orbData = {
            // cx,    cy,   r,    opacity, hue
            {150,   200,  180,   0.07,    1},
            {1050,  150,  220,   0.06,    2},
            {600,   600,  250,   0.05,    3},
            {250,   550,  140,   0.08,    1},
            {950,   500,  160,   0.06,    2},
        };

        String[] orbColors = {"#c9a96e", "#4a90d9", "#7b68ee"};

        for (double[] d : orbData) {
            Circle orb = new Circle(d[0], d[1], d[2]);
            orb.setFill(Color.web(orbColors[(int) d[4] - 1], d[3]));

            GaussianBlur blur = new GaussianBlur(60);
            orb.setEffect(blur);

            // Slow floating animation
            double duration = 4000 + Math.random() * 3000;
            double offsetY = 20 + Math.random() * 30;

            TranslateTransition tt = new TranslateTransition(Duration.millis(duration), orb);
            tt.setByY(-offsetY);
            tt.setAutoReverse(true);
            tt.setCycleCount(Timeline.INDEFINITE);
            tt.play();

            pane.getChildren().add(orb);
        }

        return pane;
    }

    // ── Grid Pattern ────────────────────────────────────────────

    private Pane createGridPattern() {
        Pane pane = new Pane();
        pane.setPrefSize(1200, 750);
        pane.setMouseTransparent(true);
        pane.setOpacity(0.04);

        // Vertical lines
        for (int x = 0; x <= 1200; x += 60) {
            Rectangle line = new Rectangle(x, 0, 1, 750);
            line.setFill(Color.web("#c9a96e"));
            pane.getChildren().add(line);
        }
        // Horizontal lines
        for (int y = 0; y <= 750; y += 60) {
            Rectangle line = new Rectangle(0, y, 1200, 1);
            line.setFill(Color.web("#c9a96e"));
            pane.getChildren().add(line);
        }
        return pane;
    }

    // ── Main Content ─────────────────────────────────────────────

    private VBox createContentBox() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        box.setMaxWidth(700);
        box.setMaxHeight(Double.MAX_VALUE);

        // ★ Star rating ★
        Label stars = new Label("★ ★ ★ ★ ★");
        stars.setStyle("-fx-text-fill: #c9a96e; -fx-font-size: 20px; -fx-letter-spacing: 8px;");

        // Decorative line above hotel name
        HBox topLine = createDecorativeLine();

        // Hotel name — large, golden, spaced
        Label hotelName = new Label("GRAND VISTA");
        hotelName.setStyle(
            "-fx-text-fill: #f0e6c8;" +
            "-fx-font-size: 72px;" +
            "-fx-font-family: 'Georgia';" +
            "-fx-letter-spacing: 20px;" +
            "-fx-font-weight: bold;"
        );
        DropShadow glow = new DropShadow(30, Color.web("#c9a96e", 0.6));
        hotelName.setEffect(glow);

        // Subtitle
        Label subtitle = new Label("H  O  T  E  L  &  R  E  S  O  R  T");
        subtitle.setStyle(
            "-fx-text-fill: #c9a96e;" +
            "-fx-font-size: 14px;" +
            "-fx-letter-spacing: 10px;" +
            "-fx-font-family: 'Georgia';"
        );

        // Decorative line below hotel name
        HBox bottomLine = createDecorativeLine();

        // Tagline
        Label tagline = new Label("Where Luxury Meets Serenity");
        tagline.setStyle(
            "-fx-text-fill: #8a9bb5;" +
            "-fx-font-size: 18px;" +
            "-fx-font-style: italic;" +
            "-fx-font-family: 'Georgia';"
        );

        // Description
        Label description = new Label(
            "Experience unparalleled elegance across our 9 exquisitely appointed rooms,\n" +
            "from serene Standard retreats to the opulent Grand Penthouse.\n" +
            "Your extraordinary journey begins here."
        );
        description.setStyle(
            "-fx-text-fill: #6a7d95;" +
            "-fx-font-size: 14px;" +
            "-fx-text-alignment: center;" +
            "-fx-line-spacing: 6px;"
        );
        description.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        VBox.setMargin(description, new Insets(10, 0, 10, 0));

        // Amenity pills
        HBox amenities = createAmenityPills();

        // CTA Button
        Button bookNowBtn = createBookNowButton();
        VBox.setMargin(bookNowBtn, new Insets(20, 0, 0, 0));

        box.getChildren().addAll(
            stars, topLine, hotelName, subtitle, bottomLine,
            tagline, description, amenities, bookNowBtn
        );

        // ── Entrance animations ──────────────────────────────────
        animateEntrance(stars, 0);
        animateEntrance(hotelName, 200);
        animateEntrance(subtitle, 350);
        animateEntrance(tagline, 500);
        animateEntrance(description, 650);
        animateEntrance(amenities, 800);
        animateEntrance(bookNowBtn, 950);

        return box;
    }

    private HBox createDecorativeLine() {
        HBox hbox = new HBox(8);
        hbox.setAlignment(Pos.CENTER);

        Rectangle leftLine = new Rectangle(80, 1);
        leftLine.setFill(Color.web("#c9a96e", 0.5));

        Label diamond = new Label("◆");
        diamond.setStyle("-fx-text-fill: #c9a96e; -fx-font-size: 10px;");

        Rectangle rightLine = new Rectangle(80, 1);
        rightLine.setFill(Color.web("#c9a96e", 0.5));

        hbox.getChildren().addAll(leftLine, diamond, rightLine);
        return hbox;
    }

    private HBox createAmenityPills() {
        HBox hbox = new HBox(12);
        hbox.setAlignment(Pos.CENTER);

        String[] amenities = {"🏊 Pool", "🍽 Fine Dining", "💆 Spa", "🏋 Gym", "🅿 Valet"};
        for (String text : amenities) {
            Label pill = new Label(text);
            pill.setPadding(new Insets(6, 16, 6, 16));
            pill.setStyle(
                "-fx-background-color: rgba(201,169,110,0.1);" +
                "-fx-border-color: rgba(201,169,110,0.3);" +
                "-fx-border-radius: 20px;" +
                "-fx-background-radius: 20px;" +
                "-fx-text-fill: #c9a96e;" +
                "-fx-font-size: 12px;"
            );
            hbox.getChildren().add(pill);
        }
        return hbox;
    }

    private Button createBookNowButton() {
        Button btn = new Button("BOOK YOUR STAY  →");
        btn.setPrefWidth(280);
        btn.setPrefHeight(56);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to right, #c9a96e, #e8c987);" +
            "-fx-text-fill: #0a0a1a;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-letter-spacing: 3px;" +
            "-fx-background-radius: 4px;" +
            "-fx-cursor: hand;" +
            "-fx-font-family: 'Georgia';"
        );

        DropShadow shadow = new DropShadow(20, Color.web("#c9a96e", 0.5));
        btn.setEffect(shadow);

        // Hover effect
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #e8c987, #f5dfa0);" +
                "-fx-text-fill: #0a0a1a;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-letter-spacing: 3px;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Georgia';"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.04);
            st.setToY(1.04);
            st.play();
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #c9a96e, #e8c987);" +
                "-fx-text-fill: #0a0a1a;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-letter-spacing: 3px;" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;" +
                "-fx-font-family: 'Georgia';"
            );
            ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        btn.setOnAction(e -> {
            // Fade out splash, then show dashboard
            FadeTransition fade = new FadeTransition(Duration.millis(500), btn.getScene().getRoot());
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(ev -> Main.showDashboard());
            fade.play();
        });

        return btn;
    }

    // ── Bottom Strip ─────────────────────────────────────────────

    private HBox createBottomStrip() {
        HBox strip = new HBox(40);
        strip.setAlignment(Pos.CENTER);
        strip.setPadding(new Insets(18, 40, 18, 40));
        strip.setMaxWidth(Double.MAX_VALUE);
        strip.setStyle("-fx-background-color: rgba(201,169,110,0.08); " +
                       "-fx-border-color: rgba(201,169,110,0.15); " +
                       "-fx-border-width: 1 0 0 0;");

        String[] info = {
            "📍  Udupi, Karnataka, India",
            "📞  +91 820 000 1234",
            "✉️  reservations@grandvista.in",
            "🌐  www.grandvistahms.in"
        };

        for (String text : info) {
            Label lbl = new Label(text);
            lbl.setStyle("-fx-text-fill: #5a6a7a; -fx-font-size: 12px;");
            strip.getChildren().add(lbl);
        }
        return strip;
    }

    // ── Animation Helper ─────────────────────────────────────────

    private void animateEntrance(javafx.scene.Node node, double delayMs) {
        node.setOpacity(0);
        node.setTranslateY(20);

        FadeTransition ft = new FadeTransition(Duration.millis(600), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setDelay(Duration.millis(delayMs));

        TranslateTransition tt = new TranslateTransition(Duration.millis(600), node);
        tt.setFromY(20);
        tt.setToY(0);
        tt.setDelay(Duration.millis(delayMs));
        tt.setInterpolator(Interpolator.EASE_OUT);

        ft.play();
        tt.play();
    }
}
