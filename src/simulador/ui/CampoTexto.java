package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import simulador.Tema;

/**
 * Campo de texto numérico desenhado com primitivas da JSGE.
 */
public class CampoTexto {

    private static final double RAIO_CANTO = 8;
    private static final int TAM_TEXTO = 16;
    private static final int MAX_CARACTERES = 6;
    private static final double PERIODO_CURSOR = 0.5;

    private final double x;
    private final double y;
    private final double largura;
    private final double altura;
    private final StringBuilder valor = new StringBuilder();

    private boolean focado;
    private double tempo;

    public CampoTexto( double x, double y, double largura, double altura ) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    private static final double ATRASO_REPETICAO = 0.4;
private static final double INTERVALO_REPETICAO = 0.05;

private double tempoBackspace;
private boolean backspaceAntes;
private boolean enterPressionado;

public void atualizar( EngineFrame e, double delta ) {
    tempo += delta;

    Vector2 m = e.getMousePositionPoint();
    boolean dentro = m != null && m.x >= x && m.x <= x + largura && m.y >= y && m.y <= y + altura;
    if ( e.isMouseButtonPressed( EngineFrame.MOUSE_BUTTON_LEFT ) ) {
        focado = dentro;
    }

    if ( !focado ) {
        return;
    }
    
    if ( e.isKeyPressed( EngineFrame.KEY_ENTER ) ) {
        enterPressionado = true;
    }

    int c;
    while ( ( c = e.getCharPressed() ) != 0 ) {
        if ( c == 8 ) { // backspace chega como caractere
            apagarUltimo();
            continue;
        }
        boolean digito = c >= '0' && c <= '9';
        boolean sinal = c == '-' && valor.length() == 0;
        if ( ( digito || sinal ) && valor.length() < MAX_CARACTERES ) {
            valor.append( (char) c );
        }
    }

    // backspace como tecla: dispara ao pressionar e repete enquanto segura
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

    private void apagarUltimo() {
        if ( valor.length() > 0 ) {
            valor.deleteCharAt( valor.length() - 1 );
        }
    }

    public void desenhar( EngineFrame e ) {
        BotaoEstilizado.retanguloArredondado( e, x, y, largura, altura, RAIO_CANTO, Tema.BORDA );
        BotaoEstilizado.retanguloArredondado( e, x + 1, y + 1, largura - 2, altura - 2, RAIO_CANTO - 1,
                Tema.PAINEL );

        if ( focado ) {
            BotaoEstilizado.retanguloArredondado( e, x, y, largura, altura, RAIO_CANTO, Tema.TRIGGER );
            BotaoEstilizado.retanguloArredondado( e, x + 2, y + 2, largura - 4, altura - 4, RAIO_CANTO - 2,
                    Tema.PAINEL );
        }

        String texto = valor.toString();
        int larg = e.measureText( texto, TAM_TEXTO );
        double baseY = y + altura / 2.0 - TAM_TEXTO / 3.0;
        e.drawText( texto, x + 10, baseY, TAM_TEXTO, Tema.TEXTO );

        if ( focado && ( tempo % ( 2 * PERIODO_CURSOR ) ) < PERIODO_CURSOR ) {
            double cursorX = x + 10 + larg + 2;
            e.drawLine( cursorX, y + 8, cursorX, y + altura - 8, Tema.TEXTO );
        }
    }

    public String getValor() {
        return valor.toString();
    }

    public void limpar() {
        valor.setLength( 0 );
    }
    
    public boolean enterFoiPressionado() {
        boolean r = enterPressionado;
        enterPressionado = false;
        return r;
    }
}
