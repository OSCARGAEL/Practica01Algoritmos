import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;
import solitaire.SolitaireGame;

public class Tablero {
    // Contenedor raiz
    GridPane raiz = new GridPane();
    // Vista del mazo (Draw)
    StackPane vistaMazo = new StackPane();
    // Pila de descarte (Waste)
    Pane descarte = new Pane();
    // 4 foundations
    List<Pane> fundacion = new ArrayList<>();
    // 7 columnas (tableau)
    List<Pane> columna = new ArrayList<>();
    // Controlador de interaccion y pintado
    ControladorJuego controlador;

    
     // Construye el tablero visual y conecta eventos basicos.
     // onVictoria: callback cuando el juego termina.
     // capaArrastre: overlay para animaciones de arrastre.
    
    public Tablero(Runnable onVictoria, Pane capaArrastre) {
        // layout base
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.setHgap(14);
        raiz.setVgap(14);
        raiz.setPadding(new Insets(16));

        // area visual del mazo (100x140)
        vistaMazo.setPrefSize(100, 140);
        Image img = CargadorCarta.cargarImagenPorNombre("back.png");
        if (img != null) {
            ImageView v = new ImageView(img);
            v.setFitWidth(100);
            v.setPreserveRatio(true);
            vistaMazo.getChildren().add(v);
        } else {
            // fallback simple si no hay imagen
            Rectangle r = new Rectangle(100, 140);
            r.setArcWidth(14);
            r.setArcHeight(14);
            r.setFill(Color.web("#2b5aa7"));
            vistaMazo.getChildren().add(r);
        }

        // area del waste
        descarte.setPrefSize(100, 140);

        // contenedor con mazo y waste
        HBox zonaMazoDescarte = new HBox(12, vistaMazo, descarte);
        zonaMazoDescarte.setAlignment(Pos.CENTER_LEFT);

        // fila de 4 foundations
        HBox filaFundacion = new HBox(12);
        for (int i = 0; i < 4; i++) {
            Pane b = new Pane();
            b.setPrefSize(100, 140);
            // marco visual
            b.setStyle("-fx-background-color:#e9f5ea;" +"-fx-border-color:darkgreen;" +"-fx-border-width:1.5;" +"-fx-background-radius:12;" +"-fx-border-radius:12;"
            );
            fundacion.add(b);
            filaFundacion.getChildren().add(b);
        }

        // fila superior: mazo/waste a la izquierda, foundations a la derecha
        HBox filaSuperior = new HBox(250, zonaMazoDescarte, filaFundacion);
        filaSuperior.setAlignment(Pos.CENTER);
        raiz.add(filaSuperior, 0, 0, 7, 1);

        // crear 7 columnas (tableau)
        for (int i = 0; i < 7; i++) {
            Pane p = new Pane();
            p.setPrefSize(120, 480);
            columna.add(p);
            // etiqueta vacia arriba para mantener separacion
            raiz.add(new VBox(new Label(""), p), i, 1);
        }

        // controlador: pinta y maneja arrastres
        controlador = new ControladorJuego(
            vistaMazo, descarte, fundacion, columna, onVictoria, capaArrastre
        );

        // click en el mazo: robar o recargar
        vistaMazo.setOnMouseClicked(e -> controlador.ejecutarAccionMazo());
    }

    // Retorna el nodo raiz para insertar en la escena.
    public Parent obtenerNodoRaiz() {
        return raiz;
    }

    // Inicia una nueva partida y crea desde el modelo.
    public void iniciarNuevaPartida() {
        controlador.prepararNuevaPartida();
    }

    // Juego actual.
    public SolitaireGame getJuego() {
        return controlador.getJuego();
    }

    // Fuerza reahacer desde la GUI desde el modelo.
    public void refrescarUI() {
        controlador.refrescarUI();
    }
}
