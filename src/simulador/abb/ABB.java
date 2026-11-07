package simulador.abb;

import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.arvore.ArvoreBase;
import simulador.arvore.No;

/**
 * Árvore binária de busca. Só define inserção e remoção; o resto vem de ArvoreBase.
 *
 * @author Vinícius Lima Sobon
 */
public class ABB extends ArvoreBase {

    @Override
    public No inserir( int c ) {
        No[] novo = new No[1];
        raiz = put( raiz, c, null, novo );
        return novo[0];
    }

    private No put( No no, int c, No pai, No[] novo ) {
        if ( no == null ) {
            no = new No( 20 );
            no.c = c;
            no.pai = pai;
            no.escala = 0;
            no.progressoLinha = pai == null ? 1 : 0;
            no.pos = pai != null ? new Vector2( pai.pos.x, pai.pos.y ) : new Vector2();
            novo[0] = no;
        } else if ( c < no.c ) {
            no.esq = put( no.esq, c, no, novo );
        } else if ( c > no.c ) {
            no.d = put( no.d, c, no, novo );
        }
        return no;
    }

    @Override
    public boolean remover( int c ) {
        No alvo = buscar( c );
        if ( alvo == null ) {
            return false;
        }
        raiz = delete( raiz, c );
        alvo.esq = null;
        alvo.d = null;
        marcarRemocao( alvo );
        return true;
    }

    private No delete( No no, int c ) {
        if ( no == null ) return null;

        if ( c < no.c ) {
            no.esq = delete( no.esq, c );
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
        return min( no.esq );
    }

    private No deleteMin( No no ) {
        if ( no.esq == null ) return no.d;
        no.esq = deleteMin( no.esq );
        return no;
    }
}