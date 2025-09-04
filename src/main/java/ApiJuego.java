
import DeckOfCards.CartaInglesa;
import solitaire.FoundationDeck;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author HP
 */
import java.util.List;

public interface ApiJuego {
    void iniciarNuevaPartida();
    boolean hayCartasEnMazo();
    void robarOCiclar();
    boolean moverWasteATableau(int destinoCero);
    boolean moverWasteAFundacion();
    boolean moverTableauATableau(int fuenteCero, int destinoCero);
    boolean moverTableauAFundacion(int fuenteCero);
    List<List<CartaInglesa>> obtenerTableau();
    CartaInglesa obtenerWasteSuperior();
    FoundationDeck obtenerUltimaFundacionActualizada();
    boolean esVictoria();
}
