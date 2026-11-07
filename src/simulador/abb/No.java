package simulador.abb;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.ui.Tema;

/**
 * Nó da árvore com o estado das animações.
 */
public class No {

    int c;
    No esq;
    No d;
    No pai;
    No paiAntigo;

    Vector2 pos = new Vector2();
    Vector2 alvo = new Vector2();
    int nivel;
    int ranque;
    int raio;

    boolean aguardando;
    boolean removendo;
    double escala = 1;
    double progressoLinha = 1;
    double progressoAntigo;
    double pulsoLinha;

    No( int raio ) {
        this.raio = raio;
    }

    /** Troca o pai: o link antigo encolhe e o novo cresce. */
    void trocarPai( No novo ) {
        if ( novo == pai ) {
            return;
        }
        if ( pai != null && progressoLinha > 0 ) {
            paiAntigo = pai;
            progressoAntigo = progressoLinha;
        }
        pai = novo;
        progressoLinha = novo == null ? 1 : 0;
    }

    void animar( double delta, double vel ) {
        if ( removendo ) {
            escala = Math.max( 0, escala - delta * 4 * vel );
            progressoLinha = Math.max( 0, progressoLinha - delta * 3 * vel );
        } else {
            double k = Math.min( 1, delta * 8 * vel );
            pos.x += ( alvo.x - pos.x ) * k;
            pos.y += ( alvo.y - pos.y ) * k;

            if ( !aguardando && escala < 1 ) {
                escala = Math.min( 1, escala + delta * 4 * vel );
            }
            if ( escala >= 1 && pai != null && progressoLinha < 1 ) {
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
    }

    void desenhar( EngineFrame e, boolean hover ) {
        if ( escala <= 0.01 ) {
            return;
        }

        double r = ( hover ? raio * 1.1 : raio ) * escala;
        e.fillCircle( pos, r, hover ? Tema.ITEM_HOVER : Tema.ITEM );
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

    boolean contemPonto( Vector2 ponto ) {
        double dx = ponto.x - pos.x;
        double dy = ponto.y - pos.y;
        return ( dx * dx + dy * dy ) <= ( raio * raio );
    }

    @Override
    public String toString() {
        return String.format( "c: %d, n: %d, r: %d", c, nivel, ranque );
    }
}