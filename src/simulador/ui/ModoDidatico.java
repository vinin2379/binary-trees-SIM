package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

/**
 * Botão da toolbar do modo didático: ativar, velocidade e uma opção extra
 * opcional (ex.: Visão 2-3 na Rubro-Negra).
 */
public class ModoDidatico {

    private static final double LARG_FECHADO = 190;
    private static final double LARG_ABERTO = 240;
    private static final double ALT_CABECALHO = 36;
    private static final double ALT_ABERTO = 130;
    private static final double ALT_ABERTO_EXTRA = 170;
    private static final double VEL_MIN = 0.5;
    private static final double VEL_MAX = 3.0;
    private static final double TRILHA_X = 18;
    private static final double TRILHA_LARG = 200;

    private final double x;
    private final double y;
    private boolean aberto;
    private boolean ativo;
    private boolean arrastandoVel;
    private boolean hoverCab;
    private boolean hoverCabAnt;
    private double progresso;
    private double velocidade = 1.0;

    private boolean extraSuportada;
    private boolean extraAtiva;

    public ModoDidatico( double x, double y ) {
        this.x = x;
        this.y = y;
    }

    public boolean ativo() {
        return ativo;
    }

    public double velocidadeAnimacao() {
        return ativo ? velocidade : 1.0;
    }

    /** Liga a opção extra (checkbox abaixo da velocidade). */
    public void suportarExtra( boolean v ) {
        extraSuportada = v;
    }

    public boolean extraAtiva() {
        return extraAtiva;
    }

    private double largura() {
        return LARG_FECHADO + ( LARG_ABERTO - LARG_FECHADO ) * progresso;
    }

    private double altura() {
        double alvo = extraSuportada ? ALT_ABERTO_EXTRA : ALT_ABERTO;
        return ALT_CABECALHO + ( alvo - ALT_CABECALHO ) * progresso;
    }

    public boolean contem( Vector2 m ) {
        return m != null && dentro( m, x, y, largura(), altura() );
    }

    private static boolean dentro( Vector2 m, double x, double y, double w, double h ) {
        return m.x >= x && m.x <= x + w && m.y >= y && m.y <= y + h;
    }

    public void atualizar( EngineFrame e, double delta ) {
        progresso += ( ( aberto ? 1 : 0 ) - progresso ) * Math.min( 1, delta * 9 );

        if ( !e.isMouseButtonDown( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            arrastandoVel = false;
        }

        Vector2 m = e.getMousePositionPoint();
        hoverCab = m != null && dentro( m, x, y, largura(), ALT_CABECALHO );
        if ( hoverCab && !hoverCabAnt ) {
            Sons.hover();
        }
        hoverCabAnt = hoverCab;

        if ( m == null ) {
            return;
        }

        boolean controlesAtivos = aberto && progresso > 0.8;

        if ( e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            if ( hoverCab && !aberto ) {
                Sons.downer();
                aberto = !aberto;
            } else if ( controlesAtivos && dentro( m, x + 12, y + 46, 230, 24 ) ) {
                Sons.clique();
                ativo = !ativo;
            } else if ( controlesAtivos && dentro( m, x + 10, y + 94, 216, 22 ) ) {
                Sons.clique();
                arrastandoVel = true;
                ajustarVelocidade( m );
            } else if ( controlesAtivos && extraSuportada && dentro( m, x + 12, y + 132, 230, 24 ) ) {
                Sons.clique();
                extraAtiva = !extraAtiva;
            } else if ( (aberto && !contem( m )) || aberto && contem( m )  ) {
                Sons.guardarDowner();
                aberto = false;
            }
        }

        if ( arrastandoVel && e.isMouseButtonDown( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            ajustarVelocidade( m );
        }
    }

    private void ajustarVelocidade( Vector2 m ) {
        double f = ( m.x - ( x + TRILHA_X ) ) / TRILHA_LARG;
        f = Math.max( 0, Math.min( 1, f ) );
        double v = VEL_MIN + f * ( VEL_MAX - VEL_MIN );
        velocidade = Math.round( v * 10 ) / 10.0;
    }

    public void desenhar( EngineFrame e ) {
        double w = largura();
        double h = altura();

        BotaoEstilizado.retanguloArredondado( e, x - 1, y - 1, w + 2, h + 2, 9, Tema.BORDA );
        BotaoEstilizado.retanguloArredondado( e, x, y, w, h, 8, Tema.PAINEL );

        Color fundoCab = Tema.mistura( Tema.TRIGGER, Tema.PAINEL, progresso );
        if ( hoverCab ) {
            fundoCab = Tema.mistura( fundoCab, Tema.TRIGGER_HOVER, 0.35 );
        }
        Color txtCab = Tema.mistura( Tema.TEXTO_CLARO, Tema.TEXTO, progresso );
        BotaoEstilizado.retanguloArredondado( e, x, y, w, ALT_CABECALHO, 8, fundoCab );
        e.drawText( "Modo Didático", x + 14, y + ALT_CABECALHO / 2.0 - 16 / 3.0, 16, txtCab );

        desenharSetinha( e, x + w - 20, y + ALT_CABECALHO / 2.0, progresso * Math.PI, txtCab );

        if ( progresso > 0.5 ) {
            e.drawLine( x + 8, y + ALT_CABECALHO, x + w - 8, y + ALT_CABECALHO, Tema.BORDA );

            double cx = x + 18;
            double cy = y + 52;
            BotaoEstilizado.retanguloArredondado( e, cx - 1, cy - 1, 18, 18, 5, Tema.BORDA );
            BotaoEstilizado.retanguloArredondado( e, cx, cy, 16, 16, 4, ativo ? Tema.TRIGGER : Tema.PAINEL );
            if ( ativo ) {
                e.fillRectangle( cx + 5, cy + 5, 6, 6, Tema.PAINEL );
            }
            e.drawText( "Ativar modo didático", x + 42, cy + 8 - 14 / 3.0, 14, Tema.TEXTO );

            e.drawText( String.format( "Velocidade: %.1fx", velocidade ), x + 18, y + 78, 14, Tema.TEXTO );
            e.fillRectangle( x + TRILHA_X, y + 102, TRILHA_LARG, 4, Tema.BORDA );
            double f = ( velocidade - VEL_MIN ) / ( VEL_MAX - VEL_MIN );
            e.fillCircle( new Vector2( x + TRILHA_X + f * TRILHA_LARG, y + 104 ), 7, Tema.TRIGGER );
        }

        if ( extraSuportada && progresso > 0.85 ) {
            double a = Math.min( 1, ( progresso - 0.85 ) / 0.15 );
            double ex = x + 18;
            double ey = y + 140;
            BotaoEstilizado.retanguloArredondado( e, ex - 1, ey - 1, 18, 18, 5,
                    ColorUtils.fade( Tema.BORDA, a ) );
            BotaoEstilizado.retanguloArredondado( e, ex, ey, 16, 16, 4,
                    Tema.mistura( Tema.PAINEL, extraAtiva ? Tema.TRIGGER : Tema.PAINEL, a ) );
            if ( extraAtiva ) {
                e.fillRectangle( ex + 5, ey + 5, 6, 6, ColorUtils.fade( Tema.PAINEL, a ) );
            }
            e.drawText( "Visão 2-3", x + 42, ey + 8 - 14 / 3.0, 14, ColorUtils.fade( Tema.TEXTO, a ) );
        }
    }

    private static void desenharSetinha( EngineFrame e, double cx, double cy, double ang, Color cor ) {
        double[][] base = { { -5, -2 }, { 0, 3 }, { 5, -2 } };
        double cos = Math.cos( ang );
        double sin = Math.sin( ang );
        double[] xs = new double[3];
        double[] ys = new double[3];
        for ( int i = 0; i < 3; i++ ) {
            double px = base[i][0];
            double py = base[i][1];
            xs[i] = cx + px * cos - py * sin;
            ys[i] = cy + px * sin + py * cos;
        }
        e.drawLine( xs[0], ys[0], xs[1], ys[1], cor );
        e.drawLine( xs[1], ys[1], xs[2], ys[2], cor );
    }
}