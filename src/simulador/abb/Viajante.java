package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import simulador.ui.Sons;
import simulador.ui.Tema;

/**
 * Círculo com o valor da chave que percorre a árvore no modo didático.
 * Toca nodeJump ao passar de um nó para o próximo, nodeHigher/nodeLower na
 * comparação, e noAparece quando chega ao destino.
 */
public class Viajante {

    private static final int RAIO = 14;

    private final int valor;
    private final List<No> alvos = new ArrayList<>();
    public final No destino;
    private final Vector2 pos;
    private int indice;
    private double tempo;
    private boolean chegouTocou;

    public Viajante( int valor, List<No> caminho, No destino ) {
        this.valor = valor;
        this.destino = destino;
        alvos.addAll( caminho );
        alvos.add( destino );

        No origem = caminho.isEmpty() ? destino : caminho.get( 0 );
        pos = new Vector2( origem.alvo.x, origem.alvo.y - 70 );
    }

    /** Retorna true quando chegou ao destino. */
    public boolean atualizar( double delta, double velocidade ) {
        tempo += delta;
        if ( indice >= alvos.size() ) {
            return true;
        }

        No atual = alvos.get( indice );
        Vector2 destinoPos = atual == destino ? atual.alvo : atual.pos;

        double k = Math.min( 1, delta * 6 * velocidade );
        pos.x += ( destinoPos.x - pos.x ) * k;
        pos.y += ( destinoPos.y - pos.y ) * k;

        if ( Math.hypot( destinoPos.x - pos.x, destinoPos.y - pos.y ) < 2 ) {
            if ( atual == destino ) {
                if ( !chegouTocou ) {
                    chegouTocou = true;
                    Sons.noAparece();
                }
            } else {
                // comparação feita ao sair do nó atual
                if ( valor > atual.c ) {
                    Sons.nodeHigher();
                } else {
                    Sons.nodeLower();
                }
                Sons.nodeJump();
            }
            indice++;
        }
        return indice >= alvos.size();
    }

    /** Verde se a chave é maior que o nó de onde saiu, vermelho se é menor. */
    private Color cor() {
        if ( indice == 0 ) {
            return Tema.TRIGGER;
        }
        No anterior = alvos.get( indice - 1 );
        return valor > anterior.c ? Tema.VERDE : Tema.VERMELHO;
    }

    public void desenhar( EngineFrame e ) {
        if ( indice >= alvos.size() ) {
            return;
        }
        Color c = Tema.mistura( cor(), Color.WHITE, 0.15 + 0.15 * Math.sin( tempo * 12 ) );
        e.fillCircle( pos, RAIO, c );
        e.drawCircle( pos, RAIO, Tema.TRIGGER );

        String t = String.valueOf( valor );
        int larg = e.measureText( t, 12 );
        e.drawText( t, pos.x - larg / 2.0, pos.y - 4, 12, Color.WHITE );
    }
}