package simulador.avl;

import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.arvore.ArvoreBase;
import simulador.arvore.JanelaSimuladorArvore;
import simulador.ui.BotaoEstilizado;
import simulador.ui.Tema;

/**
 * Simulador de AVL. O slider do limite N fica no canto inferior esquerdo:
 * desliza suavemente e trava no inteiro mais próximo ao soltar. Ao mudar N,
 * a árvore é verificada e rebalanceada se necessário.
 */
public class JanelaSimuladorAVL extends JanelaSimuladorArvore {

    private static final double X = 10;
    private static final double Y = 550;
    private static final double LARG = 260;
    private static final double ALT = 40;
    private static final double TRILHA_X = 20;
    private static final double TRILHA_LARG = 220;
    private static final double TRILHA_Y = 578;
    private static final double VELOC_SUAVIZAR = 14;

    /** Limite N atual (1 a 3). */
    private int limite = 1;
    /** Posição-alvo do slider, de 0 a 1. Enquanto arrasta, segue o mouse; ao soltar, trava. */
    private double alvoF = 0;
    /** Posição desenhada do slider, perseguindo alvoF. */
    private double posF = 0;
    private boolean arrastando;

    public JanelaSimuladorAVL() {
        super( "Simulador: Árvore AVL" );
    }

    @Override
    protected ArvoreBase criarArvore() {
        return new AVL();
    }

    @Override
    protected void atualizarExtras( double delta ) {
        Vector2 m = getMousePositionPoint();

        if ( !isMouseButtonDown( MOUSE_BUTTON_LEFT ) ) {
            arrastando = false;
        }
        if ( m != null && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && sobExtras( m ) ) {
            arrastando = true;
        }
        if ( arrastando && m != null ) {
            alvoF = Math.max( 0, Math.min( 1, ( m.x - TRILHA_X ) / TRILHA_LARG ) );
        }

        int novoLimite = 1 + (int) Math.round( alvoF * 2 );
        if ( novoLimite != limite ) {
            limite = novoLimite;
            ( (AVL) arvore ).definirLimite( limite );
            solicitarRebalanceamento();
        }

        // ao soltar, o alvo trava no valor inteiro do limite atual
        if ( !arrastando ) {
            alvoF = ( limite - 1 ) / 2.0;
        }

        // o círculo persegue o alvo com suavização exponencial (independe do FPS)
        posF += ( alvoF - posF ) * ( 1 - Math.exp( -VELOC_SUAVIZAR * delta ) );
        if ( Math.abs( posF - alvoF ) < 0.001 ) {
            posF = alvoF;
        }
    }

    @Override
    protected boolean sobExtras( Vector2 m ) {
        return m.x >= X && m.x <= X + LARG && m.y >= Y && m.y <= Y + ALT;
    }

    @Override
    protected void desenharExtras() {
        BotaoEstilizado.retanguloArredondado( this, X - 1, Y - 1, LARG + 2, ALT + 2, 9, Tema.BORDA );
        BotaoEstilizado.retanguloArredondado( this, X, Y, LARG, ALT, 8, Tema.PAINEL );

        drawText( "Limite H: " + limite, X + 10, Y + 6, 14, Tema.TEXTO );

        // marcas nos três valores possíveis
        for ( int i = 0; i < 3; i++ ) {
            double mx = TRILHA_X + ( i / 2.0 ) * TRILHA_LARG;
            fillCircle( new Vector2( mx, TRILHA_Y + 2 ), 2, Tema.BORDA );
        }

        fillRectangle( TRILHA_X, TRILHA_Y, TRILHA_LARG, 4, Tema.BORDA );

        // o círculo cresce enquanto é arrastado
        double raio = arrastando ? 9 : 7;
        fillCircle( new Vector2( TRILHA_X + posF * TRILHA_LARG, TRILHA_Y + 2 ), raio, Tema.TRIGGER );
    }
}