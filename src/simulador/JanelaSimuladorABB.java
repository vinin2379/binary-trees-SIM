package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.imgui.GuiButton;
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
    private GuiButton btnInserir;
    private GuiButton btnRemover;

    public JanelaSimuladorABB() {
        super( 900, 600, "Simulador: Árvore Binária de Busca (ABB)", 60, true );
    }

    @Override
    public void create() {
        useAsDependencyForIMGUI();
        abb = new ABB();

        setDefaultFontSize( 18 );

        txtValor = new GuiTextField( 90, 20, 100, 35, "" );
        btnInserir = new GuiButton( 205, 20, 100, 35, "Inserir" );
        btnRemover = new GuiButton( 315, 20, 100, 35, "Remover" );
    }

    @Override
    public void update( double delta ) {

        txtValor.update( delta );
        btnInserir.update( delta );
        btnRemover.update( delta );

        if ( btnInserir.isMousePressed() ) {
            executarInsercao();
        }

        if ( btnRemover.isMousePressed() ) {
            executarRemocao();
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
        drawText( "Chave:", 20, 28, 18, Tema.TEXTO );

        txtValor.draw();
        btnInserir.draw();
        btnRemover.draw();

        abb.desenhar( this, noSobMouse );
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