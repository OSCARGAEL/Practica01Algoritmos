/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import DeckOfCards.CartaInglesa;
import java.util.ArrayList;
import java.util.List;
import solitaire.FoundationDeck;
import solitaire.SolitaireGame;
import solitaire.TableauDeck;

public class ApiOriginal implements ApiJuego {
    private SolitaireGame juego;

    public ApiOriginal() {
        juego = new SolitaireGame();
    }

    public void iniciarNuevaPartida() {
        juego = new SolitaireGame();
        juego.getDrawPile().setCuantasCartasSeEntregan(1);

    }

    public boolean hayCartasEnMazo() {
        return juego.getDrawPile().hayCartas();
    }

    public void robarOCiclar() {
        if (juego.getDrawPile().hayCartas()) {
            juego.drawCards();
        } else {
            juego.reloadDrawPile();
        }
    }

    public boolean moverWasteATableau(int destinoCero) {
        return juego.moveWasteToTableau(destinoCero + 1);
    }

    public boolean moverWasteAFundacion() {
        return juego.moveWasteToFoundation();
    }

    public boolean moverTableauATableau(int fuenteCero, int destinoCero) {
        return juego.moveTableauToTableau(fuenteCero + 1, destinoCero + 1);
    }

    public boolean moverTableauAFundacion(int fuenteCero) {
        return juego.moveTableauToFoundation(fuenteCero + 1);
    }

    public List<List<CartaInglesa>> obtenerTableau() {
        ArrayList<List<CartaInglesa>> lista = new ArrayList<>();
        for (TableauDeck t : juego.getTableau()) {
            lista.add(t.getCards());
        }
        return lista;
    }

    public CartaInglesa obtenerWasteSuperior() {
        return juego.getWastePile().verCarta();
    }

    public FoundationDeck obtenerUltimaFundacionActualizada() {
        return juego.getLastFoundationUpdated();
    }

    public boolean esVictoria() {
        return juego.isGameOver();
    }
}
