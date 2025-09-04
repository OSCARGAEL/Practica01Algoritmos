/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import DeckOfCards.CartaInglesa;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.*;
import solitaire.FoundationDeck;

public class ControladorJuego {
    private final StackPane vistaMazo;
    private final Pane descarte;
    private final List<Pane> fundacion;
    private final List<Pane> columna;
    private final Runnable onVictoria;
    private final Map<StackPane, Carta> cartaPorNodo = new HashMap<>();
    private final double pasoSolape = 32;
    private final double margenSuperior = 12;
    private boolean victoriaMostrada = false;
    private final ApiJuego api;
    private final Arrastre arrastre;

    public ControladorJuego(StackPane vistaMazo, Pane descarte, List<Pane> fundacion, List<Pane> columna, Runnable onVictoria, ApiJuego api) {
        this.vistaMazo = vistaMazo;
        this.descarte = descarte;
        this.fundacion = fundacion;
        this.columna = columna;
        this.onVictoria = onVictoria;
        this.api = api;
        this.arrastre = new Arrastre(fundacion, columna, descarte, cartaPorNodo, this::manejarValido, this::manejarInvalido, this::decidirTipoMovimiento);
    }

    public void prepararNuevaPartida() {
        victoriaMostrada = false;
        api.iniciarNuevaPartida();
        limpiar();
        pintarColumnas();
        actualizarMazo();
    }

    public void ejecutarAccionMazo() {
        api.robarOCiclar();
        refrescarWaste();
        actualizarMazo();
    }

    private void limpiar() {
        for (Pane b : fundacion) {
            b.getChildren().clear();
        }
        for (Pane c : columna) {
            c.getChildren().clear();
        }
        descarte.getChildren().clear();
        cartaPorNodo.clear();
    }

    private void pintarColumnas() {
        List<List<CartaInglesa>> t = api.obtenerTableau();
        for (int i = 0; i < columna.size(); i++) {
            Pane cont = columna.get(i);
            cont.getChildren().clear();
            if (i < t.size()) {
                List<CartaInglesa> cartas = t.get(i);
                for (CartaInglesa ci : cartas) {
                    StackPane nodo = crearNodo(ci);
                    cont.getChildren().add(nodo);
                }
            }
            organizar(cont);
        }
        for (Pane f : fundacion) {
            f.getChildren().clear();
        }
        refrescarWaste();
    }

    private void refrescarWaste() {
        descarte.getChildren().clear();
        CartaInglesa w = api.obtenerWasteSuperior();
        if (w != null) {
            StackPane nodo = crearNodo(w);
            descarte.getChildren().add(nodo);
            organizar(descarte);
        }
    }

    private void actualizarMazo() {
        vistaMazo.setOpacity(api.hayCartasEnMazo() ? 1.0 : 0.3);
    }

    private StackPane crearNodo(CartaInglesa ci) {
        StackPane n = new StackPane();
        n.setPrefSize(100, 140);
        String figura = ci.getPalo().getFigura();
        String figuraNormal = figura.replace("\uFE0E", "").replace("\uFE0F", "").replace("❤", "♥");
        String rango = convertir(ci.getValor());
        Carta c = new Carta(n, rango, figuraNormal, ci.getValor(), "rojo".equals(ci.getPalo().getColor()), ci.isFaceup());
        cartaPorNodo.put(n, c);
        c.actualizarRepresentacionVisual();
        arrastre.registrarCartaArrastrable(n);
        return n;
    }

    private String convertir(int v) {
        if (v == 14) {
            return "A";
        }
        if (v == 11) {
            return "J";
        }
        if (v == 12) {
            return "Q";
        }
        if (v == 13) {
            return "K";
        }
        return String.valueOf(v);
    }

    private void organizar(Pane pila) {
        boolean base = fundacion.contains(pila) || pila == descarte;
        double y = base ? 0 : margenSuperior;
        double paso = base ? 0 : pasoSolape;
        for (var n : pila.getChildren()) {
            n.setTranslateX(0);
            n.setTranslateY(y);
            y += paso;
        }
    }

    private boolean decidirTipoMovimiento(Pane origen, List<StackPane> grupo, Pane destino) {
        if (destino == null) {
            return false;
        }
        if (origen == descarte) {
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = api.moverWasteATableau(d);
                if (ok) {
                    pintarCol(origen, destino);
                    refrescarWaste();
                    comprobar();
                }
                return ok;
            }
            if (fundacion.contains(destino)) {
                boolean ok = api.moverWasteAFundacion();
                if (ok) {
                    actualizarFundacion();
                    refrescarWaste();
                    comprobar();
                }
                return ok;
            }
            return false;
        }
        if (columna.contains(origen)) {
            int o = columna.indexOf(origen);
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = api.moverTableauATableau(o, d);
                if (ok) {
                    pintarCol(origen, destino);
                    comprobar();
                }
                return ok;
            }
            if (fundacion.contains(destino)) {
                if (grupo.size() != 1) {
                    return false;
                }
                boolean ok = api.moverTableauAFundacion(o);
                if (ok) {
                    pintarSoloColumna(o);
                    actualizarFundacion();
                    comprobar();
                }
                return ok;
            }
            return false;
        }
        return false;
    }

    private void pintarCol(Pane origen, Pane destino) {
        if (columna.contains(origen)) {
            int i = columna.indexOf(origen);
            pintarSoloColumna(i);
        }
        if (columna.contains(destino)) {
            int j = columna.indexOf(destino);
            pintarSoloColumna(j);
        }
    }

    private void pintarSoloColumna(int i) {
        List<List<CartaInglesa>> t = api.obtenerTableau();
        Pane cont = columna.get(i);
        cont.getChildren().clear();
        if (i < t.size()) {
            List<CartaInglesa> cartas = t.get(i);
            for (CartaInglesa ci : cartas) {
                StackPane nodo = crearNodo(ci);
                cont.getChildren().add(nodo);
            }
        }
        organizar(cont);
    }

    private void actualizarFundacion() {
        FoundationDeck fd = api.obtenerUltimaFundacionActualizada();
        if (fd == null) {
            return;
        }
        CartaInglesa top = fd.getUltimaCarta();
        if (top == null) {
            return;
        }
        int idx = top.getPalo().ordinal();
        Pane base = fundacion.get(idx);
        base.getChildren().clear();
        StackPane nodo = crearNodo(top);
        base.getChildren().add(nodo);
        organizar(base);
    }

    private void manejarValido() {
        RecursosAudioMedia.reproducirEfecto("sonido_de_aprobacion");
    }

    private void manejarInvalido() {
        RecursosAudioMedia.reproducirEfecto("sonido_de_error");
    }

    private void comprobar() {
        if (victoriaMostrada) {
            return;
        }
        boolean ok = api.esVictoria();
        if (ok) {
            victoriaMostrada = true;
            onVictoria.run();
        }
    }
}
