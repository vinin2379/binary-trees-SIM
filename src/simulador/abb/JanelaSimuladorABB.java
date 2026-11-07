package simulador.abb;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import simulador.ControladorCamera;
import simulador.Main;
import simulador.ui.BotaoEstilizado;
import simulador.ui.CampoTexto;
import simulador.ui.ModoDidatico;
import simulador.ui.Tema;

/**
 * Janela independente do simulador de ABB. Entra com a barra de ferramentas
 * descendo do topo e a árvore subindo; ao voltar, faz o caminho inverso.
 *
 * @author Vinícius Lima Sobon
 */
public class JanelaSimuladorABB extends EngineFrame {

    private static final double ALTURA_TOOLBAR = 75;

    private static final double DUR_TOTAL = 1.05;
    private static final double DUR_ITEM = 0.6;
    private static final double DESLOC_TOOLBAR = 90;
    private static final double DESLOC_ARVORE = 500;
    private static final double INICIO_PAINEL = 0.0;
    private static final double INICIO_CAMPO = 0.1;
    private static final double INICIO_INSERIR = 0.18;
    private static final double INICIO_LIMPAR = 0.26;
    private static final double INICIO_VOLTAR = 0.34;
    private static final double INICIO_MODO = 0.42;
    private static final double INICIO_ARVORE = 0.2;
    private static final double DUR_ARVORE = 0.8;

    private ABB abb;
    private No noSobMouse;
    private List<Viajante> viajantes;
    private ControladorCamera controlador;
    private Camera2D camToolbar;

    private CampoTexto campoValor;
    private BotaoEstilizado btnInserir;
    private BotaoEstilizado btnLimpar;
    private BotaoEstilizado btnVoltar;
    private ModoDidatico modo;

    private double espHor;
    private double espVert;
    private int margemHor;
    private int margemVert;

    private double tempoAnim;
    private boolean entrando;
    private boolean saindo;
    private boolean telaCheia;

    public JanelaSimuladorABB() {
        super( 900, 600, "Simulador: Árvore Binária de Busca (ABB)", 60, true );
    }

    @Override
    public void create() {
        abb = new ABB();
        viajantes = new ArrayList<>();
        controlador = new ControladorCamera();

        camToolbar = new Camera2D();
        camToolbar.target.x = 0;
        camToolbar.target.y = 0;
        camToolbar.offset.x = 0;
        camToolbar.offset.y = 0;
        camToolbar.zoom = 1;

        espHor = 45;
        espVert = 55;
        margemHor = 50;
        margemVert = 120;

        setDefaultFontSize( 18 );

        campoValor = new CampoTexto( 90, 20, 110, 36 );
        btnInserir = new BotaoEstilizado( 225, 20, 110, 36, "Inserir",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnLimpar = new BotaoEstilizado( 350, 20, 110, 36, "Limpar",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnVoltar = new BotaoEstilizado( 780, 20, 100, 36, "Voltar",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
        modo = new ModoDidatico( 490, 20 );

        tempoAnim = 0;
        entrando = true;
        saindo = false;
    }

    @Override
    public void update( double delta ) {
        if ( isKeyPressed( KEY_F11 ) ) {
            alternarTelaCheia();
        }
        if ( isKeyPressed( KEY_ESCAPE ) && telaCheia ) {
            alternarTelaCheia();
        }

        if ( entrando ) {
            tempoAnim = Math.min( DUR_TOTAL, tempoAnim + delta );
            if ( tempoAnim >= DUR_TOTAL ) {
                entrando = false;
            }
        } else if ( saindo ) {
            tempoAnim = Math.max( 0, tempoAnim - delta );
            if ( tempoAnim <= 0 ) {
                voltarAoMenu();
                saindo = false;
                return;
            }
        }

        // a árvore sobe de baixo: o deslocamento é aplicado na câmera,
        // então desenho e clique continuam batendo
        controlador.camera().offset.y = desl( INICIO_ARVORE, DUR_ARVORE, DESLOC_ARVORE );

        double vel = modo.velocidadeAnimacao();
        abb.animar( delta, vel );

        Iterator<Viajante> it = viajantes.iterator();
        while ( it.hasNext() ) {
            Viajante v = it.next();
            if ( v.atualizar( delta, vel ) ) {
                v.destino.aguardando = false;
                it.remove();
            }
        }

        // durante as animações de entrada e saída, nada responde ao mouse
        if ( entrando || saindo ) {
            return;
        }

        modo.atualizar( this, delta );
        campoValor.atualizar( this, delta );

        Vector2 mouse = getMousePositionPoint();

        if ( btnInserir.atualizar( this ) || campoValor.enterFoiPressionado() ) {
            executarInsercao();
        }
        if ( btnLimpar.atualizar( this ) ) {
            limparTudo();
        }
        if ( btnVoltar.atualizar( this ) ) {
            saindo = true;
            return;
        }

        boolean sobToolbar = mouse != null
                && ( mouse.y < ALTURA_TOOLBAR || modo.contem( mouse ) );

        Vector2 mundo = ( mouse != null && !sobToolbar ) ? controlador.telaParaMundo( mouse ) : null;
        noSobMouse = mundo != null ? abb.buscarPorClique( mundo ) : null;

        controlador.atualizar( this, mouse, sobToolbar || noSobMouse != null );

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && noSobMouse != null ) {
            removerChave( noSobMouse.c );
            noSobMouse = null;
        }
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        beginMode2D( controlador.camera() );
        abb.desenhar( this, noSobMouse );
        for ( Viajante v : viajantes ) {
            v.desenhar( this );
        }
        endMode2D();

        // ordem de entrada: painel, campo, inserir, limpar, voltar, modo
        comDeslocamento( -desl( INICIO_PAINEL, DUR_ITEM, DESLOC_TOOLBAR ), () -> {
            fillRectangle( 10, 10, 880, 55, Tema.PAINEL );
            drawRectangle( 10, 10, 880, 55, Tema.BORDA );
            drawText( "Chave:", 20, 31, 18, Tema.TEXTO );
        } );
        comDeslocamento( -desl( INICIO_CAMPO, DUR_ITEM, DESLOC_TOOLBAR ), () -> campoValor.desenhar( this ) );
        comDeslocamento( -desl( INICIO_INSERIR, DUR_ITEM, DESLOC_TOOLBAR ), () -> btnInserir.desenhar( this ) );
        comDeslocamento( -desl( INICIO_LIMPAR, DUR_ITEM, DESLOC_TOOLBAR ), () -> btnLimpar.desenhar( this ) );
        comDeslocamento( -desl( INICIO_VOLTAR, DUR_ITEM, DESLOC_TOOLBAR ), () -> btnVoltar.desenhar( this ) );
        comDeslocamento( -desl( INICIO_MODO, DUR_ITEM, DESLOC_TOOLBAR ), () -> modo.desenhar( this ) );
    }

    /** Desenha um grupo da barra com um deslocamento vertical próprio. */
    private void comDeslocamento( double dy, Runnable desenho ) {
        camToolbar.offset.y = dy;
        beginMode2D( camToolbar );
        desenho.run();
        endMode2D();
    }

    /** Distância restante até a posição final, de dist até 0. */
    private double desl( double inicio, double dur, double dist ) {
        double q = Math.max( 0, Math.min( 1, ( tempoAnim - inicio ) / dur ) );
        double e = 1 - Math.pow( 1 - q, 3 );
        return ( 1 - e ) * dist;
    }

    private void alternarTelaCheia() {
        telaCheia = !telaCheia;
        GraphicsDevice dispositivo = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();
        dispositivo.setFullScreenWindow( telaCheia ? this : null );
    }

    private void voltarAoMenu() {
        if ( telaCheia ) {
            alternarTelaCheia();
        }
        if ( Main.instancia != null ) {
            Main.instancia.mostrar();
        }
        dispose();
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
}