/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI extends Application {
    private VistaJuego vista;

    @Override
    public void start(Stage v) {
        Button botonNuevo = new Button("Nuevo juego");
        botonNuevo.setOnAction(e -> vista.iniciarNuevaPartida());
        HBox barra = new HBox(16, botonNuevo);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(10, 10, 10, 10));
        vista = new VistaJuego(this::mostrarVictoria);
        BorderPane capa = new BorderPane();
        capa.setTop(barra);
        capa.setCenter(vista.obtenerNodoRaiz());
        StackPane raiz = new StackPane();
        var fondo = RecursosAudioMedia.crearFondoAnimado();
        if (fondo != null) {
            fondo.setMouseTransparent(true);
            raiz.getChildren().add(fondo);
        }
        raiz.getChildren().add(capa);
        Scene s = new Scene(raiz, 1100, 700);
        if (fondo != null) {
            fondo.fitWidthProperty().bind(s.widthProperty());
            fondo.fitHeightProperty().bind(s.heightProperty());
        }
        v.setTitle("Solitario");
        v.setScene(s);
        v.show();
        RecursosAudioMedia.iniciarMusicaDeFondo();
        vista.iniciarNuevaPartida();
    }

    @Override
    public void stop() {
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
        vista.iniciarNuevaPartida();
    }

    public static void main(String[] args) {
        launch();
    }
}
