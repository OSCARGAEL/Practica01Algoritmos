package solitaire;

import DeckOfCards.CartaInglesa;


 // Registro de un movimiento para poder deshacerlo.
 // 1 = DRAW                 (robar del mazo al waste)
 // 2 = RELOAD               (pasar waste -> draw)
 // 3 = WASTE_A_TABLEAU      (waste -> columna)
 // 4 = WASTE_A_FOUNDATION   (waste -> foundation)
 // 5 = TABLEAU_A_TABLEAU    (columna -> columna, bloque)
 // 6 = TABLEAU_A_FOUNDATION (columna -> foundation, 1 carta)
public class Movimientos {

    // Datos del movimiento
    int tipo;                 // 1..6 (ver arriba)
    int fromIndex;            // columna origen (0..6) o -1
    int toIndex;              // columna destino (0..6) o -1
    int cantidad;             // k (numero de cartas movidas cuando aplica)
    boolean volteoAutomatico; // si al mover se volteo la nueva top del origen
    int foundationIndex;      // foundation afectada (0..3) o -1
    Pila<CartaInglesa> block; // bloque original bottom->top (solo para tipo 5)

    public Movimientos(int tipo,int fromIndex,int toIndex,int cantidad,boolean volteoAutomatico,int foundationIndex,Pila<CartaInglesa> block) {
        this.tipo = tipo;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.cantidad = cantidad;
        this.volteoAutomatico = volteoAutomatico;
        this.foundationIndex = foundationIndex;
        this.block = block;
    }

    public int getTipo() {
        return tipo; 
    }
    public int getFromIndex() {
        return fromIndex; 
    }
    public int getToIndex() {
        return toIndex; 
    }
    public int getCantidad() {
        return cantidad; 
    }
    public boolean isVolteoAutomatico() {
        return volteoAutomatico; 
    }
    public int getFoundationIndex() {
        return foundationIndex; 
    }
    // Bloque original bottom->top
    public Pila<CartaInglesa> getBlock() {
        return block; 
    }
}
