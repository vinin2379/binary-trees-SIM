package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

/**
 * Campo de texto numérico desenhado com primitivas da JSGE.
 */
public class CampoTexto {

    private static final double RAIO_CANTO = 8;
    private static final int TAM_TEXTO = 16;
    private static final int MAX_CARACTERES = 6;
    private static final double PERIODO_CURSOR = 0.5;
    private static final double ATRASO_REPETICAO = 0.4;
    private static final double INTERVALO_REPETICAO = 0.05;

    private final double x;
    private final double y;
    private final double largura;
    private final double altura;
    private final StringBuilder valor = new StringBuilder();

    private boolean focado;
    private boolean hover;
    private boolean hoverAnt;
    private double tempo;
    private double tempoBackspace;
    private boolean backspaceAntes;
    private boolean enterPressionado;

    public CampoTexto( double x, double y, double largura, double altura ) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public void atualizar( EngineFrame e, double delta ) {
        tempo += delta;

        Vector2 m = e.getMousePositionPoint();
        hover = m != null && m.x >= x && m.x <= x + largura && m.y >= y && m.y <= y + altura;
        if ( hover && !hoverAnt ) {
            Sons.hover();
        }
        hoverAnt = hover;

        if ( e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
            focado = hover;
        }

        if ( e.isKeyPressed( EngineFrame.KEY_ENTER ) && focado ) {
            enterPressionado = true;
        }

        if ( !focado ) {
            return;
        }

        int c;
        while ( ( c = e.getCharPressed() ) != 0 ) {
            if ( c == 8 ) {
                apagarUltimo();
                continue;
            }
            boolean digito = c >= '0' && c <= '9';
            boolean sinal = c == '-' && valor.length() == 0;
            if ( ( digito || sinal ) && valor.length() < MAX_CARACTERES ) {
                valor.append( (char) c );
            }
        }

        boolean backspace = e.isKeyPressed( EngineFrame.KEY_BACKSPACE );
        if ( backspace ) {
            if ( !backspaceAntes ) {
                apagarUltimo();
                tempoBackspace = 0;
            } else {
                tempoBackspace += delta;
                if ( tempoBackspace >= ATRASO_REPETICAO ) {
                    apagarUltimo();
                    tempoBackspace -= INTERVALO_REPETICAO;
                }
            }
        } else {
            tempoBackspace = 0;
        }
        backspaceAntes = backspace;
    }

    public void desenhar( EngineFrame e ) {
        Color borda = focado ? Tema.TRIGGER : ( hover ? Tema.LINHA : Tema.BORDA );

        BotaoEstilizado.retanguloArredondado( e, x, y, largura, altura, RAIO_CANTO, borda );
        BotaoEstilizado.retanguloArredondado( e, x + 1, y + 1, largura - 2, altura - 2, RAIO_CANTO - 1,
                Tema.PAINEL );

        String texto = valor.toString();
        int larg = e.measureText( texto, TAM_TEXTO );
        double baseY = y + altura / 2.0 - TAM_TEXTO / 3.0;
        e.drawText( texto, x + 10, baseY, TAM_TEXTO, Tema.TEXTO );

        if ( focado && ( tempo % ( 2 * PERIODO_CURSOR ) ) < PERIODO_CURSOR ) {
            double cursorX = x + 10 + larg + 2;
            e.drawLine( cursorX, y + 8, cursorX, y + altura - 8, Tema.TEXTO );
        }
    }

    public boolean enterFoiPressionado() {
        boolean r = enterPressionado;
        enterPressionado = false;
        return r;
    }

    public String getValor() {
        return valor.toString();
    }

    public void limpar() {
        valor.setLength( 0 );
    }

    private void apagarUltimo() {
        if ( valor.length() > 0 ) {
            valor.deleteCharAt( valor.length() - 1 );
        }
    }
}