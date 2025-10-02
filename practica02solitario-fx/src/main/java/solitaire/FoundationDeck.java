package solitaire;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Palo;

public class FoundationDeck {
    // Palo al que pertenece esta foundation (A -> K)
    Palo palo;
    // Pila interna de cartas (top = carta visible)
    Pila<CartaInglesa> cartas = new Pila<>(16);

    // Crea la foundation para un palo. 
    public FoundationDeck(Palo palo) {
        this.palo = palo;
    }

    
    //Intenta agregar una carta respetando palo y secuencia A->K.
    //Retorna true si se apilo, false si no procede.
     
    public boolean agregarCarta(CartaInglesa carta) {
        if (carta == null) 
            return false;
        if (!carta.tieneElMismoPalo(palo)) 
            return false;

        CartaInglesa ultima = getUltimaCarta();
        if (ultima == null) {
            // Primera carta debe ser As
            if (carta.getValorBajo() != 1) 
                return false;
        } else {
            // Siguiente debe ser +1 en valor
            if (ultima.getValorBajo() + 1 != carta.getValorBajo()) 
                return false;
        }
        carta.makeFaceUp();     // en foundation siempre visibles
        cartas.push(carta);     // apilar en el top
        return true;
    }

    // Quita y retorna la carta del top, o null si vacio.
    public CartaInglesa removerUltimaCarta() {
        return cartas.pop();
    }

    // Retorna true si no hay cartas. 
    public boolean estaVacio() {
        return cartas.peek() == null;
    }

    // Ve la carta del top sin retirarla.
    public CartaInglesa getUltimaCarta() {
        return cartas.peek();
    }

    
    // Representacion lineal de la foundation.
     
    @Override
    public String toString() {
        if (cartas.peek() == null) 
            return "---";

        // usamos dos auxiliares y restauramos
        Pila<CartaInglesa> aux1 = new Pila<>(32); // recoge top->down
        Pila<CartaInglesa> aux2 = new Pila<>(32); // reordena para restaurar
        StringBuilder sb = new StringBuilder();

        // vaciamos a aux1
        for (CartaInglesa c = cartas.pop(); 
                c != null; c = cartas.pop()) {
            aux1.push(c);
        }
        // armamos string y pasamos a aux2
        for (CartaInglesa c = aux1.pop(); 
                c != null; c = aux1.pop()) {
            sb.append(c.toString());
            aux2.push(c);
        }
        // restauramos a la pila original conservando el orden
        for (CartaInglesa c = aux2.pop(); 
                c != null; c = aux2.pop()) {
            cartas.push(c);
        }
        return sb.toString();
    }
}
