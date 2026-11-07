package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação arvore binaria de busca.
 * 
 * @author Vinícius Lima Sobon 
 */
public class ABB {
    
    private No raiz;
    private int ranqueAtual;
    private List<No> nosDesenho;
    
    public void put( int c ) {
        raiz = put( raiz, c );
        nosDesenho = new ArrayList<>();
    }
    
    private No put( No no, int c ) {
        
        if ( no == null ) {
            no = new No( 20 );
            no.c = c;
        } else {
            
            if ( c < no.c ) {
                no.esq = put( no.esq, c );
            } else if ( c > no.c ) {
                no.d = put( no.d, c );
            } else { // c == no.c
                // não faz nada
            }
            
        }
        
        return no;
        
    }
    
    public void emOrdem() {
        emOrdem( raiz );
        System.out.println();
    }
    
    private void emOrdem( No no ) {
        if ( no != null ) {
            emOrdem( no.esq );
            System.out.println( no );
            emOrdem( no.d );
        }
    }
    
    public void atualizarNos( double espHor, double espVert, int margemHor, int margemVert ) {
        ranqueAtual = 0;
        nosDesenho.clear();
        atualizarNos( raiz, 0, espHor, espVert, margemHor, margemVert );
    }
    
    private void atualizarNos( No no, int nivel, double espHor, double espVert, int margemHor, int margemVert ) {
        if ( no != null ) {
            atualizarNos( no.esq, nivel + 1, espHor, espVert, margemHor, margemVert );
            no.nivel = nivel;
            no.ranque = ranqueAtual++;
            no.pos.x = margemHor + espHor * no.ranque;
            no.pos.y = margemVert + espVert * no.nivel;
            nosDesenho.add( no );
            atualizarNos( no.d, nivel + 1, espHor, espVert, margemHor, margemVert );
        }
    }
    
   public void desenhar( EngineFrame e, No sob ) {

        for ( No no : nosDesenho ) {
            if ( no.esq != null ) {
                e.drawLine( no.pos, no.esq.pos, EngineFrame.BLACK );
            }
            if ( no.d != null ) {
                e.drawLine( no.pos, no.d.pos, EngineFrame.BLACK );
            }
        }

        for ( No no : nosDesenho ) {
            no.desenhar( e, no == sob );
        }

    }
    
    public No buscarPorClique( Vector2 mousePos ) {
        for ( No no : nosDesenho ) {
            if ( no.contemPonto( mousePos ) ) {
                return no;
            }
        }
        return null;
    }

    // delete
    public void delete( int c ) {
        raiz = delete( raiz, c );
    }

    private No delete( No no, int c ) {
        if ( no == null ) return null;

        if ( c < no.c ) {
            no.esq = delete(no.esq, c );
        } else if ( c > no.c ) {
            no.d = delete( no.d, c );
        } else {
            // caso 1 e 2: no com 0 ou 1 filho
            if ( no.d == null ) return no.esq;
            if ( no.esq == null ) return no.d;

            // caso 3: no com 2 filhos
            No t = no;
            no = min( t.d );
            no.d = deleteMin( t.d );
            no.esq = t.esq;
        }
        return no;
    }

    private No min( No no ) {
        if ( no.esq == null ) return no;
        return min(no.esq );
    }

    private No deleteMin( No no ) {
        if ( no.esq == null ) return no.d;
        no.esq = deleteMin(no.esq );
        return no;
    }
    
}