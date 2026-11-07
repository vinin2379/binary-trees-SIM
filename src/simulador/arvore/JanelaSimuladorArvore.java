package simulador.arvore;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import simulador.ControladorCamera;
import simulador.Main;
import simulador.ui.BotaoEstilizado;
import simulador.ui.CampoTexto;
import simulador.ui.ModoDidatico;
import simulador.ui.Sons;
import simulador.ui.Tema;

/**
 * Janela base dos simuladores de árvore. O modo didático é comum a todas:
 * viajar, crescer, piscar, girar e religar. Cada árvore define quais nós
 * giram (nosQueGiram) e a cor do piscar. Controles extras entram pelos hooks.
 *
 * @author Vinícius Lima Sobon
 */
public abstract class JanelaSimuladorArvore extends EngineFrame {

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
    private static final double DUR_PISCA = 1.0;

    private enum Fase { NENHUMA, VIAJANDO, PISCANDO, GIRANDO }

    protected ArvoreBase arvore;
    private No noSobMouse;
    private No noSobMouseAnt;
    private List<Viajante> viajantes;
    private Deque<Integer> operacoesPendentes;
    private List<No> piscando;
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

    private Fase fase;
    private No novoEmCurso;
    private double tempoPisca;
    private boolean verificacaoPendente;

    private double tempoAnim;
    private boolean entrando;
    private boolean saindo;
    private boolean telaCheia;

    protected JanelaSimuladorArvore( String titulo ) {
        super( 900, 600, titulo, 60, true );
    }

    /** Cria a árvore específica. Chamado em create(). */
    protected abstract ArvoreBase criarArvore();

    /** Controles extras da janela (ex.: slider de N na AVL). Chamado a cada frame. */
    protected void atualizarExtras( double delta ) {
    }

    /** Desenha os controles extras, em coordenadas de tela. */
    protected void desenharExtras() {
    }

    /** True se o mouse está sobre um controle extra (bloqueia clique na árvore). */
    protected boolean sobExtras( Vector2 mouse ) {
        return false;
    }

    /** Pede uma verificação de balanceamento (ex.: quando N muda). */
    protected void solicitarRebalanceamento() {
        verificacaoPendente = true;
    }

    /** Painel do modo didático, para as subclasses lerem opções extras. */
    protected ModoDidatico modoDidatico() {
        return modo;
    }

    @Override
    public void create() {
        Sons.iniciar( this );
        arvore = criarArvore();
        viajantes = new ArrayList<>();
        operacoesPendentes = new ArrayDeque<>();
        piscando = new ArrayList<>();
        controlador = new ControladorCamera();
        fase = Fase.NENHUMA;
        novoEmCurso = null;
        tempoPisca = 0;

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

        controlador.camera().offset.y = desl( INICIO_ARVORE, DUR_ARVORE, DESLOC_ARVORE );

        double vel = modo.velocidadeAnimacao();
        arvore.animar( delta, vel );

        Iterator<Viajante> it = viajantes.iterator();
        while ( it.hasNext() ) {
            Viajante v = it.next();
            if ( v.atualizar( delta, vel ) ) {
                v.destino.aguardando = false;
                it.remove();
            }
        }

        processarDidatico( delta, vel );

        if ( entrando || saindo ) {
            return;
        }

        modo.atualizar( this, delta );
        atualizarExtras( delta );
        campoValor.atualizar( this, delta );

        Vector2 mouse = getMousePositionPoint();
        boolean sobExtra = mouse != null && sobExtras( mouse );

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

        Vector2 mundo = ( mouse != null && !sobToolbar && !sobExtra )
                ? controlador.telaParaMundo( mouse ) : null;
        noSobMouse = mundo != null ? arvore.buscarPorClique( mundo ) : null;

        if ( noSobMouse != null && noSobMouse != noSobMouseAnt ) {
            Sons.hover();
        }
        noSobMouseAnt = noSobMouse;

        controlador.atualizar( this, mouse, sobToolbar || sobExtra || noSobMouse != null );

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) && noSobMouse != null ) {
            removerChave( noSobMouse.c );
            noSobMouse = null;
        }
    }

    /** Máquina de estados: inserções didáticas e verificações de balanceamento. */
    private void processarDidatico( double delta, double vel ) {
        switch ( fase ) {
            case NENHUMA:
                if ( !operacoesPendentes.isEmpty() && !arvore.temGirosPendentes() ) {
                    iniciarInsercaoDidatica( operacoesPendentes.poll() );
                } else if ( verificacaoPendente && !arvore.temGirosPendentes() ) {
                    verificacaoPendente = false;
                    iniciarVerificacao();
                }
                break;

            case VIAJANDO:
                if ( viajantes.isEmpty() && novoEmCurso.escala >= 0.99 ) {
                    List<No> alertados = arvore.nosAlertados();
                    if ( alertados.isEmpty() ) {
                        rebalancearAgora();
                    } else {
                        iniciarPisca( alertados );
                    }
                }
                break;

            case PISCANDO:
                tempoPisca += delta * vel;
                if ( tempoPisca >= DUR_PISCA ) {
                    for ( No n : piscando ) {
                        n.piscando = false;
                    }
                    piscando.clear();
                    arvore.rebalancear();
                    arvore.atualizarNos( espHor, espVert, margemHor, margemVert );
                    arvore.liberarGiros();
                    fase = Fase.GIRANDO;
                }
                break;

            case GIRANDO:
                if ( !arvore.temGirosPendentes() ) {
                    concluir();
                }
                break;
        }
    }

    /** Verifica se algum nó passou do limite. Se sim, pisca todos os que vão girar. */
    private void iniciarVerificacao() {
        List<No> giram = arvore.nosQueGiram();
        if ( giram.isEmpty() ) {
            return;
        }
        if ( modo.ativo() ) {
            iniciarPisca( giram );
        } else {
            rebalancearAgora();
        }
    }

    private void iniciarPisca( List<No> nos ) {
        piscando = new ArrayList<>( nos );
        for ( No n : piscando ) {
            n.piscando = true;
            n.corPisca = arvore.corAlerta();
        }
        tempoPisca = 0;
        fase = Fase.PISCANDO;
    }

    /** Rebalanceia na hora, sem piscar (modo didático desligado ou nada a piscar). */
    private void rebalancearAgora() {
        arvore.rebalancear();
        arvore.atualizarNos( espHor, espVert, margemHor, margemVert );
        arvore.liberarGiros();
        fase = Fase.GIRANDO;
    }

    private void concluir() {
        arvore.liberarLigacoes();
        novoEmCurso = null;
        fase = Fase.NENHUMA;
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        beginMode2D( controlador.camera() );
        arvore.desenharFundo( this );
        arvore.desenhar( this, noSobMouse );
        for ( Viajante v : viajantes ) {
            v.desenhar( this );
        }
        endMode2D();

        String leg = arvore.legenda();
        if ( leg != null && fase != Fase.NENHUMA ) {
            drawText( leg, 10, 575, 14, Tema.TEXTO );
        }

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

        desenharExtras();
    }

    private void comDeslocamento( double dy, Runnable desenho ) {
        camToolbar.offset.y = dy;
        beginMode2D( camToolbar );
        desenho.run();
        endMode2D();
    }

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

    /** Com modo didático (ou operação em andamento), a inserção entra na fila. */
    private void executarInsercao() {
        try {
            String texto = campoValor.getValor().trim();
            if ( texto.isEmpty() ) {
                return;
            }
            int valor = Integer.parseInt( texto );
            if ( modo.ativo() || fase != Fase.NENHUMA || !operacoesPendentes.isEmpty() ) {
                operacoesPendentes.add( valor );
            } else {
                inserirDireto( valor );
            }
            campoValor.limpar();
        } catch ( NumberFormatException e ) {
        }
    }

    /** Inserção sem animação didática: rebalanceia e gira na hora. */
    private void inserirDireto( int valor ) {
        No novo = arvore.inserir( valor );
        if ( novo == null ) {
            return;
        }
        arvore.atualizarNos( espHor, espVert, margemHor, margemVert );
        novo.pos = new Vector2( novo.alvo.x, novo.alvo.y );
        arvore.liberarGiros();
    }

    /** Fase de viagem: BST pura, o nó vai até a posição e cresce. */
    private void iniciarInsercaoDidatica( int valor ) {
        List<No> caminho = arvore.caminhoAte( valor );
        No novo = arvore.inserirSemBalancear( valor );
        if ( novo == null ) {
            return;
        }
        arvore.atualizarNos( espHor, espVert, margemHor, margemVert );
        novo.pos = new Vector2( novo.alvo.x, novo.alvo.y );
        novo.aguardando = true;
        novo.ligacaoBloqueada = true;
        novoEmCurso = novo;
        fase = Fase.VIAJANDO;
        viajantes.add( new Viajante( valor, caminho, novo ) );
    }

    private void removerChave( int c ) {
        if ( fase != Fase.NENHUMA ) {
            return;
        }
        Sons.desfazer();
        viajantes.removeIf( v -> v.destino.c == c );
        if ( arvore.remover( c ) ) {
            arvore.atualizarNos( espHor, espVert, margemHor, margemVert );
            arvore.liberarGiros();
        }
    }

    private void limparTudo() {
        arvore.limpar();
        viajantes.clear();
        operacoesPendentes.clear();
        piscando.clear();
        novoEmCurso = null;
        fase = Fase.NENHUMA;
        verificacaoPendente = false;
        noSobMouse = null;
        campoValor.limpar();
    }
}