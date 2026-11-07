package simulador.avl;

import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.List;
import simulador.arvore.ArvoreBase;
import simulador.arvore.No;

/**
 * Árvore AVL com limite N de desbalanceamento (padrão 1, que é a AVL clássica).
 * A inserção didática separa a BST pura do rebalanceamento.
 *
 * @author Vinícius Lima Sobon
 */
public class AVL extends ArvoreBase {

    private int limite = 1;

    @Override
    protected boolean usaArco() {
        return true;
    }

   
    public void definirLimite( int n ) {
        limite = Math.max( 1, n );
    }

    @Override
    public No inserir( int c ) {
        No novo = inserirSemBalancear( c );
        if ( novo != null ) {
            rebalancear();
        }
        return novo;
    }

    

    private No put( No no, int c, No pai, No[] novo ) {
        if ( no == null ) {
            No criado = criarNo( c, pai );
            novo[0] = criado;
            return criado;
        }
        if ( c < no.c ) {
            no.esq = put( no.esq, c, no, novo );
        } else if ( c > no.c ) {
            no.d = put( no.d, c, no, novo );
        } else {
            return no;
        }
        atualizarAltura( no );
        return no;
    }

    private No criarNo( int c, No pai ) {
        No n = new No( 20 );
        n.c = c;
        n.pai = pai;
        n.altura = 1;
        n.escala = 0;
        n.progressoLinha = pai == null ? 1 : 0;
        n.pos = pai != null ? new Vector2( pai.pos.x, pai.pos.y ) : new Vector2();
        return n;
    }

    @Override
    public void rebalancear() {
        raiz = balancearRec( raiz );
    }

    private No balancearRec( No no ) {
        if ( no == null ) {
            return null;
        }
        no.esq = balancearRec( no.esq );
        no.d = balancearRec( no.d );
        atualizarAltura( no );
        return balancear( no );
    }

    
    

    private void coletarDesbalanceados( No no, List<No> lista ) {
        if ( no == null ) {
            return;
        }
        if ( Math.abs( fatorBalanceamento( no ) ) > limite ) {
            lista.add( no );
        }
        coletarDesbalanceados( no.esq, lista );
        coletarDesbalanceados( no.d, lista );
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
        if ( no == null ) {
            return null;
        }
        if ( c < no.c ) {
            no.esq = delete( no.esq, c );
        } else if ( c > no.c ) {
            no.d = delete( no.d, c );
        } else {
            if ( no.d == null ) return no.esq;
            if ( no.esq == null ) return no.d;

            No t = no;
            no = min( t.d );
            no.d = deleteMin( t.d );
            no.esq = t.esq;
        }
        atualizarAltura( no );
        return balancear( no );
    }

    private No min( No no ) {
        return no.esq == null ? no : min( no.esq );
    }

    private No deleteMin( No no ) {
        if ( no.esq == null ) {
            return no.d;
        }
        no.esq = deleteMin( no.esq );
        atualizarAltura( no );
        return balancear( no );
    }

    
    private static int alturaDe( No n ) {
        return n == null ? 0 : n.altura;
    }

    private static void atualizarAltura( No n ) {
        n.altura = 1 + Math.max( alturaDe( n.esq ), alturaDe( n.d ) );
    }

    private static int fatorBalanceamento( No n ) {
        return n == null ? 0 : alturaDe( n.esq ) - alturaDe( n.d );
    }

    private No balancear( No no ) {
        int f = fatorBalanceamento( no );
        if ( f > limite ) {
            if ( fatorBalanceamento( no.esq ) < 0 ) {
                no.esq = rotacionarEsquerda( no.esq );   // LR
            }
            return rotacionarDireita( no );              // LL
        }
        if ( f < -limite ) {
            if ( fatorBalanceamento( no.d ) > 0 ) {
                no.d = rotacionarDireita( no.d );        // RL
            }
            return rotacionarEsquerda( no );             // RR
        }
        return no;
    }

    private No rotacionarDireita( No y ) {
        No x = y.esq;
        No t2 = x.d;
        registrarRotacao( x, y, t2 );
        x.d = y;
        y.esq = t2;
        atualizarAltura( y );
        atualizarAltura( x );
        marcarRotacao( x, y );
        return x;
    }

    private No rotacionarEsquerda( No x ) {
        No y = x.d;
        No t2 = y.esq;
        registrarRotacao( x, y, t2 );
        y.esq = x;
        x.d = t2;
        atualizarAltura( x );
        atualizarAltura( y );
        marcarRotacao( x, y );
        return y;
    }

    private static void marcarRotacao( No a, No b ) {
        a.destaque = 1;
        b.destaque = 1;
    }
    
    private No ultimoInserido;

@Override
public No inserirSemBalancear( int c ) {
    No[] novo = new No[1];
    raiz = put( raiz, c, null, novo );
    ultimoInserido = novo[0];
    return novo[0];
}

    /**
     * Nós que participam da rotação do caso causado por N (slides 9 a 10):
     * A é o primeiro ancestral com |fator| > limite; B é o filho de A no caminho
     * até N; C é o filho de B no caminho até N. Retorna [A, B, C] ou vazio.
     */
    @Override
    public List<No> nosAlertados() {
        List<No> resultado = new ArrayList<>();
        if ( ultimoInserido == null ) {
            return resultado;
        }
        // sobe pelos pais de N até achar o primeiro A desbalanceado
        No a = ultimoInserido.pai;
        No filho = ultimoInserido;
        while ( a != null && Math.abs( fatorBalanceamento( a ) ) <= limite ) {
            filho = a;
            a = a.pai;
        }
        if ( a == null ) {
            return resultado;
        }
        // B é o filho de A que contém N; C é o filho de B que contém N
        No b = ( a.esq == filho ) ? a.esq : a.d;
        No c = ( b.esq == ultimoInserido || b.d == ultimoInserido ) ? ultimoInserido : null;
        resultado.add( a );
        resultado.add( b );
        if ( c != null ) {
            resultado.add( c );
        }
        return resultado;
    }
    
}