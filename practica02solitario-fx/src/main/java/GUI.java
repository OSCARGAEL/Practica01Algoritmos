import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import solitaire.SolitaireGame;

public class GUI extends Application {
    private Tablero tablero;                // UI del juego
    private Pane capaArrastre;              // overlay para arrastre
    private Label lblMov = new Label("Movimientos: 0");
    private Timeline cron;

    @Override
    public void start(Stage v) {
        // Barra superior (nuevo, deshacer, contador)
        Button botonNuevo = new Button("Nuevo juego");
        Button botonDesahacer = new Button("Deshacer");

        HBox barra = new HBox(16, botonNuevo, botonDesahacer, lblMov);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(10, 10, 10, 10));

        // Overlay
        capaArrastre = new Pane();
        capaArrastre.setPickOnBounds(false);

        // Tablero
        tablero = new Tablero(this::mostrarVictoria, capaArrastre);

        // Layout raíz
        BorderPane capa = new BorderPane();
        capa.setTop(barra);
        capa.setCenter(tablero.obtenerNodoRaiz());

        StackPane raiz = new StackPane();

        // Fondo animado
        Node fondo = RecursosAudioMedia.crearFondoAnimado();
        if (fondo != null) {
            fondo.setMouseTransparent(true);
            raiz.getChildren().add(fondo);
        }

        raiz.getChildren().add(capa);
        raiz.getChildren().add(capaArrastre);

        Scene s = new Scene(raiz, 1100, 700);

        if (fondo instanceof ImageView) {
            ImageView iv = (ImageView) fondo;
            iv.fitWidthProperty().bind(s.widthProperty());
            iv.fitHeightProperty().bind(s.heightProperty());
        } else if (fondo instanceof MediaView) {
            MediaView mv = (MediaView) fondo;
            mv.fitWidthProperty().bind(s.widthProperty());
            mv.fitHeightProperty().bind(s.heightProperty());
        }

        v.setTitle("Solitario");
        v.setScene(s);
        v.show();

        // Musica de fondo
        RecursosAudioMedia.iniciarMusicaDeFondo();

        // Primer juego
        tablero.iniciarNuevaPartida();

        // Acciones
        botonNuevo.setOnAction(e -> {
            tablero.iniciarNuevaPartida();
            try { tablero.refrescarUI(); 
            } catch (Throwable ignored) {
            }
            actualizarContador();
        });

        botonDesahacer.setOnAction(e -> {
            deshacerAccion();              
            actualizarContador();
        });

        // Actualiza el contador regularmente
        cron = new Timeline(new KeyFrame(Duration.millis(200), e -> actualizarContador()));
        cron.setCycleCount(Timeline.INDEFINITE);
        cron.play();
    }

    // Llama al motor para deshacer y repinta si fue exitoso.
    private void deshacerAccion() {
        SolitaireGame game = tablero.getJuego();
        if (game == null) return;
        boolean ok = false;
        try {
            ok = game.deshacerUltimoMovimiento();
        } catch (Throwable t) {
            // log opcional
        }
        if (ok) {
            try { tablero.refrescarUI(); } catch (Throwable ignored) {
            }
        }
    }

    // Lee moveCount y lo muestra. 
    private void actualizarContador() {
        try {
            SolitaireGame g = tablero.getJuego();
            if (g != null) lblMov.setText("Movimientos: " + g.getMoveCount());
        } catch (Throwable ignored) {
            // ignora si aun no hay juego.
        }
    }

    @Override
    public void stop() {
        try {
            if (cron != null) cron.stop();
        } catch (Throwable ignored) {
        }
        RecursosAudioMedia.detenerYLiberar();
        Platform.exit();
    }

    private void mostrarVictoria() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initModality(Modality.APPLICATION_MODAL);
        a.setTitle("Victoria");
        a.setHeaderText("¡Ganaste!");
        a.setContentText("Volver a jugar");
        ((Button) a.getDialogPane().lookupButton(a.getButtonTypes().get(0))).setText("Reiniciar");
        a.showAndWait();
        tablero.iniciarNuevaPartida();
    }

    public static void main(String[] args) {
        launch();
    }
}
