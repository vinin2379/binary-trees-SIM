package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.awt.Color;
import simulador.ui.Tema;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author Vinícius Lima Sobon
 */
public abstract class ArvoreBase {

    protected No raiz;
    protected int ranqueAtual;
    protected List<No> nosDesenho = new ArrayList<>();
    protected final List<No> fantasmas = new ArrayList<>();

    /** Nós tocados pelas rotações da operação em curso. */
    private final Set<No> envolvidos = new HashSet<>();
    private Giro giroAtual;
    private boolean girosLiberados;
    private double ultEspHor, ultEspVert;
    private int ultMargemHor, ultMargemVert;
    private boolean temLayout;
    

    public abstract No inserir( int c );

    public abstract boolean remover( int c );

    /** Quando true, nós que trocam de pai fazem um arco (e rotações usam giro coletivo). */
    protected boolean usaArco() {
        return false;
    }

    /** Chamado pela subclasse antes de cada rotação, com os nós envolvidos. */
    protected void registrarRotacao( No... nos ) {
        for ( No n : nos ) {
            if ( n != null ) {
                envolvidos.add( n );
            }
        }
    }

    protected void marcarRemocao( No no ) {
        no.aguardando = false;
        no.removendo = true;
        fantasmas.add( no );
    }

    public void limpar() {
        for ( No no : nosDesenho ) {
            marcarRemocao( no );
        }
        raiz = null;
        nosDesenho = new ArrayList<>();
        envolvidos.clear();
        giroAtual = null;
        girosLiberados = false;
    }

    protected No buscar( int c ) {
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return atual;
    }

    public List<No> caminhoAte( int c ) {
        List<No> caminho = new ArrayList<>();
        No atual = raiz;
        while ( atual != null && atual.c != c ) {
            caminho.add( atual );
            atual = c < atual.c ? atual.esq : atual.d;
        }
        return caminho;
    }

    /**
     * Recalcula layout e pais de toda a árvore de uma vez. Se houve rotações,
     * todos os nós tocados entram num único giro coletivo.
     */
    public void atualizarNos( double espHor, double espVert, int margemHor, int margemVert ) {
        ultEspHor = espHor;
        ultEspVert = espVert;
        ultMargemHor = margemHor;
        ultMargemVert = margemVert;
        temLayout = true;
        ranqueAtual = 0;
        nosDesenho.clear();
        atualizarNos( raiz, null, 0, espHor, espVert, margemHor, margemVert );

        // o giro coletivo cuida dos nós envolvidos; os demais deslizam ou fazem arco
        List<No> grupo = new ArrayList<>();
        if ( usaArco() ) {
            for ( No no : nosDesenho ) {
                if ( envolvidos.contains( no ) ) {
                    grupo.add( no );
                }
            }
        }
        if ( !grupo.isEmpty() ) {
            giroAtual = new Giro( grupo );
        }
        envolvidos.clear();
    }

    private void atualizarNos( No no, No pai, int nivel,
            double espHor, double espVert, int margemHor, int margemVert ) {
        if ( no != null ) {
            atualizarNos( no.esq, no, nivel + 1, espHor, espVert, margemHor, margemVert );
            boolean mudouPai = no.trocarPai( pai );
            no.nivel = nivel;
            no.ranque = ranqueAtual++;
            double x = margemHor + espHor * no.ranque;
            double y = margemVert + espVert * no.nivel;
            if ( envolvidos.contains( no ) ) {
                no.alvo.x = x;
                no.alvo.y = margemVert + espVert * nivelLayout( no, no.nivel );
            } else {
                no.definirAlvo( x, y, mudouPai && usaArco() );
            }
            nosDesenho.add( no );
            atualizarNos( no.d, no, nivel + 1, espHor, espVert, margemHor, margemVert );
        }
    }

    /** Libera o giro coletivo. Até chamar isso, o grupo fica parado. */
    public void liberarGiros() {
        girosLiberados = true;
    }

    /** True enquanto o giro coletivo está em andamento ou pendente. */
    public boolean temGirosPendentes() {
        return giroAtual != null;
    }

    /** Religa todas as ligações bloqueadas. */
    public void liberarLigacoes() {
        for ( No no : nosDesenho ) {
            no.ligacaoBloqueada = false;
        }
    }

    public void animar( double delta, double velocidade ) {
        if ( girosLiberados && giroAtual != null ) {
            if ( giroAtual.atualizar( delta, velocidade ) ) {
                giroAtual = null;
                girosLiberados = false;
            }
        }

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
                double f = Math.min( no.linkVisivel, no.paiAntigo.linkVisivel );
                desenharAresta( e, no.paiAntigo.pos, no.pos, no.progressoAntigo * f, 0, corLink( no ) );
            }
            if ( no.pai != null && no.progressoLinha > 0 ) {
                double f = Math.min( no.linkVisivel, no.pai.linkVisivel );
                desenharAresta( e, no.pai.pos, no.pos, no.progressoLinha * f, no.pulsoLinha, corLink( no ) );
            }
        }

        for ( No no : todos ) {
            no.desenhar( e, no == sob );
        }
    }

    private static Color corLink( No no ) {
        return no.vermelho ? Tema.VERMELHO : EngineFrame.BLACK;
    }

    protected static void desenharAresta( EngineFrame e, Vector2 pai, Vector2 filho,
            double p, double pulso, Color cor ) {
        if ( p < 0.01 ) {
            return;
        }
        double mx = ( pai.x + filho.x ) / 2;
        double my = ( pai.y + filho.y ) / 2;
        double ax = mx + ( pai.x - mx ) * p;
        double ay = my + ( pai.y - my ) * p;
        double bx = mx + ( filho.x - mx ) * p;
        double by = my + ( filho.y - my ) * p;

        e.drawLine( ax, ay, bx, by, cor );

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
    
    /** Versão didática da inserção: só a BST, sem rebalancear. Padrão: igual a inserir. */
    /** Versão didática da inserção: só a BST, sem rebalancear. */
    public No inserirSemBalancear( int c ) {
        return inserir( c );
    }

    /** Rebalanceia a árvore, registrando as rotações. Padrão: nada. */
    public void rebalancear() {
    }

    /** Nós que devem piscar antes do rebalanceamento. Padrão: nenhum. */
    public List<No> nosAlertados() {
        return new ArrayList<>();
    }

    /** Cor do piscar desta árvore. */
    public Color corAlerta() {
        return Tema.VERMELHO;
    }
    
    public List<No> nosQueGiram() {
        List<No> todos = new ArrayList<>();
        coletarNos( raiz, todos );

        Map<No, No[]> ponteiros = new HashMap<>();
        Map<No, Integer> alturas = new HashMap<>();
        for ( No n : todos ) {
            ponteiros.put( n, new No[] { n.esq, n.d } );
            alturas.put( n, n.altura );
        }

        No raizAntiga = raiz;
        envolvidos.clear();
        rebalancear();
        List<No> grupo = new ArrayList<>( envolvidos );
        envolvidos.clear();

        raiz = raizAntiga;
        for ( No n : todos ) {
            No[] p = ponteiros.get( n );
            n.esq = p[0];
            n.d = p[1];
            n.altura = alturas.get( n );
            n.destaque = 0;
        }
        return grupo;
    }

   /** Quantidade de nós na subárvore de n (0 se n for null). */
   public int contarSubarvore( No n ) {
       if ( n == null ) {
           return 0;
       }
       return 1 + contarSubarvore( n.esq ) + contarSubarvore( n.d );
   }

   private static void coletarNos( No n, List<No> lista ) {
       if ( n == null ) {
           return;
       }
       lista.add( n );
       coletarNos( n.esq, lista );
       coletarNos( n.d, lista );
   }
    
   /** Altura de uma subárvore (0 se nula). Usada pela conta de balanceamento na janela. */
    public int alturaSubarvore( No n ) {
        return n == null ? 0 : n.altura;
    }
    
    /** Chamado depois de remover/inserir para recalcular pais e layout. Padrão: nada. */
    protected void aoAlterarEstrutura() {
    }
    
    /** Recalcula o layout com os últimos espaçamentos usados. */
    public void reorganizar() {
        if ( temLayout ) {
            atualizarNos( ultEspHor, ultEspVert, ultMargemHor, ultMargemVert );
        }
    }

    /** Nível usado no desenho (y). Padrão: a profundidade normal. */
    protected int nivelLayout( No no, int nivelPadrao ) {
        return nivelPadrao;
    }

    /** Texto mostrado no hover do nó. Padrão: nenhum (a janela mostra o balanço). */
    public String textoHover( No n ) {
        return null;
    }

    /** Legenda do caso em andamento, mostrada na janela. Padrão: nenhuma. */
    public String legenda() {
        return null;
    }

    /** Desenho de fundo (ex.: caixa do 4-node), chamado antes dos nós. Padrão: nada. */
    public void desenharFundo( EngineFrame e ) {
    }

}