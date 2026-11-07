package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

/**
 * Botão da toolbar que expande para mostrar as opções do modo didático.
 */
class ModoDidatico {

    private static final double LARG_FECHADO = 170;
    private static final double LARG_ABERTO = 240;
    private static final double ALT_CABECALHO = 36;
    private static final double ALT_ABERTO = 130;
    private static final double VEL_MIN = 0.5;
    private static final double VEL_MAX = 3.0;
    private static final double TRILHA_X = 18;
    private static final double TRILHA_LARG = 200;

    private final double x;
    private final double y;
    private boolean aberto;
    private boolean ativo;
    private boolean arrastando;
    private double progresso;
    private double velocidade = 1.0;

    ModoDidatico( double x, double y ) {
        this.x = x;
        this.y = y;
    }

    boolean ativo() {
        return ativo;
    }

    double velocidadeAnimacao() {
        return ativo ? velocidade : 1.0;
    }

    private double largura() {
        return LARG_FECHADO + ( LARG_ABERTO - LARG_FECHADO ) * progresso;
    }

    private double altura() {
        return ALT_CABECALHO + ( ALT_ABERTO - ALT_CABECALHO ) * progresso;
    }

    boolean contem( Vector2 m ) {
        return m != null && dentro( m, x, y, largura(), altura() );
    }

    private static boolean dentro( Vector2 m, double x, double y, double w, double h ) {
        return m.x >= x && m.x <= x + w && m.y >= y && m.y <= y + h;
    }

    void atualizar( EngineFrame e, double delta ) {
        progresso += ( ( aberto ? 1 : 0 ) - progresso ) * Math.min( 1, delta * 9 );

        if ( !e.isMouseButtonDown( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            arrastando = false;
        }

        Vector2 m = e.getMousePositionPoint();
        if ( m == null ) {
            return;
        }

        boolean controlesAtivos = aberto && progresso > 0.8;

        if ( e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            if ( dentro( m, x, y, largura(), ALT_CABECALHO ) ) {
                aberto = !aberto;
            } else if ( controlesAtivos && dentro( m, x + 12, y + 46, 230, 24 ) ) {
                ativo = !ativo;
            } else if ( controlesAtivos && dentro( m, x + 10, y + 94, 216, 22 ) ) {
                arrastando = true;
                ajustarVelocidade( m );
            } else if ( aberto && !contem( m ) ) {
                aberto = false;
            }
        }

        if ( arrastando && e.isMouseButtonDown( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            ajustarVelocidade( m );
        }
    }

    private void ajustarVelocidade( Vector2 m ) {
        double f = ( m.x - ( x + TRILHA_X ) ) / TRILHA_LARG;
        f = Math.max( 0, Math.min( 1, f ) );
        double v = VEL_MIN + f * ( VEL_MAX - VEL_MIN );
        velocidade = Math.round( v * 10 ) / 10.0;
    }

    void desenhar( EngineFrame e ) {
        double w = largura();
        double h = altura();

        BotaoEstilizado.retanguloArredondado( e, x - 1, y - 1, w + 2, h + 2, 9, Tema.BORDA );
        BotaoEstilizado.retanguloArredondado( e, x, y, w, h, 8, Tema.PAINEL );

        Color fundoCab = Tema.mistura( Tema.TRIGGER, Tema.PAINEL, progresso );
        Color txtCab = Tema.mistura( Tema.TEXTO_CLARO, Tema.TEXTO, progresso );
        BotaoEstilizado.retanguloArredondado( e, x, y, w, ALT_CABECALHO, 8, fundoCab );
        e.drawText( "Modo Didático", x + 14, y + ALT_CABECALHO / 2.0 - 16 / 3.0, 16, txtCab );

        if ( progresso > 0.5 ) {
            e.drawLine( x + 8, y + ALT_CABECALHO, x + w - 8, y + ALT_CABECALHO, Tema.BORDA );

            // checkbox
            double cx = x + 18;
            double cy = y + 52;
            BotaoEstilizado.retanguloArredondado( e, cx - 1, cy - 1, 18, 18, 5, Tema.BORDA );
            BotaoEstilizado.retanguloArredondado( e, cx, cy, 16, 16, 4, ativo ? Tema.TRIGGER : Tema.PAINEL );
            if ( ativo ) {
                e.fillRectangle( cx + 5, cy + 5, 6, 6, Tema.PAINEL );
            }
            e.drawText( "Ativar modo didático", x + 42, cy + 8 - 14 / 3.0, 14, Tema.TEXTO );

            // slider de velocidade
            e.drawText( String.format( "Velocidade: %.1fx", velocidade ), x + 18, y + 78, 14, Tema.TEXTO );
            e.fillRectangle( x + TRILHA_X, y + 102, TRILHA_LARG, 4, Tema.BORDA );
            double f = ( velocidade - VEL_MIN ) / ( VEL_MAX - VEL_MIN );
            e.fillCircle( new Vector2( x + TRILHA_X + f * TRILHA_LARG, y + 104 ), 7, Tema.TRIGGER );
        }
    }
}