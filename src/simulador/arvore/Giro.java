package simulador.arvore;

import java.util.List;

/**
 * Animação de uma rotação em três tempos: desconecta as ligações do grupo,
 * gira o grupo em volta do centro dele e reconecta. O destino é lido do nó
 * a cada frame, então sempre é o layout atual.
 */
class Giro {

    private static final double DURACAO = 1.4;
    private static final double FIM_DESCONECTAR = 0.2;
    private static final double INICIO_GIRO = 0.2;
    private static final double FIM_GIRO = 0.8;
    private static final double INICIO_RECONECTAR = 0.8;

    private final List<No> nos;
    private final double[] ang0;
    private final double[] raio0;
    private final double[] sweep;
    private final double[] raio1;
    private double cx;
    private double cy;
    private double t;
    private double opacidadeLigacao = 1;
    private boolean iniciado;

    Giro( List<No> nos ) {
        this.nos = nos;
        int n = nos.size();
        ang0 = new double[n];
        raio0 = new double[n];
        sweep = new double[n];
        raio1 = new double[n];
        for ( No no : nos ) {
            no.travado = true;
        }
    }

    /** True enquanto a fase de desconexão está no começo (para tocar o unlink uma vez). */
    boolean estaDesconectando() {
        return t < FIM_DESCONECTAR;
    }

    /** True quando a fase de giro (nós se movendo) está em andamento. */
    boolean estaGirando() {
        return t >= INICIO_GIRO && t < FIM_GIRO;
    }

    /**
     * True assim que a reconexão aparece na tela (opacidade acima de 2%).
     * Usa a opacidade real, então o som sai junto com a primeira linha visível.
     */
    boolean estaReconectando() {
        return t > INICIO_RECONECTAR && opacidadeLigacao > 0.02;
    }

    /** Avança a animação. Retorna true quando terminou. */
    boolean atualizar( double delta, double vel ) {
        if ( !iniciado ) {
            iniciar();
        }
        t = Math.min( 1, t + delta * vel / DURACAO );

        double f;
        if ( t < FIM_DESCONECTAR ) {
            f = 1 - easeInOut( t / FIM_DESCONECTAR );
        } else if ( t > INICIO_RECONECTAR ) {
            f = easeInOut( ( t - INICIO_RECONECTAR ) / ( 1 - INICIO_RECONECTAR ) );
        } else {
            f = 0;
        }
        opacidadeLigacao = f;

        double p = easeInOut( clamp01( ( t - INICIO_GIRO ) / ( FIM_GIRO - INICIO_GIRO ) ) );

        for ( int i = 0; i < nos.size(); i++ ) {
            No no = nos.get( i );
            no.linkVisivel = f;
            double ang = ang0[i] + sweep[i] * p;
            double r = raio0[i] + ( raio1[i] - raio0[i] ) * p;
            no.pos.x = cx + Math.cos( ang ) * r;
            no.pos.y = cy + Math.sin( ang ) * r;
        }

        if ( t >= 1 ) {
            for ( No no : nos ) {
                no.pos.x = no.alvo.x;
                no.pos.y = no.alvo.y;
                no.linkVisivel = 1;
                no.travado = false;
            }
            return true;
        }
        return false;
    }

    /** Guarda o ângulo e o raio de partida, e o giro que leva cada nó ao destino atual. */
    private void iniciar() {
        iniciado = true;
        int n = nos.size();

        double sx = 0;
        double sy = 0;
        for ( No no : nos ) {
            sx += no.pos.x;
            sy += no.pos.y;
        }
        cx = sx / n;
        cy = sy / n;

        for ( int i = 0; i < n; i++ ) {
            No no = nos.get( i );
            double dx = no.pos.x - cx;
            double dy = no.pos.y - cy;
            ang0[i] = Math.atan2( dy, dx );
            raio0[i] = Math.hypot( dx, dy );

            double ex = no.alvo.x - cx;
            double ey = no.alvo.y - cy;
            double d = Math.atan2( ey, ex ) - ang0[i];
            while ( d > Math.PI ) {
                d -= 2 * Math.PI;
            }
            while ( d < -Math.PI ) {
                d += 2 * Math.PI;
            }
            sweep[i] = d;
            raio1[i] = Math.hypot( ex, ey );
        }
    }

    private static double clamp01( double v ) {
        return Math.max( 0, Math.min( 1, v ) );
    }

    private static double easeInOut( double t ) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow( -2 * t + 2, 2 ) / 2;
    }
}