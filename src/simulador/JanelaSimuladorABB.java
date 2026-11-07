package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;

/**
 * Janela Independente do Simulador de ABB com componentes desenhados à mão.
 *
 * @author Vinícius Lima Sobon
 */
public class JanelaSimuladorABB extends EngineFrame {

    private ABB abb;
    private No noSobMouse;

    private double espHor = 45;
    private double espVert = 55;
    private int margemHor = 50;
    private int margemVert = 120;

    private CampoTexto campoValor;
    private BotaoEstilizado btnInserir;
    private BotaoEstilizado btnVoltar;

    public JanelaSimuladorABB() {
        super( 900, 600, "Simulador: Árvore Binária de Busca (ABB)", 60, true );
    }

    @Override
    public void create() {
        abb = new ABB();

        campoValor = new CampoTexto( 90, 20, 110, 36 );
        btnInserir = new BotaoEstilizado( 225, 20, 110, 36, "Inserir",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnVoltar = new BotaoEstilizado( 780, 20, 100, 36, "Voltar",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
    }

    @Override
    public void update( double delta ) {

        campoValor.atualizar( this, delta );

        if ( btnInserir.atualizar( this ) || campoValor.enterFoiPressionado() ) {
            executarInsercao();
        }
        

        if ( btnVoltar.atualizar( this ) ) {
            voltarAoMenu();
            return;
        }

        Vector2 mouse = getMousePositionPoint();
        noSobMouse = mouse != null ? abb.buscarPorClique( mouse ) : null;

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && noSobMouse != null ) {
            abb.delete( noSobMouse.c );
            abb.atualizarNos( espHor, espVert, margemHor, margemVert );
            noSobMouse = null;
        }
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        fillRectangle( 10, 10, 880, 55, Tema.PAINEL );
        drawRectangle( 10, 10, 880, 55, Tema.BORDA );
        drawText( "Chave:", 20, 31, 18, Tema.TEXTO );

        campoValor.desenhar( this );
        btnInserir.desenhar( this );
        btnVoltar.desenhar( this );

        abb.desenhar( this, noSobMouse );
    }

    private void voltarAoMenu() {
        if ( Main.instancia != null ) {
            Main.instancia.mostrar();
        }
        dispose();
    }

    private void executarInsercao() {
        try {
            String texto = campoValor.getValor().trim();
            if ( !texto.isEmpty() ) {
                int valor = Integer.parseInt( texto );
                abb.put( valor );
                abb.atualizarNos( espHor, espVert, margemHor, margemVert );
                campoValor.limpar();
            }
        } catch ( NumberFormatException e ) {
        }
    }

    private void executarRemocao() {
        try {
            String texto = campoValor.getValor().trim();
            if ( !texto.isEmpty() ) {
                int valor = Integer.parseInt( texto );
                abb.delete( valor );
                abb.atualizarNos( espHor, espVert, margemHor, margemVert );
                campoValor.limpar();
            }
        } catch ( NumberFormatException e ) {
        }
    }
}