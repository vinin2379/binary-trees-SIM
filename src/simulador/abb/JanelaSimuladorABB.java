package simulador.abb;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simulador.Main;
import simulador.Tema;
import simulador.ui.BotaoEstilizado;
import simulador.ui.CampoTexto;
import simulador.ui.ModoDidatico;

/**
 * Janela independente do simulador de ABB.
 *
 * @author Vinícius Lima Sobon
 */
public class JanelaSimuladorABB extends EngineFrame {

    private ABB abb;
    private No noSobMouse;
    private final List<Viajante> viajantes = new ArrayList<>();

    private double espHor = 45;
    private double espVert = 55;
    private int margemHor = 50;
    private int margemVert = 120;

    private CampoTexto campoValor;
    private BotaoEstilizado btnInserir;
    private BotaoEstilizado btnLimpar;
    private BotaoEstilizado btnVoltar;
    private ModoDidatico modo;

    public JanelaSimuladorABB() {
        super( 900, 600, "Simulador: Árvore Binária de Busca (ABB)", 60, true );
    }

    @Override
    public void create() {
        abb = new ABB();
        setDefaultFontSize( 18 );

        campoValor = new CampoTexto( 90, 20, 110, 36 );
        btnInserir = new BotaoEstilizado( 225, 20, 110, 36, "Inserir",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnLimpar = new BotaoEstilizado( 350, 20, 110, 36, "Limpar",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnVoltar = new BotaoEstilizado( 780, 20, 100, 36, "Voltar",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
        modo = new ModoDidatico( 490, 20 );
    }

    @Override
    public void update( double delta ) {
        Vector2 mouse = getMousePositionPoint();

        modo.atualizar( this, delta );
        double vel = modo.velocidadeAnimacao();

        campoValor.atualizar( this, delta );

        if ( btnInserir.atualizar( this ) || campoValor.enterFoiPressionado() ) {
            executarInsercao();
        }

        if ( btnLimpar.atualizar( this ) ) {
            limparTudo();
        }

        if ( btnVoltar.atualizar( this ) ) {
            voltarAoMenu();
            return;
        }

        noSobMouse = ( mouse != null && !modo.contem( mouse ) ) ? abb.buscarPorClique( mouse ) : null;

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && noSobMouse != null ) {
            removerChave( noSobMouse.c );
            noSobMouse = null;
        }

        Iterator<Viajante> it = viajantes.iterator();
        while ( it.hasNext() ) {
            Viajante v = it.next();
            if ( v.atualizar( delta, vel ) ) {
                v.destino.aguardando = false;
                it.remove();
            }
        }

        abb.animar( delta, vel );
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        fillRectangle( 10, 10, 880, 55, Tema.PAINEL );
        drawRectangle( 10, 10, 880, 55, Tema.BORDA );
        drawText( "Chave:", 20, 31, 18, Tema.TEXTO );

        campoValor.desenhar( this );
        btnInserir.desenhar( this );
        btnLimpar.desenhar( this );
        btnVoltar.desenhar( this );

        abb.desenhar( this, noSobMouse );

        for ( Viajante v : viajantes ) {
            v.desenhar( this );
        }

        modo.desenhar( this );
    }

    private void executarInsercao() {
        try {
            String texto = campoValor.getValor().trim();
            if ( texto.isEmpty() ) {
                return;
            }

            int valor = Integer.parseInt( texto );
            List<No> caminho = abb.caminhoAte( valor );
            No novo = abb.inserir( valor );

            if ( novo != null ) {
                abb.atualizarNos( espHor, espVert, margemHor, margemVert );
                novo.pos = new Vector2( novo.alvo.x, novo.alvo.y );

                if ( modo.ativo() ) {
                    novo.aguardando = true;
                    viajantes.add( new Viajante( valor, caminho, novo ) );
                }
            }

            campoValor.limpar();
        } catch ( NumberFormatException e ) {
        }
    }

    private void removerChave( int c ) {
        viajantes.removeIf( v -> v.destino.c == c );
        if ( abb.remover( c ) ) {
            abb.atualizarNos( espHor, espVert, margemHor, margemVert );
        }
    }

    private void limparTudo() {
        abb.limpar();
        viajantes.clear();
        noSobMouse = null;
        campoValor.limpar();
    }

    private void voltarAoMenu() {
        if ( Main.instancia != null ) {
            Main.instancia.mostrar();
        }
        dispose();
    }
}