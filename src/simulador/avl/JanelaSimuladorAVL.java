package simulador.avl;

import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.arvore.ArvoreBase;
import simulador.arvore.JanelaSimuladorArvore;
import simulador.ui.BotaoEstilizado;
import simulador.ui.Sons;
import simulador.ui.Tema;
import java.awt.Color;


/**
 * Simulador de AVL. O slider do limite H fica no canto inferior esquerdo:
 * sobe com fade depois que a janela entra, tem hover e clique com som, e
 * trava no inteiro mais próximo ao soltar.
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
    private static final double DUR_ENTRADA = 0.5;
    private static final double DESLOC_ENTRADA = 50;

    /** Limite H atual (1 a 3). */
    private int limite = 1;
    /** Posição-alvo do slider, de 0 a 1. */
    private double alvoF = 0;
    /** Posição desenhada do slider, perseguindo alvoF. */
    private double posF = 0;
    private boolean arrastando;
    private boolean hoverSlider;
    private boolean hoverSliderAnt;
    private boolean sliderTocou;
    private double tEntrada;

    public JanelaSimuladorAVL() {
        super( "Simulador: Árvore AVL" );
    }

    @Override
    protected ArvoreBase criarArvore() {
        return new AVL();
    }

    @Override
    protected void atualizarExtras( double delta ) {
        tEntrada += delta;
        if ( !sliderTocou && progressoEntrada() > 0 ) {
            sliderTocou = true;
            Sons.sliderEntra();
        }

        Vector2 m = getMousePositionPoint();

        hoverSlider = m != null && sobExtras( m );
        if ( hoverSlider && !hoverSliderAnt ) {
            Sons.hover();
        }
        hoverSliderAnt = hoverSlider;

        if ( !isMouseButtonDown( MOUSE_BUTTON_LEFT ) ) {
            arrastando = false;
        }
        if ( m != null && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && sobExtras( m ) ) {
            Sons.clique();
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

        if ( !arrastando ) {
            alvoF = ( limite - 1 ) / 2.0;
        }

        posF += ( alvoF - posF ) * ( 1 - Math.exp( -VELOC_SUAVIZAR * delta ) );
        if ( Math.abs( posF - alvoF ) < 0.001 ) {
            posF = alvoF;
        }
    }

    /** Progresso da entrada do slider (0 a 1). */
    private double progressoEntrada() {
        double p = Math.min( 1, tEntrada / DUR_ENTRADA );
        return 1 - Math.pow( 1 - p, 3 );
    }

    @Override
    protected boolean sobExtras( Vector2 m ) {
        return m.x >= X && m.x <= X + LARG && m.y >= Y && m.y <= Y + ALT;
    }

    @Override
    protected void desenharExtras() {
        double e = progressoEntrada();
        if ( e <= 0 ) {
            return;
        }
        double dy = ( 1 - e ) * DESLOC_ENTRADA;
        double y = Y + dy;
        double ty = TRILHA_Y + dy;

        BotaoEstilizado.retanguloArredondado( this, X - 1, y - 1, LARG + 2, ALT + 2, 9,
                ColorUtils.fade( Tema.BORDA, e ) );
        BotaoEstilizado.retanguloArredondado( this, X, y, LARG, ALT, 8,
                ColorUtils.fade( Tema.PAINEL, e ) );

        drawText( "Limite H: " + limite, X + 10, y + 6, 14, ColorUtils.fade( Tema.TEXTO, e ) );

        for ( int i = 0; i < 3; i++ ) {
            double mx = TRILHA_X + ( i / 2.0 ) * TRILHA_LARG;
            fillCircle( new Vector2( mx, ty + 2 ), 2, ColorUtils.fade( Tema.BORDA, e ) );
        }

        Color trilha = ( hoverSlider || arrastando ) ? Tema.LINHA : Tema.BORDA;
        fillRectangle( TRILHA_X, ty, TRILHA_LARG, 4, ColorUtils.fade( trilha, e ) );

        double raio = arrastando ? 9 : ( hoverSlider ? 8.5 : 7 );
        Color bolinha = ( hoverSlider || arrastando ) ? Tema.TRIGGER_HOVER : Tema.TRIGGER;
        fillCircle( new Vector2( TRILHA_X + posF * TRILHA_LARG, ty + 2 ), raio,
                ColorUtils.fade( bolinha, e ) );
    }
}