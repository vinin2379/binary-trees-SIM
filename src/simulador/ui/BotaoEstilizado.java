package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import simulador.Tema;

/**
 * Botão retangular com cantos arredondados, desenhado com primitivas da JSGE.
 */
public class BotaoEstilizado {

    private static final double RAIO_CANTO = 8;
    private static final int TAM_TEXTO = 16;

    private final double x;
    private final double y;
    private final double largura;
    private final double altura;
    private final String texto;
    private final Color corFundo;
    private final Color corHover;
    private final Color corTexto;

    private boolean hover;

    public BotaoEstilizado( double x, double y, double largura, double altura, String texto,
            Color corFundo, Color corHover, Color corTexto ) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.texto = texto;
        this.corFundo = corFundo;
        this.corHover = corHover;
        this.corTexto = corTexto;
    }

    /**
     * Atualiza o hover e informa se o botão foi clicado neste frame.
     */
    public boolean atualizar( EngineFrame e ) {
        Vector2 m = e.getMousePositionPoint();
        hover = m != null && m.x >= x && m.x <= x + largura && m.y >= y && m.y <= y + altura;
        return hover && e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT );
    }

    public void desenhar( EngineFrame e ) {
        Color cor = hover ? corHover : corFundo;

        // borda sutil por trás do preenchimento
        retanguloArredondado( e, x - 1, y - 1, largura + 2, altura + 2, RAIO_CANTO + 1, Tema.BORDA );
        retanguloArredondado( e, x, y, largura, altura, RAIO_CANTO, cor );

        int larg = e.measureText( texto, TAM_TEXTO );
        e.drawText( texto,
                x + largura / 2.0 - larg / 2.0,
                y + altura / 2.0 - TAM_TEXTO / 3.0,
                TAM_TEXTO,
                corTexto );
    }

    static void retanguloArredondado( EngineFrame e, double x, double y,
            double w, double h, double r, Color cor ) {
        double d = 2 * r;
        e.fillRectangle( x + r, y, w - d, h, cor );
        e.fillRectangle( x, y + r, w, h - d, cor );
        e.fillCircle( new Vector2( x + r, y + r ), r, cor );
        e.fillCircle( new Vector2( x + w - r, y + r ), r, cor );
        e.fillCircle( new Vector2( x + r, y + h - r ), r, cor );
        e.fillCircle( new Vector2( x + w - r, y + h - r ), r, cor );
    }
}