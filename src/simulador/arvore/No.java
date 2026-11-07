package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import simulador.ui.Tema;

/**
 * Nó genérico para qualquer árvore binária.
 */
public class No {

    public int c;
    public No esq;
    public No d;
    public No pai;
    public No paiAntigo;

    public Vector2 pos = new Vector2();
    public Vector2 alvo = new Vector2();
    public int nivel;
    public int ranque;
    public int raio;

    /** Usado pela AVL. */
    public int altura = 1;
    /** Usado pela Rubro-Negra. */
    public boolean vermelho;
    /** Intensidade de vermelho animada (0 a 1), para transições suaves de cor. */
    public double intensidadeVermelho;

    public boolean aguardando;
    public boolean removendo;
    public double escala = 1;
    public double progressoLinha = 1;
    public double progressoAntigo;
    public double pulsoLinha;
    /** Realce de 0 a 1, aceso quando o nó participa de uma rotação. */
    public double destaque;

    /** Quando true, o nó não se move sozinho (está sendo girado). */
    public boolean travado;
    /** Multiplica a visibilidade das ligações deste nó (0 = desconectado). */
    public double linkVisivel = 1;
    /** Quando true, a ligação com o pai não cresce. */
    public boolean ligacaoBloqueada;
    /** Quando true, o nó pisca (desbalanceamento acima de N, ou violação na RB). */
    public boolean piscando;

    /** Cor usada enquanto o nó pisca. Cada árvore escolhe a sua. */
    public Color corPisca = Tema.VERMELHO;

    private boolean emArco;
    private double tArco;
    private double tPisca;
    private final Vector2 arcoInicio = new Vector2();
    private final Vector2 arcoControle = new Vector2();

    public No( int raio ) {
        this.raio = raio;
    }

    /** Troca o pai. Retorna true se o pai mudou. */
    public boolean trocarPai( No novo ) {
        if ( novo == pai ) {
            return false;
        }
        if ( pai != null && progressoLinha > 0 ) {
            paiAntigo = pai;
            progressoAntigo = progressoLinha;
        }
        pai = novo;
        progressoLinha = novo == null ? 1 : 0;
        return true;
    }

    /** Define o destino do layout. Com emArco, percorre uma curva em vez de deslizar. */
    public void definirAlvo( double x, double y, boolean emArco ) {
        if ( alvo.x == x && alvo.y == y ) {
            return;
        }
        alvo.x = x;
        alvo.y = y;

        double dist = Math.hypot( x - pos.x, y - pos.y );
        if ( emArco && dist > 2 && !removendo ) {
            this.emArco = true;
            tArco = 0;
            arcoInicio.x = pos.x;
            arcoInicio.y = pos.y;
            arcoControle.x = ( pos.x + x ) / 2;
            arcoControle.y = ( pos.y + y ) / 2 - Math.min( 90, dist * 0.6 );
        } else {
            this.emArco = false;
        }
    }

    public void animar( double delta, double vel ) {
        intensidadeVermelho = suavizar( intensidadeVermelho, vermelho ? 0.5 : 0, 8 * vel, delta );

        if ( piscando ) {
            tPisca += delta * vel;
        }

        if ( removendo ) {
            escala = Math.max( 0, escala - delta * 4 * vel );
            progressoLinha = Math.max( 0, progressoLinha - delta * 3 * vel );
        } else {
            if ( !travado ) {
                if ( emArco ) {
                    tArco = Math.min( 1, tArco + delta * 1.6 * vel );
                    double e = easeInOutCubic( tArco );
                    pos.x = quadratica( arcoInicio.x, arcoControle.x, alvo.x, e );
                    pos.y = quadratica( arcoInicio.y, arcoControle.y, alvo.y, e );
                    if ( tArco >= 1 ) {
                        emArco = false;
                        pos.x = alvo.x;
                        pos.y = alvo.y;
                    }
                } else {
                    double k = Math.min( 1, delta * 8 * vel );
                    pos.x += ( alvo.x - pos.x ) * k;
                    pos.y += ( alvo.y - pos.y ) * k;
                }
            }

            if ( !aguardando && escala < 1 ) {
                escala = Math.min( 1, escala + delta * 4 * vel );
            }
            if ( escala >= 1 && pai != null && progressoLinha < 1 && !ligacaoBloqueada ) {
                progressoLinha = Math.min( 1, progressoLinha + delta * 3 * vel );
                if ( progressoLinha >= 1 ) {
                    pulsoLinha = 1;
                }
            }
        }

        if ( paiAntigo != null ) {
            progressoAntigo = Math.max( 0, progressoAntigo - delta * 3 * vel );
            if ( progressoAntigo <= 0 ) {
                paiAntigo = null;
            }
        }

        if ( pulsoLinha > 0 ) {
            pulsoLinha = Math.max( 0, pulsoLinha - delta * 0.8 * vel );
        }

        if ( destaque > 0 ) {
            destaque = Math.max( 0, destaque - delta * 1.2 * vel );
        }
    }

    public void desenhar( EngineFrame e, boolean hover ) {
        if ( escala <= 0.01 ) {
            return;
        }

        double r = ( hover ? raio * 1.1 : raio ) * escala;

        if ( destaque > 0.01 ) {
            double anel = r + 4 + 10 * ( 1 - destaque );
            e.drawCircle( pos, anel, ColorUtils.fade( Tema.TRIGGER, destaque * 0.7 ) );
        }

        Color base = hover ? Tema.ITEM_HOVER : Tema.ITEM;
        
        if ( piscando ) {
            base = Tema.mistura( base, corPisca, 0.5 + 0.5 * Math.sin( tPisca * 12 ) );
        }
        e.fillCircle( pos, r, base );
        e.drawCircle( pos, r, EngineFrame.BLACK );

        if ( escala > 0.6 ) {
            String label = String.valueOf( c );
            int tam = 18;
            int larg = e.measureText( label, tam );
            e.drawText( label, pos.x - ( larg / 2.0 ) + 1, pos.y - 4, tam, Tema.TEXTO );
        }

        if ( hover && escala > 0.9 && !removendo ) {
            String dica = "remover";
            int tamDica = 14;
            int largDica = e.measureText( dica, tamDica );
            e.drawText( dica, pos.x - largDica / 2.0, pos.y + r + 6, tamDica, Tema.TEXTO );
        }
    }

    public boolean contemPonto( Vector2 ponto ) {
        double dx = ponto.x - pos.x;
        double dy = ponto.y - pos.y;
        return ( dx * dx + dy * dy ) <= ( raio * raio );
    }

    /** Aproxima atual de destino com taxa exponencial, independente do FPS. */
    private static double suavizar( double atual, double destino, double taxa, double dt ) {
        return atual + ( destino - atual ) * ( 1 - Math.exp( -taxa * Math.min( dt, 1.0 / 30 ) ) );
    }

    private static double quadratica( double a, double c, double b, double t ) {
        double u = 1 - t;
        return u * u * a + 2 * u * t * c + t * t * b;
    }

    private static double easeInOutCubic( double t ) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow( -2 * t + 2, 3 ) / 2;
    }

    @Override
    public String toString() {
        return String.format( "c: %d, n: %d, r: %d", c, nivel, ranque );
    }
}