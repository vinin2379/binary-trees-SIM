package simulador;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import br.com.davidbuzatto.jsge.math.Vector2;
import java.awt.Color;
import simulador.abb.JanelaSimuladorABB;
import simulador.ui.Tema;

/**
 * Janela principal: título e nomes aparecem letra a letra, depois o menu circular.
 * Ao escolher um simulador, tudo sai na ordem inversa antes de abrir a janela.
 *
 * @author Vinícius Lima Sobon
 */
public class Main extends EngineFrame {

    private static final int LARG = 800;
    private static final int ALT = 600;

    private static final double RAIO_TRIGGER = 24;
    private static final double RAIO_ITEM = 23;
    private static final double RAIO_CIRCULO = 120;
    private static final double ATRASO_ABRIR = 0.1;
    private static final double ATRASO_FECHAR = 0.07;
    private static final double TEMPO_LIBERAR_CLIQUE = 0.3;
    private static final double DURACAO_TREMOR = 0.35;
    private static final int TAM_ICONE = 14;
    private static final int TAM_ROTULO = 14;

    private static final String TITULO = "Simulador de Árvores";
    private static final String NOMES = "Arthur Machado  -  Vinicius Sobon";
    private static final int TAM_TITULO = 52;
    private static final int TAM_NOMES = 22;
    private static final double Y_TITULO = 130;
    private static final double Y_NOMES = 540;
    private static final double CENTRO_Y = 340;

    // animação de entrada das letras
    private static final double DURACAO_LETRA = 0.5;
    private static final double ATRASO_POR_LETRA = 0.02;
    private static final double DESLOCAMENTO_Y = 30;

    // animação de saída: menu, nomes e título, nessa ordem
    private static final double DURACAO_SAIDA_MENU = 0.4;
    private static final double INICIO_SAIDA_NOMES = 0.3;
    private static final double INICIO_SAIDA_TITULO = 0.6;
    private static final double DURACAO_SAIDA_LETRA = 0.4;
    private static final double ATRASO_SAIDA_LETRA = 0.025;
    private static final double DESLOCAMENTO_SAIDA = 40;

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
        double tempo;
        double fase;
        double chacoalhar;
        boolean hoverAnt;

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

    private double tempoRevelacao;
    private boolean revelacaoTerminou;

    // saída
    private boolean saindo;
    private double tempoSaida;
    private int acaoPendente = -1;

    // entrada do botão do menu
    private double progressoTrigger;

    public Main() {
        super( LARG, ALT, "Simulador de Árvores Binárias", 60, true );
        instancia = this;
    }

    @Override
    public void create() {
        centro = new Vector2( LARG / 2.0, CENTRO_Y );

        itens = new Item[] {
            new Item( "ABB", "Árvore Binária de Busca", 0, true ),
            new Item( "AVL", "AVL (em breve)", 1, false ),
            new Item( "RN", "Rubro-Negra (em breve)", 2, false ),
            new Item( "Sair", "Sair", 3, true )
        };

        for ( int i = 0; i < itens.length; i++ ) {
            itens[i].fase = i * 1.7;
            itens[i].pos = new Vector2( centro.x, centro.y );
            itens[i].alvo = new Vector2( centro.x, centro.y );
        }
    }

    @Override
    public void update( double delta ) {
        tempo += delta;
        if ( tremor > 0 ) {
            tremor -= delta;
        }

        if ( saindo ) {
            atualizarSaida( delta );
            return;
        }

        if ( aberto ) {
            tempoAberto += delta;
        }

        if ( !revelacaoTerminou ) {
            tempoRevelacao += delta;
            if ( tempoRevelacao >= duracaoTotalRevelacao() ) {
                revelacaoTerminou = true;
            }
        }

        // entrada do botão: cresce de 0 a 1 com a mola de ui.Mola
        if ( revelacaoTerminou && progressoTrigger < 1 ) {
            progressoTrigger = Math.min( 1, progressoTrigger + delta * 1.8 );
        }

        boolean interativo = aberto && tempoAberto >= TEMPO_LIBERAR_CLIQUE;

        Vector2 mouse = getMousePositionPoint();
        triggerHover = mouse != null && mouse.distance( centro ) <= RAIO_TRIGGER;

        if ( revelacaoTerminou && mouse != null && isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
            if ( triggerHover ) {
                alternarMenu();
            } else if ( interativo ) {
                for ( Item it : itens ) {
                    if ( it.ativo && mouse.distance( it.pos ) <= RAIO_ITEM * it.escala ) {
                        iniciarSaida( it.acao );
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

            it.vel.x += ( ( it.alvo.x - it.pos.x ) * 300 - it.vel.x * 28 ) * delta;
            it.vel.y += ( ( it.alvo.y - it.pos.y ) * 300 - it.vel.y * 28 ) * delta;
            it.pos.x += it.vel.x * delta;
            it.pos.y += it.vel.y * delta;

            it.hover = interativo && mouse != null && mouse.distance( it.pos ) <= RAIO_ITEM;

            it.tempo += delta;
            if ( it.chacoalhar > 0 ) {
                it.chacoalhar = Math.max( 0, it.chacoalhar - delta );
            }
            if ( it.hover && !it.hoverAnt ) {
                it.chacoalhar = 0.35;
            }
            it.hoverAnt = it.hover;

            double escalaAlvo = it.hover ? 1.1 : 1.0;
            it.escala += ( escalaAlvo - it.escala ) * Math.min( 1, delta * 15 );

            double alphaAlvo = aberto ? 1.0 : 0.0;
            it.alpha += ( alphaAlvo - it.alpha ) * Math.min( 1, delta * 10 );
        }
    }

    @Override
    public void draw() {
        clearBackground( Tema.FUNDO );

        if ( saindo ) {
            desenharSaida();
            return;
        }

        desenharRevelado( TITULO, TAM_TITULO, Y_TITULO, 0 );
        desenharRevelado( NOMES, TAM_NOMES, Y_NOMES, TITULO.length() );

        if ( !revelacaoTerminou ) {
            return;
        }

        // botão do menu com entrada suave
        double entrada = easeOutBack( progressoTrigger );
        double raioTrigger = RAIO_TRIGGER * Math.max( 0, entrada );
        double yTrigger = centro.y + ( 1 - entrada ) * 40;

        for ( Item it : itens ) {
            if ( it.alpha <= 0.01 ) {
                continue;
            }

            double fator = it.ativo ? it.alpha : it.alpha * 0.5;
            double raio = RAIO_ITEM * it.escala;
            Color corFundo = ColorUtils.fade( it.hover ? Tema.ITEM_HOVER : Tema.ITEM, fator );
            Color corTexto = ColorUtils.fade( Tema.TEXTO, fator );

            Vector2 p = posVisual( it );
            fillCircle( p, raio, corFundo );

            int larg = measureText( it.icone, TAM_ICONE );
            drawText( it.icone, p.x - larg / 2.0, p.y - TAM_ICONE / 3.0, TAM_ICONE, corTexto );

            if ( it.hover && it.alpha > 0.5 ) {
                int largRotulo = measureText( it.rotulo, TAM_ROTULO );
                drawText( it.rotulo, p.x - largRotulo / 2.0, p.y + raio + 6, TAM_ROTULO, Tema.TEXTO );
            }
        }

        double dx = tremor > 0 ? Math.sin( tempo * 60 ) * 2 : 0;
        Vector2 pc = new Vector2( centro.x + dx, yTrigger );
        fillCircle( pc, raioTrigger, triggerHover ? Tema.TRIGGER_HOVER : Tema.TRIGGER );

        if ( entrada > 0.5 ) {
            String txt = aberto ? "X" : "MENU";
            int tam = 15;
            int larg = measureText( txt, tam );
            drawText( txt, pc.x - ( larg / 2.0 ) + 1, pc.y - 5, tam, Tema.TEXTO_CLARO );
        }
    }

    /** Entrada das letras: sobem e aparecem, uma a uma. */
    private void desenharRevelado( String texto, int tamanho, double y, int deslocamentoLetras ) {
        int larguraTotal = measureText( texto, tamanho );
        double x = LARG / 2.0 - larguraTotal / 2.0;

        for ( int i = 0; i < texto.length(); i++ ) {
            String s = String.valueOf( texto.charAt( i ) );
            double inicio = ( deslocamentoLetras + i ) * ATRASO_POR_LETRA;
            double progresso = ( tempoRevelacao - inicio ) / DURACAO_LETRA;
            if ( progresso > 0 ) {
                double e = easeInOut( Math.min( 1, progresso ) );
                double dy = ( 1 - e ) * DESLOCAMENTO_Y;
                drawText( s, x, y + dy, tamanho, ColorUtils.fade( Tema.TEXTO, e ) );
            }
            x += measureText( s, tamanho );
        }
    }

    /** Saída: menu, depois nomes da esquerda para a direita, depois o título. */
    private void desenharSaida() {
        double pMenu = Math.min( 1, tempoSaida / DURACAO_SAIDA_MENU );
        if ( pMenu < 1 ) {
            double e = 1 - easeInOut( pMenu );
            drawText( "", 0, 0, 1, Tema.TEXTO );
            fillCircle( centro, RAIO_TRIGGER * e, Tema.TRIGGER );
        }

        desenharLetrasSaindo( NOMES, TAM_NOMES, Y_NOMES, INICIO_SAIDA_NOMES, true );
        desenharLetrasSaindo( TITULO, TAM_TITULO, Y_TITULO, INICIO_SAIDA_TITULO, true );
    }

    /** Letras saindo: cada uma sobe e some, da esquerda para a direita. */
    private void desenharLetrasSaindo( String texto, int tamanho, double y, double inicioSaida, boolean daEsquerda ) {
        int larguraTotal = measureText( texto, tamanho );
        double x = LARG / 2.0 - larguraTotal / 2.0;

        for ( int i = 0; i < texto.length(); i++ ) {
            String s = String.valueOf( texto.charAt( i ) );
            double inicio = inicioSaida + i * ATRASO_SAIDA_LETRA;
            double progresso = ( tempoSaida - inicio ) / DURACAO_SAIDA_LETRA;
            if ( progresso < 1 ) {
                double t = Math.max( 0, progresso );
                double e = easeInOut( t );
                double dy = -e * DESLOCAMENTO_SAIDA;
                double alfa = 1 - e;
                if ( alfa > 0.01 ) {
                    drawText( s, x, y + dy, tamanho, ColorUtils.fade( Tema.TEXTO, alfa ) );
                }
            }
            x += measureText( s, tamanho );
        }
    }

    private void iniciarSaida( int acao ) {
        saindo = true;
        tempoSaida = 0;
        acaoPendente = acao;
    }

    /** Avança a saída. Quando termina, executa a ação do simulador escolhido. */
    private void atualizarSaida( double delta ) {
        tempoSaida += delta;
        double fimSaida = INICIO_SAIDA_TITULO + TITULO.length() * ATRASO_SAIDA_LETRA + DURACAO_SAIDA_LETRA;
        if ( tempoSaida >= fimSaida ) {
            saindo = false;
            executarAcao( acaoPendente );
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

    /** Mesmo ease do botão de entrada, com leve ultrapassagem. */
    private static double easeOutBack( double t ) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow( t - 1, 3 ) + c1 * Math.pow( t - 1, 2 );
    }

    private static double easeInOut( double t ) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow( -2 * t + 2, 2 ) / 2;
    }

    private Vector2 pontoNoCirculo( int i ) {
        double angulo = ( 2 * Math.PI * i ) / itens.length - ( Math.PI / 2 );
        return new Vector2(
                centro.x + RAIO_CIRCULO * Math.cos( angulo ),
                centro.y + RAIO_CIRCULO * Math.sin( angulo )
        );
    }

    private Vector2 posVisual( Item it ) {
        double ox = Math.sin( it.tempo * 0.9 + it.fase ) * 1.5;
        double oy = Math.cos( it.tempo * 0.7 + it.fase * 1.3 ) * 2;
        if ( it.chacoalhar > 0 ) {
            ox += Math.sin( it.chacoalhar * 55 ) * ( it.chacoalhar / 0.35 * 3 );
        }
        return new Vector2( it.pos.x + ox, it.pos.y + oy );
    }

    private double duracaoTotalRevelacao() {
        int total = TITULO.length() + NOMES.length();
        return total * ATRASO_POR_LETRA + DURACAO_LETRA;
    }

    /**
     * Mostra o menu de novo, usado pelo botão "Voltar" dos simuladores.
     */
    public void mostrar() {
        setVisible( true );
        aberto = false;
        tempoAberto = 0;
        saindo = false;
        for ( Item it : itens ) {
            it.pos = new Vector2( centro.x, centro.y );
            it.vel = new Vector2();
            it.alvo = new Vector2( centro.x, centro.y );
            it.pendente = false;
            it.alpha = 0;
            it.escala = 1;
        }
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