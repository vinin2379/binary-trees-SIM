package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.sound.Sound;

/**
 * Efeitos sonoros da interface. Carregue uma vez com Sons.iniciar(this)
 * dentro do create() da janela principal.
 */
public final class Sons {

    private static final String PASTA = "resources/sfx/";

    private static EngineFrame engine;

    private static Sound hover;
    private static Sound clique;
    private static Sound beep;
    private static Sound low;
    private static Sound back;
    private static Sound swooshItem;
    private static Sound bola;
    private static Sound letras;
    private static Sound menuFim;
    private static Sound coisas;
    private static Sound guardarDown;
    private static Sound noAparece;
    private static Sound ligacao;
    private static Sound elementos;
    private static Sound guardar;
    private static Sound downer;
    private static Sound darkspace;
    private static Sound sliderEntra;
    private static Sound unlink;
    private static Sound nodeJump;
    private static Sound nodeHigher;
    private static Sound nodeLower;
    private static Sound nodeMove;
    private static Sound[] pops;
    private static int proximoPop;

    private Sons() {
    }

    public static void iniciar( EngineFrame e ) {
        if ( engine != null ) {
            return;
        }
        engine = e;
        hover = carregar( e, "hover.wav" );
        clique = carregar( e, "creatorshome-digital-click-357350.mp3" );
        beep = carregar( e, "7624__ls__beep-003.wav" );
        low = carregar( e, "248210__jomellejager__low-short-sound.wav" );
        back = carregar( e, "418231__kierankeegan__ui-back-sound.wav" );
        swooshItem = carregar( e, "269332__kwahmah_02__swoosh47.wav" );
        bola = carregar( e, "167842__speedenza__transition-whoosh-4d.mp3" );
        letras = carregar( e, "241146__speedenza__transition-whoosh-5a.wav" );
        menuFim = carregar( e, "soundreality-whoosh-end-384629.mp3" );
        coisas = carregar( e, "soundreality-whoosh-large-sub-3846312.mp3" );
        noAparece = carregar( e, "dragon-studio-pop-402322.mp3" );
        ligacao = carregar( e, "link.wav" );
        elementos = carregar( e, "dragon-studio-heavy-whoosh-06-414584.mp3" );
        guardar = carregar( e, "800300__mahdinegari__woosh2.mp3" );
        guardarDown = carregar( e, "800300__mahdinegari__woosh3.mp3" );
        downer = carregar( e, "makoto-ae-swoosh-4-3598282.mp3" );
        darkspace = carregar( e, "98615__fons__darkspace.wav" );
        sliderEntra = carregar( e, "248210__jomellejager__low-short-sound.wav" );
        unlink = carregar( e, "unlink.mp3" );
        nodeJump = carregar( e, "nodeJump.mp3" );
        nodeHigher = carregar( e, "nodeHigher.mp3" );
        nodeLower = carregar( e, "nodeLower.mp3" );
        nodeMove = carregar( e, "nodeMove.mp3" );
        pops = new Sound[] {
            carregar( e, "850087__lbrady240__pop01.wav" ),
            carregar( e, "850088__lbrady240__pop02.wav" ),
            carregar( e, "850095__lbrady240__pop09.wav" )
        };
    }

    private static Sound carregar( EngineFrame e, String nome ) {
        return e.loadSound( PASTA + nome );
    }

    public static void hover() {
        tocar( hover );
    }

    public static void clique() {
        tocar( clique );
    }

    /** Remoção de nó: alterna entre os três pops. */
    public static void desfazer() {
        tocar( pops[proximoPop] );
        proximoPop = ( proximoPop + 1 ) % pops.length;
    }

    public static void abrirMenu() {
        tocar( beep );
    }

    public static void fecharMenu() {
        tocar( low );
    }

    /** Cada item saindo de dentro do menu ao expandir. */
    public static void itemSai() {
        tocar( swooshItem );
    }

    /** Cada item voltando para o centro ao recolher. */
    public static void itemVolta() {
        tocar( back );
    }

    /** Botão MENU aparecendo (última coisa desenhada na entrada). */
    public static void bolaAparece() {
        tocar( bola );
    }

    /** Letras da tela de menu começando a sair. */
    public static void letrasSaindo() {
        tocar( letras );
    }

    /** Menu recolhendo na saída. */
    public static void menuRecolhe() {
        tocar( menuFim );
    }

    /** Título saindo da tela de menu. */
    public static void coisasSaindo() {
        tocar( coisas );
    }

    public static void darkspace() {
        tocar( darkspace );
    }

    /** Nó novo chegando ao lugar na árvore. */
    public static void noAparece() {
        tocar( noAparece );
    }

    /** Ligações religadas ao fim de uma rotação ou inserção. */
    public static void ligacao() {
        tocar( ligacao );
    }

    /** Elementos do simulador sendo desenhados ao abrir. */
    public static void elementosSimulador() {
        tocar( elementos );
    }

    /** Elementos do simulador sendo guardados ao voltar. */
    public static void guardarElementos() {
        tocar( guardar );
    }

    /** Modo didático abrindo ou fechando. */
    public static void downer() {
        tocar( downer );
    }

    public static void guardarDowner() {
        tocar( guardarDown );
    }

    /** Slider H entrando no painel da AVL. */
    public static void sliderEntra() {
        tocar( sliderEntra );
    }

    /** Ligações sendo desconectadas (rotação ou remoção). */
    public static void unlink() {
        tocar( unlink );
    }

    /** Viajante pulando de um nó para o próximo no percurso. */
    public static void nodeJump() {
        tocar( nodeJump );
    }

    /** Comparação: chave maior que o nó, vai para a direita (verde). */
    public static void nodeHigher() {
        tocar( nodeHigher );
    }

    /** Comparação: chave menor que o nó, vai para a esquerda (vermelho). */
    public static void nodeLower() {
        tocar( nodeLower );
    }

    /** Nós girando e se movendo durante a rotação. */
    public static void nodeMove() {
        tocar( nodeMove );
    }

    private static void tocar( Sound som ) {
        if ( engine != null && som != null ) {
            engine.playSound( som );
        }
    }
}