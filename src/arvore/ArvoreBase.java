package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Parte comum das árvores: layout, animações, desenho, busca e fantasmas
 * (nós que estão saindo). Subclasses implementam só inserir e remover.
 *
 * @author Vinícius Lima Sobon
 */
public abstract class ArvoreBase {

    protected No raiz;
    protected int ranqueAtual;
    protected List<No> nosDesenho = new ArrayList<>();
    protected final List<No> fantasmas = new ArrayList<>();

    /** Insere a chave. Retorna o novo nó, ou null se a chave já existia. */
    public abstract No inserir( int c );

    /** Remove a chave. Retorna false se ela não existe. */
    public abstract boolean remover( int c );

    /** Marca um nó para a animação de saída. */
    protected void marcarRemocao( No no ) {
        no.aguardando = false;
        no.removendo = true;
        fantasmas.add( no );
    }

    /** Remove todos os nós com a animação de saída. */
    public void limpar() {
        for ( No no : nosDesenho ) {
            marcarRemocao( no );
        }
        raiz = null;
        nosDesenho = new ArrayList<>();
    }

    protected No buscar( int c ) {
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return atual;
    }

    /** Nós percorridos da raiz até a chave (ou até o ponto de inserção). */
    public List<No> caminhoAte( int c ) {
        List<No> caminho = new ArrayList<>();
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            caminho.add( atual );
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return caminho;
    }

    /** Recalcula layout e pais. Chamar depois de cada inserção, remoção ou rotação. */
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

    /** Linha que cresce a partir do ponto médio. Com pulso, pisca em branco duas vezes. */
    protected static void desenharAresta( EngineFrame e, Vector2 pai, Vector2 filho,
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
}