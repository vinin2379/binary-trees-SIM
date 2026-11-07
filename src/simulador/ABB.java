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
    private List<No> nosDesenho = new ArrayList<>();

    /**
     * Insere a chave. Retorna o novo nó, ou null se a chave já existia.
     */
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

    public void limpar() {
        raiz = null;
        ranqueAtual = 0;
        nosDesenho = new ArrayList<>();
    }

    /**
     * Nós percorridos da raiz até a chave (ou até o ponto de inserção).
     * Deve ser chamado antes de inserir.
     */
    public List<No> caminhoAte( int c ) {
        List<No> caminho = new ArrayList<>();
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            caminho.add( atual );
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return caminho;
    }

    public void atualizarNos( double espHor, double espVert, int margemHor, int margemVert ) {
        ranqueAtual = 0;
        nosDesenho.clear();
        atualizarNos( raiz, null, 0, espHor, espVert, margemHor, margemVert );
    }

    private void atualizarNos( No no, No pai, int nivel,
            double espHor, double espVert, int margemHor, int margemVert ) {
        if ( no != null ) {
            no.pai = pai;
            atualizarNos( no.esq, no, nivel + 1, espHor, espVert, margemHor, margemVert );
            no.nivel = nivel;
            no.ranque = ranqueAtual++;
            no.alvo.x = margemHor + espHor * no.ranque;
            no.alvo.y = margemVert + espVert * no.nivel;
            nosDesenho.add( no );
            atualizarNos( no.d, no, nivel + 1, espHor, espVert, margemHor, margemVert );
        }
    }

    public void animar( double delta, double velocidade ) {
        for ( No no : nosDesenho ) {
            no.animar( delta, velocidade );
        }
    }

    public void desenhar( EngineFrame e, No sob ) {
        for ( No no : nosDesenho ) {
            if ( no.pai != null && no.progressoLinha > 0 ) {
                desenharAresta( e, no.pai.pos, no.pos, no.progressoLinha );
            }
        }

        for ( No no : nosDesenho ) {
            no.desenhar( e, no == sob );
        }
    }

    /**
     * Linha que cresce a partir do ponto médio entre pai e filho, para os dois lados.
     */
    private static void desenharAresta( EngineFrame e, Vector2 pai, Vector2 filho, double p ) {
        double mx = ( pai.x + filho.x ) / 2;
        double my = ( pai.y + filho.y ) / 2;
        double ax = mx + ( pai.x - mx ) * p;
        double ay = my + ( pai.y - my ) * p;
        double bx = mx + ( filho.x - mx ) * p;
        double by = my + ( filho.y - my ) * p;
        e.drawLine( ax, ay, bx, by, EngineFrame.BLACK );
    }

    public No buscarPorClique( Vector2 mousePos ) {
        for ( No no : nosDesenho ) {
            if ( !no.aguardando && no.escala >= 1 && no.contemPonto( mousePos ) ) {
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