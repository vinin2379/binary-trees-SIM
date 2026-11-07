package simulador;

import simulador.abb.JanelaSimuladorABB;
import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;

/**
 * Menu Principal de Seleção de Simuladores de Árvores com Menu Circular.
 *
 * @author Vinícius Lima Sobon
 */
public class Main extends EngineFrame {

    private static final double RAIO_TRIGGER = 24;
    private static final double RAIO_ITEM = 23;
    private static final double RAIO_CIRCULO = 120;
    private static final double ATRASO_ABRIR = 0.1;
    private static final double ATRASO_FECHAR = 0.07;
    private static final double TEMPO_LIBERAR_CLIQUE = 0.3;
    private static final double RIGIDEZ = 300;
    private static final double AMORTECIMENTO = 28;
    private static final double DURACAO_TREMOR = 0.35;
    private static final int TAM_ICONE = 14;
    private static final int TAM_ROTULO = 14;

    public static Main instancia;

    private static class Item {

        final String icone;
        final String rotulo;
        final int acao;
        final boolean ativo;

        Vector2 pos = new Vector2();
        Vector2 vel = new Vector2();
        Vector2 alvo = new Vector2();

        double atraso;
        boolean pendente;
        double escala = 1;
        double alpha;
        boolean hover;

        Item( String icone, String rotulo, int acao, boolean ativo ) {
            this.icone = icone;
            this.rotulo = rotulo;
            this.acao = acao;
            this.ativo = ativo;
        }
    }

    private Item[] itens;
    private Vector2 centro;
    private boolean aberto;
    private boolean triggerHover;
    private double tremor;
    private double tempo;
    private double tempoAberto;

    public Main() {
        super( 800, 600, "Menu - Simuladores de Árvores Binárias", 60, true );
        instancia = this;
    }

    @Override
    public void create() {
        centro = new Vector2( getScreenWidth() / 2.0, getScreenHeight() / 2.0 );

        itens = new Item[] {
            new Item( "ABB", "Árvore Binária de Busca", 0, true ),
            new Item( "AVL", "AVL (em breve)", 1, false ),
            new Item( "RN", "Rubro-Negra (em breve)", 2, false ),
            new Item( "Sair", "Sair", 3, true )
        };

        for ( Item it : itens ) {
            it.pos = new Vector2( centro.x, centro.y );
            it.alvo = new Vector2( centro.x, centro.y );
        }
    }

    @Override
    public void update( double delta ) {
        tempo += delta;
        if ( tremor > 0 ) {
            tremor -= delta;
        }
        if ( aberto ) {
            tempoAberto += delta;
        }

        boolean interativo = aberto && tempoAberto >= TEMPO_LIBERAR_CLIQUE;

        Vector2 mouse = getMousePositionPoint();
        triggerHover = mouse != null && mouse.distance( centro ) <= RAIO_TRIGGER;

        if ( mouse != null && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
            if ( triggerHover ) {
                alternarMenu();
            } else if ( interativo ) {
                for ( Item it : itens ) {
                    if ( it.ativo && mouse.distance( it.pos ) <= RAIO_ITEM * it.escala ) {
                        executarAcao( it.acao );
                        break;
                    }
                }
            }
        }

        for ( int i = 0; i < itens.length; i++ ) {
            Item it = itens[i];

            if ( it.pendente ) {
                it.atraso -= delta;
                if ( it.atraso <= 0 ) {
                    it.pendente = false;
                    it.alvo = aberto ? pontoNoCirculo( i ) : new Vector2( centro.x, centro.y );
                }
            }

            it.vel.x += ( ( it.alvo.x - it.pos.x ) * RIGIDEZ - it.vel.x * AMORTECIMENTO ) * delta;
            it.vel.y += ( ( it.alvo.y - it.pos.y ) * RIGIDEZ - it.vel.y * AMORTECIMENTO ) * delta;
            it.pos.x += it.vel.x * delta;
            it.pos.y += it.vel.y * delta;

            it.hover = interativo && mouse != null && mouse.distance( it.pos ) <= RAIO_ITEM;
            double escalaAlvo = it.hover ? 1.1 : 1.0;
            it.escala += ( escalaAlvo - it.escala ) * Math.min( 1, delta * 15 );

            double alphaAlvo = aberto ? 1.0 : 0.0;
            it.alpha += ( alphaAlvo - it.alpha ) * Math.min( 1, delta * 10 );
        }
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        for ( Item it : itens ) {
            if ( it.alpha <= 0.01 ) {
                continue;
            }

            double fator = it.ativo ? it.alpha : it.alpha * 0.5;
            double raio = RAIO_ITEM * it.escala;
            Color corFundo = ColorUtils.fade( it.hover ? Tema.ITEM_HOVER : Tema.ITEM, fator );
            Color corTexto = ColorUtils.fade( Tema.TEXTO, fator );

            fillCircle( it.pos, raio, corFundo );

            int larg = measureText( it.icone, TAM_ICONE );
            drawText( it.icone, it.pos.x - larg / 2.0, it.pos.y - TAM_ICONE / 3.0, TAM_ICONE, corTexto );

            if ( it.hover && it.alpha > 0.5 ) {
                int largRotulo = measureText( it.rotulo, TAM_ROTULO );
                drawText( it.rotulo, it.pos.x - largRotulo / 2.0, it.pos.y + raio + 6, TAM_ROTULO, Tema.TEXTO );
            }
        }

        double dx = tremor > 0 ? Math.sin( tempo * 60 ) * 2 : 0;
        Vector2 pc = new Vector2( centro.x + dx, centro.y );
        fillCircle( pc, RAIO_TRIGGER, triggerHover ? Tema.TRIGGER_HOVER : Tema.TRIGGER );

        String txt = aberto ? "X" : "MENU";
        int tam = 15;
        int larg = measureText( txt, tam );
        drawText( txt, pc.x - ( larg / 2.0 ) + 1, pc.y - 5, tam, Tema.TEXTO_CLARO );
    }

    /**
     * Mostra o menu de novo, usado pelo botão "Voltar" dos simuladores.
     */
    public void mostrar() {
        setVisible( true );
        aberto = false;
        tempoAberto = 0;
        for ( Item it : itens ) {
            it.pos = new Vector2( centro.x, centro.y );
            it.vel = new Vector2();
            it.alvo = new Vector2( centro.x, centro.y );
            it.pendente = false;
            it.alpha = 0;
            it.escala = 1;
        }
    }

    private void alternarMenu() {
        aberto = !aberto;
        tempoAberto = 0;
        for ( int i = 0; i < itens.length; i++ ) {
            Item it = itens[i];
            it.pendente = true;
            it.atraso = i * ( aberto ? ATRASO_ABRIR : ATRASO_FECHAR );
        }
        if ( !aberto ) {
            tremor = DURACAO_TREMOR;
        }
    }

    private Vector2 pontoNoCirculo( int i ) {
        double angulo = ( 2 * Math.PI * i ) / itens.length - ( Math.PI / 2 );
        return new Vector2(
                centro.x + RAIO_CIRCULO * Math.cos( angulo ),
                centro.y + RAIO_CIRCULO * Math.sin( angulo )
        );
    }

    private void executarAcao( int acao ) {
        if ( acao == 0 ) {
            setVisible( false );
            new Thread( () -> new JanelaSimuladorABB() ).start();
        } else if ( acao == 3 ) {
            System.exit( 0 );
        }
    }

    public static void main( String[] args ) {
        new Main();
    }
}