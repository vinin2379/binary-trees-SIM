package simulador.arvore;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.ui.Tema;

/**
 * Nó genérico para qualquer árvore binária. Campos específicos
 * (altura da AVL, cor da Rubro-Negra) ficam aqui e são ignorados pelas outras árvores.
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

    public boolean aguardando;
    public boolean removendo;
    public double escala = 1;
    public double progressoLinha = 1;
    public double progressoAntigo;
    public double pulsoLinha;

    public No( int raio ) {
        this.raio = raio;
    }

    /** Troca o pai: o link antigo encolhe e o novo cresce. */
    public void trocarPai( No novo ) {
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

    public void animar( double delta, double vel ) {
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

    public void desenhar( EngineFrame e, boolean hover ) {
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

    public boolean contemPonto( Vector2 ponto ) {
        double dx = ponto.x - pos.x;
        double dy = ponto.y - pos.y;
        return ( dx * dx + dy * dy ) <= ( raio * raio );
    }

    @Override
    public String toString() {
        return String.format( "c: %d, n: %d, r: %d", c, nivel, ranque );
    }
}