package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import simulador.ui.Sons;
import simulador.ui.Tema;

/**
 * Círculo com a chave que percorre a árvore no modo didático.
 * O nodeJump toca no início do trajeto, ao sair da caixa de texto.
 * Ao chegar em cada nó, toca nodeHigher/nodeLower; ao chegar ao destino, noAparece.
 */
public class Viajante {

    private static final int RAIO = 14;
    private static final double PAUSA_NO = 0.3;

    private final int valor;
    private final List<No> alvos = new ArrayList<>();
    public final No destino;
    private final Vector2 pos;
    private int indice;
    private double tempo;
    private boolean segurando;
    private double pausa;
    private boolean chegouTocou;

    public Viajante( int valor, List<No> caminho, No destino, Vector2 inicio ) {
        this.valor = valor;
        this.destino = destino;
        alvos.addAll( caminho );
        alvos.add( destino );
        pos = new Vector2( inicio.x, inicio.y );
        Sons.nodeJump();
    }

    /** Posição no mundo (a janela converte para a tela na hora de desenhar). */
    public Vector2 getPos() {
        return pos;
    }

    /** Retorna true quando chegou ao destino. */
    public boolean atualizar( double delta, double velocidade ) {
        tempo += delta;
        if ( indice >= alvos.size() ) {
            return true;
        }

        // parado num nó: depois da pausa, segue para o próximo
        if ( segurando ) {
            pausa -= delta * velocidade;
            if ( pausa <= 0 ) {
                segurando = false;
                indice++;
                if ( indice < alvos.size() ) {
                    Sons.nodeJump();
                }
            }
            return indice >= alvos.size();
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
                indice++;
            } else {
                if ( valor > atual.c ) {
                    Sons.nodeHigher();
                } else {
                    Sons.nodeLower();
                }
                segurando = true;
                pausa = PAUSA_NO;
            }
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

    /** Desenha em {@code p}, que já está em coordenadas de tela. */
    public void desenhar( EngineFrame e, Vector2 p ) {
        if ( indice >= alvos.size() ) {
            return;
        }
        Color c = Tema.mistura( cor(), Color.WHITE, 0.15 + 0.15 * Math.sin( tempo * 12 ) );
        e.fillCircle( p, RAIO, c );
        e.drawCircle( p, RAIO, Tema.TRIGGER );

        String t = String.valueOf( valor );
        int larg = e.measureText( t, 12 );
        e.drawText( t, p.x - larg / 2.0, p.y - 4, 12, Color.WHITE );
    }
}