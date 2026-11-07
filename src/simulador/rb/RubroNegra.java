package simulador.rb;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import simulador.arvore.ArvoreBase;
import simulador.arvore.No;
import simulador.ui.Tema;

/**
 * Árvore Rubro-Negra.
 *
 * @author Vinícius Lima Sobon
 */
public class RubroNegra extends ArvoreBase {

    private static final Color ROXO = new Color( 147, 51, 234 );

    private No ultimoInserido;
    private String legendaAtual;
    private boolean modo23;
    private final Map<No, Integer> niveis = new HashMap<>();

    public void setModo23( boolean v ) {
        modo23 = v;
    }

    public boolean modo23() {
        return modo23;
    }

    @Override
    protected boolean usaArco() {
        return true;
    }

    @Override
    public Color corAlerta() {
        return ROXO;
    }

    @Override
    public String legenda() {
        return legendaAtual;
    }

    @Override
    public String textoHover( No n ) {
        Integer lv = niveis.get( n );
        return lv == null ? null : "prof. preta = " + ( lv + 1 );
    }

    @Override
    public void atualizarNos( double espHor, double espVert, int margemHor, int margemVert ) {
        niveis.clear();
        calcularNiveis( raiz, 0 );
        super.atualizarNos( espHor, espVert, margemHor, margemVert );
    }

    /** Número de nós pretos do caminho da raiz até cada nó (inclusive). */
    private void calcularNiveis( No n, int pretosAcima ) {
        if ( n == null ) {
            return;
        }
        int pretos = pretosAcima + ( n.vermelho ? 0 : 1 );
        niveis.put( n, pretos - 1 );
        calcularNiveis( n.esq, pretos );
        calcularNiveis( n.d, pretos );
    }

    @Override
    protected int nivelLayout( No no, int nivelPadrao ) {
        if ( modo23 && niveis.containsKey( no ) ) {
            return niveis.get( no );
        }
        return nivelPadrao;
    }

    /** Desenha a caixa do 4-node temporário: pai preto com os dois filhos vermelhos. */
    @Override
    public void desenharFundo( EngineFrame e ) {
        List<No> nos = new ArrayList<>();
        emOrdem( raiz, nos );
        for ( No h : nos ) {
            if ( !h.vermelho && isRed( h.esq ) && isRed( h.d ) ) {
                desenharQuatroNos( e, h );
            }
        }
    }

    private void desenharQuatroNos( EngineFrame e, No h ) {
        double r = h.raio + 10;
        double minX = Math.min( h.pos.x, Math.min( h.esq.pos.x, h.d.pos.x ) ) - r;
        double maxX = Math.max( h.pos.x, Math.max( h.esq.pos.x, h.d.pos.x ) ) + r;
        double minY = Math.min( h.pos.y, Math.min( h.esq.pos.y, h.d.pos.y ) ) - r;
        double maxY = Math.max( h.pos.y, Math.max( h.esq.pos.y, h.d.pos.y ) ) + r;
        double x = minX;
        double y = minY;
        double w = maxX - minX;
        double hh = maxY - minY;

        e.fillRoundRectangle( x, y, w, hh, 14, ColorUtils.colorAlpha( Tema.TRIGGER, 0.08 ) );
        e.drawRoundRectangle( x, y, w, hh, 14, Tema.TRIGGER );
        e.drawText( "4-node temporário", x + 4, y - 14, 12, Tema.TEXTO );
    }

    @Override
    public No inserir( int c ) {
        No novo = inserirSemBalancear( c );
        if ( novo != null ) {
            rebalancear();
        }
        return novo;
    }

    /** BST pura: pendura o nó vermelho, sem corrigir. */
    @Override
    public No inserirSemBalancear( int c ) {
        No[] novo = new No[1];
        raiz = put( raiz, c, null, novo );
        ultimoInserido = novo[0];
        return novo[0];
    }

    private No put( No h, int c, No pai, No[] novo ) {
        if ( h == null ) {
            No n = new No( 20 );
            n.c = c;
            n.pai = pai;
            n.vermelho = pai != null;
            n.escala = 0;
            n.progressoLinha = pai == null ? 1 : 0;
            n.pos = pai != null ? new Vector2( pai.pos.x, pai.pos.y ) : new Vector2();
            novo[0] = n;
            return n;
        }
        if ( c < h.c ) {
            h.esq = put( h.esq, c, h, novo );
        } else if ( c > h.c ) {
            h.d = put( h.d, c, h, novo );
        }
        return h;
    }

    /** Nós do caso causado por N, e a legenda correspondente. */
    @Override
    public List<No> nosAlertados() {
        legendaAtual = null;
        List<No> r = new ArrayList<>();
        No n = ultimoInserido;
        if ( n == null || n.pai == null || !n.vermelho ) {
            return r;
        }
        No p = n.pai;

        if ( p.vermelho ) {
            No g = p.pai;
            if ( g == null ) {
                return r;
            }
            r.add( n );
            r.add( p );
            r.add( g );
            No tio = ( g.esq == p ) ? g.d : g.esq;
            if ( isRed( tio ) ) {
                r.add( tio );
                legendaAtual = "Caso 02: 4-node temporário, tio vermelho → inversão de cores";
            } else {
                legendaAtual = "Caso 02: 4-node temporário, tio preto → rotação e inversão";
            }
            return r;
        }

        if ( p.d == n ) {
            r.add( n );
            r.add( p );
            if ( isRed( p.esq ) ) {
                r.add( p.esq );
                legendaAtual = "Caso 02: dois filhos vermelhos → inversão de cores";
            } else {
                legendaAtual = "Caso 01: vermelho à direita → rotação à esquerda";
            }
        }
        return r;
    }

    @Override
    public void rebalancear() {
        raiz = fixupRec( raiz );
        if ( raiz != null ) {
            raiz.vermelho = false;
        }
    }

    private No fixupRec( No h ) {
        if ( h == null ) {
            return null;
        }
        h.esq = fixupRec( h.esq );
        h.d = fixupRec( h.d );
        return balance( h );
    }

    @Override
    public boolean remover( int c ) {
        if ( buscar( c ) == null ) {
            return false;
        }

        List<No> antes = new ArrayList<>();
        emOrdem( raiz, antes );
        Set<No> antesSet = new HashSet<>( antes );

        if ( !isRed( raiz.esq ) && !isRed( raiz.d ) ) {
            raiz.vermelho = true;
        }
        raiz = deleteRec( raiz, c );
        if ( raiz != null ) {
            raiz.vermelho = false;
        }

        List<No> depois = new ArrayList<>();
        emOrdem( raiz, depois );
        Set<No> depoisSet = new HashSet<>( depois );

        for ( No n : antesSet ) {
            if ( !depoisSet.contains( n ) ) {
                n.esq = null;
                n.d = null;
                marcarRemocao( n );
            }
        }
        aoAlterarEstrutura();
        return true;
    }

    private No deleteRec( No h, int c ) {
        if ( c < h.c ) {
            if ( !isRed( h.esq ) && !isRed( h.esq.esq ) ) {
                h = moveRedLeft( h );
            }
            h.esq = deleteRec( h.esq, c );
        } else {
            if ( isRed( h.esq ) ) {
                h = rotacionarDireita( h );
            }
            if ( c == h.c && h.d == null ) {
                return null;
            }
            if ( !isRed( h.d ) && !isRed( h.d.esq ) ) {
                h = moveRedRight( h );
            }
            if ( c == h.c ) {
                No m = min( h.d );
                h.c = m.c;
                h.d = deleteMin( h.d );
            } else {
                h.d = deleteRec( h.d, c );
            }
        }
        return balance( h );
    }

    private No deleteMin( No h ) {
        if ( h.esq == null ) {
            return null;
        }
        if ( !isRed( h.esq ) && !isRed( h.esq.esq ) ) {
            h = moveRedLeft( h );
        }
        h.esq = deleteMin( h.esq );
        return balance( h );
    }

    private No moveRedLeft( No h ) {
        flip( h );
        if ( isRed( h.d.esq ) ) {
            h.d = rotacionarDireita( h.d );
            h = rotacionarEsquerda( h );
            flip( h );
        }
        return h;
    }

    private No moveRedRight( No h ) {
        flip( h );
        if ( isRed( h.esq.esq ) ) {
            h = rotacionarDireita( h );
            flip( h );
        }
        return h;
    }

    private No balance( No h ) {
        if ( isRed( h.d ) && !isRed( h.esq ) ) {
            h = rotacionarEsquerda( h );
        }
        if ( isRed( h.esq ) && isRed( h.esq.esq ) ) {
            h = rotacionarDireita( h );
        }
        if ( isRed( h.esq ) && isRed( h.d ) ) {
            flip( h );
        }
        return h;
    }

    private No rotacionarEsquerda( No h ) {
        No x = h.d;
        registrarRotacao( h, x, x.esq );
        h.d = x.esq;
        x.esq = h;
        x.vermelho = h.vermelho;
        h.vermelho = true;
        return x;
    }

    private No rotacionarDireita( No h ) {
        No x = h.esq;
        registrarRotacao( h, x, x.d );
        h.esq = x.d;
        x.d = h;
        x.vermelho = h.vermelho;
        h.vermelho = true;
        return x;
    }

    /** Inversão de cores: só muda as cores, sem giro. */
    private void flip( No h ) {
        h.vermelho = !h.vermelho;
        h.esq.vermelho = !h.esq.vermelho;
        h.d.vermelho = !h.d.vermelho;
    }

    private static boolean isRed( No n ) {
        return n != null && n.vermelho;
    }

    private No min( No n ) {
        return n.esq == null ? n : min( n.esq );
    }

    private void emOrdem( No n, List<No> lista ) {
        if ( n == null ) {
            return;
        }
        emOrdem( n.esq, lista );
        lista.add( n );
        emOrdem( n.d, lista );
    }
}