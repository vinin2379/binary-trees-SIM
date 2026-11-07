package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;

/**
 * Classe Nó.
 *
 * @author Vinícius Lima Sobon
 */
public class No {

    int c;
    No esq;
    No d;

    Vector2 pos;
    int nivel;
    int ranque;
    int raio;

    public No( int raio ) {
        this.pos = new Vector2();
        this.raio = raio;
    }

    public void desenhar( EngineFrame e, boolean hover ) {

        double r = hover ? raio * 1.1 : raio;

        e.fillCircle( pos, r, hover ? Tema.ITEM_HOVER : Tema.ITEM );
        e.drawCircle( pos, r, EngineFrame.BLACK );

        String label = String.valueOf( c );
        int larg = e.measureText( label, 18 );
        e.drawText( label, pos.x - larg / 2.0, pos.y - 6, 18, Tema.TEXTO );

        if ( hover ) {
            String dica = "remover";
            int largDica = e.measureText( dica, 14 );
            e.drawText( dica, pos.x - largDica / 2.0, pos.y + r + 6, 14, Tema.TEXTO );
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