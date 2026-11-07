package simulador.abb;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Árvore binária de busca com animações de inserção, remoção e limpeza.
 *
 * @author Vinícius Lima Sobon
 */
public class ABB {

    private No raiz;
    private int ranqueAtual;
    private List<No> nosDesenho = new ArrayList<>();
    private final List<No> fantasmas = new ArrayList<>();

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

    /**
     * Remove a chave. O nó some encolhendo; as ligações são refeitas em atualizarNos.
     */
    public boolean remover( int c ) {
        No alvo = buscar( c );
        if ( alvo == null ) {
            return false;
        }
        raiz = delete( raiz, c );
        alvo.esq = null;
        alvo.d = null;
        alvo.aguardando = false;
        alvo.removendo = true;
        fantasmas.add( alvo );
        return true;
    }

    /**
     * Remove todos os nós com a mesma animação de saída.
     */
    public void limpar() {
        for ( No no : nosDesenho ) {
            no.aguardando = false;
            no.removendo = true;
            fantasmas.add( no );
        }
        raiz = null;
        nosDesenho = new ArrayList<>();
    }

    /**
     * Nós percorridos da raiz até a chave (ou até o ponto de inserção).
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
            atualizarNos( no.esq, no, nivel + 1, espHor, espVert, margemHor, margemVert );
            no.trocarPai( pai );
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
        Iterator<No> it = fantasmas.iterator();
        while ( it.hasNext() ) {
            No g = it.next();
            g.animar( delta, velocidade );
            if ( g.escala <= 0 && g.progressoLinha <= 0 && g.paiAntigo == null ) {
                it.remove();
            }
        }
    }

    public void desenhar( EngineFrame e, No sob ) {
        List<No> todos = new ArrayList<>( nosDesenho );
        todos.addAll( fantasmas );

        for ( No no : todos ) {
            if ( no.paiAntigo != null && no.progressoAntigo > 0 ) {
                desenharAresta( e, no.paiAntigo.pos, no.pos, no.progressoAntigo, 0 );
            }
            if ( no.pai != null && no.progressoLinha > 0 ) {
                desenharAresta( e, no.pai.pos, no.pos, no.progressoLinha, no.pulsoLinha );
            }
        }

        for ( No no : todos ) {
            no.desenhar( e, no == sob );
        }
    }

    /**
     * Linha que cresce a partir do ponto médio. Com pulso, pisca em branco duas vezes.
     */
    private static void desenharAresta( EngineFrame e, Vector2 pai, Vector2 filho,
            double p, double pulso ) {
        double mx = ( pai.x + filho.x ) / 2;
        double my = ( pai.y + filho.y ) / 2;
        double ax = mx + ( pai.x - mx ) * p;
        double ay = my + ( pai.y - my ) * p;
        double bx = mx + ( filho.x - mx ) * p;
        double by = my + ( filho.y - my ) * p;

        e.drawLine( ax, ay, bx, by, EngineFrame.BLACK );

        if ( pulso > 0 && Math.sin( pulso * 4 * Math.PI ) > 0 ) {
            e.drawLine( ax, ay, bx, by, EngineFrame.WHITE );
            e.drawLine( ax, ay + 1, bx, by + 1, EngineFrame.WHITE );
        }
    }

    public No buscarPorClique( Vector2 mousePos ) {
        for ( No no : nosDesenho ) {
            if ( !no.aguardando && no.escala >= 1 && no.contemPonto( mousePos ) ) {
                return no;
            }
        }
        return null;
    }

    private No buscar( int c ) {
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return atual;
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