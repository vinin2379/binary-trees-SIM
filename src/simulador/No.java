package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

/**
 * Classe Nó.
 */
public class No {

    int c;
    No esq;
    No d;
    No pai;

    Vector2 pos = new Vector2();
    Vector2 alvo = new Vector2();
    int nivel;
    int ranque;
    int raio;

    // estado da animação
    boolean aguardando;        // true enquanto o viajante do modo didático não chegou
    double escala = 1;         // cresce de 0 até 1
    double progressoLinha = 1; // linha até o pai, de 0 até 1
    double pulso;              // > 0 enquanto pisca ao conectar
    Color corPulso = Tema.VERDE;

    public No( int raio ) {
        this.raio = raio;
    }

    void animar( double delta, double velocidade ) {
        double k = Math.min( 1, delta * 8 * velocidade );
        pos.x += ( alvo.x - pos.x ) * k;
        pos.y += ( alvo.y - pos.y ) * k;

        if ( !aguardando && escala < 1 ) {
            escala = Math.min( 1, escala + delta * 4 * velocidade );
            if ( escala >= 1 && pai == null ) {
                dispararPulso();
            }
        } else if ( escala >= 1 && progressoLinha < 1 ) {
            progressoLinha = Math.min( 1, progressoLinha + delta * 3 * velocidade );
            if ( progressoLinha >= 1 ) {
                dispararPulso();
            }
        }

        if ( pulso > 0 ) {
            pulso = Math.max( 0, pulso - delta * velocidade );
        }
    }

    private void dispararPulso() {
        pulso = 1;
        corPulso = Tema.VERDE;
    }

    public void desenhar( EngineFrame e, boolean hover ) {
        if ( escala <= 0.01 ) {
            return;
        }

        double r = ( hover ? raio * 1.1 : raio ) * escala;
        Color fundo = hover ? Tema.ITEM_HOVER : Tema.ITEM;
        if ( pulso > 0 ) {
            fundo = Tema.mistura( fundo, corPulso, Math.abs( Math.sin( pulso * 12 ) ) );
        }

        e.fillCircle( pos, r, fundo );
        e.drawCircle( pos, r, EngineFrame.BLACK );

        if ( escala > 0.6 ) {
            String label = String.valueOf( c );
            int tam = 18;
            int larg = e.measureText( label, tam );
            e.drawText( label, pos.x - ( larg / 2.0 ) + 1, pos.y - 4, tam, Tema.TEXTO );
        }

        if ( hover && escala > 0.9 ) {
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