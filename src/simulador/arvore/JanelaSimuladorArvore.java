package simulador.arvore;

import br.com.davidbuzatto.jsge.core.Camera2D;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
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

    private static final double ATRASO_INICIO = 0.3;
    private static final double DUR_TOTAL = 1.05;
    private static final double DUR_ITEM = 0.6;
    private static final double DUR_SAIDA = 0.7;
    private static final double DESLOC_TOOLBAR = 90;
    private static final double DESLOC_ARVORE = 500;
    private static final double INICIO_ARVORE = 0.2;
    private static final double DUR_ARVORE = 0.8;
    private static final double DUR_PISCA = 1.0;

    /** Posição da caixa de texto na tela (de onde o viajante parte). */
    private static final double CAMPO_X = 90;
    private static final double CAMPO_Y = 20;
    private static final double CAMPO_LARG = 110;
    private static final double CAMPO_ALT = 36;
    private static final int TAM_TEXTO_CAMPO = 16;

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

    private double tempoPronto;
    private boolean pronto;
    private double tempoAnim;
    private boolean entrando;
    private boolean saindo;
    private double tSaida;
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

        campoValor = new CampoTexto( CAMPO_X, CAMPO_Y, CAMPO_LARG, CAMPO_ALT );
        btnInserir = new BotaoEstilizado( 225, 20, 110, 36, "Inserir",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnLimpar = new BotaoEstilizado( 350, 20, 110, 36, "Limpar",
                Tema.TRIGGER, Tema.TRIGGER_HOVER, Tema.TEXTO_CLARO );
        btnVoltar = new BotaoEstilizado( 780, 20, 100, 36, "Voltar",
                Tema.ITEM, Tema.ITEM_HOVER, Tema.TEXTO );
        modo = new ModoDidatico( 490, 20 );

        tempoAnim = 0;
        tSaida = 0;
        entrando = true;
        saindo = false;
        pronto = false;
        tempoPronto = 0;
    }

    @Override
    public void update( double deltaBruto ) {
        double delta = Math.min( deltaBruto, 1.0 / 30 );

        // espera a janela estabilizar antes de qualquer animação ou som
        if ( !pronto ) {
            tempoPronto += delta;
            if ( tempoPronto < ATRASO_INICIO ) {
                return;
            }
            pronto = true;
            Sons.elementosSimulador();
        }

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
        }
        if ( saindo ) {
            tSaida = Math.min( DUR_SAIDA, tSaida + delta );
            if ( tSaida >= DUR_SAIDA ) {
                voltarAoMenu();
                saindo = false;
                return;
            }
        }

        controlador.camera().offset.y = saindo
                ? easeIn( tSaida / DUR_SAIDA ) * DESLOC_ARVORE
                : desl( INICIO_ARVORE, DUR_ARVORE, DESLOC_ARVORE );

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
            Sons.guardarElementos();
            saindo = true;
            tSaida = 0;
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

        if ( !pronto ) {
            return;
        }

        beginMode2D( controlador.camera() );
        arvore.desenharFundo( this );
        arvore.desenhar( this, noSobMouse );
        if ( modo.ativo() && noSobMouse != null && fase == Fase.NENHUMA ) {
            desenharBalanco( noSobMouse );
        }
        endMode2D();

        String leg = arvore.legenda();
        if ( leg != null && fase != Fase.NENHUMA ) {
            desenharLegenda( leg );
        }

        double dyToolbar = saindo
                ? -easeIn( tSaida / DUR_SAIDA ) * DESLOC_TOOLBAR
                : -desl( 0, DUR_ITEM, DESLOC_TOOLBAR );
        comDeslocamento( dyToolbar, () -> {
            fillRectangle( 10, 10, 880, 55, Tema.PAINEL );
            drawRectangle( 10, 10, 880, 55, Tema.BORDA );
            drawText( "Chave:", 20, 31, 18, Tema.TEXTO );
            campoValor.desenhar( this );
            btnInserir.desenhar( this );
            btnLimpar.desenhar( this );
            btnVoltar.desenhar( this );
            modo.desenhar( this );
        } );

        // viajantes em coordenadas de tela, por cima da barra
        for ( Viajante v : viajantes ) {
            v.desenhar( this, mundoParaTela( v.getPos() ) );
        }

        desenharExtras();
    }

    /**
     * Cartão da legenda no canto inferior direito. A primeira linha é o título
     * do caso; as demais são os detalhes (separados por quebra de linha).
     */
    private void desenharLegenda( String texto ) {
        String[] linhas = texto.split( "\n" );
        int tamTitulo = 14;
        int tamDetalhe = 12;

        int largMax = 0;
        for ( int i = 0; i < linhas.length; i++ ) {
            int tam = i == 0 ? tamTitulo : tamDetalhe;
            largMax = Math.max( largMax, measureText( linhas[i], tam ) );
        }

        double w = largMax + 32;
        double h = 16 + linhas.length * 18 + 8;
        double x = getScreenWidth() - w - 14;
        double y = getScreenHeight() - h - 14;

        BotaoEstilizado.retanguloArredondado( this, x - 1, y - 1, w + 2, h + 2, 11, Tema.BORDA );
        BotaoEstilizado.retanguloArredondado( this, x, y, w, h, 10, Tema.PAINEL );
        fillRectangle( x + 10, y + 10, 3, h - 20, Tema.TRIGGER );

        for ( int i = 0; i < linhas.length; i++ ) {
            boolean titulo = i == 0;
            int tam = titulo ? tamTitulo : tamDetalhe;
            Color cor = titulo ? Tema.TRIGGER : Tema.TEXTO;
            drawText( linhas[i], x + 22, y + 12 + i * 18, tam, cor );
        }
    }

    /** Converte uma posição do mundo para a tela, com a câmera atual. */
    private Vector2 mundoParaTela( Vector2 w ) {
        Camera2D cam = controlador.camera();
        return new Vector2(
                ( w.x - cam.target.x ) * cam.zoom + cam.offset.x,
                ( w.y - cam.target.y ) * cam.zoom + cam.offset.y
        );
    }

    /** Hover didático: subárvore direita em verde, esquerda em vermelho, e a conta acima do nó. */
    private void desenharBalanco( No no ) {
        String rotulo = arvore.textoHover( no );
        if ( rotulo != null ) {
            int tam = 14;
            int larg = measureText( rotulo, tam );
            drawText( rotulo, no.pos.x - larg / 2.0, no.pos.y - no.raio - 14, tam, Tema.TEXTO );
            return;
        }

        int esq = arvore.alturaSubarvore( no.esq );
        int dir = arvore.alturaSubarvore( no.d );
        pintarSubarvore( no.d, Tema.VERDE );
        pintarSubarvore( no.esq, Tema.VERMELHO );

        String conta = esq + " - " + dir + " = " + ( esq - dir );
        int tam = 14;
        int larg = measureText( conta, tam );
        drawText( conta, no.pos.x - larg / 2.0, no.pos.y - no.raio - 14, tam, Tema.TEXTO );
    }

    private void pintarSubarvore( No n, Color cor ) {
        if ( n == null ) {
            return;
        }
        fillCircle( n.pos, n.raio, ColorUtils.fade( cor, 0.45 ) );
        pintarSubarvore( n.esq, cor );
        pintarSubarvore( n.d, cor );
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

    private static double easeIn( double t ) {
        t = Math.max( 0, Math.min( 1, t ) );
        return t * t * t;
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
        Sons.noAparece();
        arvore.liberarGiros();
    }

    /**
     * Fase de viagem: BST pura. O círculo nasce no lugar onde o número estava
     * na caixa de texto e segue até o nó.
     */
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

        // centro do número dentro da caixa, convertido para o mundo
        double larg = measureText( String.valueOf( valor ), TAM_TEXTO_CAMPO );
        Vector2 inicioTela = new Vector2(
                CAMPO_X + 10 + larg / 2.0,
                CAMPO_Y + CAMPO_ALT / 2.0 );
        Vector2 inicio = controlador.telaParaMundo( inicioTela );
        viajantes.add( new Viajante( valor, caminho, novo, inicio ) );
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