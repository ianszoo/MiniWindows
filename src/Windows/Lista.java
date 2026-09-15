/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Windows;

import java.io.Serializable;

/**
 *
 * @author Ian Suazo Palao
 */
public class Lista<T> implements Serializable{
    private Nodo<T> head;
    private int size;

    public Lista() {
        this.head=null;
        this.size=0;
    }
    
    public void agregar(T dato){
        Nodo<T> nuevo=new Nodo<>(dato);
        if(head==null){
            head=nuevo;
        }
        
        else{
            Nodo<T> actual=head;
            while(actual.getSiguiente()!=null){
                actual=actual.getSiguiente();
            }
            actual.setSiguiente(nuevo);
        }
        size++;
    }

    public boolean eliminar(T dato){
        if (head== null){
            return false;
        }
        if (head.getDato().equals(dato)){
            head=head.getSiguiente();
            size--;
            return true;
        }
        Nodo<T> actual=head;
        while (actual.getSiguiente()!=null && !actual.getSiguiente().getDato().equals(dato)){
            actual=actual.getSiguiente();
        }
        if (actual.getSiguiente()!=null){
            actual.setSiguiente(actual.getSiguiente().getSiguiente());
            size--;
            return true;
        }
        return false;
    }
    
    
    public T obtener(int inx){
        if (inx < 0 || inx >= size){
            throw new IndexOutOfBoundsException();
        }
        Nodo<T> actual=head;
        for (int i = 0; i < inx; i++) {
            actual=actual.getSiguiente();
        }
        return actual.getDato();
    }
    
    public boolean contiene(T dato) {
        Nodo<T> actual=head;
        while (actual!=null) {
            if (actual.getDato().equals(dato)){
                return true;
            }
            actual=actual.getSiguiente();
        }
        return false;
    }

    public Nodo<T> getHead(){
        return head;
    }

    public int getSize(){
        return size;
    }
    
    public boolean estaVacia(){
        return (size==0); 
    }
}
