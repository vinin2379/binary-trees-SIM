package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.imgui.GuiTextField;
import br.com.davidbuzatto.jsge.math.Vector2;

/**
 * Janela Independente do Simulador de ABB com componentes IMGUI nativos.
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

    private GuiTextField txtValor;
    private BotaoEstilizado btnInserir;
    private BotaoEstilizado btnRemover;
    private BotaoEstilizado btnVoltar;

    public JanelaSimuladorABB() {
        super( 900, 600, "Simulador: Árvore Binária de Busca (ABB)", 60, true );
    }

    @Override
    public void create() {
        useAsDependencyForIMGUI();
        abb = new ABB();

        setDefaultFontSize( 18 );

        txtValor = new GuiTextField( 90, 20, 100, 35, "" );
        btnInserir = new BotaoEstilizado( 215, 20, 110, 36, "Inserir",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnRemover = new BotaoEstilizado( 340, 20, 110, 36, "Remover",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnVoltar = new BotaoEstilizado( 780, 20, 100, 36, "Voltar",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
    }

    @Override
    public void update( double delta ) {

        txtValor.update( delta );

        if ( btnInserir.atualizar( this ) ) {
            executarInsercao();
        }

        if ( btnRemover.atualizar( this ) ) {
            executarRemocao();
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

        txtValor.draw();
        btnInserir.desenhar( this );
        btnRemover.desenhar( this );
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
            String texto = txtValor.getValue().trim();
            if ( !texto.isEmpty() ) {
                int valor = Integer.parseInt( texto );
                abb.put( valor );
                abb.atualizarNos( espHor, espVert, margemHor, margemVert );
                txtValor.setValue( "" );
            }
        } catch ( NumberFormatException e ) {
        }
    }

    private void executarRemocao() {
        try {
            String texto = txtValor.getValue().trim();
            if ( !texto.isEmpty() ) {
                int valor = Integer.parseInt( texto );
                abb.delete( valor );
                abb.atualizarNos( espHor, espVert, margemHor, margemVert );
                txtValor.setValue( "" );
            }
        } catch ( NumberFormatException e ) {
        }
    }
}