package simulador;

import java.awt.Color;

/**
 * Paleta visual compartilhada entre o menu e as janelas dos simuladores.
 */
public final class Tema {

    public static final Color FUNDO         = new Color( 245, 245, 244 );
    public static final Color PAINEL        = new Color( 255, 255, 255 );
    public static final Color BORDA         = new Color( 212, 212, 216 );
    public static final Color LINHA         = new Color( 161, 161, 170 );
    public static final Color TRIGGER       = new Color( 24, 24, 27 );
    public static final Color TRIGGER_HOVER = new Color( 63, 63, 70 );
    public static final Color ITEM          = new Color( 228, 228, 231 );
    public static final Color ITEM_HOVER    = new Color( 212, 212, 216 );
    public static final Color TEXTO         = new Color( 24, 24, 27 );
    public static final Color TEXTO_CLARO   = new Color( 250, 250, 250 );

    private Tema() {
    }
}