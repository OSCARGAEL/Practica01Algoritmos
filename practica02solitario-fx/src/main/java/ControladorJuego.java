import DeckOfCards.CartaInglesa;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import java.util.*;
import javafx.scene.Node;
import solitaire.FoundationDeck;
import solitaire.SolitaireGame;
import solitaire.TableauDeck;

// Controla la creacion y los movimientos entre UI y el juego.
 
public class ControladorJuego {
    // Referencias a contenedores visuales
    StackPane vistaMazo;
    Pane descarte;
    List<Pane> fundacion;
    List<Pane> columna;

    // Callback cuando se gana
    private final Runnable onVictoria;

    // Mapa UI -> modelo visual de carta (para arrastre)
    private final Map<StackPane, Carta> cartaPorNodo = new HashMap<>();

    // Parametros de layout
    private final double pasoSolape = 32;
    private final double margenSuperior = 12;

    // Estado
    private boolean victoriaMostrada = false;
    private SolitaireGame juego;

    // Soporte de arrastre y capa overlay
    private final AnimacionArrastre arrastre;
    private final Pane capaArrastre;

    //Crea el controlador y registra callbacks de arrastre.
     
    public ControladorJuego(StackPane vistaMazo, Pane descarte, List<Pane> fundacion,List<Pane> columna, Runnable onVictoria, Pane capaArrastre) {
        this.vistaMazo = vistaMazo;
        this.descarte = descarte;
        this.fundacion = fundacion;
        this.columna = columna;
        this.onVictoria = onVictoria;
        this.capaArrastre = capaArrastre;

        // Callbacks: ok, error, y pedir al motor aplicar el movimiento
        this.arrastre = new AnimacionArrastre(fundacion,columna,descarte,cartaPorNodo,this::manejarValido,this::manejarInvalido,this::intentarConMotor,this.capaArrastre
        );
    }

    
    //Inicia un juego nuevo y rehace todo.
     
    public void prepararNuevaPartida() {
        victoriaMostrada = false;
        juego = new SolitaireGame();
        limpiar();
        pintarDesdeMotor();
    }

    
    //Click en el mazo: roba o recarga.
     
    public void ejecutarAccionMazo() {
        if (juego.getDrawPile().hayCartas()) {
            juego.drawCards();
        } else {
            juego.reloadDrawPile();
        }
        refrescarWaste();
    }

    
    // Limpia contenedores y desregistra cartas de los mapas.
    
    private void limpiar() {
        for (Pane b : fundacion) {
            desregistrarCartasDe(b);
            b.getChildren().clear();
        }
        for (Pane c : columna) {
            desregistrarCartasDe(c);
            c.getChildren().clear();
        }
        desregistrarCartasDe(descarte);
        descarte.getChildren().clear();
        cartaPorNodo.clear();
    }

    
    // Repinta todas las zonas desde el modelo (fuente de verdad).
    
    private void pintarDesdeMotor() {
        if (juego == null) return;

        cartaPorNodo.clear();

        // Columnas (tableau)
        TableauDeck[] tds = juego.getTableau();
        for (int i = 0; i < columna.size(); i++) {
            Pane cont = columna.get(i);
            desregistrarCartasDe(cont);
            cont.getChildren().clear();
            if (i < tds.length) {
                var cartas = tds[i].getCards(); // vista bottom->top
                for (CartaInglesa ci : cartas) {
                    StackPane nodo = crearNodo(ci);
                    cont.getChildren().add(nodo);
                }
            }
            organizar(cont);
        }

        // Foundations y waste
        pintarFundaciones();
        refrescarWaste();
    }

    
    // Repinta las 4 foundations con su carta superior.
     
    private void pintarFundaciones() {
        var fds = juego.getFoundations();
        for (int i = 0; i < fundacion.size() && i < fds.length; i++) {
            Pane base = fundacion.get(i);
            desregistrarCartasDe(base);
            base.getChildren().clear();
            var fd = fds[i];
            CartaInglesa top = (fd != null) ? fd.getUltimaCarta() : null;
            if (top != null) {
                StackPane nodo = crearNodo(top);
                base.getChildren().add(nodo);
            }
            organizar(base);
        }
    }

    
    // Repinta el waste con su carta superior.
     
    private void refrescarWaste() {
        desregistrarCartasDe(descarte);
        descarte.getChildren().clear();
        CartaInglesa w = juego.getWastePile().verCarta();
        if (w != null) {
            StackPane nodo = crearNodo(w);
            descarte.getChildren().add(nodo);
            organizar(descarte);
        }
    }

    
    // Crea un nodo visual para una carta y la registra como arrastrable.
     
    private StackPane crearNodo(CartaInglesa ci) {
        StackPane n = new StackPane();
        n.setPrefSize(100, 140);

        String figura = ci.getPalo().getFigura();
        String figuraNormal = figura.replace("\uFE0E", "").replace("\uFE0F", "").replace("❤", "♥");
        String rango = convertir(ci.getValor());

        boolean esRojo = "rojo".equals(ci.getPalo().getColor());

        Carta c = new Carta(n, rango, figuraNormal, ci.getValor(), esRojo, ci.isFaceup());
        cartaPorNodo.put(n, c);
        c.actualizarRepresentacionVisual();

        // habilitar arrastre
        arrastre.registrarCartaArrastrable(n);
        return n;
    }

    
    // Convierte valor numerico a etiqueta A,J,Q,K o numero.
     
    private String convertir(int v) {
        if (v == 14) 
            return "A";
        if (v == 11) 
            return "J";
        if (v == 12) 
            return "Q";
        if (v == 13) 
            return "K";
        return String.valueOf(v);
    }

    // Reposiciona en un contenedor segun si es base o columna.
    
    private void organizar(Pane pila) {
        boolean base = fundacion.contains(pila) || pila == descarte;
        double y = base ? 0 : margenSuperior;
        double paso = base ? 0 : pasoSolape;

        for (var n : new ArrayList<>(pila.getChildren())) {
            n.setTranslateX(0);
            n.setTranslateY(y);
            y += paso;
        }
    }

    
     // Aplica un drop, decide que movimiento pedir al motor.
     // si el motor lo rechaza, suena error y se repinta todo.
     
    private boolean intentarConMotor(Pane origen, List<StackPane> grupo, Pane destino) {
        if (destino == null) return false;

        // Desde waste
        if (origen == descarte) {
            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveWasteToTableau(d + 1);
                if (ok) {
                    pintarCol(origen, destino);
                    refrescarWaste();
                    comprobar();
                } else {
                    manejarInvalido();
                }
                return ok;
            }
            if (fundacion.contains(destino)) {
                boolean ok = juego.moveWasteToFoundation();
                if (ok) {
                    actualizarFundacion();
                    refrescarWaste();
                    comprobar();
                } else {
                    manejarInvalido();
                }
                return ok;
            }
            return false;
        }

        // Desde columna
        if (columna.contains(origen)) {
            int o = columna.indexOf(origen);

            if (columna.contains(destino)) {
                int d = columna.indexOf(destino);
                boolean ok = juego.moveTableauToTableau(o + 1, d + 1);
                if (ok) {
                    pintarCol(origen, destino);
                    comprobar();
                } else {
                    manejarInvalido();
                }
                return ok;
            }

            if (fundacion.contains(destino)) {
                // foundation solo acepta 1 carta
                if (grupo.size() != 1) {
                    manejarInvalido();
                    return false;
                }
                boolean ok = juego.moveTableauToFoundation(o + 1);
                if (ok) {
                    pintarSoloColumna(o);
                    actualizarFundacion();
                    comprobar();
                } else {
                    manejarInvalido();
                }
                return ok;
            }
            return false;
        }

        return false;
    }

    
    // Rehace las columnas de origen y destino tras un movimiento valido.
     
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

    
    // Repinta una columna completa desde el modelo.
     
    private void pintarSoloColumna(int i) {
    if (i < 0 || i >= columna.size()) {
        return;
    }

    Pane cont = columna.get(i);
    desregistrarCartasDe(cont);
    cont.getChildren().clear();

    TableauDeck[] tds = juego.getTableau();
    if (i >= 0 && i < tds.length) {
        
        List<CartaInglesa> cartas = tds[i].getCards();
        for (CartaInglesa ci : cartas) {
            StackPane nodo = crearNodo(ci);
            cont.getChildren().add(nodo);
        }
    }

    organizar(cont);
}


    
    // Rehace la foundation cuyo top cambio en el motor.
    private void actualizarFundacion() {
        FoundationDeck fd = juego.getLastFoundationUpdated();
        if (fd == null) 
            return;

        CartaInglesa top = fd.getUltimaCarta();
        if (top == null) 
            return;

        int idx = top.getPalo().ordinal();
        if (idx < 0 || idx >= fundacion.size()) 
            return;

        Pane base = fundacion.get(idx);
        desregistrarCartasDe(base);
        base.getChildren().clear();
        StackPane nodo = crearNodo(top);
        base.getChildren().add(nodo);
        organizar(base);
    }

    
    // Sonido para drop valido.
     
    private void manejarValido() {
        try { 
            RecursosAudioMedia.reproducirEfecto("sonido_de_aprobacion"); 
        } catch (Throwable ignored) {
        }
    }

    
    // Sonido para drop invalido
    private void manejarInvalido() {
        try { RecursosAudioMedia.reproducirEfecto("sonido_de_error"); } catch (Throwable ignored) {}
        pintarDesdeMotor();
    }

    
    // Comprueba fin del juego.
     
    private void comprobar() {
        if (victoriaMostrada) return;
        boolean ok = juego.isGameOver();
        if (ok) {
            victoriaMostrada = true;
            onVictoria.run();
        }
    }

    public SolitaireGame getJuego() {
        return juego;
    }

    // Rehace la UI completo desde el modelo.
    public void refrescarUI() {
        pintarDesdeMotor();
    }

    
    // Saca del mapa todas las cartas asociadas a un contenedor.
    
    private void desregistrarCartasDe(Pane cont) {
    List<Node> hijos = new ArrayList<Node>(cont.getChildren());
    for (Node node : hijos) {
        if (node instanceof StackPane) {
            StackPane sp = (StackPane) node;
            cartaPorNodo.remove(sp);
        }
    }
}

}
