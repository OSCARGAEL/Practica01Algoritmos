package solitaire;

import DeckOfCards.CartaInglesa;


 // Pila de descarte (waste). Top = carta visible.
 
public class WastePile {
    // Estructura LIFO interna
    Pila<CartaInglesa> cartas = new Pila<>(64);

     // Agrega un bloque bottom->top al waste.
     // Las cartas se dejan face-up y el ultimo del bloque queda arriba.
     
    public void addCartas(Pila<CartaInglesa> nuevas) {
        if (nuevas == null || nuevas.peek() == null) return;

        // nuevas viene bottom->top. Usamos aux para preservar ese orden.
        Pila<CartaInglesa> aux = new Pila<>(128); // aux: top->down
        for (CartaInglesa c = nuevas.pop(); c != null; c = nuevas.pop()) {
            aux.push(c);
        }
        // Pasamos a waste en bottom->top y hacemos face-up
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceUp();
            cartas.push(c);
        }
    }
    
     // Vacia el waste y devuelve un bloque bottom->top.
     
    public Pila<CartaInglesa> emptyPile() {
        Pila<CartaInglesa> aux = new Pila<>(128);    // recoge top->down
        Pila<CartaInglesa> bloque = new Pila<>(128); // entrega bottom->top
        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }
        return bloque;
    }

    // Ve la carta del top sin retirarla. 
    public CartaInglesa verCarta() {
        return cartas.peek();
    }

    // Quita y devuelve la carta del top, o null si vacio. 
    public CartaInglesa getCarta() {
        return cartas.pop();
    }

    // Indica si hay al menos una carta en waste. 
    public boolean hayCartas() {
        return cartas.peek() != null;
    }

    // Representacion de si top esta vacio. 
    @Override
    public String toString() {
        CartaInglesa top = cartas.peek();
        return (top == null) ? "---" : top.toString();
    }
}
