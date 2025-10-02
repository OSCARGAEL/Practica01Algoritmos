/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import java.io.InputStream;
import java.util.Locale;

public class CargadorCarta {
    public static String CLASSPATH_DIR = "/cartas/Playing Cards/PNG-cards-1.3/";

    public static ImageView crearImagenDeCarta(String palo, String rango, double ancho) {
        Image img = cargarImagenPorNombre(construirNombreDeArchivoDeCarta(palo, rango));
        
        ImageView v = new ImageView(img);
        v.setFitWidth(ancho);
        v.setPreserveRatio(true);
        return v;
    }

    public static Image cargarImagenPorNombre(String nombre) {
        Image i = cargarImagenDesdeRecursos(nombre);
        return i;
    }

    private static String construirNombreDeArchivoDeCarta(String palo, String rango) {
        String p = normalizarPaloAFormatoDelPaquete(palo);
        String r = rango.toUpperCase(Locale.ROOT);
        String b = switch (r) {
            case "A" -> "ace_of_" + p;
            case "J" -> "jack_of_" + p + "2";
            case "Q" -> "queen_of_" + p + "2";
            case "K" -> "king_of_" + p + "2";
            default -> r.toLowerCase(Locale.ROOT) + "_of_" + p;
        };
        return b + ".png";
    }

    private static Image cargarImagenDesdeRecursos(String n) {
        try {
            InputStream in = CargadorCarta.class.getResourceAsStream(CLASSPATH_DIR + n);
            if (in != null) {
                return new Image(in);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String normalizarPaloAFormatoDelPaquete(String p) {
        String s = p.replace("\uFE0E", "").replace("\uFE0F", "").replace("❤", "♥").trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "♠", "spade", "spades", "picas" -> "spades";
            case "♣", "club", "clubs", "treboles", "tréboles" -> "clubs";
            case "♥", "heart", "hearts", "corazones" -> "hearts";
            case "♦", "diamond", "diamonds", "diamantes" -> "diamonds";
            default -> s;
        };
    }
   
}
