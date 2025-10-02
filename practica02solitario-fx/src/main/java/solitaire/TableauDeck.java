package solitaire;

import DeckOfCards.CartaInglesa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TableauDeck {
    // Pila interna (top = carta visible)
    Pila<CartaInglesa> cartas = new Pila<>(256);

    // Carga cartas iniciales (bottom->top) y voltea la ultima a face-up. 
    public void inicializar(Pila<CartaInglesa> bloque) {
        Pila<CartaInglesa> aux = new Pila<>(256); // recoge top->down
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) aux.push(c);
        CartaInglesa ultima = null;
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            ultima = c;
            cartas.push(c);
        }
        if (ultima != null) ultima.makeFaceUp();
    }

    // Itera bottom->top sin modificar la pila real. 
    public void forEachBottomTop(Consumer<CartaInglesa> consumer) {
        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) aux.push(c);
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            consumer.accept(c);
            cartas.push(c); // restaurar
        }
    }

    //  bottom->top para la GUI. 
    public List<CartaInglesa> getCards() {
        ArrayList<CartaInglesa> list = new ArrayList<>();
        forEachBottomTop(list::add);
        return list;
    }

    // Busca la primera carta face-up con valor EXACTO. No la remueve. 
    public CartaInglesa viewCardStartingAt(int value) {
        Pila<CartaInglesa> aux = new Pila<>(256);
        CartaInglesa encontrada = null;
        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
            aux.push(c);
            if (encontrada == null && c.isFaceup() && c.getValor() == value) {
                encontrada = c;
            }
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) cartas.push(c);
        return encontrada;
    }

    
     // Quita desde la carta face-up con 'value' hacia arriba.
     // Devuelve bloque bottom->top o null si no existe.
     
    public Pila<CartaInglesa> removeStartingAt(int value) {
        Pila<CartaInglesa> auxTop = new Pila<>(256);   // recoge top->down hasta encontrar
        Pila<CartaInglesa> bloque = new Pila<>(256);   // devolucion bottom->top
        boolean found = false;

        for (CartaInglesa c = cartas.pop(); c != null; c = cartas.pop()) {
            auxTop.push(c);
            if (c.isFaceup() && c.getValor() == value) {
                found = true;
                break;
            }
        }
        if (!found) {
            // restaurar y abortar
            for (CartaInglesa c = auxTop.pop(); c != null; c = auxTop.pop()) cartas.push(c);
            return null;
        }
        // construir bloque: start (bottom) y lo que estaba encima
        for (CartaInglesa c = auxTop.pop(); c != null; c = auxTop.pop()) bloque.push(c);
        return bloque;
    }

    // Agrega una carta si cumple alterno y secuencia. 
    public boolean agregarCarta(CartaInglesa carta) {
        if (carta == null) return false;
        if (!sePuedeAgregarCarta(carta)) return false;
        carta.makeFaceUp();
        cartas.push(carta);
        return true;
    }

    
     // Agrega bloque bottom->top si la primera (bottom) encaja.
     // Consume el bloque solo si se puede.
     
    public boolean agregarBloqueDeCartas(Pila<CartaInglesa> cartasRecibidas) {
        if (cartasRecibidas == null || cartasRecibidas.peek() == null) return false;

        // hallar primera (bottom) sin consumir
        Pila<CartaInglesa> aux = new Pila<>(256);
        for (CartaInglesa c = cartasRecibidas.pop(); c != null; c = cartasRecibidas.pop()) aux.push(c);
        CartaInglesa primera = aux.peek();

        boolean puede = sePuedeAgregarCarta(primera);
        if (!puede) {
            // restaurar bloque original
            for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) cartasRecibidas.push(c);
            return false;
        }

        // consumir y apilar bottom->top
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceUp();
            cartas.push(c);
        }
        return true;
    }

     // Reponer bloque bottom->top en este tableau sin invertir el orden.
     // No consume el bloque (lo restaura como llego).
     
    public boolean reponerBloque(Pila<CartaInglesa> bloque) {
        if (bloque == null || bloque.peek() == null) return false;

        // volcar a aux (top->down)
        Pila<CartaInglesa> aux = new Pila<>(512);
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) aux.push(c);

        // regresar a bloque y a este tableau en la misma pasada (bottom->top)
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);      // restaurar bloque
            this.cartas.push(c); // reponer arriba
        }
        return true;
    }

    // Ve la ultima carta (top) sin quitarla. 
    public CartaInglesa verUltimaCarta() { return cartas.peek(); }

    
     // Quita y retorna la ultima carta del tableau.
     // No voltea la nueva top (eso lo decide el motor).
     
    public CartaInglesa removerUltimaCarta() {
        CartaInglesa out = cartas.pop();
        if (out != null) return out;
        return null;
    }

    // Indica si no hay cartas. 
    public boolean isEmpty() { return cartas.peek() == null; }

    
     // Valida si carta puede agregarse:
     // Si esta vacio, solo se puede agregar K.
     // Si no, alterno de color y valor descendente (top == carta+1).
     
    public boolean sePuedeAgregarCarta(CartaInglesa cartaInicialDePrueba) {
        if (cartaInicialDePrueba == null) return false;
        CartaInglesa ultima = cartas.peek();
        if (ultima == null) {
            return cartaInicialDePrueba.getValor() == 13; // solo K
        }
        boolean alterno = !ultima.getColor().equals(cartaInicialDePrueba.getColor());
        boolean secuencia = ultima.getValor() == cartaInicialDePrueba.getValor() + 1;
        return alterno && secuencia;
    }

    // ver la ultimaCarta.
    public CartaInglesa getUltimaCarta() { 
        return cartas.peek(); 
    }

    // Texto simple para depuracion. 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<CartaInglesa> snap = getCards();
        if (snap.isEmpty()) sb.append("---");
        else for (CartaInglesa c : snap) 
            sb.append(c);
        return sb.toString();
    }
}
