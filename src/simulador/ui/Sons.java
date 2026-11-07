package simulador.ui;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.sound.Sound;

/**
 * Efeitos sonoros da interface. Carregue uma vez com Sons.iniciar(this)
 * dentro do create() da janela.
 */
public final class Sons {

    private static final String PASTA = "resources/sfx/";

    private static Sound hover;
    private static Sound clique;
    private static Sound beep;
    private static Sound chord;
    private static Sound low;
    private static Sound back;
    private static Sound darkspace;
    private static Sound[] pops;
    private static int proximoPop;

    private static EngineFrame engine;

    private Sons() {
    }

    public static void iniciar( EngineFrame e ) {
        if ( engine != null ) {
            return;
        }
        engine = e;
        hover = e.loadSound( PASTA + "862449__avaol__button-hover-2.wav" );
        clique = e.loadSound( PASTA + "876010__signaturesoundsorg__starry_ui_click_63.wav" );
        beep = e.loadSound( PASTA + "7624__ls__beep-003.wav" );
        chord = e.loadSound( PASTA + "846358__someusername0__menu_chord-clear.wav" );
        low = e.loadSound( PASTA + "248210__jomellejager__low-short-sound.wav" );
        back = e.loadSound( PASTA + "418231__kierankeegan__ui-back-sound.wav" );
        darkspace = e.loadSound( PASTA + "98615__fons__darkspace.wav" );
        pops = new Sound[] {
            e.loadSound( PASTA + "850087__lbrady240__pop01.wav" ),
            e.loadSound( PASTA + "850088__lbrady240__pop02.wav" ),
            e.loadSound( PASTA + "850095__lbrady240__pop09.wav" )
        };
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

    public static void itemAparece() {
        tocar( chord );
    }

    public static void itemVolta() {
        tocar( back );
    }

    public static void darkspace() {
        tocar( darkspace );
    }

    private static void tocar( Sound som ) {
        if ( engine != null && som != null ) {
            engine.playSound( som );
        }
    }
}