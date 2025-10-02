package solitaire;

import DeckOfCards.CartaInglesa;
import DeckOfCards.Mazo;

public class DrawPile {
    // Pila interna del mazo (top = siguiente a robar)
    private Pila<CartaInglesa> cartas;
    // Cantidad de cartas a robar por jugada (1 o 3)
    private int cuantasCartasSeEntregan = 1;

    
    // Carga el mazo inicial desde Mazo externo.
    //Copiamos a pila invirtiendo para que pop() equivalga a remove(0) original.
    
    public DrawPile() {
         // Creamos la pila del mazo y cargamos las cartas del Mazo original.
        // Mazo.getCartas() devuelve un ArrayList, pero NO lo guardamos:
        // solo lo leemos por índice para empujar en nuestra Pila.
        this.cartas = new Pila<CartaInglesa>(128);

        Mazo mazo = new Mazo();
        // En la implementación antigua, el "top" del mazo estaba en índice 0.
        // Para que el tope de nuestra Pila sea ese mismo elemento, empujamos desde el final hacia 0.
        for (int i = mazo.getCartas().size() - 1; i >= 0; i--) {
            this.cartas.push(mazo.getCartas().get(i));
        }
        setCuantasCartasSeEntregan(1);
    }

    // Configura cuantas cartas se entregan por jugada (min 1). 
    public void setCuantasCartasSeEntregan(int n) {
        this.cuantasCartasSeEntregan = (n <= 0) ? 1 : n;
    }

    /** Regresa la cantidad configurada para robar. */
    public int getCuantasCartasSeEntregan() {
        return cuantasCartasSeEntregan;
    }

    
    // Retira exactamente 'cantidad' cartas del mazo.
    // Devuelve un bloque en orden bottom->top para repartir a los tableau.
     
    public Pila<CartaInglesa> getCartas(int cantidad) {
        Pila<CartaInglesa> aux = new Pila<>(64);    // recoge top->down
        Pila<CartaInglesa> bloque = new Pila<>(64); // entrega bottom->top
        for (int i = 0; i < cantidad; i++) {
            CartaInglesa c = cartas.pop();
            if (c == null) break;
            aux.push(c);
        }
        // invertir a bottom->top
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }
        return bloque;
    }

    
    // Retira hasta N cartas segun configuracion y las voltea a face-up.
    // Devuelve bloque bottom->top para el waste.
    
    public Pila<CartaInglesa> retirarCartas() {
        Pila<CartaInglesa> aux = new Pila<>(8);
        Pila<CartaInglesa> bloque = new Pila<>(8);
        int maximo = cuantasCartasSeEntregan;
        for (int i = 0; i < maximo; i++) {
            CartaInglesa c = cartas.pop();
            if (c == null) break;
            c.makeFaceUp();
            aux.push(c);
        }
        // invertir a bottom->top
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            bloque.push(c);
        }
        return bloque;
    }

    // Indica si aun hay cartas en el mazo. 
    public boolean hayCartas() {
        return cartas.peek() != null;
    }

    // Ve la carta del tope sin retirarla. 
    public CartaInglesa verCarta() {
        return cartas.peek();
    }

    
    // Recarga el mazo reemplazando TODO su contenido.
    // Las cartas se dejan face-down. Se espera bloque bottom->top.
    
    public void recargar(Pila<CartaInglesa> cartasAgregar) {
        // vaciar actual
        while (cartas.pop() != null) { /* discard */ }
        if (cartasAgregar == null || cartasAgregar.peek() == null) return;

        // copiar bottom->top preservando orden final del mazo
        Pila<CartaInglesa> aux = new Pila<>(128); // aux: top->down
        for (CartaInglesa c = cartasAgregar.pop(); c != null; c = cartasAgregar.pop()) {
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceDown();
            cartas.push(c);
        }
    }

    
    // Apila un bloque encima del mazo sin reemplazarlo.
    // Se usa en undo de DRAW. Las cartas quedan face-down.
    
    public void pushBlockOnTop(Pila<CartaInglesa> bloque) {
        if (bloque == null || bloque.peek() == null) return;
        Pila<CartaInglesa> aux = new Pila<>(256); // aux: top->down
        for (CartaInglesa c = bloque.pop(); c != null; c = bloque.pop()) {
            aux.push(c);
        }
        for (CartaInglesa c = aux.pop(); c != null; c = aux.pop()) {
            c.makeFaceDown();
            cartas.push(c);
        }
    }
    
    // Empuja UNA carta arriba del mazo (face-down), preservando el orden correcto.
    public void pushCardOnTop(CartaInglesa c) {
    if (c != null) {
        c.makeFaceDown();   // al regresar al stock siempre van boca abajo
        cartas.push(c);     // push directo al tope del mazo
        }
    }

    

    // Representacion compacta del mazo para texto. 
    @Override
    public String toString() {
        return hayCartas() ? "@" : "-E-";
    }
}
