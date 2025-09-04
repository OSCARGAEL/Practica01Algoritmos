/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Arrastre {
    public interface EjecutorMovimiento { boolean ejecutar(Pane origen, List<StackPane> grupo, Pane destino); }

    private final List<Pane> fundacion;
    private final List<Pane> columna;
    private final Pane descarte;
    private final Map<StackPane, Carta> cartaPorNodo;
    private final Runnable onValido;
    private final Runnable onInvalido;
    private final EjecutorMovimiento ejecutor;
    private Pane origen;
    private List<StackPane> grupo;
    private List<Double> y0;
    private Point2D delta;

    public Arrastre(List<Pane> fundacion, List<Pane> columna, Pane descarte, Map<StackPane, Carta> cartaPorNodo, Runnable onValido, Runnable onInvalido, EjecutorMovimiento ejecutor) {
        this.fundacion = fundacion;
        this.columna = columna;
        this.descarte = descarte;
        this.cartaPorNodo = cartaPorNodo;
        this.onValido = onValido;
        this.onInvalido = onInvalido;
        this.ejecutor = ejecutor;
    }

    public void registrarCartaArrastrable(StackPane carta) {
        carta.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> iniciarArrastre(carta, e));
        carta.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::moverArrastre);
        carta.addEventFilter(MouseEvent.MOUSE_RELEASED, this::soltarArrastre);
    }

    private void iniciarArrastre(StackPane carta, MouseEvent e) {
        origen = (Pane) carta.getParent();
        if (origen == null) {
            grupo = List.of();
            return;
        }
        Carta cv = cartaPorNodo.get(carta);
        int idx = origen.getChildren().indexOf(carta);
        if (columna.contains(origen)) {
            if (!cv.caraArriba) {
                grupo = List.of();
                return;
            }
            grupo = new ArrayList<>();
            for (int i = idx; i < origen.getChildren().size(); i++) {
                StackPane n = (StackPane) origen.getChildren().get(i);
                Carta c = cartaPorNodo.get(n);
                if (!c.caraArriba) {
                    break;
                }
                grupo.add(n);
            }
        } else if (origen == descarte) {
            if (idx != origen.getChildren().size() - 1 || !cv.caraArriba) {
                grupo = List.of();
                return;
            }
            grupo = new ArrayList<>(List.of(carta));
        } else {
            grupo = List.of();
        }
        y0 = new ArrayList<>();
        for (StackPane n : grupo) {
            y0.add(n.getTranslateY());
        }
        Point2D p = carta.localToScene(0, 0);
        delta = new Point2D(e.getSceneX() - p.getX(), e.getSceneY() - p.getY());
        for (StackPane n : grupo) {
            n.toFront();
        }
    }

    private void moverArrastre(MouseEvent e) {
        if (origen == null || grupo == null || grupo.isEmpty()) {
            return;
        }
        Point2D p = origen.sceneToLocal(e.getSceneX() - delta.getX(), e.getSceneY() - delta.getY());
        double dy = p.getY() - y0.get(0);
        for (int i = 0; i < grupo.size(); i++) {
            StackPane n = grupo.get(i);
            n.setTranslateX(p.getX());
            n.setTranslateY(y0.get(i) + dy);
        }
    }

    private void soltarArrastre(MouseEvent e) {
        if (origen == null || grupo == null || grupo.isEmpty()) {
            return;
        }
        Pane destino = buscarDestinoPorCoordenadas(e.getSceneX(), e.getSceneY());
        boolean ok = ejecutor.ejecutar(origen, grupo, destino);
        if (!ok) {
            for (int i = 0; i < grupo.size(); i++) {
                StackPane n = grupo.get(i);
                n.setTranslateX(0);
                n.setTranslateY(y0.get(i));
            }
            onInvalido.run();
        } else {
            onValido.run();
        }
        origen = null;
        grupo = null;
        y0 = null;
        delta = null;
    }

    private Pane buscarDestinoPorCoordenadas(double sx, double sy) {
        List<Pane> d = new ArrayList<>();
        d.addAll(fundacion);
        d.addAll(columna);
        d.add(descarte);
        for (Pane p : d) {
            Point2D a = p.localToScene(0, 0);
            boolean dx = sx >= a.getX() && sx <= a.getX() + p.getWidth();
            boolean dy = sy >= a.getY() && sy <= a.getY() + p.getHeight();
            if (dx && dy) {
                return p;
            }
        }
        return null;
    }
}
